package com.credineyra.repository;

import com.credineyra.entity.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditoriaRepository
        extends JpaRepository<Auditoria, Long> {

    List<Auditoria>
    findByUsuario_IdUsuarioOrderByFechaHoraDesc(
            Long idUsuario
    );

    List<Auditoria>
    findByEntidadAndIdentificadorOrderByFechaHoraDesc(
            String entidad,
            String identificador
    );

    List<Auditoria>
    findByEntidadOrderByFechaHoraDesc(
            String entidad
    );
}