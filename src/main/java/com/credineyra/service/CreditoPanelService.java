package com.credineyra.service;

import com.credineyra.dto.CreditoPanelDto;
import com.credineyra.dto.CreditoResumenDto;
import com.credineyra.entity.Cliente;
import com.credineyra.entity.CondicionCredito;
import com.credineyra.entity.Credito;
import com.credineyra.entity.Cuota;
import com.credineyra.entity.Pago;
import com.credineyra.entity.Persona;
import com.credineyra.entity.enums.EstadoCredito;
import com.credineyra.entity.enums.EstadoPago;
import com.credineyra.repository.CreditoRepository;
import com.credineyra.repository.CuotaRepository;
import com.credineyra.repository.PagoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class CreditoPanelService {

    /*
     * Un crédito con una cuota dentro de los próximos
     * 7 días aparecerá visualmente como POR_VENCER.
     */
    private static final int DIAS_ADVERTENCIA = 7;

    private final CreditoRepository creditoRepository;
    private final CuotaRepository cuotaRepository;
    private final PagoRepository pagoRepository;

    public CreditoPanelService(
            CreditoRepository creditoRepository,
            CuotaRepository cuotaRepository,
            PagoRepository pagoRepository
    ) {
        this.creditoRepository =
                creditoRepository;

        this.cuotaRepository =
                cuotaRepository;

        this.pagoRepository =
                pagoRepository;
    }

    public CreditoPanelDto obtenerPanel() {

        List<Credito> creditos =
                creditoRepository.findAll();

        List<CreditoResumenDto> filas =
                new ArrayList<>();

        LocalDate hoy =
                LocalDate.now();

        long activos = 0;
        long alDia = 0;
        long porVencer = 0;
        long enMora = 0;
        long atrasoProlongado = 0;

        BigDecimal capitalPrestado =
                cero();

        BigDecimal capitalRecuperado =
                cero();

        BigDecimal interesGenerado =
                cero();

        BigDecimal saldoPendiente =
                cero();

        for (Credito credito : creditos) {

            CreditoResumenDto fila =
                    convertirCredito(
                            credito,
                            hoy
                    );

            filas.add(fila);

            capitalPrestado =
                    capitalPrestado.add(
                            nz(
                                    credito.getCapitalOriginal()
                            )
                    );

            capitalRecuperado =
                    capitalRecuperado.add(
                            nz(
                                    credito.getCapitalPagado()
                            )
                    );

            interesGenerado =
                    interesGenerado.add(
                            nz(
                                    credito.getInteresGenerado()
                            )
                    );

            saldoPendiente =
                    saldoPendiente
                            .add(
                                    nz(
                                            credito.getCapitalPendiente()
                                    )
                            )
                            .add(
                                    nz(
                                            credito.getInteresPendiente()
                                    )
                            );

            if (
                    credito.getEstado()
                            != EstadoCredito.FINALIZADO
            ) {
                activos++;
            }

            switch (fila.getEstadoVisual()) {

                case "AL_DIA" ->
                        alDia++;

                case "POR_VENCER" ->
                        porVencer++;

                case "EN_MORA",
                     "EN_REGULARIZACION" ->
                        enMora++;

                case "ATRASO_PROLONGADO" ->
                        atrasoProlongado++;

                default -> {
                }
            }
        }

        /*
         * Más reciente primero.
         */
        filas.sort(
                Comparator.comparing(
                                CreditoResumenDto::getFechaSolicitud,
                                Comparator.nullsLast(
                                        Comparator.naturalOrder()
                                )
                        )
                        .reversed()
        );

        CreditoPanelDto panel =
                new CreditoPanelDto();

        panel.setTotalCreditos(
                creditos.size()
        );

        panel.setCreditosActivos(
                activos
        );

        panel.setCreditosAlDia(
                alDia
        );

        panel.setCreditosPorVencer(
                porVencer
        );

        panel.setCreditosEnMora(
                enMora
        );

        panel.setAtrasoProlongado(
                atrasoProlongado
        );

        panel.setCapitalPrestado(
                dinero(
                        capitalPrestado
                )
        );

        panel.setCapitalRecuperado(
                dinero(
                        capitalRecuperado
                )
        );

        panel.setInteresGenerado(
                dinero(
                        interesGenerado
                )
        );

        panel.setSaldoPendiente(
                dinero(
                        saldoPendiente
                )
        );

        panel.setCreditos(
                filas
        );

        cargarGraficoPagos(
                panel
        );

        return panel;
    }

    private CreditoResumenDto convertirCredito(
            Credito credito,
            LocalDate hoy
    ) {

        CreditoResumenDto dto =
                new CreditoResumenDto();

        dto.setIdCredito(
                credito.getIdCredito()
        );

        dto.setCodigoCredito(
                credito.getCodigoCredito()
        );

        dto.setFechaSolicitud(
                credito.getFechaSolicitud()
        );

        dto.setCapitalOriginal(
                dinero(
                        nz(
                                credito.getCapitalOriginal()
                        )
                )
        );

        dto.setCapitalPendiente(
                dinero(
                        nz(
                                credito.getCapitalPendiente()
                        )
                )
        );

        dto.setInteresPendiente(
                dinero(
                        nz(
                                credito.getInteresPendiente()
                        )
                )
        );

        dto.setTotalPendiente(
                dinero(
                        nz(
                                credito.getCapitalPendiente()
                        )
                                .add(
                                        nz(
                                                credito.getInteresPendiente()
                                        )
                                )
                )
        );

        dto.setEstado(
                credito.getEstado()
        );

        /*
         * =====================================================
         * CLIENTE
         * =====================================================
         */

        Cliente cliente =
                credito.getCliente();

        if (cliente != null) {

            dto.setIdCliente(
                    cliente.getIdCliente()
            );

            dto.setCodigoCliente(
                    cliente.getCodigoCliente()
            );

            Persona persona =
                    cliente.getPersona();

            if (persona != null) {

                dto.setNombreCliente(
                        nombreCompleto(
                                persona
                        )
                );

                dto.setDni(
                        persona.getDni()
                );
            }
        }

        /*
         * =====================================================
         * CONDICIONES DEL CRÉDITO
         * =====================================================
         */

        CondicionCredito condicion =
                credito.getCondicion();

        if (condicion != null) {

            if (condicion.getTipoCalculo() != null) {

                dto.setTipoInteres(
                        condicion
                                .getTipoCalculo()
                                .name()
                );
            }

            if (
                    condicion.getPeriodicidadTasa()
                            != null
            ) {

                dto.setPeriodicidadInteres(
                        condicion
                                .getPeriodicidadTasa()
                                .name()
                );
            }

            if (
                    condicion.getFrecuenciaPago()
                            != null
            ) {

                dto.setFrecuenciaPago(
                        condicion
                                .getFrecuenciaPago()
                                .name()
                );
            }

            dto.setPlazoMeses(
                    condicion.getPlazoMeses()
            );
        }

        /*
         * =====================================================
         * PRÓXIMA CUOTA PENDIENTE
         * =====================================================
         */

        Cuota proximaCuota =
                buscarProximaCuota(
                        credito.getIdCredito()
                );

        if (proximaCuota != null) {

            dto.setProximaFechaPago(
                    proximaCuota
                            .getFechaVencimiento()
            );

            dto.setProximoMontoPago(
                    dinero(
                            nz(
                                    proximaCuota
                                            .getCapitalPendiente()
                            )
                                    .add(
                                            nz(
                                                    proximaCuota
                                                            .getInteresPendiente()
                                            )
                                    )
                                    .add(
                                            nz(
                                                    proximaCuota
                                                            .getCapitalRegularizadoPendiente()
                                            )
                                    )
                    )
            );
        } else {

            dto.setProximoMontoPago(
                    cero()
            );
        }

        dto.setEstadoVisual(
                calcularEstadoVisual(
                        credito,
                        proximaCuota,
                        hoy
                )
        );

        return dto;
    }

    private Cuota buscarProximaCuota(
            Long idCredito
    ) {

        List<Cuota> cuotas =
                cuotaRepository
                        .findByCredito_IdCreditoOrderByFechaVencimientoAsc(
                                idCredito
                        );

        for (Cuota cuota : cuotas) {

            BigDecimal pendiente =
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

            if (
                    pendiente.compareTo(
                            BigDecimal.ZERO
                    ) > 0
            ) {

                return cuota;
            }
        }

        return null;
    }

    private String calcularEstadoVisual(
            Credito credito,
            Cuota proximaCuota,
            LocalDate hoy
    ) {

        if (credito.getEstado() == null) {

            return "ACTIVO";
        }

        if (
                credito.getEstado()
                        == EstadoCredito.FINALIZADO
        ) {

            return "FINALIZADO";
        }

        if (
                credito.getEstado()
                        == EstadoCredito.ATRASO_PROLONGADO
        ) {

            return "ATRASO_PROLONGADO";
        }

        if (
                credito.getEstado()
                        == EstadoCredito.EN_MORA
        ) {

            return "EN_MORA";
        }

        if (
                credito.getEstado()
                        == EstadoCredito.EN_REGULARIZACION
        ) {

            return "EN_REGULARIZACION";
        }

        if (
                proximaCuota != null
                        &&
                        proximaCuota.getFechaVencimiento()
                                != null
        ) {

            LocalDate vencimiento =
                    proximaCuota
                            .getFechaVencimiento();

            /*
             * Si ya venció, visualmente se muestra mora
             * aunque el proceso automático todavía no haya
             * actualizado el estado del crédito.
             */
            if (vencimiento.isBefore(hoy)) {

                return "EN_MORA";
            }

            LocalDate limiteAdvertencia =
                    hoy.plusDays(
                            DIAS_ADVERTENCIA
                    );

            if (
                    !vencimiento.isBefore(hoy)
                            &&
                            !vencimiento.isAfter(
                                    limiteAdvertencia
                            )
            ) {

                return "POR_VENCER";
            }
        }

        return "AL_DIA";
    }

    /*
     * =========================================================
     * GRÁFICO:
     * PAGOS APROBADOS ÚLTIMOS 6 MESES
     * =========================================================
     */

    private void cargarGraficoPagos(
            CreditoPanelDto panel
    ) {

        YearMonth actual =
                YearMonth.now();

        Map<YearMonth, BigDecimal> valores =
                new LinkedHashMap<>();

        for (int i = 5; i >= 0; i--) {

            valores.put(
                    actual.minusMonths(i),
                    cero()
            );
        }

        List<Pago> pagos =
                pagoRepository.findAll();

        for (Pago pago : pagos) {

            if (
                    pago.getEstado()
                            != EstadoPago.APROBADO
            ) {
                continue;
            }

            if (pago.getFechaPago() == null) {
                continue;
            }

            YearMonth mesPago =
                    YearMonth.from(
                            pago.getFechaPago()
                    );

            if (!valores.containsKey(mesPago)) {
                continue;
            }

            valores.put(
                    mesPago,
                    valores.get(mesPago)
                            .add(
                                    nz(
                                            pago.getMonto()
                                    )
                            )
            );
        }

        DateTimeFormatter formatoMes =
                DateTimeFormatter.ofPattern(
                        "MMM",
                        new Locale(
                                "es",
                                "PE"
                        )
                );

        List<String> meses =
                new ArrayList<>();

        List<BigDecimal> montos =
                new ArrayList<>();

        for (
                Map.Entry<YearMonth, BigDecimal> item :
                valores.entrySet()
        ) {

            String nombre =
                    item.getKey()
                            .atDay(1)
                            .format(
                                    formatoMes
                            );

            if (!nombre.isBlank()) {

                nombre =
                        nombre.substring(
                                0,
                                1
                        ).toUpperCase()
                                +
                                nombre.substring(1);
            }

            meses.add(
                    nombre
            );

            montos.add(
                    dinero(
                            item.getValue()
                    )
            );
        }
        panel.setMesesGrafico(
                meses
        );
        panel.setPagosGrafico(
                montos
        );
    }
    private String nombreCompleto(
            Persona persona
    ) {

        String nombres =
                persona.getNombres() == null
                        ? ""
                        : persona.getNombres()
                        .trim();

        String apellidos =
                persona.getApellidos() == null
                        ? ""
                        : persona.getApellidos()
                        .trim();

        return (
                nombres
                        + " "
                        + apellidos
        ).trim();
    }

    private BigDecimal nz(
            BigDecimal valor
    ) {
        return valor == null
                ? cero()
                : valor;
    }

    private BigDecimal dinero(
            BigDecimal valor
    ) {

        if (valor == null) {

            return cero();
        }

        return valor.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

    private BigDecimal cero() {

        return BigDecimal.ZERO
                .setScale(2);
    }
}