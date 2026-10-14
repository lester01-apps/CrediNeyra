package com.credineyra.service;

import com.credineyra.dto.PlanRegularizacionRegistroDto;
import com.credineyra.entity.Credito;
import com.credineyra.entity.Cuota;
import com.credineyra.entity.PlanRegularizacion;
import com.credineyra.entity.PlanRegularizacionDetalle;
import com.credineyra.entity.Usuario;
import com.credineyra.entity.enums.EstadoCredito;
import com.credineyra.entity.enums.EstadoCuota;
import com.credineyra.entity.enums.EstadoPlanDetalle;
import com.credineyra.entity.enums.EstadoPlanRegularizacion;
import com.credineyra.exception.OperacionNoPermitidaException;
import com.credineyra.exception.RecursoNoEncontradoException;
import com.credineyra.exception.ReglaNegocioException;
import com.credineyra.repository.CreditoRepository;
import com.credineyra.repository.CuotaRepository;
import com.credineyra.repository.PlanRegularizacionDetalleRepository;
import com.credineyra.repository.PlanRegularizacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class PlanRegularizacionService {

    private final PlanRegularizacionRepository planRepository;
    private final PlanRegularizacionDetalleRepository detalleRepository;
    private final CreditoRepository creditoRepository;
    private final CuotaRepository cuotaRepository;
    private final AuditoriaService auditoriaService;

    public PlanRegularizacionService(
            PlanRegularizacionRepository planRepository,
            PlanRegularizacionDetalleRepository detalleRepository,
            CreditoRepository creditoRepository,
            CuotaRepository cuotaRepository,
            AuditoriaService auditoriaService
    ) {
        this.planRepository = planRepository;
        this.detalleRepository = detalleRepository;
        this.creditoRepository = creditoRepository;
        this.cuotaRepository = cuotaRepository;
        this.auditoriaService = auditoriaService;
    }

    public PlanRegularizacion crearPlan(
            PlanRegularizacionRegistroDto dto,
            Usuario usuarioCreador
    ) {

        if (dto == null) {
            throw new ReglaNegocioException(
                    "Los datos del plan son obligatorios"
            );
        }

        if (usuarioCreador == null
                || usuarioCreador.getIdUsuario() == null) {

            throw new ReglaNegocioException(
                    "Debe identificarse el administrador"
            );
        }

        Credito credito =
                creditoRepository
                        .findById(dto.getIdCredito())
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "Crédito no encontrado"
                                )
                        );

        if (credito.getEstado()
                == EstadoCredito.FINALIZADO) {

            throw new OperacionNoPermitidaException(
                    "No se puede regularizar un crédito finalizado"
            );
        }

        boolean existePlanActivo =
                planRepository
                        .findByCredito_IdCreditoOrderByFechaCreacionDesc(
                                credito.getIdCredito()
                        )
                        .stream()
                        .anyMatch(plan ->
                                plan.getEstado()
                                        == EstadoPlanRegularizacion.ACTIVO
                        );

        if (existePlanActivo) {
            throw new OperacionNoPermitidaException(
                    "El crédito ya tiene un plan de regularización activo"
            );
        }

        List<Cuota> cuotas =
                cuotaRepository
                        .findByCredito_IdCreditoOrderByNumeroCuotaAsc(
                                credito.getIdCredito()
                        );

        LocalDate hoy = LocalDate.now();

        /*
         * Capital vencido de períodos anteriores.
         * No incluye capital futuro.
         */
        BigDecimal capitalRegularizar =
                cuotas.stream()
                        .filter(cuota ->
                                cuota.getFechaVencimiento()
                                        .isBefore(hoy)
                        )
                        .filter(cuota ->
                                cuota.getEstado()
                                        != EstadoCuota.PAGADA
                        )
                        .map(Cuota::getCapitalPendiente)
                        .map(this::nz)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        capitalRegularizar =
                dinero(capitalRegularizar);

        if (capitalRegularizar.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            throw new ReglaNegocioException(
                    "No existe capital vencido para regularizar"
            );
        }

        /*
         * Solo se utilizan cuotas existentes
         * que todavía quedan dentro del contrato.
         */
        List<Cuota> cuotasDestino =
                cuotas.stream()
                        .filter(cuota ->
                                cuota.getEstado()
                                        != EstadoCuota.PAGADA
                        )
                        .filter(cuota ->
                                !cuota.getFechaVencimiento()
                                        .isBefore(hoy)
                        )
                        .toList();

        if (dto.getCantidadAbonos() == null
                || dto.getCantidadAbonos() <= 0) {

            throw new ReglaNegocioException(
                    "La cantidad de abonos debe ser mayor que cero"
            );
        }

        if (dto.getCantidadAbonos()
                > cuotasDestino.size()) {

            throw new ReglaNegocioException(
                    "El plan no puede tener más abonos que cuotas restantes"
            );
        }

        if (dto.getMotivo() == null
                || dto.getMotivo().isBlank()) {

            throw new ReglaNegocioException(
                    "El motivo es obligatorio"
            );
        }

        PlanRegularizacion plan =
                new PlanRegularizacion();

        plan.setCredito(credito);
        plan.setUsuarioCreador(usuarioCreador);
        plan.setCapitalPendienteOrigen(
                capitalRegularizar
        );
        plan.setCantidadAbonos(
                dto.getCantidadAbonos()
        );
        plan.setMotivo(
                dto.getMotivo().trim()
        );
        plan.setEstado(
                EstadoPlanRegularizacion.ACTIVO
        );

        PlanRegularizacion guardado =
                planRepository.save(plan);

        List<BigDecimal> montos =
                distribuir(
                        capitalRegularizar,
                        dto.getCantidadAbonos()
                );

        for (int i = 0;
             i < dto.getCantidadAbonos();
             i++) {

            Cuota cuota =
                    cuotasDestino.get(i);

            BigDecimal monto =
                    montos.get(i);

            cuota.setCapitalRegularizadoAsignado(
                    nz(
                            cuota.getCapitalRegularizadoAsignado()
                    ).add(monto)
            );

            cuota.setCapitalRegularizadoPendiente(
                    nz(
                            cuota.getCapitalRegularizadoPendiente()
                    ).add(monto)
            );

            cuotaRepository.save(cuota);

            PlanRegularizacionDetalle detalle =
                    new PlanRegularizacionDetalle();

            detalle.setPlan(guardado);
            detalle.setCuota(cuota);
            detalle.setCredito(credito);
            detalle.setMontoCapitalRegularizado(
                    monto
            );
            detalle.setEstado(
                    EstadoPlanDetalle.PENDIENTE
            );

            detalleRepository.save(detalle);
        }

        credito.setEstado(
                EstadoCredito.EN_REGULARIZACION
        );

        creditoRepository.save(credito);

        auditoriaService.registrarEvento(
                usuarioCreador,
                "CREAR_PLAN_REGULARIZACION",
                "PLAN_REGULARIZACION",
                String.valueOf(
                        guardado.getIdPlan()
                ),
                dto.getMotivo(),
                null,
                "{\"capital\":\""
                        + capitalRegularizar
                        + "\",\"abonos\":"
                        + dto.getCantidadAbonos()
                        + "}",
                null
        );

        return guardado;
    }

    /*
     * Se llama después de aprobar un pago
     * con capital regularizado.
     */
    public void revisarCumplimientoCuota(
            Long idCuota
    ) {

        Cuota cuota =
                cuotaRepository
                        .findById(idCuota)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "Cuota no encontrada"
                                )
                        );

        if (nz(
                cuota.getCapitalRegularizadoPendiente()
        ).compareTo(BigDecimal.ZERO) != 0) {
            return;
        }

        List<PlanRegularizacionDetalle> detalles =
                detalleRepository
                        .findByCuota_IdCuota(
                                idCuota
                        );

        for (PlanRegularizacionDetalle detalle :
                detalles) {

            if (detalle.getEstado()
                    != EstadoPlanDetalle.PAGADO) {

                detalle.setEstado(
                        EstadoPlanDetalle.PAGADO
                );

                detalleRepository.save(detalle);
            }

            revisarPlanCompleto(
                    detalle.getPlan()
            );
        }
    }

    private void revisarPlanCompleto(
            PlanRegularizacion plan
    ) {

        if (plan.getEstado()
                != EstadoPlanRegularizacion.ACTIVO) {
            return;
        }

        List<PlanRegularizacionDetalle> detalles =
                detalleRepository
                        .findByPlan_IdPlan(
                                plan.getIdPlan()
                        );

        boolean completado =
                !detalles.isEmpty()
                        && detalles.stream()
                        .allMatch(detalle ->
                                detalle.getEstado()
                                        == EstadoPlanDetalle.PAGADO
                        );

        if (!completado) {
            return;
        }

        plan.setEstado(
                EstadoPlanRegularizacion.COMPLETADO
        );

        planRepository.save(plan);

        Credito credito =
                plan.getCredito();

        List<Cuota> cuotas =
                cuotaRepository
                        .findByCredito_IdCreditoOrderByNumeroCuotaAsc(
                                credito.getIdCredito()
                        );

        boolean existeVencida =
                cuotas.stream()
                        .anyMatch(cuota ->
                                cuota.getEstado()
                                        == EstadoCuota.VENCIDA
                        );

        credito.setEstado(
                existeVencida
                        ? EstadoCredito.EN_MORA
                        : EstadoCredito.AL_DIA
        );

        creditoRepository.save(credito);
    }

    @Transactional(readOnly = true)
    public PlanRegularizacion buscarPorId(
            Long idPlan
    ) {

        return planRepository
                .findById(idPlan)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Plan de regularización no encontrado"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<PlanRegularizacion>
    listarPorCredito(
            Long idCredito
    ) {

        return planRepository
                .findByCredito_IdCreditoOrderByFechaCreacionDesc(
                        idCredito
                );
    }

    @Transactional(readOnly = true)
    public List<PlanRegularizacionDetalle>
    listarDetalles(
            Long idPlan
    ) {

        return detalleRepository
                .findByPlan_IdPlan(idPlan);
    }

    private List<BigDecimal> distribuir(
            BigDecimal total,
            int partes
    ) {

        BigDecimal totalRedondeado =
                dinero(total);

        BigDecimal base =
                dinero(
                        totalRedondeado.divide(
                                BigDecimal.valueOf(partes),
                                10,
                                RoundingMode.HALF_UP
                        )
                );

        if (base.multiply(
                        BigDecimal.valueOf(
                                partes - 1L
                        )
                )
                .compareTo(totalRedondeado) > 0) {

            base =
                    totalRedondeado.divide(
                                    BigDecimal.valueOf(partes),
                                    0,
                                    RoundingMode.DOWN
                            )
                            .setScale(2);
        }

        List<BigDecimal> resultado =
                new ArrayList<>();

        BigDecimal acumulado =
                BigDecimal.ZERO.setScale(2);

        for (int i = 0;
             i < partes;
             i++) {

            if (i == partes - 1) {

                resultado.add(
                        totalRedondeado
                                .subtract(acumulado)
                                .setScale(2)
                );

            } else {

                resultado.add(base);

                acumulado =
                        acumulado.add(base);
            }
        }

        return resultado;
    }

    private BigDecimal dinero(
            BigDecimal valor
    ) {

        if (valor == null) {
            return BigDecimal.ZERO.setScale(2);
        }

        return valor
                .setScale(
                        0,
                        RoundingMode.HALF_UP
                )
                .setScale(2);
    }

    private BigDecimal nz(
            BigDecimal valor
    ) {

        return valor == null
                ? BigDecimal.ZERO.setScale(2)
                : valor;
    }
}