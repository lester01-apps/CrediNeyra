package com.credineyra.service;

import com.credineyra.entity.Garantia;
import com.credineyra.entity.enums.EstadoGarantia;
import com.credineyra.exception.RecursoNoEncontradoException;
import com.credineyra.exception.ReglaNegocioException;
import com.credineyra.repository.GarantiaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class GarantiaService {

    private final GarantiaRepository garantiaRepository;

    public GarantiaService(
            GarantiaRepository garantiaRepository
    ) {
        this.garantiaRepository = garantiaRepository;
    }

    public Garantia registrar(
            Garantia garantia
    ) {

        validar(garantia);

        if (garantia.getEstado() == null) {
            garantia.setEstado(
                    EstadoGarantia.ACTIVA
            );
        }

        return garantiaRepository.save(garantia);
    }

    public Garantia cambiarEstado(
            Long idGarantia,
            EstadoGarantia estado
    ) {

        if (estado == null) {
            throw new ReglaNegocioException(
                    "El estado es obligatorio"
            );
        }

        Garantia garantia =
                buscarPorId(idGarantia);

        garantia.setEstado(estado);

        return garantiaRepository.save(garantia);
    }

    @Transactional(readOnly = true)
    public Garantia buscarPorId(
            Long idGarantia
    ) {

        return garantiaRepository
                .findById(idGarantia)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Garantía no encontrada"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<Garantia> listarPorCredito(
            Long idCredito
    ) {

        return garantiaRepository
                .findByCredito_IdCredito(
                        idCredito
                );
    }

    @Transactional(readOnly = true)
    public List<Garantia> listarPorEstado(
            EstadoGarantia estado
    ) {

        return garantiaRepository
                .findByEstado(estado);
    }

    @Transactional(readOnly = true)
    public List<Garantia> listarTodos() {
        return garantiaRepository.findAll();
    }

    private void validar(
            Garantia garantia
    ) {

        if (garantia == null) {
            throw new ReglaNegocioException(
                    "Los datos de la garantía son obligatorios"
            );
        }

        if (garantia.getCredito() == null) {
            throw new ReglaNegocioException(
                    "La garantía debe pertenecer a un crédito"
            );
        }

        if (garantia.getTipoGarantia() == null
                || garantia.getTipoGarantia()
                .isBlank()) {

            throw new ReglaNegocioException(
                    "El tipo de garantía es obligatorio"
            );
        }

        if (garantia.getValorEstimado() == null
                || garantia.getValorEstimado()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new ReglaNegocioException(
                    "El valor estimado no puede ser negativo"
            );
        }
    }
}