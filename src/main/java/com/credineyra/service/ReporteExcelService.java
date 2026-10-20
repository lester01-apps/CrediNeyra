package com.credineyra.service;

import com.credineyra.dto.ReporteFilaDto;
import com.credineyra.dto.ReporteIndicadoresDto;
import com.credineyra.dto.TipoReporte;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReporteExcelService {

    public byte[] generar(TipoReporte tipo, LocalDate inicio, LocalDate fin,
                          ReporteIndicadoresDto indicadores, List<ReporteFilaDto> filas) {
        try (Workbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Sheet hoja = libro.createSheet("Reporte");
            hoja.createFreezePane(0, 5);

            CellStyle titulo = estiloTitulo(libro);
            CellStyle cabecera = estiloCabecera(libro);
            CellStyle moneda = estiloMoneda(libro);
            CellStyle fecha = estiloFecha(libro);

            Row primera = hoja.createRow(0);
            Cell tituloCelda = primera.createCell(0);
            tituloCelda.setCellValue("CREDINEYRA · " + tipo.getEtiqueta());
            tituloCelda.setCellStyle(titulo);
            hoja.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 7));

            hoja.createRow(1).createCell(0).setCellValue("Período: " + inicio + " al " + fin);
            hoja.createRow(2).createCell(0).setCellValue("Generado: " + LocalDate.now());

            Row resumen = hoja.createRow(3);
            escribirResumen(resumen, "Capital recuperado", indicadores.capitalRecuperado(), moneda, 0);
            escribirResumen(resumen, "Intereses cobrados", indicadores.interesesCobrados(), moneda, 2);
            escribirResumen(resumen, "Saldo pendiente", indicadores.saldoPendiente(), moneda, 4);
            escribirResumen(resumen, "Pagos aprobados", BigDecimal.valueOf(indicadores.pagosAprobados()), moneda, 6);

            String[] columnas = {"Código", "Cliente", "Estado", "Fecha", "Capital", "Recuperado", "Saldo"};
            Row encabezado = hoja.createRow(5);
            for (int i = 0; i < columnas.length; i++) {
                Cell celda = encabezado.createCell(i);
                celda.setCellValue(columnas[i]);
                celda.setCellStyle(cabecera);
            }

            int numeroFila = 6;
            for (ReporteFilaDto fila : filas) {
                Row row = hoja.createRow(numeroFila++);
                row.createCell(0).setCellValue(valor(fila.codigo()));
                row.createCell(1).setCellValue(valor(fila.cliente()));
                row.createCell(2).setCellValue(valor(fila.estado()));
                Cell fechaCelda = row.createCell(3);
                if (fila.fecha() != null) {
                    fechaCelda.setCellValue(java.sql.Date.valueOf(fila.fecha()));
                }
                fechaCelda.setCellStyle(fecha);
                escribirMonto(row, 4, fila.capital(), moneda);
                escribirMonto(row, 5, fila.recuperado(), moneda);
                escribirMonto(row, 6, fila.saldo(), moneda);
            }

            for (int i = 0; i < columnas.length; i++) {
                hoja.autoSizeColumn(i);
                hoja.setColumnWidth(i, Math.min(hoja.getColumnWidth(i) + 512, 9_000));
            }
            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo generar el archivo Excel del reporte.", ex);
        }
    }

    private void escribirResumen(Row row, String etiqueta, BigDecimal valor, CellStyle estilo, int columna) {
        row.createCell(columna).setCellValue(etiqueta);
        Cell celda = row.createCell(columna + 1);
        celda.setCellValue(numero(valor).doubleValue());
        celda.setCellStyle(estilo);
    }

    private void escribirMonto(Row row, int columna, BigDecimal valor, CellStyle estilo) {
        Cell celda = row.createCell(columna);
        celda.setCellValue(numero(valor).doubleValue());
        celda.setCellStyle(estilo);
    }

    private CellStyle estiloTitulo(Workbook libro) {
        CellStyle estilo = libro.createCellStyle();
        Font fuente = libro.createFont();
        fuente.setBold(true);
        fuente.setColor(IndexedColors.WHITE.getIndex());
        fuente.setFontHeightInPoints((short) 14);
        estilo.setFont(fuente);
        estilo.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return estilo;
    }

    private CellStyle estiloCabecera(Workbook libro) {
        CellStyle estilo = libro.createCellStyle();
        Font fuente = libro.createFont();
        fuente.setBold(true);
        fuente.setColor(IndexedColors.WHITE.getIndex());
        estilo.setFont(fuente);
        estilo.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return estilo;
    }

    private CellStyle estiloMoneda(Workbook libro) {
        CellStyle estilo = libro.createCellStyle();
        estilo.setDataFormat(libro.createDataFormat().getFormat("S/ #,##0.00"));
        return estilo;
    }

    private CellStyle estiloFecha(Workbook libro) {
        CellStyle estilo = libro.createCellStyle();
        estilo.setDataFormat(libro.createDataFormat().getFormat("dd/mm/yyyy"));
        return estilo;
    }

    private BigDecimal numero(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private String valor(String texto) {
        return texto == null ? "-" : texto;
    }
}
