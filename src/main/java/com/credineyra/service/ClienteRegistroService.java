package com.credineyra.service;

import com.credineyra.dto.ClienteRegistroDto;
import com.credineyra.entity.Cliente;
import com.credineyra.entity.Persona;
import com.credineyra.entity.enums.EstadoCliente;
import com.credineyra.entity.enums.TipoOrigenCliente;
import com.credineyra.exception.DatoDuplicadoException;
import com.credineyra.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ClienteRegistroService {

    private final PersonaService personaService;

    private final ClienteRepository clienteRepository;


    public ClienteRegistroService(
            PersonaService personaService,
            ClienteRepository clienteRepository
    ) {

        this.personaService =
                personaService;

        this.clienteRepository =
                clienteRepository;
    }


    /* =========================================================
       REGISTRAR NUEVO CLIENTE
       ========================================================= */

    public Cliente registrarNuevoCliente(
            ClienteRegistroDto dto
    ) {

        String dni =
                dto.getDni()
                        .trim();


        /* =====================================================
           EVITAR CLIENTE DUPLICADO
           ===================================================== */

        if (clienteRepository
                .findByPersona_Dni(dni)
                .isPresent()) {

            throw new DatoDuplicadoException(
                    "Ya existe un cliente registrado con el DNI "
                            + dni
            );
        }


        /* =====================================================
           CREAR PERSONA
           ===================================================== */

        Persona persona =
                new Persona();


        persona.setDni(
                dni
        );


        persona.setNombres(
                limpiarTexto(
                        dto.getNombres()
                )
        );


        persona.setApellidos(
                limpiarTexto(
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


        /*
         * PersonaService ya valida:
         *
         * - DNI
         * - DNI duplicado
         * - correo duplicado
         * - nombres
         * - apellidos
         */
        Persona personaGuardada =
                personaService.registrar(
                        persona
                );


        /* =====================================================
           CREAR CLIENTE
           ===================================================== */

        Cliente cliente =
                new Cliente();


        cliente.setPersona(
                personaGuardada
        );


        /*
         * La columna codigo_cliente es NOT NULL y UNIQUE.
         *
         * Necesitamos un código temporal para poder realizar
         * el INSERT y obtener el id_cliente generado por
         * PostgreSQL.
         *
         * Luego reemplazamos este código por CLI-xxxxx.
         */
        cliente.setCodigoCliente(
                generarCodigoTemporal()
        );


        cliente.setTipoOrigen(
                TipoOrigenCliente.NUEVO
        );


        cliente.setEstado(
                EstadoCliente.ACTIVO
        );


        cliente.setObservaciones(
                textoOpcional(
                        dto.getObservaciones()
                )
        );


        /*
         * saveAndFlush fuerza el INSERT.
         *
         * Después de esto Hibernate ya conoce:
         *
         * cliente.getIdCliente()
         */
        Cliente clienteGuardado =
                clienteRepository
                        .saveAndFlush(
                                cliente
                        );


        /* =====================================================
           GENERAR CODIGO DEFINITIVO
           ===================================================== */

        String codigoDefinitivo =
                generarCodigoCliente(
                        clienteGuardado
                                .getIdCliente()
                );


        /*
         * En una base limpia no debería ocurrir jamás,
         * porque el código se basa en el ID único.
         *
         * Esta comprobación protege bases que pudieran
         * tener códigos antiguos creados manualmente.
         */
        if (clienteRepository
                .existsByCodigoCliente(
                        codigoDefinitivo
                )) {

            throw new DatoDuplicadoException(
                    "No se pudo generar el código automático "
                            + codigoDefinitivo
                            + ". Ya existe en el sistema."
            );
        }


        clienteGuardado.setCodigoCliente(
                codigoDefinitivo
        );


        return clienteRepository.save(
                clienteGuardado
        );
    }


    /* =========================================================
       GENERAR CODIGO FINAL
       ========================================================= */

    private String generarCodigoCliente(
            Long idCliente
    ) {

        if (idCliente == null) {

            throw new IllegalStateException(
                    "No se pudo obtener el identificador del cliente"
            );
        }


        /*
         * Ejemplos:
         *
         * 1    -> CLI-00001
         * 12   -> CLI-00012
         * 850  -> CLI-00850
         * 10000 -> CLI-10000
         */
        return String.format(
                "CLI-%05d",
                idCliente
        );
    }


    /* =========================================================
       CODIGO TEMPORAL
       ========================================================= */

    private String generarCodigoTemporal() {

        /*
         * Máximo 20 caracteres porque
         * codigo_cliente tiene longitud 20.
         *
         * Ejemplo:
         *
         * TMP-A12B34C56D78E90F
         */
        String uuid =
                UUID.randomUUID()
                        .toString()
                        .replace(
                                "-",
                                ""
                        )
                        .substring(
                                0,
                                16
                        )
                        .toUpperCase();


        return "TMP-" + uuid;
    }


    /* =========================================================
       LIMPIAR TEXTO
       ========================================================= */

    private String limpiarTexto(
            String valor
    ) {

        if (valor == null) {
            return null;
        }


        return valor
                .trim()
                .replaceAll(
                        "\\s+",
                        " "
                );
    }


    /* =========================================================
       TEXTO OPCIONAL
       ========================================================= */

    private String textoOpcional(
            String valor
    ) {

        if (valor == null
                || valor.isBlank()) {

            return null;
        }


        return valor.trim();
    }


    /* =========================================================
       EMAIL
       ========================================================= */

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