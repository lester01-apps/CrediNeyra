package com.credineyra.service;

import com.credineyra.entity.Persona;
import com.credineyra.entity.enums.EstadoPersona;
import com.credineyra.exception.DatoDuplicadoException;
import com.credineyra.exception.RecursoNoEncontradoException;
import com.credineyra.exception.ReglaNegocioException;
import com.credineyra.repository.PersonaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PersonaService {

    private final PersonaRepository personaRepository;

    public PersonaService(PersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
    }

    public Persona registrar(Persona persona) {

        if (persona == null) {
            throw new ReglaNegocioException(
                    "Los datos de la persona son obligatorios"
            );
        }

        validarDatosBasicos(persona);

        if (personaRepository.existsByDni(persona.getDni())) {
            throw new DatoDuplicadoException(
                    "El DNI ya está registrado"
            );
        }

        if (persona.getEmail() != null
                && !persona.getEmail().isBlank()
                && personaRepository.existsByEmail(persona.getEmail())) {

            throw new DatoDuplicadoException(
                    "El correo electrónico ya está registrado"
            );
        }

        if (persona.getEstado() == null) {
            persona.setEstado(EstadoPersona.ACTIVO);
        }

        return personaRepository.save(persona);
    }

    public Persona actualizar(Persona persona) {

        if (persona == null || persona.getIdPersona() == null) {
            throw new ReglaNegocioException(
                    "La persona debe existir para poder actualizarse"
            );
        }

        Persona actual = buscarPorId(
                persona.getIdPersona()
        );

        validarDatosBasicos(persona);

        personaRepository.findByDni(persona.getDni())
                .ifPresent(encontrada -> {

                    if (!encontrada.getIdPersona()
                            .equals(persona.getIdPersona())) {

                        throw new DatoDuplicadoException(
                                "El DNI ya pertenece a otra persona"
                        );
                    }
                });

        if (persona.getEmail() != null
                && !persona.getEmail().isBlank()) {

            personaRepository
                    .findByEmail(persona.getEmail())
                    .ifPresent(encontrada -> {

                        if (!encontrada.getIdPersona()
                                .equals(persona.getIdPersona())) {

                            throw new DatoDuplicadoException(
                                    "El correo electrónico ya pertenece a otra persona"
                            );
                        }
                    });
        }

        actual.setDni(persona.getDni());
        actual.setNombres(persona.getNombres());
        actual.setApellidos(persona.getApellidos());
        actual.setTelefono(persona.getTelefono());
        actual.setEmail(persona.getEmail());
        actual.setDireccion(persona.getDireccion());

        if (persona.getEstado() != null) {
            actual.setEstado(persona.getEstado());
        }

        return personaRepository.save(actual);
    }

    public Persona cambiarEstado(
            Long idPersona,
            EstadoPersona estado
    ) {

        if (estado == null) {
            throw new ReglaNegocioException(
                    "El estado es obligatorio"
            );
        }

        Persona persona = buscarPorId(idPersona);

        persona.setEstado(estado);

        return personaRepository.save(persona);
    }

    @Transactional(readOnly = true)
    public Persona buscarPorId(Long idPersona) {

        return personaRepository.findById(idPersona)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Persona no encontrada"
                        )
                );
    }

    @Transactional(readOnly = true)
    public Persona buscarPorDni(String dni) {

        return personaRepository.findByDni(dni)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "No existe una persona con DNI " + dni
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<Persona> listarTodos() {
        return personaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Persona> listarActivos() {

        return personaRepository.findAll()
                .stream()
                .filter(persona ->
                        persona.getEstado()
                                == EstadoPersona.ACTIVO
                )
                .toList();
    }

    private void validarDatosBasicos(Persona persona) {

        if (persona.getDni() == null
                || !persona.getDni().matches("\\d{8}")) {

            throw new ReglaNegocioException(
                    "El DNI debe contener exactamente 8 dígitos"
            );
        }

        if (persona.getNombres() == null
                || persona.getNombres().isBlank()) {

            throw new ReglaNegocioException(
                    "Los nombres son obligatorios"
            );
        }

        if (persona.getApellidos() == null
                || persona.getApellidos().isBlank()) {

            throw new ReglaNegocioException(
                    "Los apellidos son obligatorios"
            );
        }
    }
}