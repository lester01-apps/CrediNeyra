package com.credineyra.service;

import com.credineyra.dto.PagoAprobacionDto;
import com.credineyra.entity.Documento;
import com.credineyra.entity.Pago;
import com.credineyra.entity.Usuario;
import com.credineyra.repository.PagoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PagoValidacionService {

    private final PagoService pagoService;

    private final PagoRepository pagoRepository;

    private final ConstanciaPagoService constanciaPagoService;

    public PagoValidacionService(
            PagoService pagoService,
            PagoRepository pagoRepository,
            ConstanciaPagoService constanciaPagoService
    ) {

        this.pagoService =
                pagoService;

        this.pagoRepository =
                pagoRepository;

        this.constanciaPagoService =
                constanciaPagoService;
    }

    @Transactional
    public ResultadoAprobacion aprobar(
            Long idPago,
            Usuario usuario,
            PagoAprobacionDto dto
    ) {

        Pago pago =
                pagoService
                        .aprobar(
                                idPago,
                                usuario,
                                dto
                        );

        Documento constancia =
                null;

        String advertencia =
                null;

        try {

            constancia =
                    constanciaPagoService
                            .generarSiFalta(
                                    idPago
                            );

        } catch (RuntimeException ex) {

            /*
             * El pago financiero ya quedó aprobado.
             *
             * No debe aplicarse nuevamente.
             * La constancia puede regenerarse.
             */
            advertencia =
                    "El pago fue aprobado, pero la constancia PDF "
                            + "no pudo generarse: "
                            + ex.getMessage();
        }

        return new ResultadoAprobacion(
                pago,
                constancia,
                advertencia
        );
    }

    @Transactional
    public Pago rechazar(
            Long idPago,
            Usuario usuario,
            String motivo
    ) {

        Pago pago =
                pagoService
                        .rechazar(
                                idPago,
                                usuario,
                                motivo
                        );

        String anterior =
                pago.getObservaciones();

        String nuevo =
                "RECHAZADO: "
                        + motivo.trim();

        if (
                anterior != null
                        &&
                        !anterior.isBlank()
        ) {

            nuevo =
                    anterior
                            + " | "
                            + nuevo;
        }

        if (nuevo.length() > 200) {

            nuevo =
                    nuevo.substring(
                            0,
                            200
                    );
        }

        pago.setObservaciones(
                nuevo
        );

        return pagoRepository
                .save(pago);
    }

    public record ResultadoAprobacion(

            Pago pago,

            Documento constancia,

            String advertencia
    ) {
    }
}