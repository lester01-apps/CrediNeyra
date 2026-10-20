package com.credineyra.service;

import com.credineyra.entity.Usuario;
import com.credineyra.exception.ReglaNegocioException;
import com.credineyra.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

/** Almacena la foto privada del administrador sin reutilizar documentos de clientes. */
@Service
@Transactional
public class AdministradorPerfilFotoService {

    private static final long MAXIMO_FOTO_BYTES = 5L * 1024 * 1024;

    private final UsuarioRepository usuarioRepository;
    private final DocumentoStorageService storageService;
    private final AuditoriaService auditoriaService;

    public AdministradorPerfilFotoService(
            UsuarioRepository usuarioRepository,
            DocumentoStorageService storageService,
            AuditoriaService auditoriaService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.storageService = storageService;
        this.auditoriaService = auditoriaService;
    }

    public void guardarFotoPropia(Usuario administrador, MultipartFile archivo) {
        validarArchivo(archivo);
        String contentType = normalizarTipo(archivo.getContentType());
        byte[] contenido;
        try {
            contenido = archivo.getBytes();
        } catch (IOException ex) {
            throw new ReglaNegocioException("No se pudo leer la foto seleccionada");
        }

        String marcaTiempo = OffsetDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")
        );
        String ruta = "administradores/" + administrador.getIdUsuario()
                + "/perfil/" + marcaTiempo + extension(contentType);
        storageService.subir(contenido, ruta, contentType, false);
        administrador.setFotoPerfilRuta(ruta);
        usuarioRepository.save(administrador);
        auditoriaService.registrarEvento(
                administrador,
                "Foto de perfil actualizada",
                "PERFIL",
                "ADMIN-" + administrador.getIdUsuario(),
                "El administrador actualizó su foto de perfil",
                null,
                "{\"estado\":\"ACTUALIZADA\"}",
                null
        );
    }

    @Transactional(readOnly = true)
    public Optional<String> obtenerRuta(Usuario administrador) {
        return Optional.ofNullable(administrador.getFotoPerfilRuta())
                .filter(ruta -> !ruta.isBlank());
    }

    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ReglaNegocioException("Selecciona una imagen para tu foto de perfil");
        }
        if (archivo.getSize() > MAXIMO_FOTO_BYTES) {
            throw new ReglaNegocioException("La foto no puede superar los 5 MB");
        }
        normalizarTipo(archivo.getContentType());
    }

    private String normalizarTipo(String contentType) {
        if ("image/jpeg".equalsIgnoreCase(contentType)
                || "image/png".equalsIgnoreCase(contentType)
                || "image/webp".equalsIgnoreCase(contentType)) {
            return contentType.toLowerCase(Locale.ROOT);
        }
        throw new ReglaNegocioException("La foto debe estar en formato JPG, PNG o WEBP");
    }

    private String extension(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }
}
