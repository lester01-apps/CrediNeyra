package com.credineyra.service;

import com.credineyra.entity.Permiso;
import com.credineyra.exception.DatoDuplicadoException;
import com.credineyra.exception.RecursoNoEncontradoException;
import com.credineyra.exception.ReglaNegocioException;
import com.credineyra.repository.PermisoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PermisoService {

    private final PermisoRepository permisoRepository;

    public PermisoService(
            PermisoRepository permisoRepository
    ) {
        this.permisoRepository = permisoRepository;
    }

    public Permiso registrar(Permiso permiso) {

        validar(permiso);

        permisoRepository
                .findByModuloAndNombre(
                        permiso.getModulo(),
                        permiso.getNombre()
                )
                .ifPresent(existente -> {
                    throw new DatoDuplicadoException(
                            "El permiso ya existe para este módulo"
                    );
                });

        return permisoRepository.save(permiso);
    }

    public Permiso actualizar(Permiso permiso) {

        if (permiso == null
                || permiso.getIdPermiso() == null) {

            throw new ReglaNegocioException(
                    "El permiso debe existir para actualizarse"
            );
        }

        validar(permiso);

        Permiso actual =
                buscarPorId(permiso.getIdPermiso());

        permisoRepository
                .findByModuloAndNombre(
                        permiso.getModulo(),
                        permiso.getNombre()
                )
                .ifPresent(existente -> {

                    if (!existente.getIdPermiso()
                            .equals(permiso.getIdPermiso())) {

                        throw new DatoDuplicadoException(
                                "El permiso ya existe"
                        );
                    }
                });

        actual.setNombre(permiso.getNombre());
        actual.setModulo(permiso.getModulo());
        actual.setDescripcion(
                permiso.getDescripcion()
        );

        return permisoRepository.save(actual);
    }

    @Transactional(readOnly = true)
    public Permiso buscarPorId(Long idPermiso) {

        return permisoRepository
                .findById(idPermiso)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Permiso no encontrado"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<Permiso> listarTodos() {
        return permisoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Permiso> listarPorModulo(
            String modulo
    ) {
        return permisoRepository.findByModulo(modulo);
    }

    private void validar(Permiso permiso) {

        if (permiso == null) {
            throw new ReglaNegocioException(
                    "Los datos del permiso son obligatorios"
            );
        }

        if (permiso.getNombre() == null
                || permiso.getNombre().isBlank()) {

            throw new ReglaNegocioException(
                    "El nombre del permiso es obligatorio"
            );
        }

        if (permiso.getModulo() == null
                || permiso.getModulo().isBlank()) {

            throw new ReglaNegocioException(
                    "El módulo es obligatorio"
            );
        }
    }
}