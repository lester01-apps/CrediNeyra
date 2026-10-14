package com.credineyra.service;

import com.credineyra.dto.CreditoRegistroDto;
import com.credineyra.dto.CreditoSimulacionDto;
import com.credineyra.dto.CuotaSimuladaDto;
import com.credineyra.entity.Cliente;
import com.credineyra.entity.CondicionCredito;
import com.credineyra.entity.Credito;
import com.credineyra.entity.Cuota;
import com.credineyra.entity.Garantia;
import com.credineyra.entity.Tasa;
import com.credineyra.entity.Usuario;
import com.credineyra.entity.enums.EstadoCliente;
import com.credineyra.entity.enums.EstadoCredito;
import com.credineyra.entity.enums.EstadoCuota;
import com.credineyra.entity.enums.EstadoGarantia;
import com.credineyra.entity.enums.EstadoTasa;
import com.credineyra.exception.DatoDuplicadoException;
import com.credineyra.exception.OperacionNoPermitidaException;
import com.credineyra.exception.RecursoNoEncontradoException;
import com.credineyra.exception.ReglaNegocioException;
import com.credineyra.repository.ClienteRepository;
import com.credineyra.repository.CondicionCreditoRepository;
import com.credineyra.repository.CreditoRepository;
import com.credineyra.repository.CuotaRepository;
import com.credineyra.repository.GarantiaRepository;
import com.credineyra.repository.TasaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class CreditoService {

    /*
     * =========================================================
     * REGLAS GENERALES
     * =========================================================
     */

    private static final BigDecimal LIMITE_GARANTIA =
            new BigDecimal("500.00");


    /*
     * =========================================================
     * REPOSITORIOS
     * =========================================================
     */

    private final CreditoRepository creditoRepository;

    private final ClienteRepository clienteRepository;

    private final CondicionCreditoRepository condicionRepository;

    private final CuotaRepository cuotaRepository;

    private final TasaRepository tasaRepository;

    private final GarantiaRepository garantiaRepository;


    /*
     * =========================================================
     * SERVICIOS
     * =========================================================
     */

    private final CreditoCalculoService creditoCalculoService;

    private final AuditoriaService auditoriaService;


    /*
     * =========================================================
     * CONSTRUCTOR
     * =========================================================
     */

    public CreditoService(
            CreditoRepository creditoRepository,
            ClienteRepository clienteRepository,
            CondicionCreditoRepository condicionRepository,
            CuotaRepository cuotaRepository,
            TasaRepository tasaRepository,
            GarantiaRepository garantiaRepository,
            CreditoCalculoService creditoCalculoService,
            AuditoriaService auditoriaService
    ) {

        this.creditoRepository =
                creditoRepository;

        this.clienteRepository =
                clienteRepository;

        this.condicionRepository =
                condicionRepository;

        this.cuotaRepository =
                cuotaRepository;

        this.tasaRepository =
                tasaRepository;

        this.garantiaRepository =
                garantiaRepository;

        this.creditoCalculoService =
                creditoCalculoService;

        this.auditoriaService =
                auditoriaService;
    }


    /*
     * =========================================================
     * SIMULACIÓN DEL CRÉDITO
     * =========================================================
     */

    @Transactional(readOnly = true)
    public CreditoSimulacionDto simular(
            CreditoRegistroDto dto
    ) {

        return creditoCalculoService
                .simular(dto);
    }


    /*
     * =========================================================
     * REGISTRAR CRÉDITO COMPLETO
     * =========================================================
     */

    public Credito registrarCompleto(
            CreditoRegistroDto dto,
            Usuario usuarioCreador
    ) {

        validarDto(dto);


        /*
         * La simulación oficial del backend es la misma
         * que luego utilizaremos para guardar las cuotas.
         */
        CreditoSimulacionDto simulacion =
                creditoCalculoService
                        .simular(dto);


        /*
         * =====================================================
         * VALIDAR CÓDIGO
         * =====================================================
         */

        if (
                dto.getCodigoCredito() == null
                        ||
                        dto.getCodigoCredito().isBlank()
        ) {

            throw new ReglaNegocioException(
                    "El código del crédito es obligatorio"
            );
        }


        String codigoCredito =
                dto.getCodigoCredito()
                        .trim();


        if (
                creditoRepository
                        .existsByCodigoCredito(
                                codigoCredito
                        )
        ) {

            throw new DatoDuplicadoException(
                    "El código de crédito ya está registrado"
            );
        }


        /*
         * =====================================================
         * CLIENTE
         * =====================================================
         */

        Cliente cliente =
                clienteRepository
                        .findById(
                                dto.getIdCliente()
                        )
                        .orElseThrow(
                                () ->
                                        new RecursoNoEncontradoException(
                                                "Cliente no encontrado"
                                        )
                        );


        if (
                cliente.getEstado()
                        != EstadoCliente.ACTIVO
        ) {

            throw new OperacionNoPermitidaException(
                    "El cliente debe estar activo para recibir un crédito"
            );
        }


        BigDecimal capital =
                simulacion.getCapital();


        /*
         * =====================================================
         * GARANTÍA
         * =====================================================
         */

        validarGarantia(
                dto,
                capital
        );


        /*
         * =====================================================
         * CONDICIONES CONGELADAS
         * =====================================================
         */

        CondicionCredito condicion =
                crearCondicion(
                        dto,
                        simulacion
                );


        condicion =
                condicionRepository
                        .save(
                                condicion
                        );


        /*
         * =====================================================
         * CRÉDITO
         * =====================================================
         */

        Credito credito =
                new Credito();


        credito.setCliente(
                cliente
        );


        credito.setCondicion(
                condicion
        );


        credito.setCodigoCredito(
                codigoCredito
        );


        credito.setCapitalOriginal(
                capital
        );


        credito.setCapitalPagado(
                cero()
        );


        credito.setCapitalPendiente(
                capital
        );


        credito.setInteresGenerado(
                simulacion.getInteresTotal()
        );


        credito.setInteresPagado(
                cero()
        );


        credito.setInteresPendiente(
                simulacion.getInteresTotal()
        );


        credito.setEstado(
                EstadoCredito.ACTIVO
        );


        credito.setObservaciones(
                limpiar(
                        dto.getObservaciones()
                )
        );


        Credito creditoGuardado =
                creditoRepository
                        .save(
                                credito
                        );


        /*
         * =====================================================
         * CUOTAS
         * =====================================================
         */

        guardarCuotas(
                creditoGuardado,
                simulacion
        );


        /*
         * =====================================================
         * GARANTÍA
         * =====================================================
         */

        guardarGarantiaSiCorresponde(
                creditoGuardado,
                dto
        );


        /*
         * =====================================================
         * AUDITORÍA
         * =====================================================
         */

        if (usuarioCreador != null) {

            auditoriaService
                    .registrarEvento(
                            usuarioCreador,

                            "CREAR_CREDITO",

                            "CREDITO",

                            String.valueOf(
                                    creditoGuardado
                                            .getIdCredito()
                            ),

                            "Registro de crédito",

                            null,

                            "{"
                                    + "\"codigo\":\""
                                    + creditoGuardado
                                    .getCodigoCredito()
                                    + "\","
                                    + "\"capital\":\""
                                    + creditoGuardado
                                    .getCapitalOriginal()
                                    + "\","
                                    + "\"interes\":\""
                                    + creditoGuardado
                                    .getInteresGenerado()
                                    + "\""
                                    + "}",

                            null
                    );
        }


        return creditoGuardado;
    }


    /*
     * =========================================================
     * REGISTRO SIMPLE
     * =========================================================
     */

    public Credito registrar(
            Credito credito
    ) {

        if (credito == null) {

            throw new ReglaNegocioException(
                    "Los datos del crédito son obligatorios"
            );
        }


        if (credito.getCliente() == null) {

            throw new ReglaNegocioException(
                    "El crédito debe tener un cliente"
            );
        }


        if (credito.getCondicion() == null) {

            throw new ReglaNegocioException(
                    "El crédito debe tener una condición"
            );
        }


        if (
                credito.getCodigoCredito() == null
                        ||
                        credito.getCodigoCredito()
                                .isBlank()
        ) {

            throw new ReglaNegocioException(
                    "El código del crédito es obligatorio"
            );
        }


        String codigo =
                credito.getCodigoCredito()
                        .trim();


        if (
                creditoRepository
                        .existsByCodigoCredito(
                                codigo
                        )
        ) {

            throw new DatoDuplicadoException(
                    "El código de crédito ya existe"
            );
        }


        credito.setCodigoCredito(
                codigo
        );


        if (credito.getEstado() == null) {

            credito.setEstado(
                    EstadoCredito.ACTIVO
            );
        }


        return creditoRepository
                .save(
                        credito
                );
    }


    /*
     * =========================================================
     * CREAR CONDICIÓN DEL CRÉDITO
     * =========================================================
     */

    private CondicionCredito crearCondicion(
            CreditoRegistroDto dto,
            CreditoSimulacionDto simulacion
    ) {

        CondicionCredito condicion =
                new CondicionCredito();


        /*
         * TASA DE ORIGEN
         */
        if (dto.getIdTasaOrigen() != null) {

            Tasa tasa =
                    tasaRepository
                            .findById(
                                    dto.getIdTasaOrigen()
                            )
                            .orElseThrow(
                                    () ->
                                            new RecursoNoEncontradoException(
                                                    "Tasa no encontrada"
                                            )
                            );


            if (
                    tasa.getEstado()
                            != EstadoTasa.ACTIVA
            ) {

                throw new OperacionNoPermitidaException(
                        "La tasa seleccionada no está activa"
                );
            }


            condicion.setTasaOrigen(
                    tasa
            );
        }


        /*
         * CONDICIONES FINANCIERAS
         */

        condicion.setTipoCalculo(
                dto.getTipoCalculo()
        );


        condicion.setTasaInteres(
                dto.getTasaInteres()
        );


        condicion.setPeriodicidadTasa(
                dto.getPeriodicidadTasa()
        );


        condicion.setFrecuenciaPago(
                dto.getFrecuenciaPago()
        );


        condicion.setPlazoMeses(
                dto.getPlazoMeses()
        );


        condicion.setNumeroCuotas(
                simulacion.getNumeroCuotas()
        );


        condicion.setFechaInicio(
                dto.getFechaInicio()
        );


        condicion.setFechaPrimerPago(
                dto.getFechaPrimerPago()
        );


        condicion.setPermitePagoSoloInteres(
                Boolean.TRUE.equals(
                        dto.getPermitePagoSoloInteres()
                )
        );


        return condicion;
    }


    /*
     * =========================================================
     * GUARDAR CUOTAS
     * =========================================================
     */

    private void guardarCuotas(
            Credito credito,
            CreditoSimulacionDto simulacion
    ) {

        List<Cuota> cuotas =
                new ArrayList<>();


        for (
                CuotaSimuladaDto fila :
                simulacion.getCronograma()
        ) {

            Cuota cuota =
                    new Cuota();


            cuota.setCredito(
                    credito
            );


            cuota.setNumeroCuota(
                    fila.getNumero()
            );


            cuota.setFechaVencimiento(
                    fila.getFechaVencimiento()
            );


            cuota.setCapitalOriginal(
                    fila.getCapital()
            );


            cuota.setInteresOriginal(
                    fila.getInteres()
            );


            cuota.setCapitalPagado(
                    cero()
            );


            cuota.setInteresPagado(
                    cero()
            );


            cuota.setCapitalPendiente(
                    fila.getCapital()
            );


            cuota.setInteresPendiente(
                    fila.getInteres()
            );


            cuota.setCapitalRegularizadoAsignado(
                    cero()
            );


            cuota.setCapitalRegularizadoPagado(
                    cero()
            );


            cuota.setCapitalRegularizadoPendiente(
                    cero()
            );


            cuota.setEstado(
                    EstadoCuota.PENDIENTE
            );


            cuotas.add(
                    cuota
            );
        }


        cuotaRepository
                .saveAll(
                        cuotas
                );
    }


    /*
     * =========================================================
     * GUARDAR GARANTÍA
     * =========================================================
     */

    private Garantia guardarGarantiaSiCorresponde(
            Credito credito,
            CreditoRegistroDto dto
    ) {

        /*
         * Si no se indicó garantía y el crédito es
         * menor de S/500, seguimos normalmente.
         */
        if (
                dto.getTipoGarantia() == null
                        ||
                        dto.getTipoGarantia()
                                .isBlank()
        ) {

            return null;
        }


        Garantia garantia =
                new Garantia();


        garantia.setCredito(
                credito
        );


        garantia.setTipoGarantia(
                dto.getTipoGarantia()
                        .trim()
        );


        garantia.setDescripcion(
                limpiar(
                        dto.getDescripcionGarantia()
                )
        );


        garantia.setValorEstimado(
                creditoCalculoService
                        .dinero(
                                dto.getValorEstimadoGarantia()
                        )
        );


        garantia.setEstado(
                EstadoGarantia.ACTIVA
        );


        garantia.setObservaciones(
                limpiar(
                        dto.getObservacionesGarantia()
                )
        );


        return garantiaRepository
                .save(
                        garantia
                );
    }


    /*
     * =========================================================
     * VALIDACIONES DTO
     * =========================================================
     */

    private void validarDto(
            CreditoRegistroDto dto
    ) {

        if (dto == null) {

            throw new ReglaNegocioException(
                    "Los datos del crédito son obligatorios"
            );
        }


        /*
         * CLIENTE
         */
        if (dto.getIdCliente() == null) {

            throw new ReglaNegocioException(
                    "Debe seleccionar un cliente"
            );
        }


        /*
         * CAPITAL
         */
        if (
                dto.getCapitalOriginal() == null
                        ||
                        dto.getCapitalOriginal()
                                .compareTo(
                                        BigDecimal.ZERO
                                ) <= 0
        ) {

            throw new ReglaNegocioException(
                    "El capital debe ser mayor que cero"
            );
        }


        /*
         * TASA
         */
        if (
                dto.getTasaInteres() == null
                        ||
                        dto.getTasaInteres()
                                .compareTo(
                                        BigDecimal.ZERO
                                ) < 0
        ) {

            throw new ReglaNegocioException(
                    "La tasa no puede ser negativa"
            );
        }


        /*
         * TIPO / PERIODICIDAD / FRECUENCIA
         */
        if (
                dto.getTipoCalculo() == null
                        ||
                        dto.getPeriodicidadTasa() == null
                        ||
                        dto.getFrecuenciaPago() == null
        ) {

            throw new ReglaNegocioException(
                    "Las condiciones financieras son obligatorias"
            );
        }


        /*
         * NO PERMITIMOS INTERÉS DIARIO
         */
        if (
                dto.getPeriodicidadTasa()
                        .name()
                        .equals("DIARIA")
        ) {

            throw new ReglaNegocioException(
                    "La periodicidad del interés solo puede ser semanal, quincenal o mensual"
            );
        }


        /*
         * PLAZO
         */
        if (
                dto.getPlazoMeses() == null
                        ||
                        dto.getPlazoMeses() <= 0
        ) {

            throw new ReglaNegocioException(
                    "El plazo debe ser mayor que cero"
            );
        }


        /*
         * FECHAS
         */
        if (
                dto.getFechaInicio() == null
                        ||
                        dto.getFechaPrimerPago() == null
        ) {

            throw new ReglaNegocioException(
                    "Las fechas son obligatorias"
            );
        }


        if (
                dto.getFechaPrimerPago()
                        .isBefore(
                                dto.getFechaInicio()
                        )
        ) {

            throw new ReglaNegocioException(
                    "El primer pago no puede ser anterior a la fecha de desembolso"
            );
        }
    }


    /*
     * =========================================================
     * VALIDACIÓN DE GARANTÍA
     * =========================================================
     */

    private void validarGarantia(
            CreditoRegistroDto dto,
            BigDecimal capital
    ) {

        /*
         * Menor de S/500:
         * garantía opcional.
         */
        if (
                capital.compareTo(
                        LIMITE_GARANTIA
                ) < 0
        ) {

            return;
        }


        /*
         * Desde S/500:
         * garantía obligatoria.
         */
        if (
                dto.getTipoGarantia() == null
                        ||
                        dto.getTipoGarantia()
                                .isBlank()
        ) {

            throw new ReglaNegocioException(
                    "Los créditos desde S/500 requieren garantía"
            );
        }


        /*
         * Valor estimado.
         */
        if (
                dto.getValorEstimadoGarantia() == null
                        ||
                        dto.getValorEstimadoGarantia()
                                .compareTo(
                                        BigDecimal.ZERO
                                ) < 0
        ) {

            throw new ReglaNegocioException(
                    "Debe indicar el valor estimado de la garantía"
            );
        }
    }


    /*
     * =========================================================
     * ACTUALIZAR OBSERVACIONES
     * =========================================================
     */

    public Credito actualizarObservaciones(
            Long idCredito,
            String observaciones
    ) {

        Credito credito =
                buscarPorId(
                        idCredito
                );


        if (
                credito.getEstado()
                        == EstadoCredito.FINALIZADO
        ) {

            throw new OperacionNoPermitidaException(
                    "No se puede modificar un crédito finalizado"
            );
        }


        credito.setObservaciones(
                limpiar(
                        observaciones
                )
        );


        return creditoRepository
                .save(
                        credito
                );
    }


    /*
     * =========================================================
     * BUSCAR POR ID
     * =========================================================
     */

    @Transactional(readOnly = true)
    public Credito buscarPorId(
            Long idCredito
    ) {

        Credito credito =
                creditoRepository
                        .findById(
                                idCredito
                        )
                        .orElseThrow(
                                () ->
                                        new RecursoNoEncontradoException(
                                                "Crédito no encontrado"
                                        )
                        );


        /*
         * Dejamos preparado Cliente -> Persona
         * si luego una vista Thymeleaf lo necesita.
         */
        inicializarClienteParaVista(
                credito
        );


        return credito;
    }


    /*
     * =========================================================
     * BUSCAR POR CÓDIGO
     * =========================================================
     */

    @Transactional(readOnly = true)
    public Credito buscarPorCodigo(
            String codigo
    ) {

        Credito credito =
                creditoRepository
                        .findByCodigoCredito(
                                codigo
                        )
                        .orElseThrow(
                                () ->
                                        new RecursoNoEncontradoException(
                                                "Crédito no encontrado"
                                        )
                        );


        inicializarClienteParaVista(
                credito
        );


        return credito;
    }


    /*
     * =========================================================
     * LISTAR TODOS
     * =========================================================
     */

    @Transactional(readOnly = true)
    public List<Credito> listarTodos() {

        List<Credito> creditos =
                creditoRepository
                        .findAll();


        /*
         * Evita el mismo problema LAZY en otras
         * vistas que muestren Cliente -> Persona.
         */
        for (Credito credito : creditos) {

            inicializarClienteParaVista(
                    credito
            );
        }


        return creditos;
    }


    /*
     * =========================================================
     * LISTAR POR CLIENTE
     * =========================================================
     */

    @Transactional(readOnly = true)
    public List<Credito> listarPorCliente(
            Long idCliente
    ) {

        List<Credito> creditos =
                creditoRepository
                        .findByCliente_IdClienteOrderByFechaSolicitudDesc(
                                idCliente
                        );


        for (Credito credito : creditos) {

            inicializarClienteParaVista(
                    credito
            );
        }


        return creditos;
    }


    /*
     * =========================================================
     * DASHBOARD
     * CONTAR CRÉDITOS ACTIVOS
     * =========================================================
     */

    @Transactional(readOnly = true)
    public long contarCreditosActivos() {

        return creditoRepository
                .findAll()
                .stream()
                .filter(
                        credito ->
                                credito.getEstado()
                                        != null
                                        &&
                                        credito.getEstado()
                                                != EstadoCredito.FINALIZADO
                )
                .count();
    }


    /*
     * =========================================================
     * DASHBOARD
     * CONTAR CRÉDITOS EN MORA
     * =========================================================
     */

    @Transactional(readOnly = true)
    public long contarCreditosEnMora() {

        return creditoRepository
                .findAll()
                .stream()
                .filter(
                        credito -> {

                            if (
                                    credito.getEstado()
                                            == null
                            ) {

                                return false;
                            }


                            String estado =
                                    credito.getEstado()
                                            .name();


                            return estado.equals(
                                    "EN_MORA"
                            )
                                    ||
                                    estado.equals(
                                            "ATRASO_PROLONGADO"
                                    );
                        }
                )
                .count();
    }


    /*
     * =========================================================
     * DASHBOARD
     * ÚLTIMOS CINCO CRÉDITOS
     * =========================================================
     */

    @Transactional(readOnly = true)
    public List<Credito> listarUltimosCinco() {

        List<Credito> creditos =
                creditoRepository
                        .findAll()
                        .stream()
                        .sorted(
                                (creditoA, creditoB) -> {

                                    /*
                                     * Ambos sin fecha.
                                     */
                                    if (
                                            creditoA.getFechaSolicitud()
                                                    == null
                                                    &&
                                                    creditoB.getFechaSolicitud()
                                                            == null
                                    ) {

                                        return 0;
                                    }


                                    /*
                                     * A no tiene fecha.
                                     */
                                    if (
                                            creditoA.getFechaSolicitud()
                                                    == null
                                    ) {

                                        return 1;
                                    }


                                    /*
                                     * B no tiene fecha.
                                     */
                                    if (
                                            creditoB.getFechaSolicitud()
                                                    == null
                                    ) {

                                        return -1;
                                    }


                                    /*
                                     * Más reciente primero.
                                     */
                                    return creditoB
                                            .getFechaSolicitud()
                                            .compareTo(
                                                    creditoA
                                                            .getFechaSolicitud()
                                            );
                                }
                        )
                        .limit(5)
                        .toList();


        /*
         * =====================================================
         * IMPORTANTE
         * =====================================================
         *
         * dashboard.html usa:
         *
         * credito.cliente.persona.nombres
         * credito.cliente.persona.apellidos
         *
         * open-in-view está desactivado.
         *
         * Por eso debemos cargar estas relaciones
         * DENTRO de la transacción.
         */
        for (Credito credito : creditos) {

            inicializarClienteParaVista(
                    credito
            );
        }


        return creditos;
    }


    /*
     * =========================================================
     * PREPARAR CLIENTE PARA THYMELEAF
     * =========================================================
     */

    private void inicializarClienteParaVista(
            Credito credito
    ) {

        if (credito == null) {

            return;
        }


        Cliente cliente =
                credito.getCliente();


        if (cliente == null) {

            return;
        }


        /*
         * Fuerza la carga del proxy Cliente.
         */
        cliente.getCodigoCliente();


        /*
         * Fuerza la carga de Persona.
         */
        if (cliente.getPersona() == null) {

            return;
        }


        cliente.getPersona()
                .getNombres();


        cliente.getPersona()
                .getApellidos();


        cliente.getPersona()
                .getDni();
    }


    /*
     * =========================================================
     * UTILIDAD
     * CERO MONETARIO
     * =========================================================
     */

    private BigDecimal cero() {

        return BigDecimal.ZERO
                .setScale(2);
    }


    /*
     * =========================================================
     * UTILIDAD
     * LIMPIAR TEXTO
     * =========================================================
     */

    private String limpiar(
            String valor
    ) {

        if (valor == null) {

            return null;
        }


        String limpio =
                valor.trim();


        return limpio.isBlank()
                ? null
                : limpio;
    }
}