package com.credineyra.service;

import com.credineyra.dto.ClienteEdicionDto;
import com.credineyra.entity.Cliente;
import com.credineyra.entity.Credito;
import com.credineyra.entity.Persona;
import com.credineyra.entity.enums.EstadoCliente;
import com.credineyra.entity.enums.EstadoCredito;
import com.credineyra.exception.DatoDuplicadoException;
import com.credineyra.exception.OperacionNoPermitidaException;
import com.credineyra.exception.RecursoNoEncontradoException;
import com.credineyra.exception.ReglaNegocioException;
import com.credineyra.repository.ClienteRepository;
import com.credineyra.repository.CreditoRepository;
import com.credineyra.repository.PersonaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ClienteGestionService {

    private final ClienteRepository clienteRepository;

    private final PersonaRepository personaRepository;

    private final CreditoRepository creditoRepository;


    public ClienteGestionService(
            ClienteRepository clienteRepository,
            PersonaRepository personaRepository,
            CreditoRepository creditoRepository
    ) {

        this.clienteRepository =
                clienteRepository;

        this.personaRepository =
                personaRepository;

        this.creditoRepository =
                creditoRepository;
    }


    /* =========================================================
       OBTENER DATOS PARA EDITAR
       ========================================================= */

    @Transactional(readOnly = true)
    public ClienteEdicionDto obtenerParaEdicion(
            Long idCliente
    ) {

        Cliente cliente =
                buscarCliente(
                        idCliente
                );


        Persona persona =
                cliente.getPersona();


        if (persona == null) {

            throw new ReglaNegocioException(
                    "El cliente no tiene una persona asociada"
            );
        }


        ClienteEdicionDto dto =
                new ClienteEdicionDto();


        dto.setIdCliente(
                cliente.getIdCliente()
        );


        dto.setCodigoCliente(
                cliente.getCodigoCliente()
        );


        dto.setDni(
                persona.getDni()
        );


        dto.setTipoOrigen(
                cliente.getTipoOrigen()
        );


        dto.setEstado(
                cliente.getEstado()
        );


        dto.setNombres(
                persona.getNombres()
        );


        dto.setApellidos(
                persona.getApellidos()
        );


        dto.setTelefono(
                persona.getTelefono()
        );


        dto.setEmail(
                persona.getEmail()
        );


        dto.setDireccion(
                persona.getDireccion()
        );


        dto.setObservaciones(
                cliente.getObservaciones()
        );


        return dto;
    }


    /* =========================================================
       ACTUALIZAR CLIENTE
       ========================================================= */

    public Cliente actualizarCliente(
            Long idCliente,
            ClienteEdicionDto dto
    ) {

        if (dto == null) {

            throw new ReglaNegocioException(
                    "Los datos del cliente son obligatorios"
            );
        }


        Cliente cliente =
                buscarCliente(
                        idCliente
                );


        Persona persona =
                cliente.getPersona();


        if (persona == null) {

            throw new ReglaNegocioException(
                    "El cliente no tiene una persona asociada"
            );
        }


        /* =====================================================
           PERSONA
           ===================================================== */

        persona.setNombres(
                limpiarTextoObligatorio(
                        dto.getNombres()
                )
        );


        persona.setApellidos(
                limpiarTextoObligatorio(
                        dto.getApellidos()
                )
        );


        persona.setTelefono(
                textoOpcional(
                        dto.getTelefono()
                )
        );


        persona.setEmail(
                emailOpcional(
                        dto.getEmail()
                )
        );


        persona.setDireccion(
                textoOpcional(
                        dto.getDireccion()
                )
        );


        /* =====================================================
           CLIENTE
           ===================================================== */

        cliente.setObservaciones(
                textoOpcional(
                        dto.getObservaciones()
                )
        );


        try {

            /*
             * saveAndFlush permite detectar aquí mismo
             * restricciones UNIQUE como correo duplicado.
             */

            personaRepository
                    .saveAndFlush(
                            persona
                    );


            return clienteRepository
                    .saveAndFlush(
                            cliente
                    );


        } catch (
                DataIntegrityViolationException ex
        ) {

            throw new DatoDuplicadoException(
                    "No se pudo actualizar el cliente. "
                            + "Verifique que el correo no esté registrado "
                            + "por otra persona."
            );
        }
    }


    /* =========================================================
       CAMBIAR ESTADO
       ========================================================= */

    public Cliente cambiarEstado(
            Long idCliente,
            EstadoCliente nuevoEstado
    ) {

        if (nuevoEstado == null) {

            throw new ReglaNegocioException(
                    "Debe seleccionar un estado"
            );
        }


        Cliente cliente =
                buscarCliente(
                        idCliente
                );


        if (cliente.getEstado()
                == nuevoEstado) {

            throw new ReglaNegocioException(
                    "El cliente ya se encuentra en estado "
                            + nuevoEstado
            );
        }


        /*
         * Para INACTIVAR:
         *
         * El cliente no puede tener créditos
         * pendientes.
         */
        if (nuevoEstado
                == EstadoCliente.INACTIVO) {

            validarPuedeInactivarse(
                    cliente
            );
        }


        cliente.setEstado(
                nuevoEstado
        );


        return clienteRepository.save(
                cliente
        );
    }


    /* =========================================================
       VALIDAR INACTIVACION
       ========================================================= */

    private void validarPuedeInactivarse(
            Cliente cliente
    ) {

        List<Credito> creditos =
                creditoRepository
                        .findByCliente_IdClienteOrderByFechaSolicitudDesc(
                                cliente.getIdCliente()
                        );


        List<Credito> pendientes =
                creditos.stream()
                        .filter(
                                credito ->
                                        credito.getEstado()
                                                != EstadoCredito.FINALIZADO
                        )
                        .toList();


        if (pendientes.isEmpty()) {
            return;
        }


        Credito primerCredito =
                pendientes.get(0);


        throw new OperacionNoPermitidaException(
                "No se puede inactivar al cliente. "
                        + "Tiene "
                        + pendientes.size()
                        + " crédito(s) pendiente(s). "
                        + "Ejemplo: "
                        + primerCredito.getCodigoCredito()
                        + " - "
                        + primerCredito.getEstado()
        );
    }


    /* =========================================================
       BUSCAR CLIENTE
       ========================================================= */

    private Cliente buscarCliente(
            Long idCliente
    ) {

        if (idCliente == null) {

            throw new ReglaNegocioException(
                    "El identificador del cliente es obligatorio"
            );
        }


        return clienteRepository
                .findById(
                        idCliente
                )
                .orElseThrow(
                        () ->
                                new RecursoNoEncontradoException(
                                        "Cliente no encontrado"
                                )
                );
    }


    /* =========================================================
       LIMPIEZA
       ========================================================= */

    private String limpiarTextoObligatorio(
            String valor
    ) {

        if (valor == null
                || valor.isBlank()) {

            return valor;
        }


        return valor
                .trim()
                .replaceAll(
                        "\\s+",
                        " "
                );
    }


    private String textoOpcional(
            String valor
    ) {

        if (valor == null
                || valor.isBlank()) {

            return null;
        }


        return valor.trim();
    }


    private String emailOpcional(
            String valor
    ) {

        if (valor == null
                || valor.isBlank()) {

            return null;
        }


        return valor
                .trim()
                .toLowerCase();
    }
}