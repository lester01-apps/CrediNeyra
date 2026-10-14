package com.credineyra.service;

import com.credineyra.dto.CreditoRegistroDto;
import com.credineyra.dto.CreditoSimulacionDto;
import com.credineyra.dto.CuotaSimuladaDto;
import com.credineyra.entity.enums.Periodicidad;
import com.credineyra.entity.enums.TipoCalculoCredito;
import com.credineyra.exception.ReglaNegocioException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class CreditoCalculoService {

    private static final BigDecimal CIEN =
            new BigDecimal("100");

    private static final BigDecimal LIMITE_GARANTIA =
            new BigDecimal("500.00");

    public CreditoSimulacionDto simular(
            CreditoRegistroDto dto
    ) {

        validar(dto);

        BigDecimal capital =
                dinero(dto.getCapitalOriginal());

        BigDecimal tasa =
                dto.getTasaInteres()
                        .divide(
                                CIEN,
                                10,
                                RoundingMode.HALF_UP
                        );

        int pagosPorMes =
                pagosPorMes(
                        dto.getFrecuenciaPago()
                );

        int periodosInteresPorMes =
                periodosInteresPorMes(
                        dto.getPeriodicidadTasa()
                );

        int numeroCuotas =
                dto.getPlazoMeses()
                        * pagosPorMes;

        List<BigDecimal> capitalPorMes =
                distribuir(
                        capital,
                        dto.getPlazoMeses()
                );

        List<CuotaSimuladaDto> cronograma =
                new ArrayList<>();

        BigDecimal saldoGlobal =
                capital;

        BigDecimal interesTotal =
                cero();

        int numeroCuota = 1;

        for (
                int mes = 0;
                mes < dto.getPlazoMeses();
                mes++
        ) {

            BigDecimal capitalMes =
                    capitalPorMes.get(mes);

            List<BigDecimal> capitalPagos =
                    distribuir(
                            capitalMes,
                            pagosPorMes
                    );

            List<BigDecimal> interesPagos =
                    calcularInteresDelMes(
                            capital,
                            saldoGlobal,
                            tasa,
                            dto.getTipoCalculo(),
                            pagosPorMes,
                            periodosInteresPorMes,
                            capitalPagos
                    );

            for (
                    int pago = 0;
                    pago < pagosPorMes;
                    pago++
            ) {

                BigDecimal saldoInicial =
                        saldoGlobal;

                BigDecimal capitalCuota =
                        capitalPagos.get(pago);

                BigDecimal interesCuota =
                        interesPagos.get(pago);

                BigDecimal totalCuota =
                        capitalCuota
                                .add(interesCuota)
                                .setScale(2);

                saldoGlobal =
                        saldoGlobal
                                .subtract(capitalCuota)
                                .max(BigDecimal.ZERO)
                                .setScale(2);

                LocalDate vencimiento =
                        calcularFechaVencimiento(
                                dto.getFechaPrimerPago(),
                                dto.getFrecuenciaPago(),
                                numeroCuota - 1
                        );

                cronograma.add(
                        new CuotaSimuladaDto(
                                numeroCuota,
                                vencimiento,
                                saldoInicial,
                                capitalCuota,
                                interesCuota,
                                totalCuota,
                                saldoGlobal
                        )
                );

                interesTotal =
                        interesTotal
                                .add(interesCuota)
                                .setScale(2);

                numeroCuota++;
            }
        }

        BigDecimal totalPagar =
                capital
                        .add(interesTotal)
                        .setScale(2);

        CreditoSimulacionDto resultado =
                new CreditoSimulacionDto();

        resultado.setCapital(capital);

        resultado.setTasaInteres(
                dto.getTasaInteres()
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        )
        );

        resultado.setTipoCalculo(
                dto.getTipoCalculo()
        );

        resultado.setPeriodicidadInteres(
                dto.getPeriodicidadTasa()
        );

        resultado.setFrecuenciaPago(
                dto.getFrecuenciaPago()
        );

        resultado.setPlazoMeses(
                dto.getPlazoMeses()
        );

        resultado.setNumeroCuotas(
                numeroCuotas
        );

        resultado.setInteresTotal(
                interesTotal
        );

        resultado.setTotalPagar(
                totalPagar
        );

        resultado.setGarantiaObligatoria(
                capital.compareTo(
                        LIMITE_GARANTIA
                ) >= 0
        );

        resultado.setFechaPrimerPago(
                dto.getFechaPrimerPago()
        );

        if (!cronograma.isEmpty()) {

            resultado.setPrimeraCuota(
                    cronograma.getFirst()
                            .getTotalCuota()
            );

            resultado.setUltimaCuota(
                    cronograma.getLast()
                            .getTotalCuota()
            );

            resultado.setFechaUltimoPago(
                    cronograma.getLast()
                            .getFechaVencimiento()
            );
        }

        resultado.setCronograma(
                cronograma
        );

        return resultado;
    }

    /*
     * Calcula el interés correspondiente a UN MES,
     * considerando por separado:
     *
     * - frecuencia de pago
     * - periodicidad del interés
     * - NORMAL
     * - INTERES_ECONOMICO
     */
    private List<BigDecimal> calcularInteresDelMes(
            BigDecimal capitalOriginal,
            BigDecimal saldoInicioMes,
            BigDecimal tasa,
            TipoCalculoCredito tipoCalculo,
            int pagosPorMes,
            int periodosInteresPorMes,
            List<BigDecimal> capitalPagos
    ) {

        List<BigDecimal> intereses =
                new ArrayList<>();

        for (int i = 0; i < pagosPorMes; i++) {
            intereses.add(cero());
        }

        /*
         * Creamos una línea temporal común.
         *
         * Ejemplos:
         *
         * Pago semanal      = 4 eventos
         * Interés quincenal = 2 eventos
         *
         * mcm(4,2) = 4 posiciones.
         */
        int posiciones =
                mcm(
                        pagosPorMes,
                        periodosInteresPorMes
                );

        int pasoPago =
                posiciones / pagosPorMes;

        int pasoInteres =
                posiciones / periodosInteresPorMes;

        int[] posicionPago =
                new int[pagosPorMes];

        for (
                int i = 0;
                i < pagosPorMes;
                i++
        ) {
            posicionPago[i] =
                    (i + 1) * pasoPago;
        }

        for (
                int periodo = 0;
                periodo < periodosInteresPorMes;
                periodo++
        ) {

            int inicioPeriodo =
                    periodo * pasoInteres;

            int finPeriodo =
                    (periodo + 1)
                            * pasoInteres;

            BigDecimal capitalPagadoAntes =
                    cero();

            for (
                    int pago = 0;
                    pago < pagosPorMes;
                    pago++
            ) {

                /*
                 * Si el pago ocurrió antes o exactamente
                 * al inicio del nuevo período,
                 * ese capital ya redujo el saldo.
                 */
                if (
                        posicionPago[pago]
                                <= inicioPeriodo
                ) {
                    capitalPagadoAntes =
                            capitalPagadoAntes
                                    .add(
                                            capitalPagos.get(
                                                    pago
                                            )
                                    );
                }
            }

            BigDecimal baseInteres;

            if (
                    tipoCalculo
                            == TipoCalculoCredito.NORMAL
            ) {

                /*
                 * NORMAL:
                 * siempre sobre el capital original.
                 */
                baseInteres =
                        capitalOriginal;

            } else {

                /*
                 * ECONÓMICO:
                 * sobre el capital pendiente al
                 * inicio del período de interés.
                 */
                baseInteres =
                        saldoInicioMes
                                .subtract(
                                        capitalPagadoAntes
                                )
                                .max(
                                        BigDecimal.ZERO
                                );
            }

            BigDecimal interesPeriodo =
                    dinero(
                            baseInteres
                                    .multiply(tasa)
                    );

            List<Integer> pagosDentroPeriodo =
                    new ArrayList<>();

            for (
                    int pago = 0;
                    pago < pagosPorMes;
                    pago++
            ) {

                if (
                        posicionPago[pago]
                                > inicioPeriodo
                                &&
                                posicionPago[pago]
                                        <= finPeriodo
                ) {

                    pagosDentroPeriodo.add(
                            pago
                    );
                }
            }

            /*
             * Si dentro del período de interés
             * existen varios pagos, distribuimos
             * ese interés entre ellos.
             */
            if (!pagosDentroPeriodo.isEmpty()) {

                List<BigDecimal> partes =
                        distribuir(
                                interesPeriodo,
                                pagosDentroPeriodo.size()
                        );

                for (
                        int i = 0;
                        i < pagosDentroPeriodo.size();
                        i++
                ) {

                    int indicePago =
                            pagosDentroPeriodo.get(i);

                    intereses.set(
                            indicePago,
                            intereses.get(
                                            indicePago
                                    )
                                    .add(
                                            partes.get(i)
                                    )
                                    .setScale(2)
                    );
                }

            } else {

                /*
                 * Ejemplo:
                 *
                 * interés semanal
                 * pago mensual
                 *
                 * No hay pago dentro de algunas semanas,
                 * por lo que ese interés se acumula en
                 * el siguiente pago.
                 */
                int siguientePago = -1;

                for (
                        int pago = 0;
                        pago < pagosPorMes;
                        pago++
                ) {

                    if (
                            posicionPago[pago]
                                    > finPeriodo
                    ) {
                        siguientePago = pago;
                        break;
                    }
                }

                if (siguientePago == -1) {
                    siguientePago =
                            pagosPorMes - 1;
                }

                intereses.set(
                        siguientePago,
                        intereses.get(
                                        siguientePago
                                )
                                .add(
                                        interesPeriodo
                                )
                                .setScale(2)
                );
            }
        }

        return intereses;
    }

    private void validar(
            CreditoRegistroDto dto
    ) {

        if (dto == null) {
            throw new ReglaNegocioException(
                    "Los datos del crédito son obligatorios"
            );
        }

        if (
                dto.getCapitalOriginal() == null
                        ||
                        dto.getCapitalOriginal()
                                .compareTo(
                                        BigDecimal.ZERO
                                ) <= 0
        ) {

            throw new ReglaNegocioException(
                    "El capital debe ser mayor que cero"
            );
        }

        if (
                dto.getTasaInteres() == null
                        ||
                        dto.getTasaInteres()
                                .compareTo(
                                        BigDecimal.ZERO
                                ) < 0
        ) {

            throw new ReglaNegocioException(
                    "La tasa de interés no puede ser negativa"
            );
        }

        if (dto.getTipoCalculo() == null) {
            throw new ReglaNegocioException(
                    "Debe seleccionar el tipo de interés"
            );
        }

        if (dto.getPeriodicidadTasa() == null) {
            throw new ReglaNegocioException(
                    "Debe seleccionar la periodicidad del interés"
            );
        }

        /*
         * Acuerdo actual de crediNeyra:
         *
         * El interés NO se configura diario.
         */
        if (
                dto.getPeriodicidadTasa()
                        == Periodicidad.DIARIA
        ) {

            throw new ReglaNegocioException(
                    "El interés solo puede ser semanal, quincenal o mensual"
            );
        }

        if (dto.getFrecuenciaPago() == null) {
            throw new ReglaNegocioException(
                    "Debe seleccionar la forma de pago"
            );
        }

        if (
                dto.getPlazoMeses() == null
                        ||
                        dto.getPlazoMeses() <= 0
        ) {

            throw new ReglaNegocioException(
                    "El plazo debe ser mayor que cero"
            );
        }

        if (dto.getFechaPrimerPago() == null) {
            throw new ReglaNegocioException(
                    "Debe indicar la primera fecha de pago"
            );
        }
    }

    public int pagosPorMes(
            Periodicidad frecuencia
    ) {

        if (frecuencia == null) {
            throw new ReglaNegocioException(
                    "La frecuencia de pago es obligatoria"
            );
        }

        return switch (frecuencia) {

            case MENSUAL -> 1;

            case QUINCENAL -> 2;

            case SEMANAL -> 4;

            case DIARIA -> 30;
        };
    }

    public int periodosInteresPorMes(
            Periodicidad periodicidad
    ) {

        if (periodicidad == null) {
            throw new ReglaNegocioException(
                    "La periodicidad del interés es obligatoria"
            );
        }

        return switch (periodicidad) {

            case MENSUAL -> 1;

            case QUINCENAL -> 2;

            case SEMANAL -> 4;

            case DIARIA ->
                    throw new ReglaNegocioException(
                            "El interés diario no está habilitado"
                    );
        };
    }

    public LocalDate calcularFechaVencimiento(
            LocalDate primeraFecha,
            Periodicidad frecuencia,
            int desplazamiento
    ) {

        if (primeraFecha == null) {
            throw new ReglaNegocioException(
                    "La primera fecha de pago es obligatoria"
            );
        }

        return switch (frecuencia) {

            case MENSUAL ->
                    primeraFecha.plusMonths(
                            desplazamiento
                    );

            case QUINCENAL ->
                    primeraFecha.plusDays(
                            desplazamiento * 15L
                    );

            case SEMANAL ->
                    primeraFecha.plusWeeks(
                            desplazamiento
                    );

            case DIARIA ->
                    primeraFecha.plusDays(
                            desplazamiento
                    );
        };
    }

    /*
     * Regla monetaria actual:
     *
     * 100.49 -> 100.00
     * 100.50 -> 101.00
     *
     * La BD conserva NUMERIC(...,2).
     */
    public BigDecimal dinero(
            BigDecimal valor
    ) {

        if (valor == null) {
            return cero();
        }

        return valor
                .setScale(
                        0,
                        RoundingMode.HALF_UP
                )
                .setScale(2);
    }

    /*
     * Distribuye sin crear ni perder dinero.
     * La última parte absorbe la diferencia.
     */
    public List<BigDecimal> distribuir(
            BigDecimal total,
            int partes
    ) {

        if (partes <= 0) {
            throw new ReglaNegocioException(
                    "La cantidad de partes debe ser mayor que cero"
            );
        }

        BigDecimal monto =
                dinero(total);

        BigDecimal base =
                dinero(
                        monto.divide(
                                BigDecimal.valueOf(
                                        partes
                                ),
                                10,
                                RoundingMode.HALF_UP
                        )
                );

        if (
                base.multiply(
                                BigDecimal.valueOf(
                                        partes - 1L
                                )
                        )
                        .compareTo(
                                monto
                        ) > 0
        ) {

            base =
                    monto.divide(
                                    BigDecimal.valueOf(
                                            partes
                                    ),
                                    0,
                                    RoundingMode.DOWN
                            )
                            .setScale(2);
        }

        List<BigDecimal> resultado =
                new ArrayList<>();

        BigDecimal acumulado =
                cero();

        for (
                int i = 0;
                i < partes;
                i++
        ) {

            if (i == partes - 1) {

                resultado.add(
                        monto.subtract(
                                        acumulado
                                )
                                .setScale(2)
                );

            } else {

                resultado.add(base);

                acumulado =
                        acumulado
                                .add(base)
                                .setScale(2);
            }
        }

        return resultado;
    }

    private int mcd(
            int a,
            int b
    ) {

        while (b != 0) {

            int temporal =
                    b;

            b =
                    a % b;

            a =
                    temporal;
        }

        return a;
    }

    private int mcm(
            int a,
            int b
    ) {

        return Math.abs(
                a * b
        ) / mcd(a, b);
    }

    private BigDecimal cero() {
        return BigDecimal.ZERO.setScale(2);
    }
}