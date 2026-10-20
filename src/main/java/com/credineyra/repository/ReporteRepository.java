package com.credineyra.repository;

import com.credineyra.dto.ReporteEstadoDto;
import com.credineyra.dto.ReporteFilaDto;
import com.credineyra.dto.ReporteIndicadoresDto;
import com.credineyra.dto.ReporteSerieDto;
import com.credineyra.dto.TipoReporte;
import com.credineyra.entity.Credito;
import com.credineyra.entity.Cuota;
import com.credineyra.entity.Pago;
import com.credineyra.entity.PagoDetalle;
import com.credineyra.entity.enums.EstadoCredito;
import com.credineyra.entity.enums.EstadoCuota;
import com.credineyra.entity.enums.EstadoPago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Lectura del módulo de Reportes usando únicamente los repositorios JPA y las
 * entidades existentes. No contiene SQL nativo ni configuración de Supabase.
 */
@Repository
public class ReporteRepository {

    private static final EnumSet<EstadoCredito> ESTADOS_ACTIVOS =
            EnumSet.of(EstadoCredito.ACTIVO, EstadoCredito.AL_DIA);
    private static final EnumSet<EstadoCredito> ESTADOS_MORA =
            EnumSet.of(EstadoCredito.EN_MORA, EstadoCredito.ATRASO_PROLONGADO);
    private static final EnumSet<EstadoCuota> ESTADOS_CUOTA_ABIERTA =
            EnumSet.of(EstadoCuota.PENDIENTE, EstadoCuota.PARCIAL, EstadoCuota.VENCIDA);
    private static final DateTimeFormatter ETIQUETA_MES =
            DateTimeFormatter.ofPattern("MMM yy", new Locale("es", "PE"));

    private final CreditoRepository creditoRepository;
    private final CuotaRepository cuotaRepository;
    private final PagoRepository pagoRepository;
    private final PagoDetalleRepository pagoDetalleRepository;
    private final PlanRegularizacionRepository planRegularizacionRepository;

    public ReporteRepository(CreditoRepository creditoRepository,
                             CuotaRepository cuotaRepository,
                             PagoRepository pagoRepository,
                             PagoDetalleRepository pagoDetalleRepository,
                             PlanRegularizacionRepository planRegularizacionRepository) {
        this.creditoRepository = creditoRepository;
        this.cuotaRepository = cuotaRepository;
        this.pagoRepository = pagoRepository;
        this.pagoDetalleRepository = pagoDetalleRepository;
        this.planRegularizacionRepository = planRegularizacionRepository;
    }

    public ReporteIndicadoresDto indicadores(LocalDate inicio, LocalDate fin, EstadoCredito estado) {
        List<Credito> creditos = creditosEnRango(inicio, fin, estado);
        List<Pago> pagos = pagosEnRango(inicio, fin);
        Map<Long, List<PagoDetalle>> detalles = detallesPorPago();
        long cuotasVencidas = cuotaRepository.findAll().stream()
                .filter(cuota -> cuota.getFechaVencimiento().isBefore(LocalDate.now()))
                .filter(cuota -> ESTADOS_CUOTA_ABIERTA.contains(cuota.getEstado()))
                .filter(cuota -> coincideEstado(cuota.getCredito(), estado))
                .count();
        BigDecimal capital = pagos.stream().flatMap(pago -> detalles.getOrDefault(pago.getIdPago(), List.of()).stream())
                .map(detalle -> suma(detalle.getMontoCapital(), detalle.getMontoCapitalRegularizado()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal interes = pagos.stream().flatMap(pago -> detalles.getOrDefault(pago.getIdPago(), List.of()).stream())
                .map(PagoDetalle::getMontoInteres).map(ReporteRepository::seguro)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal total = pagos.stream().map(Pago::getMonto).map(ReporteRepository::seguro)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ReporteIndicadoresDto(
                creditos.stream().filter(credito -> ESTADOS_ACTIVOS.contains(credito.getEstado())).count(),
                creditos.stream().filter(credito -> ESTADOS_MORA.contains(credito.getEstado())).count(),
                cuotasVencidas, pagos.size(), capital, interes,
                creditos.stream().map(this::saldoCredito).reduce(BigDecimal.ZERO, BigDecimal::add), total
        );
    }

    public BigDecimal deudaVencida(EstadoCredito estado) {
        return cuotaRepository.findAll().stream()
                .filter(cuota -> cuota.getFechaVencimiento().isBefore(LocalDate.now()))
                .filter(cuota -> ESTADOS_CUOTA_ABIERTA.contains(cuota.getEstado()))
                .filter(cuota -> coincideEstado(cuota.getCredito(), estado))
                .map(cuota -> suma(cuota.getCapitalPendiente(), cuota.getInteresPendiente()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<ReporteSerieDto> recuperacionMensual(LocalDate inicio, LocalDate fin) {
        Map<Long, List<PagoDetalle>> detalles = detallesPorPago();
        return pagosEnRango(inicio, fin).stream()
                .collect(Collectors.groupingBy(pago -> YearMonth.from(pago.getFechaPago())))
                .entrySet().stream().sorted(Map.Entry.comparingByKey())
                .map(entrada -> serieRecuperacion(entrada.getKey(), entrada.getValue(), detalles))
                .toList();
    }

    public List<ReporteSerieDto> evolucionCreditos(LocalDate inicio, LocalDate fin) {
        return creditosEnRango(inicio, fin, null).stream()
                .collect(Collectors.groupingBy(credito -> YearMonth.from(credito.getFechaSolicitud())))
                .entrySet().stream().sorted(Map.Entry.comparingByKey())
                .map(entrada -> new ReporteSerieDto(etiqueta(entrada.getKey()),
                        entrada.getValue().stream().map(Credito::getCapitalOriginal).map(ReporteRepository::seguro)
                                .reduce(BigDecimal.ZERO, BigDecimal::add),
                        BigDecimal.valueOf(entrada.getValue().size())))
                .toList();
    }

    public List<ReporteEstadoDto> distribucionEstados(LocalDate inicio, LocalDate fin, EstadoCredito estado) {
        return creditosEnRango(inicio, fin, estado).stream()
                .collect(Collectors.groupingBy(Credito::getEstado, Collectors.counting()))
                .entrySet().stream().sorted(Map.Entry.comparingByKey())
                .map(entrada -> new ReporteEstadoDto(entrada.getKey().name(), entrada.getValue()))
                .toList();
    }

    public Page<ReporteFilaDto> filas(TipoReporte tipo, LocalDate inicio, LocalDate fin,
                                      EstadoCredito estado, Pageable pageable) {
        List<ReporteFilaDto> filas = filasSinPaginar(tipo, inicio, fin, estado);
        filas.sort(Comparator.comparing(ReporteFilaDto::fecha, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(ReporteFilaDto::codigo));
        int desde = Math.min((int) pageable.getOffset(), filas.size());
        int hasta = Math.min(desde + pageable.getPageSize(), filas.size());
        return new PageImpl<>(filas.subList(desde, hasta), pageable, filas.size());
    }

    private List<ReporteFilaDto> filasSinPaginar(TipoReporte tipo, LocalDate inicio, LocalDate fin, EstadoCredito estado) {
        return switch (tipo) {
            case PAGOS_APROBADOS, CAPITAL_PRESTADO_RECUPERADO, RENDIMIENTO_FINANCIERO -> filasPagos(inicio, fin, estado);
            case CUOTAS_PENDIENTES -> filasCuotas(inicio, fin, estado);
            case REGULARIZACIONES -> filasRegularizaciones(inicio, fin, estado);
            default -> filasCreditos(tipo, inicio, fin, estado);
        };
    }

    private List<ReporteFilaDto> filasCreditos(TipoReporte tipo, LocalDate inicio, LocalDate fin, EstadoCredito estado) {
        Predicate<Credito> condicion = switch (tipo) {
            case CREDITOS_ACTIVOS -> credito -> ESTADOS_ACTIVOS.contains(credito.getEstado());
            case CREDITOS_EN_MORA -> credito -> ESTADOS_MORA.contains(credito.getEstado());
            case SALDOS_PENDIENTES, CLIENTES_CON_DEUDA -> credito -> saldoCredito(credito).signum() > 0;
            default -> credito -> true;
        };
        return creditosEnRango(inicio, fin, estado).stream().filter(condicion)
                .map(credito -> new ReporteFilaDto(credito.getIdCredito(), credito.getCodigoCredito(), nombreCliente(credito),
                        credito.getEstado().name(), credito.getFechaSolicitud().toLocalDate(), seguro(credito.getCapitalOriginal()),
                        suma(credito.getCapitalPagado(), credito.getInteresPagado()), saldoCredito(credito)))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<ReporteFilaDto> filasPagos(LocalDate inicio, LocalDate fin, EstadoCredito estado) {
        Map<Long, List<PagoDetalle>> detalles = detallesPorPago();
        return pagosEnRango(inicio, fin).stream().filter(pago -> coincideEstado(pago.getCredito(), estado))
                .map(pago -> new ReporteFilaDto(pago.getIdPago(), "PAG-%06d".formatted(pago.getIdPago()),
                        nombreCliente(pago.getCredito()), pago.getEstado().name(), pago.getFechaPago().toLocalDate(),
                        BigDecimal.ZERO, detalles.getOrDefault(pago.getIdPago(), List.of()).stream().map(PagoDetalle::getMontoTotal)
                        .map(ReporteRepository::seguro).reduce(BigDecimal.ZERO, BigDecimal::add), BigDecimal.ZERO))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<ReporteFilaDto> filasCuotas(LocalDate inicio, LocalDate fin, EstadoCredito estado) {
        return cuotaRepository.findAll().stream().filter(cuota -> enRango(cuota.getFechaVencimiento(), inicio, fin))
                .filter(cuota -> ESTADOS_CUOTA_ABIERTA.contains(cuota.getEstado()))
                .filter(cuota -> coincideEstado(cuota.getCredito(), estado))
                .map(cuota -> new ReporteFilaDto(cuota.getIdCuota(), cuota.getCredito().getCodigoCredito() + " · cuota " + cuota.getNumeroCuota(),
                        nombreCliente(cuota.getCredito()), cuota.getEstado().name(), cuota.getFechaVencimiento(), seguro(cuota.getCapitalOriginal()),
                        suma(cuota.getCapitalPagado(), cuota.getInteresPagado()), suma(cuota.getCapitalPendiente(), cuota.getInteresPendiente())))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<ReporteFilaDto> filasRegularizaciones(LocalDate inicio, LocalDate fin, EstadoCredito estado) {
        return planRegularizacionRepository.findAll().stream()
                .filter(plan -> enRango(plan.getFechaCreacion().toLocalDate(), inicio, fin))
                .filter(plan -> coincideEstado(plan.getCredito(), estado))
                .map(plan -> new ReporteFilaDto(plan.getIdPlan(), "REG-%06d".formatted(plan.getIdPlan()),
                        nombreCliente(plan.getCredito()), plan.getEstado().name(), plan.getFechaCreacion().toLocalDate(),
                        seguro(plan.getCapitalPendienteOrigen()), BigDecimal.ZERO, saldoCredito(plan.getCredito())))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<Credito> creditosEnRango(LocalDate inicio, LocalDate fin, EstadoCredito estado) {
        return creditoRepository.findAll().stream()
                .filter(credito -> enRango(credito.getFechaSolicitud().toLocalDate(), inicio, fin))
                .filter(credito -> coincideEstado(credito, estado)).toList();
    }

    private List<Pago> pagosEnRango(LocalDate inicio, LocalDate fin) {
        return pagoRepository.findByEstado(EstadoPago.APROBADO).stream()
                .filter(pago -> enRango(pago.getFechaPago().toLocalDate(), inicio, fin)).toList();
    }

    private Map<Long, List<PagoDetalle>> detallesPorPago() {
        return pagoDetalleRepository.findAll().stream()
                .collect(Collectors.groupingBy(detalle -> detalle.getPago().getIdPago()));
    }

    private ReporteSerieDto serieRecuperacion(YearMonth mes, List<Pago> pagos, Map<Long, List<PagoDetalle>> detalles) {
        List<PagoDetalle> detallesDelMes = pagos.stream()
                .flatMap(pago -> detalles.getOrDefault(pago.getIdPago(), List.of()).stream()).toList();
        return new ReporteSerieDto(etiqueta(mes),
                detallesDelMes.stream().map(detalle -> suma(detalle.getMontoCapital(), detalle.getMontoCapitalRegularizado()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add),
                detallesDelMes.stream().map(PagoDetalle::getMontoInteres).map(ReporteRepository::seguro)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private boolean coincideEstado(Credito credito, EstadoCredito estado) {
        return estado == null || credito.getEstado() == estado;
    }

    private boolean enRango(LocalDate fecha, LocalDate inicio, LocalDate fin) {
        return fecha != null && !fecha.isBefore(inicio) && !fecha.isAfter(fin);
    }

    private String nombreCliente(Credito credito) {
        return credito.getCliente().getPersona().getNombres() + " " + credito.getCliente().getPersona().getApellidos();
    }

    private BigDecimal saldoCredito(Credito credito) {
        return suma(credito.getCapitalPendiente(), credito.getInteresPendiente());
    }

    private static BigDecimal suma(BigDecimal primero, BigDecimal segundo) {
        return seguro(primero).add(seguro(segundo));
    }

    private static BigDecimal seguro(BigDecimal monto) {
        return monto == null ? BigDecimal.ZERO : monto;
    }

    private String etiqueta(YearMonth mes) {
        return mes.format(ETIQUETA_MES);
    }
}
