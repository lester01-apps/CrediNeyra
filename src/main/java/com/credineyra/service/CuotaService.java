package com.credineyra.service;

import com.credineyra.entity.Credito;
import com.credineyra.entity.Cuota;
import com.credineyra.entity.Usuario;
import com.credineyra.entity.enums.EstadoCredito;
import com.credineyra.entity.enums.EstadoCuota;
import com.credineyra.entity.enums.EstadoUsuario;
import com.credineyra.exception.OperacionNoPermitidaException;
import com.credineyra.exception.RecursoNoEncontradoException;
import com.credineyra.exception.ReglaNegocioException;
import com.credineyra.repository.CreditoRepository;
import com.credineyra.repository.CuotaRepository;
import com.credineyra.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class CuotaService {

    private final CuotaRepository cuotaRepository;
    private final CreditoRepository creditoRepository;
    private final UsuarioRepository usuarioRepository;

    private final int diasGracia;
    private final int mesesAtrasoProlongado;

    public CuotaService(
            CuotaRepository cuotaRepository,
            CreditoRepository creditoRepository,
            UsuarioRepository usuarioRepository,
            @Value(
                    "${credineyra.mora.dias-gracia:3}"
            )
            int diasGracia,
            @Value(
                    "${credineyra.mora.meses-atraso-prolongado:4}"
            )
            int mesesAtrasoProlongado
    ) {
        this.cuotaRepository = cuotaRepository;
        this.creditoRepository = creditoRepository;
        this.usuarioRepository = usuarioRepository;
        this.diasGracia = diasGracia;
        this.mesesAtrasoProlongado =
                mesesAtrasoProlongado;
    }

    public Cuota registrar(Cuota cuota) {

        if (cuota == null
                || cuota.getCredito() == null) {

            throw new ReglaNegocioException(
                    "La cuota debe pertenecer a un crédito"
            );
        }

        if (cuota.getNumeroCuota() == null
                || cuota.getNumeroCuota() <= 0) {

            throw new ReglaNegocioException(
                    "El número de cuota debe ser mayor que cero"
            );
        }

        cuotaRepository
                .findByCredito_IdCreditoAndNumeroCuota(
                        cuota.getCredito()
                                .getIdCredito(),
                        cuota.getNumeroCuota()
                )
                .ifPresent(existente -> {
                    throw new ReglaNegocioException(
                            "La cuota ya existe"
                    );
                });

        return cuotaRepository.save(cuota);
    }

    public Cuota cambiarEstado(
            Long idCuota,
            EstadoCuota estado
    ) {

        Cuota cuota =
                buscarPorId(idCuota);

        if (cuota.getEstado()
                == EstadoCuota.PAGADA
                && estado != EstadoCuota.PAGADA) {

            throw new OperacionNoPermitidaException(
                    "Una cuota pagada no puede reabrirse directamente"
            );
        }

        cuota.setEstado(estado);

        return cuotaRepository.save(cuota);
    }

    /*
     * SchedulingConfig ya tiene @EnableScheduling.
     * Se revisa una vez al día.
     */
    @Scheduled(cron = "0 5 0 * * *")
    public void actualizarEstadosAutomaticamente() {

        actualizarEstados(
                LocalDate.now()
        );
    }

    public void actualizarEstados(
            LocalDate fechaActual
    ) {

        List<Cuota> cuotas =
                cuotaRepository.findAll();

        Set<Long> creditosAfectados =
                new HashSet<>();

        for (Cuota cuota : cuotas) {

            if (cuota.getEstado()
                    == EstadoCuota.PAGADA) {
                continue;
            }

            BigDecimal saldo =
                    saldoCuota(cuota);

            if (saldo.compareTo(
                    BigDecimal.ZERO
            ) <= 0) {

                cuota.setEstado(
                        EstadoCuota.PAGADA
                );

                cuotaRepository.save(cuota);

                creditosAfectados.add(
                        cuota.getCredito()
                                .getIdCredito()
                );

                continue;
            }

            LocalDate finGracia =
                    cuota.getFechaVencimiento()
                            .plusDays(
                                    diasGracia
                            );

            if (fechaActual.isAfter(
                    finGracia
            )) {

                cuota.setEstado(
                        EstadoCuota.VENCIDA
                );

            } else if (tienePagos(cuota)) {

                cuota.setEstado(
                        EstadoCuota.PARCIAL
                );

            } else {

                cuota.setEstado(
                        EstadoCuota.PENDIENTE
                );
            }

            cuotaRepository.save(cuota);

            creditosAfectados.add(
                    cuota.getCredito()
                            .getIdCredito()
            );
        }

        for (Long idCredito :
                creditosAfectados) {

            recalcularEstadoCredito(
                    idCredito,
                    fechaActual
            );
        }
    }

    private void recalcularEstadoCredito(
            Long idCredito,
            LocalDate fechaActual
    ) {

        Credito credito =
                creditoRepository
                        .findById(idCredito)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "Crédito no encontrado"
                                )
                        );

        List<Cuota> cuotas =
                cuotaRepository
                        .findByCredito_IdCreditoOrderByNumeroCuotaAsc(
                                idCredito
                        );

        boolean todasPagadas =
                cuotas.stream()
                        .allMatch(c ->
                                c.getEstado()
                                        == EstadoCuota.PAGADA
                        );

        if (todasPagadas) {

            credito.setEstado(
                    EstadoCredito.FINALIZADO
            );

            creditoRepository.save(credito);
            return;
        }

        LocalDate limiteProlongado =
                fechaActual.minusMonths(
                        mesesAtrasoProlongado
                );

        boolean atrasoProlongado =
                cuotas.stream()
                        .filter(c ->
                                c.getEstado()
                                        != EstadoCuota.PAGADA
                        )
                        .anyMatch(c ->
                                !c.getFechaVencimiento()
                                        .isAfter(
                                                limiteProlongado
                                        )
                        );

        if (atrasoProlongado) {

            credito.setEstado(
                    EstadoCredito.ATRASO_PROLONGADO
            );

            restringirCuentaCliente(
                    credito
            );

            creditoRepository.save(credito);
            return;
        }

        if (credito.getEstado()
                == EstadoCredito.EN_REGULARIZACION) {

            creditoRepository.save(credito);
            return;
        }

        boolean existeMora =
                cuotas.stream()
                        .anyMatch(c ->
                                c.getEstado()
                                        == EstadoCuota.VENCIDA
                        );

        credito.setEstado(
                existeMora
                        ? EstadoCredito.EN_MORA
                        : EstadoCredito.AL_DIA
        );

        creditoRepository.save(credito);
    }

    private void restringirCuentaCliente(
            Credito credito
    ) {

        if (credito.getCliente() == null
                || credito.getCliente()
                .getPersona() == null) {
            return;
        }

        String dni =
                credito.getCliente()
                        .getPersona()
                        .getDni();

        usuarioRepository
                .findByPersona_Dni(dni)
                .ifPresent(usuario -> {

                    if (usuario.getEstado()
                            == EstadoUsuario.ACTIVA) {

                        usuario.setEstado(
                                EstadoUsuario.RESTRINGIDA
                        );

                        usuarioRepository.save(
                                usuario
                        );
                    }
                });
    }

    private boolean tienePagos(
            Cuota cuota
    ) {

        return nz(cuota.getCapitalPagado())
                .add(
                        nz(
                                cuota.getInteresPagado()
                        )
                )
                .add(
                        nz(
                                cuota.getCapitalRegularizadoPagado()
                        )
                )
                .compareTo(
                        BigDecimal.ZERO
                ) > 0;
    }

    private BigDecimal saldoCuota(
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

    @Transactional(readOnly = true)
    public Cuota buscarPorId(
            Long idCuota
    ) {

        return cuotaRepository
                .findById(idCuota)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Cuota no encontrada"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<Cuota> listarTodos() {

        return cuotaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Cuota> listarPorCredito(
            Long idCredito
    ) {

        return cuotaRepository
                .findByCredito_IdCreditoOrderByNumeroCuotaAsc(
                        idCredito
                );
    }
}