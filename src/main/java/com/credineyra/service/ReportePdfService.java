package com.credineyra.service;

import com.credineyra.dto.ReporteFilaDto;
import com.credineyra.dto.ReporteIndicadoresDto;
import com.credineyra.dto.TipoReporte;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class ReportePdfService {

    private static final PDType1Font REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font NEGRITA = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("es", "PE"));

    public byte[] generar(TipoReporte tipo, LocalDate inicio, LocalDate fin,
                          ReporteIndicadoresDto indicadores, List<ReporteFilaDto> filas) {
        try (PDDocument documento = new PDDocument(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Pagina pagina = nuevaPagina(documento);
            cabecera(pagina, tipo, inicio, fin, indicadores);
            float y = 640;
            encabezadoTabla(pagina, y);
            y -= 16;
            int filasEnPagina = 0;
            for (ReporteFilaDto fila : filas) {
                if (filasEnPagina == 31) {
                    pagina.cerrar();
                    pagina = nuevaPagina(documento);
                    encabezadoTabla(pagina, 790);
                    y = 774;
                    filasEnPagina = 0;
                }
                fila(pagina, y, fila, filasEnPagina % 2 == 0);
                y -= 16;
                filasEnPagina++;
            }
            if (filas.isEmpty()) {
                texto(pagina, "No hay registros para los filtros seleccionados.", REGULAR, 10, 45, y - 18, Color.DARK_GRAY);
            }
            pagina.cerrar();
            documento.save(salida);
            return salida.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo generar el PDF del reporte.", ex);
        }
    }

    private Pagina nuevaPagina(PDDocument documento) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        documento.addPage(page);
        return new Pagina(page, new PDPageContentStream(documento, page));
    }

    private void cabecera(Pagina pagina, TipoReporte tipo, LocalDate inicio, LocalDate fin,
                          ReporteIndicadoresDto indicadores) throws IOException {
        pagina.canvas().setNonStrokingColor(new Color(6, 45, 98));
        pagina.canvas().addRect(0, 760, 595, 82);
        pagina.canvas().fill();
        texto(pagina, "crediNeyra", NEGRITA, 20, 42, 808, Color.WHITE);
        texto(pagina, tipo.getEtiqueta(), NEGRITA, 14, 42, 783, Color.WHITE);
        texto(pagina, "Período: " + FECHA.format(inicio) + " al " + FECHA.format(fin), REGULAR, 9, 42, 767, new Color(210, 230, 255));
        texto(pagina, "Generado: " + FECHA.format(LocalDate.now()), REGULAR, 8, 395, 767, new Color(210, 230, 255));

        resumen(pagina, 42, "Capital recuperado", moneda(indicadores.capitalRecuperado()));
        resumen(pagina, 190, "Intereses cobrados", moneda(indicadores.interesesCobrados()));
        resumen(pagina, 338, "Saldo pendiente", moneda(indicadores.saldoPendiente()));
        resumen(pagina, 486, "Pagos aprobados", String.valueOf(indicadores.pagosAprobados()));
    }

    private void resumen(Pagina pagina, float x, String etiqueta, String valor) throws IOException {
        pagina.canvas().setNonStrokingColor(new Color(240, 246, 253));
        pagina.canvas().addRect(x, 685, 130, 48);
        pagina.canvas().fill();
        texto(pagina, etiqueta, REGULAR, 7, x + 7, 718, new Color(65, 88, 120));
        texto(pagina, valor, NEGRITA, 9, x + 7, 700, new Color(8, 50, 105));
    }

    private void encabezadoTabla(Pagina pagina, float y) throws IOException {
        pagina.canvas().setNonStrokingColor(new Color(8, 72, 157));
        pagina.canvas().addRect(38, y - 12, 520, 15);
        pagina.canvas().fill();
        String[] etiquetas = {"Código", "Cliente", "Estado", "Fecha", "Capital", "Recuperado", "Saldo"};
        float[] x = {42, 108, 230, 310, 370, 433, 500};
        for (int i = 0; i < etiquetas.length; i++) {
            texto(pagina, etiquetas[i], NEGRITA, 6.5f, x[i], y - 7, Color.WHITE);
        }
    }

    private void fila(Pagina pagina, float y, ReporteFilaDto fila, boolean sombreada) throws IOException {
        if (sombreada) {
            pagina.canvas().setNonStrokingColor(new Color(246, 249, 253));
            pagina.canvas().addRect(38, y - 12, 520, 15);
            pagina.canvas().fill();
        }
        Color color = new Color(30, 45, 65);
        texto(pagina, recortar(fila.codigo(), 13), REGULAR, 6.5f, 42, y - 7, color);
        texto(pagina, recortar(fila.cliente(), 20), REGULAR, 6.5f, 108, y - 7, color);
        texto(pagina, recortar(fila.estado(), 14), REGULAR, 6.5f, 230, y - 7, color);
        texto(pagina, fila.fecha() == null ? "-" : FECHA.format(fila.fecha()), REGULAR, 6.5f, 310, y - 7, color);
        texto(pagina, recortar(moneda(fila.capital()), 10), REGULAR, 6.5f, 370, y - 7, color);
        texto(pagina, recortar(moneda(fila.recuperado()), 10), REGULAR, 6.5f, 433, y - 7, color);
        texto(pagina, recortar(moneda(fila.saldo()), 10), REGULAR, 6.5f, 500, y - 7, color);
    }

    private void texto(Pagina pagina, String contenido, PDType1Font fuente, float tamano,
                       float x, float y, Color color) throws IOException {
        pagina.canvas().beginText();
        pagina.canvas().setNonStrokingColor(color);
        pagina.canvas().setFont(fuente, tamano);
        pagina.canvas().newLineAtOffset(x, y);
        pagina.canvas().showText(limpiar(contenido));
        pagina.canvas().endText();
    }

    private String moneda(BigDecimal monto) {
        return "S/ " + (monto == null ? BigDecimal.ZERO : monto).setScale(2).toPlainString();
    }

    private String recortar(String texto, int maximo) {
        String seguro = texto == null ? "-" : texto;
        return seguro.length() <= maximo ? seguro : seguro.substring(0, maximo - 1) + "…";
    }

    private String limpiar(String texto) {
        return (texto == null ? "" : texto).replace("…", "...").replace("–", "-").replace("—", "-");
    }

    private record Pagina(PDPage page, PDPageContentStream canvas) {
        void cerrar() throws IOException { canvas.close(); }
    }
}
