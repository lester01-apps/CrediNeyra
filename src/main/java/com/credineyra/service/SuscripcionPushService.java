package com.credineyra.service;

import com.credineyra.entity.SuscripcionPush;
import com.credineyra.exception.DatoDuplicadoException;
import com.credineyra.exception.RecursoNoEncontradoException;
import com.credineyra.exception.ReglaNegocioException;
import com.credineyra.repository.SuscripcionPushRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SuscripcionPushService {

    private final SuscripcionPushRepository repository;

    public SuscripcionPushService(
            SuscripcionPushRepository repository
    ) {
        this.repository = repository;
    }

    public SuscripcionPush registrar(
            SuscripcionPush suscripcion
    ) {

        validar(suscripcion);

        if (repository.existsByTokenFcm(
                suscripcion.getTokenFcm()
        )) {
            throw new DatoDuplicadoException(
                    "El dispositivo ya está registrado para notificaciones"
            );
        }

        suscripcion.setActivo(true);

        return repository.save(suscripcion);
    }

    public SuscripcionPush activar(
            Long idSuscripcion
    ) {

        SuscripcionPush suscripcion =
                buscarPorId(idSuscripcion);

        suscripcion.setActivo(true);

        return repository.save(suscripcion);
    }

    public SuscripcionPush desactivar(
            Long idSuscripcion
    ) {

        SuscripcionPush suscripcion =
                buscarPorId(idSuscripcion);

        suscripcion.setActivo(false);

        return repository.save(suscripcion);
    }

    @Transactional(readOnly = true)
    public SuscripcionPush buscarPorId(
            Long idSuscripcion
    ) {

        return repository
                .findById(idSuscripcion)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Suscripción PUSH no encontrada"
                        )
                );
    }

    @Transactional(readOnly = true)
    public SuscripcionPush buscarPorToken(
            String token
    ) {

        return repository
                .findByTokenFcm(token)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Token PUSH no encontrado"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<SuscripcionPush>
    listarActivasPorUsuario(
            Long idUsuario
    ) {

        return repository
                .findByUsuario_IdUsuarioAndActivoTrue(
                        idUsuario
                );
    }

    @Transactional(readOnly = true)
    public List<SuscripcionPush>
    listarPorUsuario(
            Long idUsuario
    ) {

        return repository
                .findByUsuario_IdUsuario(
                        idUsuario
                );
    }

    private void validar(
            SuscripcionPush suscripcion
    ) {

        if (suscripcion == null) {
            throw new ReglaNegocioException(
                    "La suscripción PUSH es obligatoria"
            );
        }

        if (suscripcion.getUsuario() == null) {
            throw new ReglaNegocioException(
                    "La suscripción debe pertenecer a un usuario"
            );
        }

        if (suscripcion.getTokenFcm() == null
                || suscripcion.getTokenFcm()
                .isBlank()) {

            throw new ReglaNegocioException(
                    "El token FCM es obligatorio"
            );
        }
    }
}