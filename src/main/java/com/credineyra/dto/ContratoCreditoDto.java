package com.credineyra.dto;

import com.credineyra.entity.enums.Periodicidad;
import com.credineyra.entity.enums.TipoCalculoCredito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ContratoCreditoDto {

    private Long idCredito;

    private String codigoCredito;

    private LocalDate fechaDesembolso;


    /*
     * =========================================================
     * PRESTATARIO
     * =========================================================
     */

    private String prestatarioNombre;

    private String prestatarioDocumento;

    private String prestatarioDomicilio;


    /*
     * =========================================================
     * PRESTAMISTA
     * =========================================================
     */

    private String prestamistaNombre;

    private String prestamistaDocumento;

    private String prestamistaDomicilio;


    /*
     * =========================================================
     * CONDICIONES DEL CRÉDITO
     * =========================================================
     */

    private BigDecimal capitalOriginal;

    /*
     * IMPORTANTE:
     *
     * Esta es la tasa ORIGINAL congelada del crédito.
     *
     * Ej:
     *
     * 8.00
     * 10.00
     * 15.00
     *
     * No es una tasa recalculada.
     */
    private BigDecimal tasaInteres;

    private TipoCalculoCredito tipoCalculo;

    private Periodicidad periodicidadInteres;

    private Periodicidad frecuenciaPago;

    private Integer plazoMeses;

    private BigDecimal interesTotal;

    private BigDecimal totalPagar;


    /*
     * =========================================================
     * GARANTÍA
     * =========================================================
     */

    private String garantiaDescripcion;


    /*
     * =========================================================
     * CRONOGRAMA
     * =========================================================
     */

    private List<CuotaContratoDto> cronograma =
            new ArrayList<>();


    /*
     * =========================================================
     * CONSTRUCTOR
     * =========================================================
     */

    public ContratoCreditoDto() {
    }


    /*
     * =========================================================
     * ID CRÉDITO
     * =========================================================
     */

    public Long getIdCredito() {
        return idCredito;
    }

    public void setIdCredito(
            Long idCredito
    ) {
        this.idCredito =
                idCredito;
    }


    /*
     * =========================================================
     * CÓDIGO
     * =========================================================
     */

    public String getCodigoCredito() {
        return codigoCredito;
    }

    public void setCodigoCredito(
            String codigoCredito
    ) {
        this.codigoCredito =
                codigoCredito;
    }


    /*
     * =========================================================
     * FECHA DE DESEMBOLSO
     * =========================================================
     */

    public LocalDate getFechaDesembolso() {
        return fechaDesembolso;
    }

    public void setFechaDesembolso(
            LocalDate fechaDesembolso
    ) {
        this.fechaDesembolso =
                fechaDesembolso;
    }


    /*
     * =========================================================
     * PRESTATARIO
     * =========================================================
     */

    public String getPrestatarioNombre() {
        return prestatarioNombre;
    }

    public void setPrestatarioNombre(
            String prestatarioNombre
    ) {
        this.prestatarioNombre =
                prestatarioNombre;
    }


    public String getPrestatarioDocumento() {
        return prestatarioDocumento;
    }

    public void setPrestatarioDocumento(
            String prestatarioDocumento
    ) {
        this.prestatarioDocumento =
                prestatarioDocumento;
    }


    public String getPrestatarioDomicilio() {
        return prestatarioDomicilio;
    }

    public void setPrestatarioDomicilio(
            String prestatarioDomicilio
    ) {
        this.prestatarioDomicilio =
                prestatarioDomicilio;
    }


    /*
     * =========================================================
     * PRESTAMISTA
     * =========================================================
     */

    public String getPrestamistaNombre() {
        return prestamistaNombre;
    }

    public void setPrestamistaNombre(
            String prestamistaNombre
    ) {
        this.prestamistaNombre =
                prestamistaNombre;
    }


    public String getPrestamistaDocumento() {
        return prestamistaDocumento;
    }

    public void setPrestamistaDocumento(
            String prestamistaDocumento
    ) {
        this.prestamistaDocumento =
                prestamistaDocumento;
    }


    public String getPrestamistaDomicilio() {
        return prestamistaDomicilio;
    }

    public void setPrestamistaDomicilio(
            String prestamistaDomicilio
    ) {
        this.prestamistaDomicilio =
                prestamistaDomicilio;
    }


    /*
     * =========================================================
     * CAPITAL
     * =========================================================
     */

    public BigDecimal getCapitalOriginal() {
        return capitalOriginal;
    }

    public void setCapitalOriginal(
            BigDecimal capitalOriginal
    ) {
        this.capitalOriginal =
                capitalOriginal;
    }


    /*
     * =========================================================
     * TASA
     * =========================================================
     */

    public BigDecimal getTasaInteres() {
        return tasaInteres;
    }

    public void setTasaInteres(
            BigDecimal tasaInteres
    ) {
        this.tasaInteres =
                tasaInteres;
    }


    /*
     * =========================================================
     * TIPO DE CÁLCULO
     * =========================================================
     */

    public TipoCalculoCredito getTipoCalculo() {
        return tipoCalculo;
    }

    public void setTipoCalculo(
            TipoCalculoCredito tipoCalculo
    ) {
        this.tipoCalculo =
                tipoCalculo;
    }


    /*
     * =========================================================
     * PERIODICIDAD DEL INTERÉS
     * =========================================================
     */

    public Periodicidad getPeriodicidadInteres() {
        return periodicidadInteres;
    }

    public void setPeriodicidadInteres(
            Periodicidad periodicidadInteres
    ) {
        this.periodicidadInteres =
                periodicidadInteres;
    }


    /*
     * =========================================================
     * FRECUENCIA DE PAGO
     * =========================================================
     */

    public Periodicidad getFrecuenciaPago() {
        return frecuenciaPago;
    }

    public void setFrecuenciaPago(
            Periodicidad frecuenciaPago
    ) {
        this.frecuenciaPago =
                frecuenciaPago;
    }


    /*
     * =========================================================
     * PLAZO
     * =========================================================
     */

    public Integer getPlazoMeses() {
        return plazoMeses;
    }

    public void setPlazoMeses(
            Integer plazoMeses
    ) {
        this.plazoMeses =
                plazoMeses;
    }


    /*
     * =========================================================
     * INTERÉS TOTAL
     * =========================================================
     */

    public BigDecimal getInteresTotal() {
        return interesTotal;
    }

    public void setInteresTotal(
            BigDecimal interesTotal
    ) {
        this.interesTotal =
                interesTotal;
    }


    /*
     * =========================================================
     * TOTAL A PAGAR
     * =========================================================
     */

    public BigDecimal getTotalPagar() {
        return totalPagar;
    }

    public void setTotalPagar(
            BigDecimal totalPagar
    ) {
        this.totalPagar =
                totalPagar;
    }


    /*
     * =========================================================
     * GARANTÍA
     * =========================================================
     */

    public String getGarantiaDescripcion() {
        return garantiaDescripcion;
    }

    public void setGarantiaDescripcion(
            String garantiaDescripcion
    ) {
        this.garantiaDescripcion =
                garantiaDescripcion;
    }


    /*
     * =========================================================
     * CRONOGRAMA
     * =========================================================
     *
     * Nombre principal utilizado por ContratoPdfService.
     * =========================================================
     */

    public List<CuotaContratoDto> getCronograma() {

        if (cronograma == null) {

            cronograma =
                    new ArrayList<>();
        }

        return cronograma;
    }


    public void setCronograma(
            List<CuotaContratoDto> cronograma
    ) {

        this.cronograma =
                cronograma == null
                        ? new ArrayList<>()
                        : cronograma;
    }


    /*
     * =========================================================
     * COMPATIBILIDAD CON CÓDIGO ANTERIOR
     * =========================================================
     *
     * Si ContratoCreditoService utiliza:
     *
     * setCuotas(...)
     *
     * seguirá funcionando.
     *
     * Y si otra clase utiliza:
     *
     * getCuotas()
     *
     * también seguirá funcionando.
     * =========================================================
     */

    public List<CuotaContratoDto> getCuotas() {

        return getCronograma();
    }


    public void setCuotas(
            List<CuotaContratoDto> cuotas
    ) {

        setCronograma(
                cuotas
        );
    }


    /*
     * =========================================================
     * CUOTA DEL CONTRATO
     * =========================================================
     */

    public static class CuotaContratoDto {

        private Integer numero;

        private LocalDate fecha;

        private BigDecimal saldoInicial;

        private BigDecimal capital;

        private BigDecimal interes;

        private BigDecimal total;

        private BigDecimal saldoFinal;


        public CuotaContratoDto() {
        }


        public CuotaContratoDto(
                Integer numero,
                LocalDate fecha,
                BigDecimal saldoInicial,
                BigDecimal capital,
                BigDecimal interes,
                BigDecimal total,
                BigDecimal saldoFinal
        ) {

            this.numero =
                    numero;

            this.fecha =
                    fecha;

            this.saldoInicial =
                    saldoInicial;

            this.capital =
                    capital;

            this.interes =
                    interes;

            this.total =
                    total;

            this.saldoFinal =
                    saldoFinal;
        }


        /*
         * NÚMERO
         */

        public Integer getNumero() {
            return numero;
        }

        public void setNumero(
                Integer numero
        ) {
            this.numero =
                    numero;
        }


        /*
         * FECHA
         */

        public LocalDate getFecha() {
            return fecha;
        }

        public void setFecha(
                LocalDate fecha
        ) {
            this.fecha =
                    fecha;
        }


        /*
         * SALDO INICIAL
         */

        public BigDecimal getSaldoInicial() {
            return saldoInicial;
        }

        public void setSaldoInicial(
                BigDecimal saldoInicial
        ) {
            this.saldoInicial =
                    saldoInicial;
        }


        /*
         * CAPITAL
         */

        public BigDecimal getCapital() {
            return capital;
        }

        public void setCapital(
                BigDecimal capital
        ) {
            this.capital =
                    capital;
        }


        /*
         * INTERÉS
         */

        public BigDecimal getInteres() {
            return interes;
        }

        public void setInteres(
                BigDecimal interes
        ) {
            this.interes =
                    interes;
        }


        /*
         * TOTAL
         */

        public BigDecimal getTotal() {
            return total;
        }

        public void setTotal(
                BigDecimal total
        ) {
            this.total =
                    total;
        }


        /*
         * SALDO FINAL
         */

        public BigDecimal getSaldoFinal() {
            return saldoFinal;
        }

        public void setSaldoFinal(
                BigDecimal saldoFinal
        ) {
            this.saldoFinal =
                    saldoFinal;
        }
    }
}