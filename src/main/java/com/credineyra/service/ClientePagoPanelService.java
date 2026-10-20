package com.credineyra.service;

import com.credineyra.dto.ClientePagoPanelDto;
import com.credineyra.dto.ClienteCreditoDetalleDto;
import com.credineyra.dto.PagoClienteRegistroDto;
import com.credineyra.entity
        .Credito;
import com.credineyra.entity.Cuota;
import com.credineyra.entity.Pago;
import com.credineyra.entity.Usuario;
import com.credineyra.entity.enums.EstadoDocumento;
import com.credineyra.entity.enums.EstadoCuota;
import com.credineyra.entity.enums.TipoDocumento;
import com.credineyra.exception.OperacionNoPermitidaException;
import com.credineyra.exception.RecursoNoEncontradoException;
import com.credineyra.repository.CreditoRepository;
import com.credineyra.repository.CuotaRepository;
import com.credineyra.repository.DocumentoRepository;
import com.credineyra.repository.PagoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ClientePagoPanelService {

    private final CreditoRepository creditoRepository;

    private final CuotaRepository cuotaRepository;

    private final PagoRepository pagoRepository;

    private final DocumentoRepository documentoRepository;

    public ClientePagoPanelService(
            CreditoRepository creditoRepository,
            CuotaRepository cuotaRepository,
            PagoRepository pagoRepository,
            DocumentoRepository documentoRepository
    ) {

        this.creditoRepository =
                creditoRepository;

        this.cuotaRepository =
                cuotaRepository;

        this.pagoRepository =
                pagoRepository;

        this.documentoRepository =
                documentoRepository;
    }

    @Transactional(readOnly = true)
    public ClientePagoPanelDto obtener(
            Usuario usuario
    ) {

        Long idPersona =
                idPersona(usuario);

        String nombre =
                usuario.getPersona()
                        .getNombres()
                        + " "
                        + usuario.getPersona()
                        .getApellidos();

        List<Credito> creditos =
                creditoRepository
                        .findByCliente_Persona_IdPersonaOrderByFechaSolicitudDesc(
                                idPersona
                        );

        List<ClientePagoPanelDto.CreditoFila> creditosDto =
                creditos.stream()
                        .map(this::mapearCredito)
                        .toList();

        List<Pago> pagos =
                pagoRepository
                        .findByCredito_Cliente_Persona_IdPersonaOrderByFechaPagoDesc(
                                idPersona
                        );

        List<ClientePagoPanelDto.PagoFila> pagosDto =
                pagos.stream()
                        .map(this::mapearPago)
                        .toList();

        return new ClientePagoPanelDto(
                nombre.trim(),
                usuario.getPersona().getDni(),
                creditosDto,
                pagosDto
        );
    }

    @Transactional(readOnly = true)
    public Credito obtenerCreditoPropio(
            Long idCredito,
            Usuario usuario
    ) {

        Credito credito =
                creditoRepository
                        .findById(idCredito)
                        .orElseThrow(
                                () ->
                                        new RecursoNoEncontradoException(
                                                "Crédito no encontrado"
                                        )
                        );

        Long personaUsuario =
                idPersona(usuario);

        Long personaCredito =
                credito.getCliente()
                        .getPersona()
                        .getIdPersona();

        if (
                !personaUsuario.equals(
                        personaCredito
                )
        ) {

            throw new OperacionNoPermitidaException(
                    "No puedes acceder a un crédito de otro cliente"
            );
        }

        return credito;
    }

    /** Devuelve el resumen visible de un crédito, únicamente para su titular. */
    @Transactional(readOnly = true)
    public ClienteCreditoDetalleDto obtenerDetallePropio(
            Long idCredito,
            Usuario usuario
    ) {

        Credito credito = obtenerCreditoPropio(idCredito, usuario);
        var condicion = credito.getCondicion();

        List<Cuota> cuotasEntidad = cuotaRepository
                .findByCredito_IdCreditoOrderByNumeroCuotaAsc(idCredito);

        var cuotaActual = cuotasEntidad.stream()
                .filter(c -> c.getEstado() != EstadoCuota.PAGADA)
                .filter(c -> totalCuota(c).compareTo(BigDecimal.ZERO) > 0)
                .findFirst();

        BigDecimal montoExigible = cuotaActual
                .map(this::totalCuota)
                .orElse(BigDecimal.ZERO.setScale(2));

        List<ClienteCreditoDetalleDto.CuotaFila> cuotas = cuotasEntidad.stream()
                .map(c -> new ClienteCreditoDetalleDto.CuotaFila(
                        c.getNumeroCuota(),
                        c.getFechaVencimiento(),
                        nz(c.getCapitalPendiente()),
                        nz(c.getInteresPendiente()),
                        totalCuota(c),
                        c.getEstado() == null ? "-" : c.getEstado().name()
                ))
                .toList();

        return new ClienteCreditoDetalleDto(
                credito.getIdCredito(),
                credito.getCodigoCredito(),
                credito.getEstado() == null ? "-" : credito.getEstado().name(),
                nz(credito.getCapitalOriginal()),
                nz(credito.getCapitalPendiente()),
                nz(credito.getInteresPendiente()),
                nz(credito.getCapitalPendiente()).add(nz(credito.getInteresPendiente())),
                montoExigible,
                cuotaActual.map(Cuota::getNumeroCuota).orElse(null),
                cuotaActual.map(Cuota::getFechaVencimiento).orElse(null),
                condicion == null ? null : condicion.getFechaInicio(),
                condicion == null ? null : condicion.getPlazoMeses(),
                condicion == null ? null : condicion.getNumeroCuotas(),
                condicion == null ? BigDecimal.ZERO.setScale(2) : nz(condicion.getTasaInteres()),
                condicion == null || condicion.getFrecuenciaPago() == null
                        ? "-"
                        : condicion.getFrecuenciaPago().name(),
                cuotas
        );
    }

    @Transactional(readOnly = true)
    public PagoClienteRegistroDto crearRegistroInicial(
            Long idCredito,
            Usuario usuario
    ) {

        Credito credito =
                obtenerCreditoPropio(
                        idCredito,
                        usuario
                );

        PagoClienteRegistroDto dto =
                new PagoClienteRegistroDto();

        dto.setIdCredito(
                credito.getIdCredito()
        );
        dto.setTipoPago("CUOTA_COMPLETA");

        cuotaExigible(
                credito.getIdCredito()
        ).ifPresent(
                cuota -> {

                    BigDecimal total =
                            nz(cuota.getCapitalPendiente())
                                    .add(
                                            nz(
                                                    cuota.getInteresPendiente()
                                            )
                                    )
                                    .add(
                                            nz(
                                                    cuota.getCapitalRegularizadoPendiente()
                                            )
                                    );

                    dto.setMonto(
                            total
                    );
                }
        );

        return dto;
    }

    private ClientePagoPanelDto.CreditoFila mapearCredito(
            Credito credito
    ) {

        var cuota =
                cuotaExigible(
                        credito.getIdCredito()
                );

        BigDecimal montoExigible =
                cuota.map(
                                c ->
                                        nz(
                                                c.getCapitalPendiente()
                                        )
                                                .add(
                                                        nz(
                                                                c.getInteresPendiente()
                                                        )
                                                )
                                                .add(
                                                        nz(
                                                                c.getCapitalRegularizadoPendiente()
                                                        )
                                                )
                        )
                        .orElse(
                                BigDecimal.ZERO.setScale(2)
                        );

        return new ClientePagoPanelDto.CreditoFila(

                credito.getIdCredito(),

                credito.getCodigoCredito(),

                credito.getEstado() == null
                        ? "-"
                        : credito.getEstado().name(),

                nz(
                        credito.getCapitalOriginal()
                ),

                nz(
                        credito.getCapitalPendiente()
                ),

                nz(
                        credito.getInteresPendiente()
                ),

                nz(
                        credito.getCapitalPendiente()
                ).add(
                        nz(
                                credito.getInteresPendiente()
                        )
                ),

                cuota.map(
                                Cuota::getNumeroCuota
                        )
                        .orElse(null),

                cuota.map(
                                Cuota::getFechaVencimiento
                        )
                        .orElse(null),

                montoExigible
        );
    }

    private ClientePagoPanelDto.PagoFila mapearPago(
            Pago pago
    ) {

        Long comprobante =
                documentoRepository
                        .findFirstByPago_IdPagoAndTipoDocumentoAndEstadoOrderByFechaSubidaDesc(
                                pago.getIdPago(),
                                TipoDocumento.COMPROBANTE_PAGO,
                                EstadoDocumento.ACTIVO
                        )
                        .map(d -> d.getIdDocumento())
                        .orElse(null);

        Long constancia =
                documentoRepository
                        .findFirstByPago_IdPagoAndTipoDocumentoAndEstadoOrderByFechaSubidaDesc(
                                pago.getIdPago(),
                                TipoDocumento.CONSTANCIA_PAGO,
                                EstadoDocumento.ACTIVO
                        )
                        .map(d -> d.getIdDocumento())
                        .orElse(null);

        return new ClientePagoPanelDto.PagoFila(

                pago.getIdPago(),

                String.format(
                        "PAG-%06d",
                        pago.getIdPago()
                ),

                pago.getCredito()
                        .getCodigoCredito(),

                pago.getMonto(),

                pago.getReferencia(),

                pago.getEstado()
                        .name(),

                pago.getFechaPago(),

                comprobante,

                constancia
        );
    }

    private java.util.Optional<Cuota> cuotaExigible(
            Long idCredito
    ) {

        LocalDate hoy =
                LocalDate.now();

        return cuotaRepository
                .findByCredito_IdCreditoOrderByFechaVencimientoAsc(
                        idCredito
                )
                .stream()
                .filter(
                        cuota ->
                                cuota.getEstado()
                                        != EstadoCuota.PAGADA
                )
                .filter(
                        cuota ->
                                !cuota.getFechaVencimiento()
                                        .isAfter(hoy)
                )
                .filter(
                        cuota ->
                                nz(
                                        cuota.getCapitalPendiente()
                                )
                                        .add(
                                                nz(
                                                        cuota.getInteresPendiente()
                                                )
                                        )
                                        .add(
                                                nz(
                                                        cuota.getCapitalRegularizadoPendiente()
                                                )
                                        )
                                        .compareTo(
                                                BigDecimal.ZERO
                                        ) > 0
                )
                .findFirst();
    }

    private BigDecimal totalCuota(Cuota cuota) {
        return nz(cuota.getCapitalPendiente())
                .add(nz(cuota.getInteresPendiente()))
                .add(nz(cuota.getCapitalRegularizadoPendiente()));
    }

    private Long idPersona(
            Usuario usuario
    ) {

        if (
                usuario == null
                        ||
                        usuario.getPersona() == null
                        ||
                        usuario.getPersona()
                                .getIdPersona() == null
        ) {

            throw new OperacionNoPermitidaException(
                    "El usuario no tiene una persona asociada"
            );
        }

        return usuario.getPersona()
                .getIdPersona();
    }

    private BigDecimal nz(
            BigDecimal valor
    ) {

        return valor == null
                ? BigDecimal.ZERO.setScale(2)
                : valor;
    }
}
