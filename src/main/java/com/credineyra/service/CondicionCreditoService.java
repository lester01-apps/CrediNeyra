package com.credineyra.service;

import com.credineyra.entity.CondicionCredito;
import com.credineyra.entity.enums.Periodicidad;
import com.credineyra.entity.enums.TipoCalculoCredito;
import com.credineyra.exception.RecursoNoEncontradoException;
import com.credineyra.exception.ReglaNegocioException;
import com.credineyra.repository.CondicionCreditoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class CondicionCreditoService {

    private final CondicionCreditoRepository repository;

    public CondicionCreditoService(
            CondicionCreditoRepository repository
    ) {
        this.repository = repository;
    }

    public CondicionCredito registrar(
            CondicionCredito condicion
    ) {

        validar(condicion);

        condicion.setNumeroCuotas(
                calcularNumeroCuotas(
                        condicion.getPlazoMeses(),
                        condicion.getFrecuenciaPago()
                )
        );

        return repository.save(condicion);
    }

    @Transactional(readOnly = true)
    public CondicionCredito buscarPorId(
            Long idCondicion
    ) {

        return repository.findById(idCondicion)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Condición de crédito no encontrada"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<CondicionCredito> listarTodos() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<CondicionCredito> listarPorTipo(
            TipoCalculoCredito tipo
    ) {

        return repository.findByTipoCalculo(tipo);
    }

    public int calcularNumeroCuotas(
            Integer plazoMeses,
            Periodicidad frecuencia
    ) {

        if (plazoMeses == null
                || plazoMeses <= 0) {

            throw new ReglaNegocioException(
                    "El plazo debe ser mayor que cero"
            );
        }

        if (frecuencia == null) {
            throw new ReglaNegocioException(
                    "La frecuencia de pago es obligatoria"
            );
        }

        return switch (frecuencia) {

            case MENSUAL ->
                    plazoMeses;

            case QUINCENAL ->
                    plazoMeses * 2;

            case SEMANAL ->
                    plazoMeses * 4;

            case DIARIA ->
                    plazoMeses * 30;
        };
    }

    private void validar(
            CondicionCredito condicion
    ) {

        if (condicion == null) {
            throw new ReglaNegocioException(
                    "La condición de crédito es obligatoria"
            );
        }

        if (condicion.getTipoCalculo() == null) {
            throw new ReglaNegocioException(
                    "El tipo de cálculo es obligatorio"
            );
        }

        if (condicion.getTasaInteres() == null
                || condicion.getTasaInteres()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new ReglaNegocioException(
                    "La tasa de interés no puede ser negativa"
            );
        }

        if (condicion.getPeriodicidadTasa() == null) {
            throw new ReglaNegocioException(
                    "La periodicidad de la tasa es obligatoria"
            );
        }

        if (condicion.getFrecuenciaPago() == null) {
            throw new ReglaNegocioException(
                    "La frecuencia de pago es obligatoria"
            );
        }

        if (condicion.getPlazoMeses() == null
                || condicion.getPlazoMeses() <= 0) {

            throw new ReglaNegocioException(
                    "El plazo debe ser mayor que cero"
            );
        }

        if (condicion.getFechaInicio() == null
                || condicion.getFechaPrimerPago() == null) {

            throw new ReglaNegocioException(
                    "Las fechas del crédito son obligatorias"
            );
        }

        if (condicion.getFechaPrimerPago()
                .isBefore(
                        condicion.getFechaInicio()
                )) {

            throw new ReglaNegocioException(
                    "El primer pago no puede ser anterior al inicio del crédito"
            );
        }

        /*
         * INTERES_ECONOMICO:
         * la tasa siempre es mensual.
         *
         * La frecuencia de pago sí puede ser:
         * DIARIA, SEMANAL, QUINCENAL o MENSUAL.
         */
        if (condicion.getTipoCalculo()
                == TipoCalculoCredito.INTERES_ECONOMICO
                && condicion.getPeriodicidadTasa()
                != Periodicidad.MENSUAL) {

            throw new ReglaNegocioException(
                    "En interés económico la periodicidad de la tasa debe ser mensual"
            );
        }
    }
}