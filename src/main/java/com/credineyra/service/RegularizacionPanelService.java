package com.credineyra.service;

import com.credineyra.dto.RegularizacionPanelDto;
import com.credineyra.entity.Credito;
import com.credineyra.entity.Cuota;
import com.credineyra.entity.enums.EstadoCuota;
import com.credineyra.entity.enums.EstadoPlanRegularizacion;
import com.credineyra.repository.CreditoRepository;
import com.credineyra.repository.CuotaRepository;
import com.credineyra.repository.PlanRegularizacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RegularizacionPanelService {

    private final CreditoRepository creditoRepository;

    private final CuotaRepository cuotaRepository;

    private final PlanRegularizacionRepository planRepository;

    public RegularizacionPanelService(
            CreditoRepository creditoRepository,
            CuotaRepository cuotaRepository,
            PlanRegularizacionRepository planRepository
    ) {

        this.creditoRepository =
                creditoRepository;

        this.cuotaRepository =
                cuotaRepository;

        this.planRepository =
                planRepository;
    }

    @Transactional(readOnly = true)
    public RegularizacionPanelDto obtener() {

        LocalDate hoy =
                LocalDate.now();

        List<RegularizacionPanelDto.Fila> filas =
                new ArrayList<>();

        for (
                Credito credito :
                creditoRepository.findAll()
        ) {

            List<Cuota> vencidas =
                    cuotaRepository
                            .findByCredito_IdCreditoOrderByFechaVencimientoAsc(
                                    credito.getIdCredito()
                            )
                            .stream()
                            .filter(
                                    c ->
                                            c.getEstado()
                                                    != EstadoCuota.PAGADA
                            )
                            .filter(
                                    c ->
                                            c.getFechaVencimiento()
                                                    .isBefore(hoy)
                            )
                            .filter(
                                    c ->
                                            saldo(c)
                                                    .compareTo(
                                                            BigDecimal.ZERO
                                                    ) > 0
                            )
                            .toList();

            if (vencidas.isEmpty()) {
                continue;
            }

            LocalDate fecha =
                    vencidas.getFirst()
                            .getFechaVencimiento();

            long dias =
                    ChronoUnit.DAYS
                            .between(
                                    fecha,
                                    hoy
                            );

            String situacion;

            if (
                    dias >= 1
                            &&
                            dias <= 3
            ) {

                situacion =
                        "REGULARIZACION";

            } else if (
                    !fecha.plusMonths(4)
                            .isAfter(hoy)
            ) {

                situacion =
                        "ATRASO_PROLONGADO";

            } else {

                situacion =
                        "EN_MORA";
            }

            BigDecimal deuda =
                    vencidas.stream()
                            .map(this::saldo)
                            .reduce(
                                    BigDecimal.ZERO.setScale(2),
                                    BigDecimal::add
                            );

            boolean planActivo =
                    planRepository
                            .findByCredito_IdCreditoOrderByFechaCreacionDesc(
                                    credito.getIdCredito()
                            )
                            .stream()
                            .anyMatch(
                                    p ->
                                            p.getEstado()
                                                    == EstadoPlanRegularizacion.ACTIVO
                            );

            var persona =
                    credito.getCliente()
                            .getPersona();

            String cliente =
                    (
                            persona.getNombres()
                                    + " "
                                    + persona.getApellidos()
                    ).trim();

            filas.add(
                    new RegularizacionPanelDto.Fila(

                            credito.getIdCredito(),

                            credito.getCodigoCredito(),

                            cliente,

                            deuda,

                            fecha,

                            dias,

                            situacion,

                            planActivo
                    )
            );
        }

        filas.sort(
                Comparator.comparingLong(
                                RegularizacionPanelDto.Fila::diasAtraso
                        )
                        .reversed()
        );

        long gracia =
                filas.stream()
                        .filter(
                                f ->
                                        f.situacion()
                                                .equals(
                                                        "REGULARIZACION"
                                                )
                        )
                        .count();

        long mora =
                filas.stream()
                        .filter(
                                f ->
                                        f.situacion()
                                                .equals(
                                                        "EN_MORA"
                                                )
                        )
                        .count();

        long prolongado =
                filas.stream()
                        .filter(
                                f ->
                                        f.situacion()
                                                .equals(
                                                        "ATRASO_PROLONGADO"
                                                )
                        )
                        .count();

        long activos =
                planRepository
                        .findByEstado(
                                EstadoPlanRegularizacion.ACTIVO
                        )
                        .size();

        return new RegularizacionPanelDto(
                gracia,
                mora,
                prolongado,
                activos,
                filas
        );
    }

    private BigDecimal saldo(
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
                );
    }

    private BigDecimal nz(
            BigDecimal valor
    ) {

        return valor == null
                ? BigDecimal.ZERO.setScale(2)
                : valor;
    }
}