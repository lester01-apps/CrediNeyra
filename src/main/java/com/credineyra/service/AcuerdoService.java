package com.credineyra.service;

import com.credineyra.entity.Acuerdo;
import com.credineyra.entity.enums.EstadoAcuerdo;
import com.credineyra.entity.enums.TipoAcuerdo;
import com.credineyra.exception.RecursoNoEncontradoException;
import com.credineyra.exception.ReglaNegocioException;
import com.credineyra.repository.AcuerdoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AcuerdoService {

    private final AcuerdoRepository repository;

    public AcuerdoService(
            AcuerdoRepository repository
    ) {
        this.repository = repository;
    }

    public Acuerdo registrar(
            Acuerdo acuerdo
    ) {

        validar(acuerdo);

        return repository.save(acuerdo);
    }

    public Acuerdo cambiarEstado(
            Long idAcuerdo,
            EstadoAcuerdo estado
    ) {

        if (estado == null) {
            throw new ReglaNegocioException(
                    "El estado es obligatorio"
            );
        }

        Acuerdo acuerdo =
                buscarPorId(idAcuerdo);

        acuerdo.setEstado(estado);

        return repository.save(acuerdo);
    }

    @Transactional(readOnly = true)
    public Acuerdo buscarPorId(Long idAcuerdo) {

        return repository.findById(idAcuerdo)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Acuerdo no encontrado"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<Acuerdo> listarTodos() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Acuerdo> listarPorCredito(
            Long idCredito
    ) {

        return repository
                .findByCredito_IdCreditoOrderByFechaCreacionDesc(
                        idCredito
                );
    }

    @Transactional(readOnly = true)
    public List<Acuerdo> listarPorEstado(
            EstadoAcuerdo estado
    ) {

        return repository.findByEstado(estado);
    }

    @Transactional(readOnly = true)
    public List<Acuerdo> listarPorTipo(
            TipoAcuerdo tipo
    ) {

        return repository.findByTipoAcuerdo(tipo);
    }

    private void validar(Acuerdo acuerdo) {

        if (acuerdo == null) {
            throw new ReglaNegocioException(
                    "Los datos del acuerdo son obligatorios"
            );
        }

        if (acuerdo.getCredito() == null) {
            throw new ReglaNegocioException(
                    "El acuerdo debe pertenecer a un crédito"
            );
        }

        if (acuerdo.getUsuarioCreador() == null) {
            throw new ReglaNegocioException(
                    "Debe indicarse el usuario creador"
            );
        }

        if (acuerdo.getTipoAcuerdo() == null) {
            throw new ReglaNegocioException(
                    "El tipo de acuerdo es obligatorio"
            );
        }

        if (acuerdo.getMotivo() == null
                || acuerdo.getMotivo().isBlank()) {

            throw new ReglaNegocioException(
                    "El motivo es obligatorio"
            );
        }
    }
}