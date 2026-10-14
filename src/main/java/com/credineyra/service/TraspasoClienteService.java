package com.credineyra.service;

import com.credineyra.entity.TraspasoCliente;
import com.credineyra.exception.DatoDuplicadoException;
import com.credineyra.exception.RecursoNoEncontradoException;
import com.credineyra.exception.ReglaNegocioException;
import com.credineyra.repository.TraspasoClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class TraspasoClienteService {

    private final TraspasoClienteRepository repository;

    public TraspasoClienteService(
            TraspasoClienteRepository repository
    ) {
        this.repository = repository;
    }

    public TraspasoCliente registrar(
            TraspasoCliente traspaso
    ) {

        validar(traspaso);

        Long idCliente =
                traspaso.getCliente().getIdCliente();

        if (repository.existsByCliente_IdCliente(
                idCliente
        )) {
            throw new DatoDuplicadoException(
                    "El cliente ya tiene un traspaso registrado"
            );
        }

        return repository.save(traspaso);
    }

    @Transactional(readOnly = true)
    public TraspasoCliente buscarPorId(
            Long idTraspaso
    ) {

        return repository.findById(idTraspaso)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Traspaso no encontrado"
                        )
                );
    }

    @Transactional(readOnly = true)
    public TraspasoCliente buscarPorCliente(
            Long idCliente
    ) {

        return repository
                .findByCliente_IdCliente(idCliente)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El cliente no tiene un traspaso registrado"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<TraspasoCliente> listarTodos() {
        return repository.findAll();
    }

    private void validar(
            TraspasoCliente traspaso
    ) {

        if (traspaso == null) {
            throw new ReglaNegocioException(
                    "Los datos del traspaso son obligatorios"
            );
        }

        if (traspaso.getCliente() == null) {
            throw new ReglaNegocioException(
                    "El cliente es obligatorio"
            );
        }

        if (traspaso.getRegistradoPor() == null) {
            throw new ReglaNegocioException(
                    "Debe indicarse quién registra el traspaso"
            );
        }

        if (traspaso.getCapitalPendienteInicial() != null
                && traspaso
                .getCapitalPendienteInicial()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new ReglaNegocioException(
                    "El capital pendiente no puede ser negativo"
            );
        }

        if (traspaso.getInteresPendienteInicial() != null
                && traspaso
                .getInteresPendienteInicial()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new ReglaNegocioException(
                    "El interés pendiente no puede ser negativo"
            );
        }
    }
}