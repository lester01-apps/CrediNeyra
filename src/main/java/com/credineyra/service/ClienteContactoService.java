package com.credineyra.service;

import com.credineyra.dto.ClienteContactoDto;
import com.credineyra.entity.Cliente;
import com.credineyra.entity.Persona;
import com.credineyra.exception.RecursoNoEncontradoException;
import com.credineyra.exception.ReglaNegocioException;
import com.credineyra.repository.ClienteRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class ClienteContactoService {

    /* =========================================================
       LOGGER
       ========================================================= */

    private static final Logger log =
            LoggerFactory.getLogger(
                    ClienteContactoService.class
            );


    /* =========================================================
       DEPENDENCIAS
       ========================================================= */

    private final ClienteRepository clienteRepository;

    private final JavaMailSender mailSender;

    private final String correoEmisor;


    public ClienteContactoService(
            ClienteRepository clienteRepository,
            JavaMailSender mailSender,

            @Value("${spring.mail.username:}")
            String correoEmisor
    ) {

        this.clienteRepository =
                clienteRepository;

        this.mailSender =
                mailSender;

        this.correoEmisor =
                correoEmisor;
    }


    /* =========================================================
       ENVIAR CORREO AL CLIENTE
       ========================================================= */

    public String enviarCorreo(
            Long idCliente,
            ClienteContactoDto dto
    ) {

        /* =====================================================
           VALIDAR ID
           ===================================================== */

        if (idCliente == null) {

            throw new ReglaNegocioException(
                    "El identificador del cliente es obligatorio"
            );
        }


        /* =====================================================
           VALIDAR DTO
           ===================================================== */

        if (dto == null) {

            throw new ReglaNegocioException(
                    "Los datos del mensaje son obligatorios"
            );
        }


        /* =====================================================
           VALIDAR ASUNTO
           ===================================================== */

        if (
                dto.getAsunto() == null
                        ||
                        dto.getAsunto().isBlank()
        ) {

            throw new ReglaNegocioException(
                    "El asunto del correo es obligatorio"
            );
        }


        /* =====================================================
           VALIDAR MENSAJE
           ===================================================== */

        if (
                dto.getMensaje() == null
                        ||
                        dto.getMensaje().isBlank()
        ) {

            throw new ReglaNegocioException(
                    "El mensaje del correo es obligatorio"
            );
        }


        /* =====================================================
           BUSCAR CLIENTE
           ===================================================== */

        Cliente cliente =
                clienteRepository
                        .findById(
                                idCliente
                        )
                        .orElseThrow(
                                () ->
                                        new RecursoNoEncontradoException(
                                                "Cliente no encontrado"
                                        )
                        );


        /* =====================================================
           OBTENER PERSONA
           ===================================================== */

        Persona persona =
                cliente.getPersona();


        if (persona == null) {

            throw new ReglaNegocioException(
                    "El cliente no tiene datos personales asociados"
            );
        }


        /* =====================================================
           CORREO DESTINO
           ===================================================== */

        String correoDestino =
                persona.getEmail();


        if (
                correoDestino == null
                        ||
                        correoDestino.isBlank()
        ) {

            throw new ReglaNegocioException(
                    "El cliente no tiene un correo electrónico registrado"
            );
        }


        correoDestino =
                correoDestino
                        .trim()
                        .toLowerCase();


        /* =====================================================
           VALIDAR CORREO EMISOR
           ===================================================== */

        if (
                correoEmisor == null
                        ||
                        correoEmisor.isBlank()
        ) {

            log.error(
                    "MAIL_USERNAME no está configurado."
            );


            throw new ReglaNegocioException(
                    "El correo saliente de crediNeyra no está configurado"
            );
        }


        /* =====================================================
           PREPARAR DATOS
           ===================================================== */

        String asunto =
                dto.getAsunto()
                        .trim();


        String mensaje =
                dto.getMensaje()
                        .trim();


        /* =====================================================
           ENVIAR
           ===================================================== */

        try {

            MimeMessage mimeMessage =
                    mailSender
                            .createMimeMessage();


            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            mimeMessage,
                            false,
                            StandardCharsets.UTF_8.name()
                    );


            /* =================================================
               REMITENTE
               ================================================= */

            helper.setFrom(
                    correoEmisor.trim()
            );


            /* =================================================
               DESTINATARIO
               ================================================= */

            helper.setTo(
                    correoDestino
            );


            /* =================================================
               ASUNTO
               ================================================= */

            helper.setSubject(
                    asunto
            );


            /* =================================================
               MENSAJE
               ================================================= */

            /*
             * false:
             * correo de texto normal.
             *
             * Soporta emojis y caracteres UTF-8.
             */
            helper.setText(
                    mensaje,
                    false
            );


            /* =================================================
               ENVÍO REAL
               ================================================= */

            mailSender.send(
                    mimeMessage
            );


            /* =================================================
               LOG CORRECTO
               ================================================= */

            log.info(
                    "Correo enviado correctamente al cliente {} - {}",
                    cliente.getCodigoCliente(),
                    correoDestino
            );


            return correoDestino;


        } catch (
                MessagingException
                |
                MailException ex
        ) {

            /* =================================================
               MOSTRAR ERROR REAL EN INTELLIJ
               ================================================= */

            log.error(
                    "ERROR SMTP CrediNeyra. "
                            + "No se pudo enviar correo al cliente {} "
                            + "al correo {} desde {}",
                    cliente.getCodigoCliente(),
                    correoDestino,
                    correoEmisor,
                    ex
            );


            /* =================================================
               MENSAJE SEGURO PARA LA WEB
               ================================================= */

            throw new ReglaNegocioException(
                    "No se pudo enviar el correo. "
                            + "Verifique la configuración del correo saliente."
            );
        }
    }
}