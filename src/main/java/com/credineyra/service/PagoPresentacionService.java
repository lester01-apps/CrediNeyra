package com.credineyra.service;

import com.credineyra.dto.PagoClienteRegistroDto;
import com.credineyra.dto.PagoEfectivoCitaDto;
import com.credineyra.entity.Credito;
import com.credineyra.entity.Pago;
import com.credineyra.entity.Usuario;
import com.credineyra.entity.enums.EstadoCredito;
import com.credineyra.entity.enums.EstadoPago;
import com.credineyra.entity.enums.MetodoPago;
import com.credineyra.exception.OperacionNoPermitidaException;
import com.credineyra.exception.RecursoNoEncontradoException;
import com.credineyra.exception.ReglaNegocioException;
import com.credineyra.repository.CreditoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class PagoPresentacionService {

    private final CreditoRepository creditoRepository;

    private final PagoService pagoService;

    private final PagoComprobanteService comprobanteService;
    private final AuditoriaService auditoriaService;


    public PagoPresentacionService(
            CreditoRepository creditoRepository,
            PagoService pagoService,
            PagoComprobanteService comprobanteService,
            AuditoriaService auditoriaService
    ) {

        this.creditoRepository =
                creditoRepository;

        this.pagoService =
                pagoService;

        this.comprobanteService =
                comprobanteService;
        this.auditoriaService = auditoriaService;
    }

    /**
     * Registra la intención de pago en efectivo. El administrador debe validar
     * el pago únicamente después de recibir el efectivo en la cita.
     */
    @Transactional
    public Pago agendarPagoEfectivo(
            PagoEfectivoCitaDto dto,
            Usuario usuarioCliente
    ) {

        if (dto == null || usuarioCliente == null || usuarioCliente.getIdUsuario() == null) {
            throw new ReglaNegocioException("Los datos de la cita son obligatorios");
        }

        if (dto.getFecha() == null || dto.getHora() == null) {
            throw new ReglaNegocioException("Debe indicar fecha y hora para la cita");
        }

        OffsetDateTime fechaCita = dto.getFecha().atTime(dto.getHora())
                .atZone(ZoneId.of("America/Lima")).toOffsetDateTime();

        if (!fechaCita.isAfter(OffsetDateTime.now(ZoneId.of("America/Lima")))) {
            throw new ReglaNegocioException("La cita debe ser posterior a la hora actual");
        }

        Credito credito = creditoRepository.findById(dto.getIdCredito())
                .orElseThrow(() -> new RecursoNoEncontradoException("Crédito no encontrado"));

        if (usuarioCliente.getPersona() == null
                || credito.getCliente() == null
                || credito.getCliente().getPersona() == null
                || !usuarioCliente.getPersona().getIdPersona()
                .equals(credito.getCliente().getPersona().getIdPersona())) {
            throw new OperacionNoPermitidaException(
                    "No puedes agendar un pago para un crédito de otro cliente");
        }

        if (credito.getEstado() == EstadoCredito.FINALIZADO) {
            throw new OperacionNoPermitidaException("El crédito ya se encuentra finalizado");
        }

        String fechaLegible = fechaCita.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        Pago pago = new Pago();
        pago.setCredito(credito);
        pago.setRegistradoPor(usuarioCliente);
        pago.setMonto(dto.getMonto());
        pago.setMetodoPago(MetodoPago.EFECTIVO);
        pago.setReferencia("CITA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        String observacion = "Cita para pago en efectivo: " + fechaLegible
                + textoCita("Lugar", dto.getLugarPago())
                + textoCita("Dirección", dto.getDireccion())
                + textoCita("Referencia", dto.getReferenciaDireccion())
                + (dto.getObservaciones() == null || dto.getObservaciones().isBlank()
                ? "" : ". " + dto.getObservaciones().trim());
        pago.setObservaciones(observacion.length() > 200
                ? observacion.substring(0, 200) : observacion);
        pago.setEstado(EstadoPago.PENDIENTE_VALIDACION);
        pago.setIdempotencyKey(UUID.randomUUID().toString());

        Pago guardado = pagoService.registrar(pago);
        auditoriaService.registrarEvento(
                usuarioCliente,
                "SOLICITAR_PAGO_EFECTIVO",
                "PAGO",
                String.valueOf(guardado.getIdPago()),
                "Cita solicitada para " + fechaLegible,
                null,
                "{\"metodo\":\"EFECTIVO\",\"fechaCita\":\"" + fechaCita + "\"}",
                null
        );

        return guardado;
    }

    private String textoCita(String etiqueta, String valor) {
        return valor == null || valor.isBlank() ? "" : ". " + etiqueta + ": " + valor.trim();
    }


    /*
     * =========================================================
     * CLIENTE PRESENTA PAGO YAPE
     * =========================================================
     */

    @Transactional
    public Pago presentarYape(
            PagoClienteRegistroDto dto,
            MultipartFile comprobante,
            Usuario usuarioCliente
    ) {

        /*
         * =====================================================
         * VALIDACIONES GENERALES
         * =====================================================
         */

        if (dto == null) {

            throw new ReglaNegocioException(
                    "Los datos del pago son obligatorios"
            );
        }


        if (
                usuarioCliente == null
                        ||
                        usuarioCliente
                                .getIdUsuario() == null
        ) {

            throw new ReglaNegocioException(
                    "Debe identificarse el cliente"
            );
        }


        if (
                usuarioCliente.getPersona() == null
                        ||
                        usuarioCliente
                                .getPersona()
                                .getIdPersona() == null
        ) {

            throw new ReglaNegocioException(
                    "El usuario no tiene una persona asociada"
            );
        }


        /*
         * =====================================================
         * CRÉDITO
         * =====================================================
         */

        Credito credito =
                creditoRepository
                        .findById(
                                dto.getIdCredito()
                        )
                        .orElseThrow(
                                () ->
                                        new RecursoNoEncontradoException(
                                                "Crédito no encontrado"
                                        )
                        );


        /*
         * =====================================================
         * PROPIEDAD DEL RECURSO
         * =====================================================
         *
         * Usuario.Persona
         *
         * debe ser:
         *
         * Crédito.Cliente.Persona
         * =====================================================
         */

        if (
                credito.getCliente() == null
                        ||
                        credito
                                .getCliente()
                                .getPersona() == null
                        ||
                        credito
                                .getCliente()
                                .getPersona()
                                .getIdPersona() == null
                        ||
                        !credito
                                .getCliente()
                                .getPersona()
                                .getIdPersona()
                                .equals(
                                        usuarioCliente
                                                .getPersona()
                                                .getIdPersona()
                                )
        ) {

            throw new OperacionNoPermitidaException(
                    "No puedes registrar pagos para un crédito de otro cliente"
            );
        }


        /*
         * =====================================================
         * CRÉDITO FINALIZADO
         * =====================================================
         */

        if (
                credito.getEstado()
                        == EstadoCredito.FINALIZADO
        ) {

            throw new OperacionNoPermitidaException(
                    "El crédito ya se encuentra finalizado"
            );
        }


        /*
         * =====================================================
         * REFERENCIA YAPE
         * =====================================================
         */

        if (
                dto.getReferencia() == null
                        ||
                        dto.getReferencia()
                                .isBlank()
        ) {

            throw new ReglaNegocioException(
                    "Debes ingresar el número de operación de Yape"
            );
        }


        /*
         * =====================================================
         * COMPROBANTE OBLIGATORIO
         * =====================================================
         */

        if (
                comprobante == null
                        ||
                        comprobante.isEmpty()
        ) {

            throw new ReglaNegocioException(
                    "Debes adjuntar la captura o comprobante del Yape"
            );
        }


        /*
         * =====================================================
         * CREAR PAGO
         * =====================================================
         */

        Pago pago =
                new Pago();


        pago.setCredito(
                credito
        );


        /*
         * El usuario que presenta el pago es
         * el propio cliente autenticado.
         */
        pago.setRegistradoPor(
                usuarioCliente
        );


        pago.setMonto(
                dto.getMonto()
        );


        pago.setMetodoPago(
                MetodoPago.YAPE
        );


        pago.setReferencia(
                dto
                        .getReferencia()
                        .trim()
        );


        pago.setObservaciones(
                limpiar(
                        dto.getObservaciones()
                )
        );


        pago.setEstado(
                EstadoPago.PENDIENTE_VALIDACION
        );


        pago.setIdempotencyKey(
                UUID
                        .randomUUID()
                        .toString()
        );


        /*
         * =====================================================
         * GUARDAR PAGO
         * =====================================================
         *
         * IMPORTANTE:
         *
         * registrar() NO modifica deuda.
         *
         * Solo queda:
         *
         * PENDIENTE_VALIDACION.
         * =====================================================
         */

        Pago guardado =
                pagoService
                        .registrar(
                                pago
                        );


        /*
         * =====================================================
         * SUBIR COMPROBANTE
         * =====================================================
         */

        comprobanteService
                .subirComprobante(
                        guardado.getIdPago(),
                        comprobante
                );


        return guardado;
    }


    /*
     * =========================================================
     * HELPER
     * =========================================================
     */

    private String limpiar(
            String valor
    ) {

        if (
                valor == null
                        ||
                        valor.isBlank()
        ) {

            return null;
        }


        return valor.trim();
    }
}
