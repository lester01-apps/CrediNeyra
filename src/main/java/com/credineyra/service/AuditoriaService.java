package com.credineyra.service;

import com.credineyra.entity.Auditoria;
import com.credineyra.entity.Usuario;
import com.credineyra.exception.RecursoNoEncontradoException;
import com.credineyra.exception.ReglaNegocioException;
import com.credineyra.repository.AuditoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AuditoriaService {

    private final AuditoriaRepository repository;

    public AuditoriaService(
            AuditoriaRepository repository
    ) {
        this.repository = repository;
    }

    public Auditoria registrar(
            Auditoria auditoria
    ) {

        if (auditoria == null) {
            throw new ReglaNegocioException(
                    "La auditoría es obligatoria"
            );
        }

        if (auditoria.getAccion() == null
                || auditoria.getAccion().isBlank()) {

            throw new ReglaNegocioException(
                    "La acción es obligatoria"
            );
        }

        if (auditoria.getEntidad() == null
                || auditoria.getEntidad().isBlank()) {

            throw new ReglaNegocioException(
                    "La entidad es obligatoria"
            );
        }

        return repository.save(auditoria);
    }

    public Auditoria registrarEvento(
            Usuario usuario,
            String accion,
            String entidad,
            String identificador,
            String motivo,
            String datosAnteriores,
            String datosNuevos,
            String direccionIp
    ) {

        Auditoria auditoria =
                new Auditoria();

        auditoria.setUsuario(usuario);
        auditoria.setAccion(accion);
        auditoria.setEntidad(entidad);
        auditoria.setIdentificador(
                identificador
        );
        auditoria.setMotivo(motivo);
        auditoria.setDatosAnteriores(
                datosAnteriores
        );
        auditoria.setDatosNuevos(
                datosNuevos
        );
        auditoria.setDireccionIp(
                direccionIp
        );

        return repository.save(auditoria);
    }

    @Transactional(readOnly = true)
    public Auditoria buscarPorId(
            Long idAuditoria
    ) {

        return repository
                .findById(idAuditoria)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Auditoría no encontrada"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<Auditoria> listarTodos() {

        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Auditoria> listarPorUsuario(
            Long idUsuario
    ) {

        return repository
                .findByUsuario_IdUsuarioOrderByFechaHoraDesc(
                        idUsuario
                );
    }

    @Transactional(readOnly = true)
    public List<Auditoria> listarPorEntidad(
            String entidad
    ) {

        return repository
                .findByEntidadOrderByFechaHoraDesc(
                        entidad
                );
    }

    @Transactional(readOnly = true)
    public List<Auditoria>
    listarPorEntidadEIdentificador(
            String entidad,
            String identificador
    ) {

        return repository
                .findByEntidadAndIdentificadorOrderByFechaHoraDesc(
                        entidad,
                        identificador
                );
    }
}