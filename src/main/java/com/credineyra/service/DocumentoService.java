package com.credineyra.service;

import com.credineyra.entity.Documento;
import com.credineyra.entity.enums.EstadoDocumento;
import com.credineyra.entity.enums.TipoDocumento;
import com.credineyra.exception.DatoDuplicadoException;
import com.credineyra.exception.RecursoNoEncontradoException;
import com.credineyra.exception.ReglaNegocioException;
import com.credineyra.repository.DocumentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DocumentoService {

    private final DocumentoRepository repository;

    public DocumentoService(
            DocumentoRepository repository
    ) {
        this.repository = repository;
    }

    public Documento registrar(
            Documento documento
    ) {

        validar(documento);

        if (documento.getCodigoDocumento() != null
                && !documento.getCodigoDocumento()
                .isBlank()
                && repository
                .findByCodigoDocumento(
                        documento.getCodigoDocumento()
                )
                .isPresent()) {

            throw new DatoDuplicadoException(
                    "El código del documento ya existe"
            );
        }

        return repository.save(documento);
    }

    @Transactional(readOnly = true)
    public Documento buscarPorId(
            Long idDocumento
    ) {

        return repository.findById(idDocumento)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Documento no encontrado"
                        )
                );
    }

    @Transactional(readOnly = true)
    public Documento buscarPorCodigo(
            String codigo
    ) {

        return repository
                .findByCodigoDocumento(codigo)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Documento no encontrado"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<Documento> listarTodos() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Documento> listarPorCliente(
            Long idCliente
    ) {

        return repository
                .findByCliente_IdClienteOrderByFechaSubidaDesc(
                        idCliente
                );
    }

    @Transactional(readOnly = true)
    public List<Documento> listarPorCredito(
            Long idCredito
    ) {

        return repository
                .findByCredito_IdCredito(idCredito);
    }

    @Transactional(readOnly = true)
    public List<Documento> listarPorTipo(
            TipoDocumento tipo
    ) {

        return repository.findByTipoDocumento(tipo);
    }

    @Transactional(readOnly = true)
    public List<Documento> listarPorEstado(
            EstadoDocumento estado
    ) {

        return repository.findByEstado(estado);
    }

    private void validar(
            Documento documento
    ) {

        if (documento == null) {
            throw new ReglaNegocioException(
                    "Los datos del documento son obligatorios"
            );
        }

        if (documento.getCliente() == null) {
            throw new ReglaNegocioException(
                    "El documento debe pertenecer a un cliente"
            );
        }

        if (documento.getTipoDocumento() == null) {
            throw new ReglaNegocioException(
                    "El tipo de documento es obligatorio"
            );
        }

        if (documento.getNombreArchivo() == null
                || documento.getNombreArchivo()
                .isBlank()) {

            throw new ReglaNegocioException(
                    "El nombre del archivo es obligatorio"
            );
        }

        if (documento.getRutaArchivo() == null
                || documento.getRutaArchivo()
                .isBlank()) {

            throw new ReglaNegocioException(
                    "La ruta del archivo es obligatoria"
            );
        }
    }
}