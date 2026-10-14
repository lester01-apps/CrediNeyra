package com.credineyra.service;

import com.credineyra.dto.ClientePanelDto;
import com.credineyra.dto.ClienteResumenDto;
import com.credineyra.entity.Cliente;
import com.credineyra.entity.Credito;
import com.credineyra.entity.Cuota;
import com.credineyra.entity.Persona;
import com.credineyra.entity.enums.EstadoCliente;
import com.credineyra.entity.enums.SituacionPagoCliente;
import com.credineyra.repository.ClienteRepository;
import com.credineyra.repository.CreditoRepository;
import com.credineyra.repository.CuotaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ClientePanelService {

    /*
     * Personas que tengan una cuota dentro
     * de los próximos 7 días aparecerán
     * como advertencia.
     */
    private static final int DIAS_ADVERTENCIA =
            7;


    /*
     * Regla actual de crediNeyra:
     * 3 días calendario de gracia.
     */
    private static final int DIAS_GRACIA =
            3;


    private static final ZoneId ZONA_CREDINEYRA =
            ZoneId.of("America/Lima");


    private final ClienteRepository clienteRepository;

    private final CreditoRepository creditoRepository;

    private final CuotaRepository cuotaRepository;


    public ClientePanelService(
            ClienteRepository clienteRepository,
            CreditoRepository creditoRepository,
            CuotaRepository cuotaRepository
    ) {

        this.clienteRepository =
                clienteRepository;

        this.creditoRepository =
                creditoRepository;

        this.cuotaRepository =
                cuotaRepository;
    }


    /* =========================================================
       CONSTRUIR PANEL COMPLETO
       ========================================================= */

    public ClientePanelDto obtenerPanelClientes() {

        List<Cliente> clientes =
                clienteRepository.findAll();


        List<Credito> creditos =
                creditoRepository.findAll();


        List<Cuota> cuotas =
                cuotaRepository.findAll();


        /*
         * Créditos agrupados por cliente.
         */
        Map<Long, List<Credito>> creditosPorCliente =
                agruparCreditosPorCliente(
                        creditos
                );


        /*
         * Cuotas agrupadas directamente
         * por cliente.
         */
        Map<Long, List<Cuota>> cuotasPorCliente =
                agruparCuotasPorCliente(
                        cuotas
                );


        List<ClienteResumenDto> resumenClientes =
                new ArrayList<>();


        for (Cliente cliente : clientes) {

            List<Credito> creditosCliente =
                    creditosPorCliente.getOrDefault(
                            cliente.getIdCliente(),
                            List.of()
                    );


            List<Cuota> cuotasCliente =
                    cuotasPorCliente.getOrDefault(
                            cliente.getIdCliente(),
                            List.of()
                    );


            ClienteResumenDto resumen =
                    construirResumenCliente(
                            cliente,
                            creditosCliente,
                            cuotasCliente
                    );


            resumenClientes.add(
                    resumen
            );

        }


        /*
         * Los clientes registrados más recientemente
         * aparecen primero.
         */
        resumenClientes.sort(
                Comparator.comparing(
                        ClienteResumenDto::getFechaRegistro,
                        Comparator.nullsLast(
                                Comparator.reverseOrder()
                        )
                )
        );


        long total =
                resumenClientes.size();


        long activos =
                resumenClientes
                        .stream()
                        .filter(cliente ->
                                cliente.getEstado()
                                        == EstadoCliente.ACTIVO
                        )
                        .count();


        long inactivos =
                resumenClientes
                        .stream()
                        .filter(cliente ->
                                cliente.getEstado()
                                        == EstadoCliente.INACTIVO
                        )
                        .count();


        /*
         * Advertencia incluye:
         *
         * - POR_VENCER
         * - EN_GRACIA
         *
         * Ambos casos son buenos candidatos
         * para enviar recordatorio.
         */
        long advertencia =
                resumenClientes
                        .stream()
                        .filter(cliente ->

                                cliente.getSituacionPago()
                                        == SituacionPagoCliente.POR_VENCER

                                        ||

                                        cliente.getSituacionPago()
                                                == SituacionPagoCliente.EN_GRACIA

                        )
                        .count();


        long mora =
                resumenClientes
                        .stream()
                        .filter(cliente ->
                                cliente.getSituacionPago()
                                        == SituacionPagoCliente.EN_MORA
                        )
                        .count();


        return new ClientePanelDto(
                total,
                activos,
                inactivos,
                advertencia,
                mora,
                resumenClientes
        );

    }


    /* =========================================================
       CONSTRUIR FILA DE CLIENTE
       ========================================================= */

    private ClienteResumenDto construirResumenCliente(
            Cliente cliente,
            List<Credito> creditos,
            List<Cuota> cuotas
    ) {

        ClienteResumenDto dto =
                new ClienteResumenDto();


        dto.setIdCliente(
                cliente.getIdCliente()
        );


        dto.setCodigoCliente(
                cliente.getCodigoCliente()
        );


        dto.setTipoOrigen(
                cliente.getTipoOrigen()
        );


        dto.setEstado(
                cliente.getEstado()
        );


        dto.setFechaRegistro(
                cliente.getFechaRegistro()
        );


        Persona persona =
                cliente.getPersona();


        if (persona != null) {

            dto.setNombreCompleto(
                    construirNombreCompleto(
                            persona
                    )
            );


            dto.setDni(
                    persona.getDni()
            );


            dto.setTelefono(
                    persona.getTelefono()
            );


            dto.setEmail(
                    persona.getEmail()
            );

        }


        /*
         * Cliente sin ningún crédito.
         */
        if (creditos == null
                || creditos.isEmpty()) {

            dto.setSituacionPago(
                    SituacionPagoCliente.SIN_CREDITO
            );


            dto.setMontoCuotaReferencia(
                    BigDecimal.ZERO
                            .setScale(2)
            );


            return dto;

        }


        List<Cuota> pendientes =
                cuotas
                        .stream()
                        .filter(this::tieneSaldoPendiente)
                        .sorted(
                                Comparator.comparing(
                                        Cuota::getFechaVencimiento
                                )
                        )
                        .toList();


        /*
         * Tiene créditos, pero ya no tiene
         * ninguna cuota pendiente.
         */
        if (pendientes.isEmpty()) {

            dto.setSituacionPago(
                    SituacionPagoCliente.AL_DIA
            );


            dto.setMontoCuotaReferencia(
                    BigDecimal.ZERO
                            .setScale(2)
            );


            return dto;

        }


        LocalDate hoy =
                LocalDate.now(
                        ZONA_CREDINEYRA
                );


        /*
         * La primera cuota pendiente es la más
         * importante para la tabla.
         */
        Cuota cuotaReferencia =
                pendientes.get(0);


        dto.setFechaCuotaReferencia(
                cuotaReferencia
                        .getFechaVencimiento()
        );


        dto.setMontoCuotaReferencia(
                calcularSaldoCuota(
                        cuotaReferencia
                )
        );


        /*
         * Primero revisamos mora.
         *
         * Basta con que exista UNA cuota que
         * ya haya superado los días de gracia.
         */
        boolean tieneMora =
                pendientes
                        .stream()
                        .anyMatch(cuota ->

                                hoy.isAfter(
                                        cuota
                                                .getFechaVencimiento()
                                                .plusDays(
                                                        DIAS_GRACIA
                                                )
                                )

                        );


        if (tieneMora) {

            dto.setSituacionPago(
                    SituacionPagoCliente.EN_MORA
            );

            return dto;

        }


        LocalDate fechaCuota =
                cuotaReferencia
                        .getFechaVencimiento();


        /*
         * Venció recientemente, pero todavía
         * está dentro del período de gracia.
         */
        if (fechaCuota.isBefore(hoy)) {

            dto.setSituacionPago(
                    SituacionPagoCliente.EN_GRACIA
            );

            return dto;

        }


        /*
         * Vence hoy o dentro de los próximos
         * siete días.
         */
        LocalDate limiteAdvertencia =
                hoy.plusDays(
                        DIAS_ADVERTENCIA
                );


        if (!fechaCuota.isAfter(
                limiteAdvertencia
        )) {

            dto.setSituacionPago(
                    SituacionPagoCliente.POR_VENCER
            );

            return dto;

        }


        dto.setSituacionPago(
                SituacionPagoCliente.AL_DIA
        );


        return dto;
    }


    /* =========================================================
       AGRUPAR CRÉDITOS POR CLIENTE
       ========================================================= */

    private Map<Long, List<Credito>>
    agruparCreditosPorCliente(
            List<Credito> creditos
    ) {

        Map<Long, List<Credito>> resultado =
                new HashMap<>();


        for (Credito credito : creditos) {

            if (credito.getCliente() == null
                    || credito.getCliente()
                    .getIdCliente() == null) {

                continue;

            }


            Long idCliente =
                    credito.getCliente()
                            .getIdCliente();


            resultado
                    .computeIfAbsent(
                            idCliente,
                            key -> new ArrayList<>()
                    )
                    .add(
                            credito
                    );

        }


        return resultado;
    }


    /* =========================================================
       AGRUPAR CUOTAS POR CLIENTE
       ========================================================= */

    private Map<Long, List<Cuota>>
    agruparCuotasPorCliente(
            List<Cuota> cuotas
    ) {

        Map<Long, List<Cuota>> resultado =
                new HashMap<>();


        for (Cuota cuota : cuotas) {

            if (cuota.getCredito() == null
                    || cuota.getCredito()
                    .getCliente() == null
                    || cuota.getCredito()
                    .getCliente()
                    .getIdCliente() == null) {

                continue;

            }


            Long idCliente =
                    cuota.getCredito()
                            .getCliente()
                            .getIdCliente();


            resultado
                    .computeIfAbsent(
                            idCliente,
                            key -> new ArrayList<>()
                    )
                    .add(
                            cuota
                    );

        }


        return resultado;
    }


    /* =========================================================
       SABER SI CUOTA AÚN TIENE SALDO
       ========================================================= */

    private boolean tieneSaldoPendiente(
            Cuota cuota
    ) {

        return calcularSaldoCuota(
                cuota
        )
                .compareTo(
                        BigDecimal.ZERO
                ) > 0;

    }


    /* =========================================================
       CALCULAR SALDO REAL DE CUOTA
       ========================================================= */

    private BigDecimal calcularSaldoCuota(
            Cuota cuota
    ) {

        BigDecimal capital =
                nz(
                        cuota.getCapitalPendiente()
                );


        BigDecimal interes =
                nz(
                        cuota.getInteresPendiente()
                );


        BigDecimal regularizacion =
                nz(
                        cuota.getCapitalRegularizadoPendiente()
                );


        return capital
                .add(
                        interes
                )
                .add(
                        regularizacion
                )
                .setScale(2);

    }


    /* =========================================================
       NOMBRE COMPLETO
       ========================================================= */

    private String construirNombreCompleto(
            Persona persona
    ) {

        String nombres =
                persona.getNombres() == null
                        ? ""
                        : persona.getNombres().trim();


        String apellidos =
                persona.getApellidos() == null
                        ? ""
                        : persona.getApellidos().trim();


        return (
                nombres
                        + " "
                        + apellidos
        ).trim();

    }


    /* =========================================================
       NULL A CERO
       ========================================================= */

    private BigDecimal nz(
            BigDecimal valor
    ) {

        return valor == null
                ? BigDecimal.ZERO.setScale(2)
                : valor;

    }

}