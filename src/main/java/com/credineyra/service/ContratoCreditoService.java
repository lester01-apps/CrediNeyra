package com.credineyra.service;

import com.credineyra.dto.ContratoCreditoDto;
import com.credineyra.entity.*;
import com.credineyra.exception.RecursoNoEncontradoException;
import com.credineyra.repository.CreditoRepository;
import com.credineyra.repository.CuotaRepository;
import com.credineyra.repository.GarantiaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ContratoCreditoService {

    private final CreditoRepository creditoRepository;
    private final CuotaRepository cuotaRepository;
    private final GarantiaRepository garantiaRepository;

    @Value("${credineyra.prestamista.nombre:}")
    private String prestamistaNombre;

    @Value("${credineyra.prestamista.documento:}")
    private String prestamistaDocumento;

    @Value("${credineyra.prestamista.domicilio:}")
    private String prestamistaDomicilio;

    public ContratoCreditoService(
            CreditoRepository creditoRepository,
            CuotaRepository cuotaRepository,
            GarantiaRepository garantiaRepository
    ) {
        this.creditoRepository =
                creditoRepository;

        this.cuotaRepository =
                cuotaRepository;

        this.garantiaRepository =
                garantiaRepository;
    }

    public ContratoCreditoDto construir(
            Long idCredito
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

        Cliente cliente =
                credito.getCliente();

        Persona persona =
                cliente.getPersona();

        CondicionCredito condicion =
                credito.getCondicion();

        List<Cuota> cuotas =
                cuotaRepository
                        .findByCredito_IdCreditoOrderByNumeroCuotaAsc(
                                idCredito
                        );

        Garantia garantia =
                garantiaRepository
                        .findFirstByCredito_IdCreditoOrderByFechaRegistroDesc(
                                idCredito
                        )
                        .orElse(null);

        ContratoCreditoDto dto =
                new ContratoCreditoDto();

        dto.setIdCredito(
                credito.getIdCredito()
        );

        dto.setCodigoCredito(
                credito.getCodigoCredito()
        );

        dto.setFechaDesembolso(
                condicion.getFechaInicio()
        );

        dto.setPrestatarioNombre(
                nombreCompleto(persona)
        );

        dto.setPrestatarioDocumento(
                persona.getDni()
        );

        dto.setPrestatarioDomicilio(
                valorSeguro(
                        persona.getDireccion()
                )
        );

        dto.setPrestamistaNombre(
                valorSeguro(
                        prestamistaNombre
                )
        );

        dto.setPrestamistaDocumento(
                valorSeguro(
                        prestamistaDocumento
                )
        );

        dto.setPrestamistaDomicilio(
                valorSeguro(
                        prestamistaDomicilio
                )
        );

        dto.setCapitalOriginal(
                credito.getCapitalOriginal()
        );

        dto.setTasaInteres(
                condicion.getTasaInteres()
        );

        dto.setTipoCalculo(
                condicion.getTipoCalculo()

        );

        dto.setPeriodicidadInteres(
                condicion.getPeriodicidadTasa()

        );

        dto.setFrecuenciaPago(
                condicion.getFrecuenciaPago()

        );

        dto.setPlazoMeses(
                condicion.getPlazoMeses()
        );

        dto.setInteresTotal(
                credito.getInteresGenerado()
        );

        dto.setTotalPagar(
                credito.getCapitalOriginal()
                        .add(
                                credito.getInteresGenerado()
                        )
        );

        dto.setGarantiaDescripcion(
                descripcionGarantia(
                        garantia
                )
        );

        List<ContratoCreditoDto.CuotaContratoDto>
                filas = new ArrayList<>();

        BigDecimal saldo =
                credito.getCapitalOriginal();

        for (Cuota cuota : cuotas) {

            BigDecimal saldoInicial =
                    saldo;

            BigDecimal capital =
                    cuota.getCapitalOriginal();

            BigDecimal interes =
                    cuota.getInteresOriginal();

            BigDecimal total =
                    capital.add(interes);

            saldo =
                    saldo.subtract(capital)
                            .max(BigDecimal.ZERO);

            filas.add(
                    new ContratoCreditoDto
                            .CuotaContratoDto(
                            cuota.getNumeroCuota(),
                            cuota.getFechaVencimiento(),
                            saldoInicial,
                            capital,
                            interes,
                            total,
                            saldo
                    )
            );
        }

        dto.setCuotas(
                filas
        );

        return dto;
    }

    private String nombreCompleto(
            Persona persona
    ) {

        String nombres =
                persona.getNombres() == null
                        ? ""
                        : persona.getNombres().trim();

        String apellidos =
                persona.getApellidos() == null
                        ? ""
                        : persona.getApellidos().trim();

        return (
                nombres + " " + apellidos
        ).trim();
    }

    private String descripcionGarantia(
            Garantia garantia
    ) {

        if (garantia == null) {
            return "Sin garantía registrada.";
        }

        StringBuilder texto =
                new StringBuilder();

        texto.append(
                garantia.getTipoGarantia()
        );

        if (
                garantia.getDescripcion() != null
                        &&
                        !garantia.getDescripcion().isBlank()
        ) {

            texto.append(": ")
                    .append(
                            garantia.getDescripcion()
                    );
        }

        if (
                garantia.getValorEstimado() != null
        ) {

            texto.append(
                    " - Valor estimado S/ "
            ).append(
                    garantia.getValorEstimado()
            );
        }

        return texto.toString();
    }

    private String valorSeguro(
            String valor
    ) {

        if (
                valor == null
                        ||
                        valor.isBlank()
        ) {
            return "No especificado";
        }

        return valor.trim();
    }
}