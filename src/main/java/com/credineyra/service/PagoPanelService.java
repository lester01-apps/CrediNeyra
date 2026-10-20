package com.credineyra.service;

import com.credineyra.dto.PagoPanelDto;
import com.credineyra.entity.Pago;
import com.credineyra.entity.enums.EstadoDocumento;
import com.credineyra.entity.enums.EstadoPago;
import com.credineyra.entity.enums.TipoDocumento;
import com.credineyra.repository.DocumentoRepository;
import com.credineyra.repository.PagoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PagoPanelService {

    private final PagoRepository pagoRepository;
    private final DocumentoRepository documentoRepository;

    public PagoPanelService(
            PagoRepository pagoRepository,
            DocumentoRepository documentoRepository
    ) {
        this.pagoRepository = pagoRepository;
        this.documentoRepository = documentoRepository;
    }

    @Transactional(readOnly = true)
    public PagoPanelDto obtenerPanel() {

        List<Pago> pagos =
                pagoRepository
                        .findAllByOrderByFechaPagoDesc();

        long pendientes =
                pagos.stream()
                        .filter(p ->
                                p.getEstado()
                                        == EstadoPago.PENDIENTE_VALIDACION
                        )
                        .count();

        long aprobados =
                pagos.stream()
                        .filter(p ->
                                p.getEstado()
                                        == EstadoPago.APROBADO
                        )
                        .count();

        long rechazados =
                pagos.stream()
                        .filter(p ->
                                p.getEstado()
                                        == EstadoPago.RECHAZADO
                        )
                        .count();

        BigDecimal totalAprobado =
                pagos.stream()
                        .filter(p ->
                                p.getEstado()
                                        == EstadoPago.APROBADO
                        )
                        .map(Pago::getMonto)
                        .filter(monto -> monto != null)
                        .reduce(
                                BigDecimal.ZERO.setScale(2),
                                BigDecimal::add
                        );

        List<PagoPanelDto.Fila> filas =
                pagos.stream()
                        .map(this::mapear)
                        .toList();

        return new PagoPanelDto(
                pagos.size(),
                pendientes,
                aprobados,
                rechazados,
                totalAprobado,
                filas
        );
    }

    private PagoPanelDto.Fila mapear(
            Pago pago
    ) {

        String clienteNombre =
                "Cliente";

        if (
                pago.getCredito() != null
                        &&
                        pago.getCredito().getCliente() != null
                        &&
                        pago.getCredito()
                                .getCliente()
                                .getPersona() != null
        ) {

            var persona =
                    pago.getCredito()
                            .getCliente()
                            .getPersona();

            clienteNombre =
                    (
                            valor(persona.getNombres())
                                    + " "
                                    + valor(persona.getApellidos())
                    ).trim();
        }

        boolean comprobante =
                documentoRepository
                        .existsByPago_IdPagoAndTipoDocumentoAndEstado(
                                pago.getIdPago(),
                                TipoDocumento.COMPROBANTE_PAGO,
                                EstadoDocumento.ACTIVO
                        );

        return new PagoPanelDto.Fila(

                pago.getIdPago(),

                String.format(
                        "PAG-%06d",
                        pago.getIdPago()
                ),

                clienteNombre,

                pago.getCredito() == null
                        ? "-"
                        : pago.getCredito()
                        .getCodigoCredito(),

                pago.getMonto(),

                pago.getMetodoPago() == null
                        ? "-"
                        : pago.getMetodoPago().name(),

                pago.getReferencia() == null
                        ? "-"
                        : pago.getReferencia(),

                pago.getEstado() == null
                        ? "-"
                        : pago.getEstado().name(),

                pago.getFechaPago(),

                comprobante
        );
    }

    private String valor(
            String texto
    ) {
        return texto == null
                ? ""
                : texto;
    }
}