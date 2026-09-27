package com.credineyra.repository;

import com.credineyra.entity.SuscripcionPush;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SuscripcionPushRepository
        extends JpaRepository<SuscripcionPush, Long> {

    Optional<SuscripcionPush> findByTokenFcm(
            String tokenFcm
    );

    boolean existsByTokenFcm(
            String tokenFcm
    );

    List<SuscripcionPush>
    findByUsuario_IdUsuarioAndActivoTrue(
            Long idUsuario
    );

    List<SuscripcionPush>
    findByUsuario_IdUsuario(
            Long idUsuario
    );
}