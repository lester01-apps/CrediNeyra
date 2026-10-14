package com.credineyra.service;

import com.credineyra.dto.PagoAplicacionDto;
import com.credineyra.dto.PagoAprobacionDto;
import com.credineyra.entity.*;
import com.credineyra.entity.enums.EstadoCredito;
import com.credineyra.entity.enums.EstadoCuota;
import com.credineyra.entity.enums.EstadoPago;
import com.credineyra.exception.DatoDuplicadoException;
import com.credineyra.exception.OperacionNoPermitidaException;
import com.credineyra.exception.RecursoNoEncontradoException;
import com.credineyra.exception.ReglaNegocioException;
import com.credineyra.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class PagoService {

    /*
     * Zona horaria usada por crediNeyra
     * para las estadísticas del dashboard.
     */
    private static final ZoneId ZONA_CREDINEYRA =
            ZoneId.of("America/Lima");


    private final PagoRepository pagoRepository;
    private final PagoDetalleRepository pagoDetalleRepository;
    private final CuotaRepository cuotaRepository;
    private final CreditoRepository creditoRepository;
    private final AuditoriaService auditoriaService;


    public PagoService(
            PagoRepository pagoRepository,
            PagoDetalleRepository pagoDetalleRepository,
            CuotaRepository cuotaRepository,
            CreditoRepository creditoRepository,
            AuditoriaService auditoriaService
    ) {

        this.pagoRepository = pagoRepository;

        this.pagoDetalleRepository =
                pagoDetalleRepository;

        this.cuotaRepository =
                cuotaRepository;

        this.creditoRepository =
                creditoRepository;

        this.auditoriaService =
                auditoriaService;
    }


    /* =========================================================
       REGISTRAR PAGO
       ========================================================= */

    public Pago registrar(
            Pago pago
    ) {

        validarPago(pago);


        if (pagoRepository
                .findByIdempotencyKey(
                        pago.getIdempotencyKey()
                )
                .isPresent()) {

            throw new DatoDuplicadoException(
                    "El pago ya fue registrado"
            );

        }


        if (pago.getReferencia() != null
                && !pago.getReferencia().isBlank()
                && pagoRepository
                .existsByMetodoPagoAndReferencia(
                        pago.getMetodoPago(),
                        pago.getReferencia()
                )) {

            throw new DatoDuplicadoException(
                    "Ya existe un pago con la misma referencia"
            );

        }


        pago.setMonto(
                dinero(
                        pago.getMonto()
                )
        );


        if (pago.getEstado() == null) {

            pago.setEstado(
                    EstadoPago.PENDIENTE_VALIDACION
            );

        }


        return pagoRepository.save(
                pago
        );
    }


    /* =========================================================
       APROBAR PAGO
       ========================================================= */

    public Pago aprobar(
            Long idPago,
            Usuario validador,
            PagoAprobacionDto dto
    ) {

        if (validador == null
                || validador.getIdUsuario() == null) {

            throw new ReglaNegocioException(
                    "Debe identificarse el usuario que valida el pago"
            );

        }


        if (dto == null
                || dto.getAplicaciones() == null
                || dto.getAplicaciones().isEmpty()) {

            throw new ReglaNegocioException(
                    "Debe indicar cómo se aplicará el pago"
            );

        }


        Pago pago =
                buscarPorId(
                        idPago
                );


        if (pago.getEstado()
                != EstadoPago.PENDIENTE_VALIDACION) {

            throw new OperacionNoPermitidaException(
                    "El pago ya fue validado anteriormente"
            );

        }


        Credito credito =
                pago.getCredito();


        if (credito.getEstado()
                == EstadoCredito.FINALIZADO) {

            throw new OperacionNoPermitidaException(
                    "El crédito ya está finalizado"
            );

        }


        Set<Long> cuotasUsadas =
                new HashSet<>();


        BigDecimal sumaTotal =
                BigDecimal.ZERO.setScale(2);


        BigDecimal capitalAplicado =
                BigDecimal.ZERO.setScale(2);


        BigDecimal interesAplicado =
                BigDecimal.ZERO.setScale(2);


        BigDecimal regularizadoAplicado =
                BigDecimal.ZERO.setScale(2);


        boolean existeCapital =
                false;


        for (
                PagoAplicacionDto aplicacion :
                dto.getAplicaciones()
        ) {


            if (aplicacion.getIdCuota() == null) {

                throw new ReglaNegocioException(
                        "Debe seleccionar una cuota"
                );

            }


            if (!cuotasUsadas.add(
                    aplicacion.getIdCuota()
            )) {

                throw new ReglaNegocioException(
                        "Una cuota no puede repetirse en la misma aplicación"
                );

            }


            Cuota cuota =
                    cuotaRepository
                            .findById(
                                    aplicacion.getIdCuota()
                            )
                            .orElseThrow(() ->
                                    new RecursoNoEncontradoException(
                                            "Cuota no encontrada"
                                    )
                            );


            if (!cuota.getCredito()
                    .getIdCredito()
                    .equals(
                            credito.getIdCredito()
                    )) {

                throw new ReglaNegocioException(
                        "La cuota no pertenece al crédito del pago"
                );

            }


            /*
             * No se puede pagar capital/cuotas futuras.
             */
            if (cuota.getFechaVencimiento()
                    .isAfter(
                            LocalDate.now()
                    )) {

                throw new OperacionNoPermitidaException(
                        "No se puede adelantar una cuota futura"
                );

            }


            BigDecimal montoCapital =
                    dinero(
                            aplicacion.getMontoCapital()
                    );


            BigDecimal montoInteres =
                    dinero(
                            aplicacion.getMontoInteres()
                    );


            BigDecimal montoRegularizado =
                    dinero(
                            aplicacion
                                    .getMontoCapitalRegularizado()
                    );


            validarNoNegativo(
                    montoCapital,
                    "capital"
            );


            validarNoNegativo(
                    montoInteres,
                    "interés"
            );


            validarNoNegativo(
                    montoRegularizado,
                    "capital regularizado"
            );


            if (montoCapital.compareTo(
                    nz(
                            cuota.getCapitalPendiente()
                    )
            ) > 0) {

                throw new ReglaNegocioException(
                        "El capital aplicado supera el capital pendiente de la cuota"
                );

            }


            if (montoInteres.compareTo(
                    nz(
                            cuota.getInteresPendiente()
                    )
            ) > 0) {

                throw new ReglaNegocioException(
                        "El interés aplicado supera el interés pendiente de la cuota"
                );

            }


            if (montoRegularizado.compareTo(
                    nz(
                            cuota.getCapitalRegularizadoPendiente()
                    )
            ) > 0) {

                throw new ReglaNegocioException(
                        "El capital regularizado supera el saldo asignado"
                );

            }


            BigDecimal totalDetalle =
                    montoCapital
                            .add(
                                    montoInteres
                            )
                            .add(
                                    montoRegularizado
                            );


            if (totalDetalle
                    .compareTo(
                            BigDecimal.ZERO
                    ) <= 0) {

                throw new ReglaNegocioException(
                        "Cada detalle debe aplicar un monto mayor que cero"
                );

            }


            if (montoCapital
                    .add(
                            montoRegularizado
                    )
                    .compareTo(
                            BigDecimal.ZERO
                    ) > 0) {

                existeCapital =
                        true;

            }


            PagoDetalle detalle =
                    new PagoDetalle();


            detalle.setPago(
                    pago
            );


            detalle.setCuota(
                    cuota
            );


            detalle.setCredito(
                    credito
            );


            detalle.setMontoCapital(
                    montoCapital
            );


            detalle.setMontoInteres(
                    montoInteres
            );


            detalle.setMontoCapitalRegularizado(
                    montoRegularizado
            );


            detalle.setMontoTotal(
                    totalDetalle
            );


            pagoDetalleRepository.save(
                    detalle
            );


            /* =================================================
               ACTUALIZAR CUOTA
               ================================================= */

            cuota.setCapitalPagado(
                    nz(
                            cuota.getCapitalPagado()
                    )
                            .add(
                                    montoCapital
                            )
            );


            cuota.setCapitalPendiente(
                    nz(
                            cuota.getCapitalPendiente()
                    )
                            .subtract(
                                    montoCapital
                            )
            );


            cuota.setInteresPagado(
                    nz(
                            cuota.getInteresPagado()
                    )
                            .add(
                                    montoInteres
                            )
            );


            cuota.setInteresPendiente(
                    nz(
                            cuota.getInteresPendiente()
                    )
                            .subtract(
                                    montoInteres
                            )
            );


            cuota.setCapitalRegularizadoPagado(
                    nz(
                            cuota.getCapitalRegularizadoPagado()
                    )
                            .add(
                                    montoRegularizado
                            )
            );


            cuota.setCapitalRegularizadoPendiente(
                    nz(
                            cuota.getCapitalRegularizadoPendiente()
                    )
                            .subtract(
                                    montoRegularizado
                            )
            );


            actualizarEstadoCuota(
                    cuota
            );


            cuotaRepository.save(
                    cuota
            );


            sumaTotal =
                    sumaTotal.add(
                            totalDetalle
                    );


            capitalAplicado =
                    capitalAplicado.add(
                            montoCapital
                    );


            interesAplicado =
                    interesAplicado.add(
                            montoInteres
                    );


            regularizadoAplicado =
                    regularizadoAplicado.add(
                            montoRegularizado
                    );

        }


        /* =====================================================
           VALIDAR TOTAL DEL PAGO
           ===================================================== */

        if (sumaTotal.compareTo(
                pago.getMonto()
        ) != 0) {

            throw new ReglaNegocioException(
                    "La suma de los detalles debe ser exactamente igual al monto del pago"
            );

        }


        /*
         * Si todo el pago fue exclusivamente a interés,
         * la condición del crédito debe permitirlo.
         */
        if (!existeCapital
                && !Boolean.TRUE.equals(
                credito.getCondicion()
                        .getPermitePagoSoloInteres()
        )) {

            throw new OperacionNoPermitidaException(
                    "Este crédito no permite pagos solo de interés"
            );

        }


        BigDecimal capitalTotalAplicado =
                capitalAplicado
                        .add(
                                regularizadoAplicado
                        );


        if (capitalTotalAplicado.compareTo(
                nz(
                        credito.getCapitalPendiente()
                )
        ) > 0) {

            throw new ReglaNegocioException(
                    "El capital aplicado supera el saldo pendiente del crédito"
            );

        }


        if (interesAplicado.compareTo(
                nz(
                        credito.getInteresPendiente()
                )
        ) > 0) {

            throw new ReglaNegocioException(
                    "El interés aplicado supera el saldo de interés del crédito"
            );

        }


        /* =====================================================
           ACTUALIZAR CRÉDITO
           ===================================================== */

        credito.setCapitalPagado(
                nz(
                        credito.getCapitalPagado()
                )
                        .add(
                                capitalTotalAplicado
                        )
        );


        credito.setCapitalPendiente(
                nz(
                        credito.getCapitalPendiente()
                )
                        .subtract(
                                capitalTotalAplicado
                        )
        );


        credito.setInteresPagado(
                nz(
                        credito.getInteresPagado()
                )
                        .add(
                                interesAplicado
                        )
        );


        credito.setInteresPendiente(
                nz(
                        credito.getInteresPendiente()
                )
                        .subtract(
                                interesAplicado
                        )
        );


        if (credito.getCapitalPendiente()
                .compareTo(
                        BigDecimal.ZERO
                ) == 0
                &&
                credito.getInteresPendiente()
                        .compareTo(
                                BigDecimal.ZERO
                        ) == 0) {

            credito.setEstado(
                    EstadoCredito.FINALIZADO
            );

        }


        creditoRepository.save(
                credito
        );


        /* =====================================================
           APROBAR PAGO
           ===================================================== */

        pago.setValidadoPor(
                validador
        );


        pago.setFechaValidacion(
                OffsetDateTime.now()
        );


        pago.setEstado(
                EstadoPago.APROBADO
        );


        Pago aprobado =
                pagoRepository.save(
                        pago
                );


        /* =====================================================
           AUDITORÍA
           ===================================================== */

        auditoriaService.registrarEvento(
                validador,
                "APROBAR_PAGO",
                "PAGO",
                String.valueOf(
                        idPago
                ),
                "Pago aprobado",
                null,
                "{\"monto\":\""
                        + pago.getMonto()
                        + "\"}",
                null
        );


        return aprobado;
    }


    /* =========================================================
       RECHAZAR PAGO
       ========================================================= */

    public Pago rechazar(
            Long idPago,
            Usuario validador,
            String motivo
    ) {

        Pago pago =
                buscarPorId(
                        idPago
                );


        if (pago.getEstado()
                != EstadoPago.PENDIENTE_VALIDACION) {

            throw new OperacionNoPermitidaException(
                    "Solo se puede rechazar un pago pendiente"
            );

        }


        pago.setEstado(
                EstadoPago.RECHAZADO
        );


        pago.setValidadoPor(
                validador
        );


        pago.setFechaValidacion(
                OffsetDateTime.now()
        );


        Pago rechazado =
                pagoRepository.save(
                        pago
                );


        if (validador != null) {

            auditoriaService.registrarEvento(
                    validador,
                    "RECHAZAR_PAGO",
                    "PAGO",
                    String.valueOf(
                            idPago
                    ),
                    motivo,
                    null,
                    null,
                    null
            );

        }


        return rechazado;
    }


    /* =========================================================
       ACTUALIZAR ESTADO DE CUOTA
       ========================================================= */

    private void actualizarEstadoCuota(
            Cuota cuota
    ) {

        BigDecimal saldo =
                nz(
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
                        );


        if (saldo.compareTo(
                BigDecimal.ZERO
        ) == 0) {

            cuota.setEstado(
                    EstadoCuota.PAGADA
            );

            return;

        }


        if (LocalDate.now()
                .isAfter(
                        cuota.getFechaVencimiento()
                                .plusDays(3)
                )) {

            cuota.setEstado(
                    EstadoCuota.VENCIDA
            );

            return;

        }


        boolean tienePago =
                nz(
                        cuota.getCapitalPagado()
                )
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


        cuota.setEstado(
                tienePago
                        ? EstadoCuota.PARCIAL
                        : EstadoCuota.PENDIENTE
        );

    }


    /* =========================================================
       VALIDAR PAGO
       ========================================================= */

    private void validarPago(
            Pago pago
    ) {

        if (pago == null) {

            throw new ReglaNegocioException(
                    "Los datos del pago son obligatorios"
            );

        }


        if (pago.getCredito() == null) {

            throw new ReglaNegocioException(
                    "El pago debe pertenecer a un crédito"
            );

        }


        if (pago.getRegistradoPor() == null) {

            throw new ReglaNegocioException(
                    "Debe indicarse quién registra el pago"
            );

        }


        if (pago.getMonto() == null
                || pago.getMonto()
                .compareTo(
                        BigDecimal.ZERO
                ) <= 0) {

            throw new ReglaNegocioException(
                    "El monto debe ser mayor que cero"
            );

        }


        if (pago.getMetodoPago() == null) {

            throw new ReglaNegocioException(
                    "El método de pago es obligatorio"
            );

        }


        if (pago.getIdempotencyKey() == null
                || pago.getIdempotencyKey()
                .isBlank()) {

            throw new ReglaNegocioException(
                    "La clave de idempotencia es obligatoria"
            );

        }

    }


    /* =========================================================
       VALIDAR MONTO NO NEGATIVO
       ========================================================= */

    private void validarNoNegativo(
            BigDecimal monto,
            String concepto
    ) {

        if (monto.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            throw new ReglaNegocioException(
                    "El monto de "
                            + concepto
                            + " no puede ser negativo"
            );

        }

    }


    /* =========================================================
       REDONDEO DE DINERO
       ========================================================= */

    private BigDecimal dinero(
            BigDecimal valor
    ) {

        if (valor == null) {

            return BigDecimal.ZERO
                    .setScale(2);

        }


        return valor
                .setScale(
                        0,
                        RoundingMode.HALF_UP
                )
                .setScale(2);

    }


    /* =========================================================
       VALOR NULO A CERO
       ========================================================= */

    private BigDecimal nz(
            BigDecimal valor
    ) {

        return valor == null
                ? BigDecimal.ZERO.setScale(2)
                : valor;

    }


    /* =========================================================
       BUSCAR PAGO POR ID
       ========================================================= */

    @Transactional(readOnly = true)
    public Pago buscarPorId(
            Long idPago
    ) {

        return pagoRepository
                .findById(
                        idPago
                )
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Pago no encontrado"
                        )
                );

    }


    /* =========================================================
       LISTAR TODOS LOS PAGOS
       ========================================================= */

    @Transactional(readOnly = true)
    public List<Pago> listarTodos() {

        return pagoRepository.findAll();

    }


    /* =========================================================
       LISTAR PAGOS POR CRÉDITO
       ========================================================= */

    @Transactional(readOnly = true)
    public List<Pago> listarPorCredito(
            Long idCredito
    ) {

        return pagoRepository
                .findByCredito_IdCreditoOrderByFechaPagoDesc(
                        idCredito
                );

    }


    /* =========================================================
       LISTAR PAGOS PENDIENTES
       ========================================================= */

    @Transactional(readOnly = true)
    public List<Pago> listarPendientes() {

        return pagoRepository.findByEstado(
                EstadoPago.PENDIENTE_VALIDACION
        );

    }


    /* =========================================================
       DASHBOARD
       TOTAL DE PAGOS REGISTRADOS
       ========================================================= */

    @Transactional(readOnly = true)
    public long contarPagos() {

        return pagoRepository.count();

    }


    /* =========================================================
       DASHBOARD
       PAGOS APROBADOS DE LA SEMANA ACTUAL

       Devuelve 7 valores:
       LUN - MAR - MIE - JUE - VIE - SAB - DOM
       ========================================================= */

    @Transactional(readOnly = true)
    public List<BigDecimal> obtenerPagosSemanaActual() {

        LocalDate hoy =
                LocalDate.now(
                        ZONA_CREDINEYRA
                );


        LocalDate lunes =
                hoy.with(
                        TemporalAdjusters
                                .previousOrSame(
                                        DayOfWeek.MONDAY
                                )
                );


        List<BigDecimal> resultado =
                new ArrayList<>();


        for (
                int i = 0;
                i < 7;
                i++
        ) {

            LocalDate dia =
                    lunes.plusDays(
                            i
                    );


            BigDecimal total =
                    sumarPagosAprobados(
                            dia,
                            dia.plusDays(1)
                    );


            resultado.add(
                    total
            );

        }


        return resultado;
    }


    /* =========================================================
       DASHBOARD
       PAGOS APROBADOS DEL MES ACTUAL

       Semana 1 = días 1 - 7
       Semana 2 = días 8 - 14
       Semana 3 = días 15 - 21
       Semana 4 = días 22 - 28
       Semana 5 = días 29 - fin del mes
       ========================================================= */

    @Transactional(readOnly = true)
    public List<BigDecimal> obtenerPagosMesActual() {

        LocalDate hoy =
                LocalDate.now(
                        ZONA_CREDINEYRA
                );


        LocalDate primerDiaMes =
                hoy.withDayOfMonth(1);


        LocalDate primerDiaMesSiguiente =
                primerDiaMes.plusMonths(1);


        List<BigDecimal> resultado =
                new ArrayList<>();


        for (
                int semana = 0;
                semana < 5;
                semana++
        ) {

            LocalDate inicio =
                    primerDiaMes.plusDays(
                            semana * 7L
                    );


            /*
             * Algunos meses terminan antes
             * de comenzar una posible semana 5.
             */
            if (!inicio.isBefore(
                    primerDiaMesSiguiente
            )) {

                resultado.add(
                        BigDecimal.ZERO
                                .setScale(2)
                );

                continue;

            }


            LocalDate fin =
                    inicio.plusDays(7);


            /*
             * La última semana no debe entrar
             * en el siguiente mes.
             */
            if (fin.isAfter(
                    primerDiaMesSiguiente
            )) {

                fin =
                        primerDiaMesSiguiente;

            }


            BigDecimal total =
                    sumarPagosAprobados(
                            inicio,
                            fin
                    );


            resultado.add(
                    total
            );

        }


        return resultado;
    }


    /* =========================================================
       DASHBOARD
       PAGOS APROBADOS DEL AÑO ACTUAL

       Devuelve 12 valores:
       ENE - FEB - MAR ... DIC
       ========================================================= */

    @Transactional(readOnly = true)
    public List<BigDecimal> obtenerPagosAnioActual() {

        LocalDate hoy =
                LocalDate.now(
                        ZONA_CREDINEYRA
                );


        int anio =
                hoy.getYear();


        List<BigDecimal> resultado =
                new ArrayList<>();


        for (
                int mes = 1;
                mes <= 12;
                mes++
        ) {

            LocalDate inicioMes =
                    LocalDate.of(
                            anio,
                            mes,
                            1
                    );


            LocalDate finMes =
                    inicioMes.plusMonths(1);


            BigDecimal total =
                    sumarPagosAprobados(
                            inicioMes,
                            finMes
                    );


            resultado.add(
                    total
            );

        }


        return resultado;
    }


    /* =========================================================
       DASHBOARD
       SUMAR PAGOS APROBADOS DE UN PERÍODO

       inicio       = incluido
       finExclusivo = excluido
       ========================================================= */

    private BigDecimal sumarPagosAprobados(
            LocalDate inicio,
            LocalDate finExclusivo
    ) {

        OffsetDateTime fechaInicio =
                inicio
                        .atStartOfDay(
                                ZONA_CREDINEYRA
                        )
                        .toOffsetDateTime();


        OffsetDateTime fechaFin =
                finExclusivo
                        .atStartOfDay(
                                ZONA_CREDINEYRA
                        )
                        .toOffsetDateTime();


        List<Pago> pagos =
                pagoRepository
                        .findByEstadoAndFechaPagoGreaterThanEqualAndFechaPagoLessThan(
                                EstadoPago.APROBADO,
                                fechaInicio,
                                fechaFin
                        );


        return pagos
                .stream()
                .map(
                        Pago::getMonto
                )
                .filter(
                        monto ->
                                monto != null
                )
                .reduce(
                        BigDecimal.ZERO
                                .setScale(2),
                        BigDecimal::add
                )
                .setScale(2);

    }

}