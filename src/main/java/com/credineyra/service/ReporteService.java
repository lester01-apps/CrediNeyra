package com.credineyra.service;

import com.credineyra.dto.ReporteFilaDto;
import com.credineyra.dto.ReportePanelDto;
import com.credineyra.dto.TipoReporte;
import com.credineyra.entity.enums.EstadoCredito;
import com.credineyra.repository.ReporteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class ReporteService {

    private static final int TAMANO_MAXIMO = 100;
    private final ReporteRepository reporteRepository;

    public ReporteService(ReporteRepository reporteRepository) {
        this.reporteRepository = reporteRepository;
    }

    @Transactional(readOnly = true)
    public ReportePanelDto obtenerPanel(LocalDate inicio, LocalDate fin, EstadoCredito estado,
                                        TipoReporte tipo, int pagina, int tamano) {
        Rango rango = rango(inicio, fin);
        Pageable pageable = PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamano, 10), TAMANO_MAXIMO));
        Page<ReporteFilaDto> filas = reporteRepository.filas(tipo, rango.inicio(), rango.fin(), estado, pageable);
        return new ReportePanelDto(
                reporteRepository.indicadores(rango.inicio(), rango.fin(), estado),
                reporteRepository.recuperacionMensual(rango.inicio(), rango.fin()),
                reporteRepository.evolucionCreditos(rango.inicio(), rango.fin()),
                reporteRepository.distribucionEstados(rango.inicio(), rango.fin(), estado),
                filas,
                reporteRepository.deudaVencida(estado)
        );
    }

    @Transactional(readOnly = true)
    public List<ReporteFilaDto> obtenerFilasExportacion(LocalDate inicio, LocalDate fin,
                                                         EstadoCredito estado, TipoReporte tipo) {
        Rango rango = rango(inicio, fin);
        return reporteRepository.filas(tipo, rango.inicio(), rango.fin(), estado,
                        PageRequest.of(0, 10_000))
                .getContent();
    }

    public LocalDate inicioPredeterminado() {
        return YearMonth.now().minusMonths(5).atDay(1);
    }

    public LocalDate finPredeterminado() {
        return LocalDate.now();
    }

    private Rango rango(LocalDate inicio, LocalDate fin) {
        LocalDate inicioSeguro = inicio == null ? inicioPredeterminado() : inicio;
        LocalDate finSeguro = fin == null ? finPredeterminado() : fin;
        if (finSeguro.isBefore(inicioSeguro)) {
            throw new IllegalArgumentException("La fecha final no puede ser anterior a la fecha inicial.");
        }
        if (inicioSeguro.plusYears(3).isBefore(finSeguro)) {
            throw new IllegalArgumentException("El período máximo permitido para un reporte es de tres años.");
        }
        return new Rango(inicioSeguro, finSeguro);
    }

    private record Rango(LocalDate inicio, LocalDate fin) { }
}
