package com.credineyra.repository;

import com.credineyra.entity.RecuperacionPassword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecuperacionPasswordRepository
        extends JpaRepository<RecuperacionPassword, Long> {

    Optional<RecuperacionPassword> findByTokenHash(String tokenHash);

    List<RecuperacionPassword> findByUsuario_IdUsuario(
            Long idUsuario
    );
}