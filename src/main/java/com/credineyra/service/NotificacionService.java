package com.credineyra.service;

import com.credineyra.entity.Cuota;
import com.credineyra.entity.Notificacion;
import com.credineyra.entity.enums.CanalNotificacion;
import com.credineyra.entity.enums.EstadoNotificacion;
import com.credineyra.entity.enums.TipoNotificacion;
import com.credineyra.exception.DatoDuplicadoException;
import com.credineyra.exception.RecursoNoEncontradoException;
import com.credineyra.exception.ReglaNegocioException;
import com.credineyra.repository.CuotaRepository;
import com.credineyra.repository.NotificacionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class NotificacionService {

    private final NotificacionRepository repository;
    private final CuotaRepository cuotaRepository;

    public NotificacionService(
            NotificacionRepository repository,
            CuotaRepository cuotaRepository
    ) {
        this.repository = repository;
        this.cuotaRepository = cuotaRepository;
    }

    public Notificacion registrar(
            Notificacion notificacion
    ) {

        validar(notificacion);

        if (notificacion.getClaveEvento() != null
                && !notificacion.getClaveEvento()
                .isBlank()
                && repository.existsByClaveEvento(
                notificacion.getClaveEvento()
        )) {

            throw new DatoDuplicadoException(
                    "La notificación ya existe"
            );
        }

        if (notificacion.getEstado() == null) {
            notificacion.setEstado(
                    EstadoNotificacion.PENDIENTE
            );
        }

        if (notificacion.getFechaProgramada()
                == null) {

            notificacion.setFechaProgramada(
                    OffsetDateTime.now()
            );
        }

        return repository.save(notificacion);
    }

    /*
     * Se ejecutará todos los días aproximadamente
     * a las 8:00 AM.
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void generarRecordatoriosAutomaticos() {

        LocalDate hoy = LocalDate.now();

        generarParaFecha(
                hoy.plusDays(3),
                TipoNotificacion.VENCIMIENTO_PROXIMO,
                "VENCE_3_DIAS",
                "Tu cuota vence en 3 días"
        );

        generarParaFecha(
                hoy.plusDays(1),
                TipoNotificacion.VENCIMIENTO_PROXIMO,
                "VENCE_1_DIA",
                "Tu cuota vence mañana"
        );

        generarParaFecha(
                hoy,
                TipoNotificacion.VENCE_HOY,
                "VENCE_HOY",
                "Tu cuota vence hoy"
        );

        /*
         * Vencimiento + tres días completos de gracia.
         * Al día siguiente se genera el aviso vencido.
         */
        generarParaFecha(
                hoy.minusDays(4),
                TipoNotificacion.CUOTA_VENCIDA,
                "CUOTA_VENCIDA",
                "Tu cuota se encuentra vencida"
        );
    }

    private void generarParaFecha(
            LocalDate fechaCuota,
            TipoNotificacion tipo,
            String evento,
            String titulo
    ) {

        List<Cuota> cuotas =
                cuotaRepository
                        .findByFechaVencimiento(
                                fechaCuota
                        );

        for (Cuota cuota : cuotas) {

            if (saldo(cuota).compareTo(
                    BigDecimal.ZERO
            ) <= 0) {
                continue;
            }

            String clave =
                    "CUOTA:"
                            + cuota.getIdCuota()
                            + ":"
                            + evento
                            + ":WEB";

            if (repository.existsByClaveEvento(
                    clave
            )) {
                continue;
            }

            Notificacion notificacion =
                    new Notificacion();

            notificacion.setCliente(
                    cuota.getCredito()
                            .getCliente()
            );

            notificacion.setCredito(
                    cuota.getCredito()
            );

            notificacion.setCuota(cuota);

            notificacion.setTipo(tipo);

            notificacion.setCanal(
                    CanalNotificacion.WEB
            );

            notificacion.setTitulo(titulo);

            notificacion.setMensaje(
                    "Cuota "
                            + cuota.getNumeroCuota()
                            + " - fecha: "
                            + cuota.getFechaVencimiento()
                            + " - monto pendiente: S/ "
                            + saldo(cuota)
            );

            notificacion.setClaveEvento(
                    clave
            );

            notificacion.setEstado(
                    EstadoNotificacion.PENDIENTE
            );

            notificacion.setLeida(false);

            notificacion.setFechaProgramada(
                    OffsetDateTime.now()
            );

            repository.save(notificacion);
        }
    }

    public Notificacion marcarLeida(
            Long id
    ) {

        Notificacion notificacion =
                buscarPorId(id);

        notificacion.setLeida(true);

        return repository.save(
                notificacion
        );
    }

    public Notificacion marcarEnviada(
            Long id
    ) {

        Notificacion notificacion =
                buscarPorId(id);

        notificacion.setEstado(
                EstadoNotificacion.ENVIADA
        );

        return repository.save(
                notificacion
        );
    }

    public Notificacion marcarFallida(
            Long id
    ) {

        Notificacion notificacion =
                buscarPorId(id);

        notificacion.setEstado(
                EstadoNotificacion.FALLIDA
        );

        return repository.save(
                notificacion
        );
    }

    @Transactional(readOnly = true)
    public Notificacion buscarPorId(
            Long id
    ) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Notificación no encontrada"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<Notificacion> listarTodos() {

        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Notificacion> listarPorCliente(
            Long idCliente
    ) {

        return repository
                .findByCliente_IdClienteOrderByFechaProgramadaDesc(
                        idCliente
                );
    }

    @Transactional(readOnly = true)
    public List<Notificacion> listarNoLeidas(
            Long idCliente
    ) {

        return repository
                .findByCliente_IdClienteAndLeidaFalseOrderByFechaProgramadaDesc(
                        idCliente
                );
    }

    private BigDecimal saldo(
            Cuota cuota
    ) {

        return nz(cuota.getCapitalPendiente())
                .add(
                        nz(
                                cuota.getInteresPendiente()
                        )
                )
                .add(
                        nz(
                                cuota.getCapitalRegularizadoPendiente()
                        )
                );
    }

    private BigDecimal nz(
            BigDecimal valor
    ) {

        return valor == null
                ? BigDecimal.ZERO
                : valor;
    }

    private void validar(
            Notificacion notificacion
    ) {

        if (notificacion == null
                || notificacion.getCliente() == null) {

            throw new ReglaNegocioException(
                    "La notificación debe pertenecer a un cliente"
            );
        }

        if (notificacion.getTipo() == null
                || notificacion.getCanal() == null) {

            throw new ReglaNegocioException(
                    "El tipo y canal son obligatorios"
            );
        }

        if (notificacion.getMensaje() == null
                || notificacion.getMensaje()
                .isBlank()) {

            throw new ReglaNegocioException(
                    "El mensaje es obligatorio"
            );
        }
    }
}