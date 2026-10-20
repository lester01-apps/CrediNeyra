package com.credineyra.controller;

import com.credineyra.dto.ReporteIndicadoresDto;
import com.credineyra.dto.ReportePanelDto;
import com.credineyra.dto.TipoReporte;
import com.credineyra.entity.enums.EstadoCredito;
import com.credineyra.service.ReporteExcelService;
import com.credineyra.service.ReportePdfService;
import com.credineyra.service.ReporteService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/admin/reportes")
public class ReporteController {

    private final ReporteService reporteService;
    private final ReportePdfService reportePdfService;
    private final ReporteExcelService reporteExcelService;

    public ReporteController(ReporteService reporteService, ReportePdfService reportePdfService,
                             ReporteExcelService reporteExcelService) {
        this.reporteService = reporteService;
        this.reportePdfService = reportePdfService;
        this.reporteExcelService = reporteExcelService;
    }

    @GetMapping
    public String panel(@RequestParam(required = false) LocalDate inicio,
                        @RequestParam(required = false) LocalDate fin,
                        @RequestParam(required = false) EstadoCredito estado,
                        @RequestParam(required = false) String tipo,
                        @RequestParam(defaultValue = "0") int pagina,
                        Authentication authentication,
                        Model model) {
        LocalDate inicioSeguro = inicio == null ? reporteService.inicioPredeterminado() : inicio;
        LocalDate finSeguro = fin == null ? reporteService.finPredeterminado() : fin;
        TipoReporte tipoSeguro = TipoReporte.desde(tipo);
        ReportePanelDto reporte = reporteService.obtenerPanel(inicioSeguro, finSeguro, estado, tipoSeguro, pagina, 15);
        cargarModelo(model, authentication, inicioSeguro, finSeguro, estado, tipoSeguro, reporte);
        return "admin/reportes/panel";
    }

    @GetMapping("/exportar/pdf")
    public ResponseEntity<ByteArrayResource> exportarPdf(@RequestParam(required = false) LocalDate inicio,
                                                           @RequestParam(required = false) LocalDate fin,
                                                           @RequestParam(required = false) EstadoCredito estado,
                                                           @RequestParam(required = false) String tipo) {
        Filtros filtros = filtros(inicio, fin, estado, tipo);
        ReportePanelDto panel = reporteService.obtenerPanel(filtros.inicio(), filtros.fin(), filtros.estado(), filtros.tipo(), 0, 15);
        byte[] archivo = reportePdfService.generar(filtros.tipo(), filtros.inicio(), filtros.fin(), panel.indicadores(),
                reporteService.obtenerFilasExportacion(filtros.inicio(), filtros.fin(), filtros.estado(), filtros.tipo()));
        return descarga(archivo, "application/pdf", nombre(filtros.tipo(), "pdf"));
    }

    @GetMapping("/exportar/excel")
    public ResponseEntity<ByteArrayResource> exportarExcel(@RequestParam(required = false) LocalDate inicio,
                                                             @RequestParam(required = false) LocalDate fin,
                                                             @RequestParam(required = false) EstadoCredito estado,
                                                             @RequestParam(required = false) String tipo) {
        Filtros filtros = filtros(inicio, fin, estado, tipo);
        ReportePanelDto panel = reporteService.obtenerPanel(filtros.inicio(), filtros.fin(), filtros.estado(), filtros.tipo(), 0, 15);
        byte[] archivo = reporteExcelService.generar(filtros.tipo(), filtros.inicio(), filtros.fin(), panel.indicadores(),
                reporteService.obtenerFilasExportacion(filtros.inicio(), filtros.fin(), filtros.estado(), filtros.tipo()));
        return descarga(archivo, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", nombre(filtros.tipo(), "xlsx"));
    }

    private void cargarModelo(Model model, Authentication authentication, LocalDate inicio, LocalDate fin,
                              EstadoCredito estado, TipoReporte tipo, ReportePanelDto reporte) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("inicio", inicio);
        model.addAttribute("fin", fin);
        model.addAttribute("estadoSeleccionado", estado == null ? "" : estado.name());
        model.addAttribute("tipoSeleccionado", tipo.name());
        model.addAttribute("tipoReporteEtiqueta", tipo.getEtiqueta());
        model.addAttribute("estadosCredito", EstadoCredito.values());
        model.addAttribute("tiposReporte", TipoReporte.values());
        model.addAttribute("reporte", reporte);
        model.addAttribute("seccionActiva", "reportes");
    }

    private Filtros filtros(LocalDate inicio, LocalDate fin, EstadoCredito estado, String tipo) {
        return new Filtros(inicio == null ? reporteService.inicioPredeterminado() : inicio,
                fin == null ? reporteService.finPredeterminado() : fin,
                estado, TipoReporte.desde(tipo));
    }

    private ResponseEntity<ByteArrayResource> descarga(byte[] contenido, String contentType, String nombre) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(contenido.length)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(nombre, StandardCharsets.UTF_8).build().toString())
                .body(new ByteArrayResource(contenido));
    }

    private String nombre(TipoReporte tipo, String extension) {
        return "credineyra-" + tipo.name().toLowerCase() + "-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "." + extension;
    }

    private record Filtros(LocalDate inicio, LocalDate fin, EstadoCredito estado, TipoReporte tipo) { }
}
