package com.credineyra.controller;

import com.credineyra.dto.CreditoPanelDto;
import com.credineyra.dto.CreditoRegistroDto;
import com.credineyra.dto.CreditoRegistroResultadoDto;
import com.credineyra.entity.Documento;
import com.credineyra.entity.Usuario;
import com.credineyra.entity.enums.EstadoDocumento;
import com.credineyra.entity.enums.Periodicidad;
import com.credineyra.entity.enums.TipoCalculoCredito;
import com.credineyra.entity.enums.TipoDocumento;
import com.credineyra.repository.DocumentoRepository;
import com.credineyra.service.*;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/creditos")
public class CreditoController {

    private final CreditoService creditoService;

    private final CreditoPanelService creditoPanelService;

    private final CreditoDetalleService creditoDetalleService;

    private final ClienteService clienteService;

    private final TasaService tasaService;

    private final UsuarioService usuarioService;

    private final CreditoCodigoService codigoService;

    private final CreditoRegistroOrquestadorService
            registroOrquestadorService;

    private final DocumentoRepository documentoRepository;

    private final ContratoFirmadoService contratoFirmadoService;


    public CreditoController(
            CreditoService creditoService,
            CreditoPanelService creditoPanelService,
            CreditoDetalleService creditoDetalleService,
            ClienteService clienteService,
            TasaService tasaService,
            UsuarioService usuarioService,
            CreditoCodigoService codigoService,
            CreditoRegistroOrquestadorService registroOrquestadorService,
            DocumentoRepository documentoRepository,
            ContratoFirmadoService contratoFirmadoService
    ) {

        this.creditoService =
                creditoService;

        this.creditoPanelService =
                creditoPanelService;

        this.creditoDetalleService =
                creditoDetalleService;

        this.clienteService =
                clienteService;

        this.tasaService =
                tasaService;

        this.usuarioService =
                usuarioService;

        this.codigoService =
                codigoService;

        this.registroOrquestadorService =
                registroOrquestadorService;

        this.documentoRepository =
                documentoRepository;

        this.contratoFirmadoService =
                contratoFirmadoService;
    }


    /*
     * =========================================================
     * PANEL
     * =========================================================
     */

    @GetMapping
    public String listar(
            Model model
    ) {

        CreditoPanelDto panel =
                creditoPanelService
                        .obtenerPanel();


        model.addAttribute(
                "panel",
                panel
        );


        model.addAttribute(
                "creditos",
                panel.getCreditos()
        );


        model.addAttribute(
                "seccionActiva",
                "creditos"
        );


        return "admin/creditos/lista";
    }


    /*
     * =========================================================
     * NUEVO
     * =========================================================
     */

    @GetMapping("/nuevo")
    public String nuevo(
            @RequestParam(required = false)
            Long idCliente,

            Model model
    ) {

        CreditoRegistroDto dto =
                crearDtoInicial();


        if (idCliente != null) {

            dto.setIdCliente(
                    idCliente
            );
        }


        prepararFormulario(
                model,
                dto
        );


        return "admin/creditos/formulario";
    }


    /*
     * =========================================================
     * GUARDAR
     * =========================================================
     */

    @PostMapping("/guardar")
    public String guardar(
            @Valid
            @ModelAttribute("creditoDto")
            CreditoRegistroDto dto,

            BindingResult result,

            @RequestParam(
                    value = "archivoGarantia",
                    required = false
            )
            MultipartFile archivoGarantia,

            Authentication authentication,

            Model model,

            RedirectAttributes redirectAttributes
    ) {

        if (
                dto.getCodigoCredito() == null
                        ||
                        dto.getCodigoCredito()
                                .isBlank()
        ) {

            dto.setCodigoCredito(
                    codigoService
                            .generarSiguienteCodigo()
            );
        }


        if (result.hasErrors()) {

            prepararFormulario(
                    model,
                    dto
            );


            return "admin/creditos/formulario";
        }


        try {

            Usuario usuario =
                    usuarioService
                            .buscarPorUsername(
                                    authentication
                                            .getName()
                            );


            CreditoRegistroResultadoDto resultado =
                    registroOrquestadorService
                            .registrar(
                                    dto,
                                    archivoGarantia,
                                    usuario
                            );


            redirectAttributes
                    .addAttribute(
                            "documento",
                            resultado
                                    .idDocumentoContrato()
                    );


            return "redirect:/admin/creditos/"
                    + resultado.idCredito()
                    + "/firma";


        } catch (RuntimeException ex) {

            dto.setCodigoCredito(
                    codigoService
                            .generarSiguienteCodigo()
            );


            model.addAttribute(
                    "errorRegistro",
                    ex.getMessage() == null
                            ||
                            ex.getMessage()
                                    .isBlank()
                            ? "No se pudo registrar el crédito."
                            : ex.getMessage()
            );


            prepararFormulario(
                    model,
                    dto
            );


            return "admin/creditos/formulario";
        }
    }


    /*
     * =========================================================
     * FIRMA
     * =========================================================
     */

    @GetMapping("/{idCredito}/firma")
    public String firma(
            @PathVariable
            Long idCredito,

            @RequestParam("documento")
            Long idDocumento,

            Model model
    ) {

        Documento contratoGenerado =
                documentoRepository
                        .findFirstByCredito_IdCreditoAndTipoDocumentoAndEstadoOrderByFechaSubidaDesc(
                                idCredito,
                                TipoDocumento.CONTRATO_GENERADO,
                                EstadoDocumento.ACTIVO
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "No se encontró el contrato generado."
                                        )
                        );


        if (
                !contratoGenerado
                        .getIdDocumento()
                        .equals(idDocumento)
        ) {

            throw new IllegalArgumentException(
                    "El contrato no pertenece a este crédito."
            );
        }


        model.addAttribute(
                "credito",
                creditoService
                        .buscarPorId(
                                idCredito
                        )
        );


        model.addAttribute(
                "idDocumentoContrato",
                idDocumento
        );


        model.addAttribute(
                "seccionActiva",
                "creditos"
        );


        return "admin/creditos/registro-pendiente-firma";
    }


    /*
     * =========================================================
     * SUBIR CONTRATO FIRMADO
     * =========================================================
     */

    @PostMapping(
            value = "/{idCredito}/firma",
            consumes = "multipart/form-data"
    )
    public String subirContratoFirmado(
            @PathVariable
            Long idCredito,

            @RequestParam("contratoFirmado")
            MultipartFile contratoFirmado,

            RedirectAttributes redirectAttributes
    ) {

        try {

            Documento firmado =
                    contratoFirmadoService
                            .subir(
                                    idCredito,
                                    contratoFirmado
                            );


            redirectAttributes
                    .addAttribute(
                            "firmado",
                            firmado
                                    .getIdDocumento()
                    );


            return "redirect:/admin/creditos/"
                    + idCredito
                    + "/registrado";


        } catch (RuntimeException ex) {

            redirectAttributes
                    .addFlashAttribute(
                            "errorFirma",
                            ex.getMessage() == null
                                    ? "No se pudo subir el contrato firmado."
                                    : ex.getMessage()
                    );


            Documento generado =
                    documentoRepository
                            .findFirstByCredito_IdCreditoAndTipoDocumentoAndEstadoOrderByFechaSubidaDesc(
                                    idCredito,
                                    TipoDocumento.CONTRATO_GENERADO,
                                    EstadoDocumento.ACTIVO
                            )
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "No se encontró el contrato generado."
                                            )
                            );


            redirectAttributes
                    .addAttribute(
                            "documento",
                            generado
                                    .getIdDocumento()
                    );


            return "redirect:/admin/creditos/"
                    + idCredito
                    + "/firma";
        }
    }


    /*
     * =========================================================
     * ÉXITO FINAL
     * =========================================================
     */

    @GetMapping("/{idCredito}/registrado")
    public String registrado(
            @PathVariable
            Long idCredito,

            @RequestParam("firmado")
            Long idDocumentoFirmado,

            Model model
    ) {

        Documento firmado =
                documentoRepository
                        .findFirstByCredito_IdCreditoAndTipoDocumentoAndEstadoOrderByFechaSubidaDesc(
                                idCredito,
                                TipoDocumento.CONTRATO_FIRMADO,
                                EstadoDocumento.ACTIVO
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Este crédito todavía no tiene contrato firmado."
                                        )
                        );


        if (
                !firmado
                        .getIdDocumento()
                        .equals(
                                idDocumentoFirmado
                        )
        ) {

            throw new IllegalArgumentException(
                    "El documento firmado no pertenece a este crédito."
            );
        }


        model.addAttribute(
                "credito",
                creditoService
                        .buscarPorId(
                                idCredito
                        )
        );


        model.addAttribute(
                "idDocumentoFirmado",
                firmado
                        .getIdDocumento()
        );


        model.addAttribute(
                "seccionActiva",
                "creditos"
        );


        return "admin/creditos/registro-exitoso";
    }


    /*
     * =========================================================
     * OJITO - DETALLE COMPLETO DEL CRÉDITO
     * =========================================================
     */

    @GetMapping("/{id}")
    public String detalle(
            @PathVariable
            Long id,

            Model model,

            RedirectAttributes redirectAttributes
    ) {

        try {

            model.addAttribute(
                    "detalle",
                    creditoDetalleService
                            .obtener(id)
            );


            model.addAttribute(
                    "seccionActiva",
                    "creditos"
            );


            return "admin/creditos/detalle";


        } catch (RuntimeException ex) {

            redirectAttributes
                    .addFlashAttribute(
                            "mensajeError",
                            ex.getMessage() == null
                                    ? "No se pudo abrir el detalle del crédito."
                                    : ex.getMessage()
                    );


            return "redirect:/admin/creditos";
        }
    }


    /*
     * =========================================================
     * POR CLIENTE
     * =========================================================
     */

    @GetMapping("/cliente/{idCliente}")
    public String porCliente(
            @PathVariable
            Long idCliente,

            Model model
    ) {

        model.addAttribute(
                "cliente",
                clienteService
                        .buscarPorId(
                                idCliente
                        )
        );


        model.addAttribute(
                "creditos",
                creditoService
                        .listarPorCliente(
                                idCliente
                        )
        );


        model.addAttribute(
                "seccionActiva",
                "creditos"
        );


        return "admin/creditos/por-cliente";
    }


    /*
     * =========================================================
     * DTO INICIAL
     * =========================================================
     */

    private CreditoRegistroDto crearDtoInicial() {

        CreditoRegistroDto dto =
                new CreditoRegistroDto();


        dto.setCodigoCredito(
                codigoService
                        .generarSiguienteCodigo()
        );


        dto.setTipoCalculo(
                TipoCalculoCredito.NORMAL
        );


        dto.setPeriodicidadTasa(
                Periodicidad.MENSUAL
        );


        dto.setFrecuenciaPago(
                Periodicidad.MENSUAL
        );


        dto.setPlazoMeses(
                1
        );


        LocalDate hoy =
                LocalDate.now();


        dto.setFechaInicio(
                hoy
        );


        dto.setFechaPrimerPago(
                hoy.plusMonths(1)
        );


        dto.setPermitePagoSoloInteres(
                false
        );


        return dto;
    }


    /*
     * =========================================================
     * FORMULARIO
     * =========================================================
     */

    private void prepararFormulario(
            Model model,
            CreditoRegistroDto dto
    ) {

        if (
                dto.getCodigoCredito() == null
                        ||
                        dto.getCodigoCredito()
                                .isBlank()
        ) {

            dto.setCodigoCredito(
                    codigoService
                            .generarSiguienteCodigo()
            );
        }


        model.addAttribute(
                "creditoDto",
                dto
        );


        model.addAttribute(
                "clientes",
                clienteService
                        .listarActivos()
        );


        model.addAttribute(
                "tasas",
                tasaService
                        .listarActivas()
        );


        model.addAttribute(
                "tiposInteres",
                List.of(
                        TipoCalculoCredito.NORMAL,
                        TipoCalculoCredito.INTERES_ECONOMICO
                )
        );


        model.addAttribute(
                "periodicidadesInteres",
                List.of(
                        Periodicidad.SEMANAL,
                        Periodicidad.QUINCENAL,
                        Periodicidad.MENSUAL
                )
        );


        model.addAttribute(
                "frecuenciasPago",
                List.of(
                        Periodicidad.DIARIA,
                        Periodicidad.SEMANAL,
                        Periodicidad.QUINCENAL,
                        Periodicidad.MENSUAL
                )
        );


        model.addAttribute(
                "seccionActiva",
                "creditos"
        );
    }
}