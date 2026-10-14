package com.credineyra.service;

import com.credineyra.entity.Rol;
import com.credineyra.entity.enums.EstadoRol;
import com.credineyra.exception.DatoDuplicadoException;
import com.credineyra.exception.RecursoNoEncontradoException;
import com.credineyra.exception.ReglaNegocioException;
import com.credineyra.repository.RolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RolService {

    private final RolRepository rolRepository;

    public RolService(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    public Rol registrar(Rol rol) {

        if (rol == null) {
            throw new ReglaNegocioException(
                    "Los datos del rol son obligatorios"
            );
        }

        if (rol.getNombre() == null
                || rol.getNombre().isBlank()) {

            throw new ReglaNegocioException(
                    "El nombre del rol es obligatorio"
            );
        }

        if (rolRepository.existsByNombre(
                rol.getNombre()
        )) {
            throw new DatoDuplicadoException(
                    "El rol ya existe"
            );
        }

        if (rol.getEstado() == null) {
            rol.setEstado(EstadoRol.ACTIVO);
        }

        return rolRepository.save(rol);
    }

    public Rol actualizar(Rol rol) {

        if (rol == null || rol.getIdRol() == null) {
            throw new ReglaNegocioException(
                    "El rol debe existir para poder actualizarse"
            );
        }

        Rol actual = buscarPorId(rol.getIdRol());

        rolRepository.findByNombre(rol.getNombre())
                .ifPresent(encontrado -> {

                    if (!encontrado.getIdRol()
                            .equals(rol.getIdRol())) {

                        throw new DatoDuplicadoException(
                                "El nombre del rol ya existe"
                        );
                    }
                });

        actual.setNombre(rol.getNombre());
        actual.setDescripcion(rol.getDescripcion());

        if (rol.getEstado() != null) {
            actual.setEstado(rol.getEstado());
        }

        return rolRepository.save(actual);
    }

    public Rol cambiarEstado(
            Long idRol,
            EstadoRol estado
    ) {

        if (estado == null) {
            throw new ReglaNegocioException(
                    "El estado es obligatorio"
            );
        }

        Rol rol = buscarPorId(idRol);

        rol.setEstado(estado);

        return rolRepository.save(rol);
    }

    @Transactional(readOnly = true)
    public Rol buscarPorId(Long idRol) {

        return rolRepository.findById(idRol)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Rol no encontrado"
                        )
                );
    }

    @Transactional(readOnly = true)
    public Rol buscarPorNombre(String nombre) {

        return rolRepository.findByNombre(nombre)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Rol no encontrado"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<Rol> listarTodos() {
        return rolRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Rol> listarActivos() {

        return rolRepository.findByEstado(
                EstadoRol.ACTIVO
        );
    }
}