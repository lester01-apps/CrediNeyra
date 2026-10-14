package com.credineyra.controller;

import com.credineyra.entity.Documento;
import com.credineyra.repository.DocumentoRepository;
import com.credineyra.service.DocumentoCreditoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class DocumentoArchivoController {

    private final DocumentoRepository documentoRepository;

    private final DocumentoCreditoService documentoCreditoService;


    public DocumentoArchivoController(
            DocumentoRepository documentoRepository,
            DocumentoCreditoService documentoCreditoService
    ) {

        this.documentoRepository =
                documentoRepository;

        this.documentoCreditoService =
                documentoCreditoService;
    }


    /*
     * =========================================================
     * VER
     * =========================================================
     *
     * No fuerza descarga.
     *
     * PDF:
     * navegador intenta abrir visor PDF.
     *
     * JPG / PNG:
     * navegador muestra imagen.
     * =========================================================
     */

    @GetMapping("/admin/documentos/{idDocumento}/ver")
    public ResponseEntity<Void> ver(
            @PathVariable
            Long idDocumento
    ) {

        documentoRepository
                .findById(idDocumento)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Documento no encontrado."
                                )
                );


        String url =
                documentoCreditoService
                        .crearUrlTemporal(
                                idDocumento
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


    /*
     * =========================================================
     * DESCARGAR
     * =========================================================
     */

    @GetMapping("/admin/documentos/{idDocumento}/descargar")
    public ResponseEntity<Void> descargar(
            @PathVariable
            Long idDocumento
    ) {

        Documento documento =
                documentoRepository
                        .findById(idDocumento)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Documento no encontrado."
                                        )
                        );


        String url =
                documentoCreditoService
                        .crearUrlTemporal(
                                idDocumento
                        );


        String nombre =
                documento.getNombreArchivo() == null
                        ||
                        documento.getNombreArchivo()
                                .isBlank()
                        ? "documento"
                        : documento
                        .getNombreArchivo();


        String nombreSeguro =
                URLEncoder
                        .encode(
                                nombre,
                                StandardCharsets.UTF_8
                        )
                        .replace(
                                "+",
                                "%20"
                        );


        String separador =
                url.contains("?")
                        ? "&"
                        : "?";


        String urlDescarga =
                url
                        + separador
                        + "download="
                        + nombreSeguro;


        return ResponseEntity
                .status(
                        HttpStatus.FOUND
                )
                .location(
                        URI.create(
                                urlDescarga
                        )
                )
                .build();
    }
}