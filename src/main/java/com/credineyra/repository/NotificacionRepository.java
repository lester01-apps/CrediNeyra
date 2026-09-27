package com.credineyra.repository;

import com.credineyra.entity.Notificacion;
import com.credineyra.entity.enums.CanalNotificacion;
import com.credineyra.entity.enums.EstadoNotificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificacionRepository
        extends JpaRepository<Notificacion, Long> {

    List<Notificacion>
    findByCliente_IdClienteOrderByFechaProgramadaDesc(
            Long idCliente
    );

    List<Notificacion>
    findByCliente_IdClienteAndLeidaFalseOrderByFechaProgramadaDesc(
            Long idCliente
    );

    Optional<Notificacion> findByClaveEvento(
            String claveEvento
    );

    boolean existsByClaveEvento(
            String claveEvento
    );

    List<Notificacion>
    findByEstadoAndFechaProgramadaLessThanEqual(
            EstadoNotificacion estado,
            OffsetDateTime fecha
    );

    List<Notificacion> findByCanal(
            CanalNotificacion canal
    );
}