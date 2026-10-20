package com.credineyra.service;

import com.credineyra.exception.ReglaNegocioException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

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

    public void enviarCodigoRecuperacionPassword(String destino, String codigo) {

        JavaMailSender mailSender =
                mailSenderProvider
                        .getIfAvailable();

        if (mailSender == null) {
            throw new ReglaNegocioException(
                    "El servicio de correo todavía no está configurado"
            );
        }

        SimpleMailMessage mensaje =
                new SimpleMailMessage();

        mensaje.setTo(destino);

        mensaje.setSubject(
                "Recuperación de contraseña - crediNeyra"
        );

        mensaje.setText(
                "Se solicitó recuperar la contraseña de tu cuenta crediNeyra.\n\n"
                        + "Código temporal: " + codigo + "\n\n"
                        + "Ingresa este código en la aplicación. Vence en 15 minutos y "
                        + "solo puede utilizarse una vez. No lo compartas con nadie."
        );

        mailSender.send(mensaje);
    }

    public void enviarCodigoCambioPasswordInicial(
            String destino,
            String codigo
    ) {

        JavaMailSender mailSender =
                mailSenderProvider.getIfAvailable();

        if (mailSender == null) {
            throw new ReglaNegocioException(
                    "El servicio de correo todavía no está configurado"
            );
        }

        SimpleMailMessage mensaje =
                new SimpleMailMessage();

        mensaje.setTo(destino);
        mensaje.setSubject(
                "Código para cambiar tu contraseña - crediNeyra"
        );
        mensaje.setText(
                "Solicitaste cambiar la contraseña inicial de tu cuenta crediNeyra.\n\n"
                        + "Código temporal: " + codigo + "\n\n"
                        + "Ingresa este código en la aplicación para crear tu nueva contraseña. "
                        + "Vence en 15 minutos y solo puede utilizarse una vez. "
                        + "No compartas este código con nadie."
        );

        mailSender.send(mensaje);
    }
}
