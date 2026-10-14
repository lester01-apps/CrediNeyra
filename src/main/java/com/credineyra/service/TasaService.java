package com.credineyra.service;

import com.credineyra.entity.Tasa;
import com.credineyra.entity.enums.EstadoTasa;
import com.credineyra.exception.DatoDuplicadoException;
import com.credineyra.exception.RecursoNoEncontradoException;
import com.credineyra.exception.ReglaNegocioException;
import com.credineyra.repository.TasaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class TasaService {

    private final TasaRepository tasaRepository;

    public TasaService(
            TasaRepository tasaRepository
    ) {
        this.tasaRepository = tasaRepository;
    }

    public Tasa registrar(Tasa tasa) {

        validar(tasa);

        if (tasaRepository
                .findByCodigoAndVersion(
                        tasa.getCodigo(),
                        tasa.getVersion()
                )
                .isPresent()) {

            throw new DatoDuplicadoException(
                    "Ya existe esa versión de la tasa"
            );
        }

        if (tasa.getEstado() == null) {
            tasa.setEstado(EstadoTasa.ACTIVA);
        }

        return tasaRepository.save(tasa);
    }

    public Tasa cambiarEstado(
            Long idTasa,
            EstadoTasa estado
    ) {

        if (estado == null) {
            throw new ReglaNegocioException(
                    "El estado es obligatorio"
            );
        }

        Tasa tasa = buscarPorId(idTasa);
        tasa.setEstado(estado);

        return tasaRepository.save(tasa);
    }

    @Transactional(readOnly = true)
    public Tasa buscarPorId(Long idTasa) {

        return tasaRepository.findById(idTasa)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Tasa no encontrada"
                        )
                );
    }

    @Transactional(readOnly = true)
    public Tasa buscar(
            String codigo,
            Integer version
    ) {

        return tasaRepository
                .findByCodigoAndVersion(
                        codigo,
                        version
                )
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Tasa no encontrada"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<Tasa> listarVersiones(
            String codigo
    ) {

        return tasaRepository
                .findByCodigoOrderByVersionDesc(
                        codigo
                );
    }

    @Transactional(readOnly = true)
    public List<Tasa> listarActivas() {

        return tasaRepository.findByEstado(
                EstadoTasa.ACTIVA
        );
    }

    @Transactional(readOnly = true)
    public List<Tasa> listarTodos() {
        return tasaRepository.findAll();
    }

    private void validar(Tasa tasa) {

        if (tasa == null) {
            throw new ReglaNegocioException(
                    "Los datos de la tasa son obligatorios"
            );
        }

        if (tasa.getCodigo() == null
                || tasa.getCodigo().isBlank()) {

            throw new ReglaNegocioException(
                    "El código de la tasa es obligatorio"
            );
        }

        if (tasa.getVersion() == null
                || tasa.getVersion() <= 0) {

            throw new ReglaNegocioException(
                    "La versión debe ser mayor que cero"
            );
        }

        if (tasa.getValor() == null
                || tasa.getValor()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new ReglaNegocioException(
                    "La tasa no puede ser negativa"
            );
        }

        if (tasa.getPeriodicidad() == null) {
            throw new ReglaNegocioException(
                    "La periodicidad es obligatoria"
            );
        }

        if (tasa.getUsuarioCreador() == null) {
            throw new ReglaNegocioException(
                    "Debe indicarse el usuario creador"
            );
        }

        if (tasa.getMotivo() == null
                || tasa.getMotivo().isBlank()) {

            throw new ReglaNegocioException(
                    "El motivo es obligatorio"
            );
        }
    }
}