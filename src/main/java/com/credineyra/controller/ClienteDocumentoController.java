package com.credineyra.controller;

import com.credineyra.entity.Documento;
import com.credineyra.entity.Usuario;
import com.credineyra.exception.OperacionNoPermitidaException;
import com.credineyra.exception.RecursoNoEncontradoException;
import com.credineyra.repository.DocumentoRepository;
import com.credineyra.service.DocumentoStorageService;
import com.credineyra.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URI;

@Controller
public class ClienteDocumentoController {

    private final DocumentoRepository documentoRepository;

    private final DocumentoStorageService storageService;

    private final UsuarioService usuarioService;

    public ClienteDocumentoController(
            DocumentoRepository documentoRepository,
            DocumentoStorageService storageService,
            UsuarioService usuarioService
    ) {

        this.documentoRepository =
                documentoRepository;

        this.storageService =
                storageService;

        this.usuarioService =
                usuarioService;
    }

    @GetMapping(
            "/cliente/documentos/{idDocumento}/ver"
    )
    public ResponseEntity<Void> ver(
            @PathVariable Long idDocumento,
            Authentication authentication
    ) {

        Documento documento =
                obtenerAutorizado(
                        idDocumento,
                        authentication
                );

        String url =
                storageService
                        .crearUrlFirmada(
                                documento.getRutaArchivo(),
                                600
                        );

        return ResponseEntity
                .status(
                        HttpStatus.FOUND
                )
                .location(
                        URI.create(url)
                )
                .build();
    }

    @GetMapping(
            "/cliente/documentos/{idDocumento}/descargar"
    )
    public ResponseEntity<Void> descargar(
            @PathVariable Long idDocumento,
            Authentication authentication
    ) {

        Documento documento =
                obtenerAutorizado(
                        idDocumento,
                        authentication
                );

        String url =
                storageService
                        .crearUrlFirmada(
                                documento.getRutaArchivo(),
                                600
                        );

        String separador =
                url.contains("?")
                        ? "&"
                        : "?";

        url =
                url
                        + separador
                        + "download=true";

        return ResponseEntity
                .status(
                        HttpStatus.FOUND
                )
                .location(
                        URI.create(url)
                )
                .build();
    }

    private Documento obtenerAutorizado(
            Long idDocumento,
            Authentication authentication
    ) {

        Documento documento =
                documentoRepository
                        .findById(idDocumento)
                        .orElseThrow(
                                () ->
                                        new RecursoNoEncontradoException(
                                                "Documento no encontrado"
                                        )
                        );

        if (
                !Boolean.TRUE.equals(
                        documento.getVisibleCliente()
                )
        ) {

            throw new OperacionNoPermitidaException(
                    "Este documento no está disponible para el cliente"
            );
        }

        Usuario usuario =
                usuarioService
                        .buscarPorUsername(
                                authentication.getName()
                        );

        if (
                usuario.getPersona() == null
                        ||
                        documento.getCliente() == null
                        ||
                        documento.getCliente()
                                .getPersona() == null
                        ||
                        !usuario.getPersona()
                                .getIdPersona()
                                .equals(
                                        documento
                                                .getCliente()
                                                .getPersona()
                                                .getIdPersona()
                                )
        ) {

            throw new OperacionNoPermitidaException(
                    "No tienes permiso para ver este documento"
            );
        }

        return documento;
    }
}