package com.credineyra.service;

import com.credineyra.entity.Cliente;
import com.credineyra.entity.enums.EstadoCliente;
import com.credineyra.exception.DatoDuplicadoException;
import com.credineyra.exception.RecursoNoEncontradoException;
import com.credineyra.exception.ReglaNegocioException;
import com.credineyra.repository.ClienteRepository;
import com.credineyra.repository.PersonaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final PersonaRepository personaRepository;


    public ClienteService(
            ClienteRepository clienteRepository,
            PersonaRepository personaRepository
    ) {
        this.clienteRepository = clienteRepository;
        this.personaRepository = personaRepository;
    }


    /* =========================================================
       REGISTRAR CLIENTE
       ========================================================= */

    public Cliente registrar(Cliente cliente) {

        validarCliente(cliente);

        Long idPersona =
                cliente.getPersona().getIdPersona();


        if (!personaRepository.existsById(idPersona)) {

            throw new RecursoNoEncontradoException(
                    "La persona asociada no existe"
            );

        }


        if (clienteRepository.existsByCodigoCliente(
                cliente.getCodigoCliente()
        )) {

            throw new DatoDuplicadoException(
                    "El código de cliente ya está registrado"
            );

        }


        clienteRepository
                .findByPersona_Dni(
                        cliente.getPersona().getDni()
                )
                .ifPresent(existente -> {

                    throw new DatoDuplicadoException(
                            "La persona ya está registrada como cliente"
                    );

                });


        if (cliente.getEstado() == null) {

            cliente.setEstado(
                    EstadoCliente.ACTIVO
            );

        }


        return clienteRepository.save(cliente);
    }


    /* =========================================================
       ACTUALIZAR CLIENTE
       ========================================================= */

    public Cliente actualizar(Cliente cliente) {

        if (cliente == null
                || cliente.getIdCliente() == null) {

            throw new ReglaNegocioException(
                    "El cliente debe existir para poder actualizarse"
            );

        }


        validarCliente(cliente);


        Cliente actual =
                buscarPorId(
                        cliente.getIdCliente()
                );


        clienteRepository
                .findByCodigoCliente(
                        cliente.getCodigoCliente()
                )
                .ifPresent(encontrado -> {

                    if (!encontrado
                            .getIdCliente()
                            .equals(
                                    cliente.getIdCliente()
                            )) {

                        throw new DatoDuplicadoException(
                                "El código pertenece a otro cliente"
                        );

                    }

                });


        actual.setCodigoCliente(
                cliente.getCodigoCliente()
        );


        actual.setTipoOrigen(
                cliente.getTipoOrigen()
        );


        actual.setObservaciones(
                cliente.getObservaciones()
        );


        if (cliente.getEstado() != null) {

            actual.setEstado(
                    cliente.getEstado()
            );

        }


        return clienteRepository.save(actual);
    }


    /* =========================================================
       CAMBIAR ESTADO
       ========================================================= */

    public Cliente cambiarEstado(
            Long idCliente,
            EstadoCliente estado
    ) {

        if (estado == null) {

            throw new ReglaNegocioException(
                    "El estado es obligatorio"
            );

        }


        Cliente cliente =
                buscarPorId(idCliente);


        cliente.setEstado(estado);


        return clienteRepository.save(cliente);
    }


    /* =========================================================
       BUSCAR POR ID
       ========================================================= */

    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long idCliente) {

        return clienteRepository
                .findById(idCliente)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Cliente no encontrado"
                        )
                );

    }


    /* =========================================================
       BUSCAR POR CÓDIGO
       ========================================================= */

    @Transactional(readOnly = true)
    public Cliente buscarPorCodigo(
            String codigoCliente
    ) {

        return clienteRepository
                .findByCodigoCliente(
                        codigoCliente
                )
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Cliente no encontrado"
                        )
                );

    }


    /* =========================================================
       BUSCAR POR DNI
       ========================================================= */

    @Transactional(readOnly = true)
    public Cliente buscarPorDni(String dni) {

        return clienteRepository
                .findByPersona_Dni(dni)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "No existe un cliente con DNI " + dni
                        )
                );

    }


    /* =========================================================
       LISTAR TODOS
       ========================================================= */

    @Transactional(readOnly = true)
    public List<Cliente> listarTodos() {

        return clienteRepository.findAll();

    }


    /* =========================================================
       LISTAR ACTIVOS
       ========================================================= */

    @Transactional(readOnly = true)
    public List<Cliente> listarActivos() {

        return clienteRepository.findByEstado(
                EstadoCliente.ACTIVO
        );

    }


    /* =========================================================
       DASHBOARD - TOTAL DE CLIENTES
       ========================================================= */

    @Transactional(readOnly = true)
    public long contarClientes() {

        return clienteRepository.count();

    }


    /* =========================================================
       DASHBOARD - ÚLTIMOS 5 CLIENTES
       ========================================================= */

    @Transactional(readOnly = true)
    public List<Cliente> listarUltimosCinco() {

        List<Cliente> clientes =
                clienteRepository
                        .findAll(
                                PageRequest.of(
                                        0,
                                        5,
                                        Sort.by(
                                                Sort.Direction.DESC,
                                                "fechaRegistro"
                                        )
                                )
                        )
                        .getContent();


        /*
         * Inicializamos la relación Persona dentro
         * de la transacción para que Thymeleaf pueda
         * mostrar DNI, nombres, teléfono, etc.
         * aunque open-in-view esté desactivado.
         */
        clientes.forEach(cliente -> {

            if (cliente.getPersona() != null) {

                cliente.getPersona()
                        .getNombres();

            }

        });


        return clientes;
    }


    /* =========================================================
       VALIDACIONES
       ========================================================= */

    private void validarCliente(
            Cliente cliente
    ) {

        if (cliente == null) {

            throw new ReglaNegocioException(
                    "Los datos del cliente son obligatorios"
            );

        }


        if (cliente.getPersona() == null
                || cliente.getPersona()
                .getIdPersona() == null) {

            throw new ReglaNegocioException(
                    "Debe seleccionar una persona"
            );

        }


        if (cliente.getCodigoCliente() == null
                || cliente
                .getCodigoCliente()
                .isBlank()) {

            throw new ReglaNegocioException(
                    "El código del cliente es obligatorio"
            );

        }


        if (cliente.getTipoOrigen() == null) {

            throw new ReglaNegocioException(
                    "El tipo de origen es obligatorio"
            );

        }

    }

}