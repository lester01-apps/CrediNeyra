package com.credineyra.service;

import com.credineyra.exception.ReglaNegocioException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class CorreoService {

    private final ObjectProvider<JavaMailSender>
            mailSenderProvider;

    private final String baseUrl;

    public CorreoService(
            ObjectProvider<JavaMailSender>
                    mailSenderProvider,

            @Value(
                    "${credineyra.app.base-url:http://localhost:8087}"
            )
            String baseUrl
    ) {
        this.mailSenderProvider =
                mailSenderProvider;

        this.baseUrl = baseUrl;
    }

    public void enviarRecuperacionPassword(
            String destino,
            String token
    ) {

        JavaMailSender mailSender =
                mailSenderProvider
                        .getIfAvailable();

        if (mailSender == null) {
            throw new ReglaNegocioException(
                    "El servicio de correo todavía no está configurado"
            );
        }

        String enlace =
                UriComponentsBuilder
                        .fromUriString(baseUrl)
                        .path(
                                "/recuperar-password/restablecer"
                        )
                        .queryParam(
                                "token",
                                token
                        )
                        .build()
                        .toUriString();

        SimpleMailMessage mensaje =
                new SimpleMailMessage();

        mensaje.setTo(destino);

        mensaje.setSubject(
                "Recuperación de contraseña - crediNeyra"
        );

        mensaje.setText(
                "Se solicitó restablecer la contraseña de tu cuenta crediNeyra.\n\n"
                        + "Usa el siguiente enlace:\n"
                        + enlace
                        + "\n\n"
                        + "El enlace vence en 30 minutos y solo puede utilizarse una vez."
        );

        mailSender.send(mensaje);
    }
}