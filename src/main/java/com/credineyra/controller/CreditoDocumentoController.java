package com.credineyra.controller;

import com.credineyra.dto.DocumentoResumenDto;
import com.credineyra.entity.Documento;
import com.credineyra.service.DocumentoCreditoService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class CreditoDocumentoController {

    private final DocumentoCreditoService
            documentoCreditoService;

    public CreditoDocumentoController(
            DocumentoCreditoService documentoCreditoService
    ) {
        this.documentoCreditoService =
                documentoCreditoService;
    }

    @GetMapping(
            "/creditos/{idCredito}/documentos"
    )
    public ResponseEntity<List<DocumentoResumenDto>>
    listar(
            @PathVariable Long idCredito
    ) {

        return ResponseEntity.ok(
                documentoCreditoService
                        .listarCredito(
                                idCredito
                        )
        );
    }

    @PostMapping(
            "/creditos/{idCredito}/documentos/contrato/generar"
    )
    public ResponseEntity<DocumentoResumenDto>
    generarContrato(
            @PathVariable Long idCredito
    ) {

        Documento documento =
                documentoCreditoService
                        .generarContratoSiFalta(
                                idCredito
                        );

        return ResponseEntity.ok(
                convertir(
                        documento
                )
        );
    }

    @PostMapping(
            value = "/creditos/{idCredito}/documentos/contrato-firmado",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<DocumentoResumenDto>
    subirContratoFirmado(
            @PathVariable Long idCredito,
            @RequestPart("archivo")
            MultipartFile archivo
    ) {

        Documento documento =
                documentoCreditoService
                        .subirContratoFirmado(
                                idCredito,
                                archivo
                        );

        return ResponseEntity.ok(
                convertir(
                        documento
                )
        );
    }

    @PostMapping(
            value = "/creditos/{idCredito}/garantias/{idGarantia}/documentos",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<DocumentoResumenDto>
    subirGarantia(
            @PathVariable Long idCredito,
            @PathVariable Long idGarantia,
            @RequestPart("archivo")
            MultipartFile archivo
    ) {

        Documento documento =
                documentoCreditoService
                        .subirDocumentoGarantia(
                                idCredito,
                                idGarantia,
                                archivo
                        );

        return ResponseEntity.ok(
                convertir(
                        documento
                )
        );
    }

    @GetMapping(
            "/documentos/{idDocumento}/abrir"
    )
    public ResponseEntity<Void> abrir(
            @PathVariable Long idDocumento
    ) {

        String url =
                documentoCreditoService
                        .crearUrlTemporal(
                                idDocumento
                        );

        HttpHeaders headers =
                new HttpHeaders();

        headers.setLocation(
                URI.create(
                        url
                )
        );

        return new ResponseEntity<>(
                headers,
                HttpStatus.FOUND
        );
    }

    private DocumentoResumenDto convertir(
            Documento documento
    ) {

        return new DocumentoResumenDto(
                documento.getIdDocumento(),
                documento.getCodigoDocumento(),
                documento.getTipoDocumento(),
                documento.getNombreArchivo(),
                documento.getEstado(),
                documento.getFechaSubida(),
                documento.getVisibleCliente(),
                documento.getObservaciones()
        );
    }
}