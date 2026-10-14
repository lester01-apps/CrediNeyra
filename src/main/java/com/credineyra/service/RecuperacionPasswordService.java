package com.credineyra.service;

import com.credineyra.entity.Persona;
import com.credineyra.entity.RecuperacionPassword;
import com.credineyra.entity.Usuario;
import com.credineyra.exception.OperacionNoPermitidaException;
import com.credineyra.exception.RecursoNoEncontradoException;
import com.credineyra.exception.ReglaNegocioException;
import com.credineyra.repository.PersonaRepository;
import com.credineyra.repository.RecuperacionPasswordRepository;
import com.credineyra.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

@Service
@Transactional
public class RecuperacionPasswordService {

    private final RecuperacionPasswordRepository repository;
    private final PersonaRepository personaRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final CorreoService correoService;

    private final SecureRandom secureRandom =
            new SecureRandom();

    public RecuperacionPasswordService(
            RecuperacionPasswordRepository repository,
            PersonaRepository personaRepository,
            UsuarioRepository usuarioRepository,
            UsuarioService usuarioService,
            CorreoService correoService
    ) {
        this.repository = repository;
        this.personaRepository = personaRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.correoService = correoService;
    }

    public void solicitarRecuperacion(
            String email
    ) {

        if (email == null
                || email.isBlank()) {

            throw new ReglaNegocioException(
                    "El correo es obligatorio"
            );
        }

        /*
         * No revelamos si el correo existe o no.
         */
        Persona persona =
                personaRepository
                        .findByEmail(email.trim())
                        .orElse(null);

        if (persona == null) {
            return;
        }

        Usuario usuario =
                usuarioRepository
                        .findByPersona_Dni(
                                persona.getDni()
                        )
                        .orElse(null);

        if (usuario == null) {
            return;
        }

        invalidarTokensAnteriores(
                usuario.getIdUsuario()
        );

        String tokenPlano =
                generarTokenSeguro();

        RecuperacionPassword recuperacion =
                new RecuperacionPassword();

        recuperacion.setUsuario(usuario);

        recuperacion.setTokenHash(
                hash(tokenPlano)
        );

        recuperacion.setExpiraEn(
                OffsetDateTime.now()
                        .plusMinutes(30)
        );

        repository.save(recuperacion);

        correoService
                .enviarRecuperacionPassword(
                        persona.getEmail(),
                        tokenPlano
                );
    }

    public void restablecerPassword(
            String tokenPlano,
            String nuevaPassword,
            String confirmarPassword
    ) {

        RecuperacionPassword recuperacion =
                validarToken(tokenPlano);

        usuarioService.cambiarPassword(
                recuperacion.getUsuario()
                        .getIdUsuario(),
                nuevaPassword,
                confirmarPassword
        );

        recuperacion.setUsadoEn(
                OffsetDateTime.now()
        );

        repository.save(recuperacion);
    }

    @Transactional(readOnly = true)
    public RecuperacionPassword validarToken(
            String tokenPlano
    ) {

        if (tokenPlano == null
                || tokenPlano.isBlank()) {

            throw new ReglaNegocioException(
                    "El token es obligatorio"
            );
        }

        RecuperacionPassword recuperacion =
                repository
                        .findByTokenHash(
                                hash(tokenPlano)
                        )
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "Token inválido"
                                )
                        );

        if (recuperacion.getUsadoEn()
                != null) {

            throw new OperacionNoPermitidaException(
                    "El token ya fue utilizado"
            );
        }

        if (recuperacion.getExpiraEn()
                .isBefore(
                        OffsetDateTime.now()
                )) {

            throw new OperacionNoPermitidaException(
                    "El token ha expirado"
            );
        }

        return recuperacion;
    }

    private void invalidarTokensAnteriores(
            Long idUsuario
    ) {

        List<RecuperacionPassword> tokens =
                repository
                        .findByUsuario_IdUsuario(
                                idUsuario
                        );

        OffsetDateTime ahora =
                OffsetDateTime.now();

        for (RecuperacionPassword token :
                tokens) {

            if (token.getUsadoEn() == null
                    && token.getExpiraEn()
                    .isAfter(ahora)) {

                token.setUsadoEn(ahora);
            }
        }

        repository.saveAll(tokens);
    }

    private String generarTokenSeguro() {

        byte[] bytes = new byte[32];

        secureRandom.nextBytes(bytes);

        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String hash(
            String valor
    ) {

        try {

            MessageDigest digest =
                    MessageDigest
                            .getInstance(
                                    "SHA-256"
                            );

            byte[] resultado =
                    digest.digest(
                            valor.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return HexFormat
                    .of()
                    .formatHex(resultado);

        } catch (NoSuchAlgorithmException ex) {

            throw new IllegalStateException(
                    "No se pudo generar el hash del token",
                    ex
            );
        }
    }
}