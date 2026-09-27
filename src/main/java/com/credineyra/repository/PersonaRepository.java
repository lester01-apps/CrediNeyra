package com.credineyra.repository;

import com.credineyra.entity.Persona;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonaRepository extends JpaRepository<Persona, Long> {

    Optional<Persona> findByDni(String dni);

    Optional<Persona> findByEmail(String email);

    boolean existsByDni(String dni);

    boolean existsByEmail(String email);
}