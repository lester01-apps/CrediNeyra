package com.credineyra.config;

import com.credineyra.entity.Rol;
import com.credineyra.entity.enums.EstadoRol;
import com.credineyra.repository.RolRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatosInicialesConfig {

    @Bean
    public CommandLineRunner crearRolesIniciales(
            RolRepository rolRepository
    ) {

        return args -> {

            if (!rolRepository.existsByNombre(
                    "ADMINISTRADOR"
            )) {

                Rol administrador =
                        new Rol();

                administrador.setNombre(
                        "ADMINISTRADOR"
                );

                administrador.setDescripcion(
                        "Administrador de crediNeyra"
                );

                administrador.setEstado(
                        EstadoRol.ACTIVO
                );

                rolRepository.save(
                        administrador
                );
            }

            if (!rolRepository.existsByNombre(
                    "CLIENTE"
            )) {

                Rol cliente =
                        new Rol();

                cliente.setNombre(
                        "CLIENTE"
                );

                cliente.setDescripcion(
                        "Cliente de crediNeyra"
                );

                cliente.setEstado(
                        EstadoRol.ACTIVO
                );

                rolRepository.save(cliente);
            }
        };
    }
}