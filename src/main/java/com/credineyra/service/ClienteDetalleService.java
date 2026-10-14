package com.credineyra.service;

import com.credineyra.dto.ClienteDetalleDto;
import com.credineyra.dto.CreditoClienteResumenDto;
import com.credineyra.entity.Cliente;
import com.credineyra.entity.Credito;
import com.credineyra.entity.Cuota;
import com.credineyra.entity.Persona;
import com.credineyra.entity.enums.EstadoCredito;
import com.credineyra.entity.enums.SituacionPagoCliente;
import com.credineyra.repository.CuotaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ClienteDetalleService {

    private static final int DIAS_ADVERTENCIA = 7;

    private static final ZoneId ZONA_CREDINEYRA =
            ZoneId.of("America/Lima");


    private final ClienteService clienteService;

    private final CreditoService creditoService;

    private final CuotaRepository cuotaRepository;

    private final int diasGracia;


    public ClienteDetalleService(
            ClienteService clienteService,
            CreditoService creditoService,
            CuotaRepository cuotaRepository,

            @Value("${credineyra.mora.dias-gracia:3}")
            int diasGracia
    ) {

        this.clienteService =
                clienteService;

        this.creditoService =
                creditoService;

        this.cuotaRepository =
                cuotaRepository;

        this.diasGracia =
                diasGracia;
    }


    /* =========================================================
       DETALLE DEL CLIENTE
       ========================================================= */

    public ClienteDetalleDto obtenerDetalle(
            Long idCliente
    ) {

        Cliente cliente =
                clienteService.buscarPorId(
                        idCliente
                );


        Persona persona =
                cliente.getPersona();


        List<Credito> creditos =
                creditoService.listarPorCliente(
                        idCliente
                );


        List<Cuota> cuotas =
                cuotaRepository
                        .findByCredito_Cliente_IdClienteOrderByFechaVencimientoAsc(
                                idCliente
                        );


        ClienteDetalleDto detalle =
                new ClienteDetalleDto();


        cargarDatosCliente(
                detalle,
                cliente,
                persona
        );


        cargarDatosCreditos(
                detalle,
                creditos
        );


        cargarSituacionFinanciera(
                detalle,
                creditos,
                cuotas
        );


        return detalle;
    }


    /* =========================================================
       DATOS PERSONALES
       ========================================================= */

    private void cargarDatosCliente(
            ClienteDetalleDto detalle,
            Cliente cliente,
            Persona persona
    ) {

        detalle.setIdCliente(
                cliente.getIdCliente()
        );


        detalle.setCodigoCliente(
                cliente.getCodigoCliente()
        );


        detalle.setTipoOrigen(
                cliente.getTipoOrigen()
        );


        detalle.setEstado(
                cliente.getEstado()
        );


        detalle.setFechaRegistro(
                cliente.getFechaRegistro()
        );


        detalle.setObservaciones(
                cliente.getObservaciones()
        );


        if (persona == null) {
            return;
        }


        detalle.setDni(
                persona.getDni()
        );


        detalle.setNombres(
                persona.getNombres()
        );


        detalle.setApellidos(
                persona.getApellidos()
        );


        detalle.setNombreCompleto(
                construirNombreCompleto(
                        persona
                )
        );


        detalle.setTelefono(
                persona.getTelefono()
        );


        detalle.setEmail(
                persona.getEmail()
        );


        detalle.setDireccion(
                persona.getDireccion()
        );
    }


    /* =========================================================
       CREDITOS
       ========================================================= */

    private void cargarDatosCreditos(
            ClienteDetalleDto detalle,
            List<Credito> creditos
    ) {

        List<CreditoClienteResumenDto> resumenes =
                new ArrayList<>();


        long activos = 0;


        for (Credito credito : creditos) {

            CreditoClienteResumenDto dto =
                    convertirCredito(
                            credito
                    );


            resumenes.add(
                    dto
            );


            if (credito.getEstado()
                    != EstadoCredito.FINALIZADO) {

                activos++;
            }
        }


        detalle.setCreditos(
                resumenes
        );


        detalle.setTotalCreditos(
                creditos.size()
        );


        detalle.setCreditosActivos(
                activos
        );
    }


    /* =========================================================
       SITUACION FINANCIERA
       ========================================================= */

    private void cargarSituacionFinanciera(
            ClienteDetalleDto detalle,
            List<Credito> creditos,
            List<Cuota> cuotas
    ) {

        /*
         * Cliente recién registrado,
         * todavía sin créditos.
         */
        if (creditos.isEmpty()) {

            detalle.setSituacionPago(
                    SituacionPagoCliente.SIN_CREDITO
            );


            detalle.setSaldoPendienteTotal(
                    cero()
            );


            detalle.setMontoProximaCuota(
                    cero()
            );


            detalle.setFechaProximaCuota(
                    null
            );


            return;
        }


        List<Cuota> pendientes =
                cuotas.stream()
                        .filter(
                                this::tieneSaldoPendiente
                        )
                        .toList();


        /*
         * Tiene créditos, pero todas las
         * cuotas están pagadas.
         */
        if (pendientes.isEmpty()) {

            detalle.setSituacionPago(
                    SituacionPagoCliente.AL_DIA
            );


            detalle.setSaldoPendienteTotal(
                    cero()
            );


            detalle.setMontoProximaCuota(
                    cero()
            );


            detalle.setFechaProximaCuota(
                    null
            );


            return;
        }


        BigDecimal saldoTotal =
                pendientes.stream()
                        .map(
                                this::saldoCuota
                        )
                        .reduce(
                                cero(),
                                BigDecimal::add
                        )
                        .setScale(2);


        detalle.setSaldoPendienteTotal(
                saldoTotal
        );


        /*
         * El repositorio ya entrega las cuotas
         * ordenadas por vencimiento.
         */
        Cuota cuotaReferencia =
                pendientes.get(0);


        detalle.setFechaProximaCuota(
                cuotaReferencia
                        .getFechaVencimiento()
        );


        detalle.setMontoProximaCuota(
                saldoCuota(
                        cuotaReferencia
                )
        );


        detalle.setSituacionPago(
                calcularSituacion(
                        pendientes
                )
        );
    }


    /* =========================================================
       CALCULAR SITUACION
       ========================================================= */

    private SituacionPagoCliente calcularSituacion(
            List<Cuota> pendientes
    ) {

        LocalDate hoy =
                LocalDate.now(
                        ZONA_CREDINEYRA
                );


        /*
         * Si una sola cuota ya superó los días
         * de gracia, el cliente está en mora.
         */
        boolean tieneMora =
                pendientes.stream()
                        .anyMatch(
                                cuota ->
                                        hoy.isAfter(
                                                cuota
                                                        .getFechaVencimiento()
                                                        .plusDays(
                                                                diasGracia
                                                        )
                                        )
                        );


        if (tieneMora) {

            return SituacionPagoCliente.EN_MORA;
        }


        Cuota primeraCuota =
                pendientes.get(0);


        LocalDate fecha =
                primeraCuota
                        .getFechaVencimiento();


        /*
         * Venció pero todavía se encuentra
         * dentro del período de gracia.
         */
        if (fecha.isBefore(hoy)) {

            return SituacionPagoCliente.EN_GRACIA;
        }


        /*
         * Vence hoy o en los próximos 7 días.
         */
        if (!fecha.isAfter(
                hoy.plusDays(
                        DIAS_ADVERTENCIA
                )
        )) {

            return SituacionPagoCliente.POR_VENCER;
        }


        return SituacionPagoCliente.AL_DIA;
    }


    /* =========================================================
       CREDITO -> DTO
       ========================================================= */

    private CreditoClienteResumenDto convertirCredito(
            Credito credito
    ) {

        CreditoClienteResumenDto dto =
                new CreditoClienteResumenDto();


        dto.setIdCredito(
                credito.getIdCredito()
        );


        dto.setCodigoCredito(
                credito.getCodigoCredito()
        );


        dto.setCapitalOriginal(
                nz(
                        credito.getCapitalOriginal()
                )
        );


        dto.setCapitalPendiente(
                nz(
                        credito.getCapitalPendiente()
                )
        );


        dto.setInteresPendiente(
                nz(
                        credito.getInteresPendiente()
                )
        );


        BigDecimal saldo =
                nz(
                        credito.getCapitalPendiente()
                )
                        .add(
                                nz(
                                        credito.getInteresPendiente()
                                )
                        )
                        .setScale(2);


        dto.setSaldoPendiente(
                saldo
        );


        dto.setEstado(
                credito.getEstado()
        );


        dto.setFechaSolicitud(
                credito.getFechaSolicitud()
        );


        return dto;
    }


    /* =========================================================
       SALDO DE CUOTA
       ========================================================= */

    private BigDecimal saldoCuota(
            Cuota cuota
    ) {

        return nz(
                cuota.getCapitalPendiente()
        )
                .add(
                        nz(
                                cuota.getInteresPendiente()
                        )
                )
                .add(
                        nz(
                                cuota.getCapitalRegularizadoPendiente()
                        )
                )
                .setScale(2);
    }


    private boolean tieneSaldoPendiente(
            Cuota cuota
    ) {

        return saldoCuota(
                cuota
        ).compareTo(
                BigDecimal.ZERO
        ) > 0;
    }


    /* =========================================================
       NOMBRE
       ========================================================= */

    private String construirNombreCompleto(
            Persona persona
    ) {

        String nombres =
                persona.getNombres() == null
                        ? ""
                        : persona
                        .getNombres()
                        .trim();


        String apellidos =
                persona.getApellidos() == null
                        ? ""
                        : persona
                        .getApellidos()
                        .trim();


        return (
                nombres
                        + " "
                        + apellidos
        ).trim();
    }


    /* =========================================================
       DINERO
       ========================================================= */

    private BigDecimal nz(
            BigDecimal valor
    ) {

        return valor == null
                ? cero()
                : valor;
    }


    private BigDecimal cero() {

        return BigDecimal.ZERO
                .setScale(2);
    }
}