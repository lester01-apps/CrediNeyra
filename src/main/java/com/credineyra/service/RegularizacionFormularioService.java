package com.credineyra.service;

import com.credineyra.dto.RegularizacionFormularioDto;
import com.credineyra.entity.Cliente;
import com.credineyra.entity.Credito;
import com.credineyra.entity.Cuota;
import com.credineyra.entity.Persona;
import com.credineyra.entity.enums.EstadoCuota;
import com.credineyra.exception.RecursoNoEncontradoException;
import com.credineyra.repository.CreditoRepository;
import com.credineyra.repository.CuotaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class RegularizacionFormularioService {

    private final CreditoRepository creditoRepository;
    private final CuotaRepository cuotaRepository;

    public RegularizacionFormularioService(
            CreditoRepository creditoRepository,
            CuotaRepository cuotaRepository
    ) {

        this.creditoRepository =
                creditoRepository;

        this.cuotaRepository =
                cuotaRepository;
    }


    @Transactional(readOnly = true)
    public RegularizacionFormularioDto obtener(
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


        LocalDate hoy =
                LocalDate.now();


        List<Cuota> cuotas =
                cuotaRepository
                        .findByCredito_IdCreditoOrderByFechaVencimientoAsc(
                                idCredito
                        );


        List<Cuota> vencidas =
                cuotas.stream()
                        .filter(
                                cuota ->
                                        cuota.getEstado()
                                                != EstadoCuota.PAGADA
                        )
                        .filter(
                                cuota ->
                                        cuota.getFechaVencimiento()
                                                .isBefore(hoy)
                        )
                        .filter(
                                cuota ->
                                        saldo(cuota)
                                                .compareTo(
                                                        BigDecimal.ZERO
                                                ) > 0
                        )
                        .toList();


        LocalDate vencimientoMasAntiguo =
                vencidas.isEmpty()
                        ? null
                        : vencidas
                        .getFirst()
                        .getFechaVencimiento();


        String nombre =
                (
                        valor(persona.getNombres())
                                + " "
                                + valor(persona.getApellidos())
                ).trim();


        BigDecimal capital =
                nz(
                        credito.getCapitalPendiente()
                );


        BigDecimal interes =
                nz(
                        credito.getInteresPendiente()
                );


        return new RegularizacionFormularioDto(

                credito.getIdCredito(),

                credito.getCodigoCredito(),

                cliente.getIdCliente(),

                cliente.getCodigoCliente(),

                nombre,

                persona.getDni(),

                capital,

                interes,

                capital.add(interes),

                vencidas.size(),

                vencimientoMasAntiguo,

                credito.getEstado() == null
                        ? "-"
                        : credito.getEstado().name()
        );
    }


    private BigDecimal saldo(
            Cuota cuota
    ) {

        return nz(
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
                );
    }


    private BigDecimal nz(
            BigDecimal valor
    ) {

        return valor == null
                ? BigDecimal.ZERO.setScale(2)
                : valor;
    }


    private String valor(
            String valor
    ) {

        return valor == null
                ? ""
                : valor;
    }
}