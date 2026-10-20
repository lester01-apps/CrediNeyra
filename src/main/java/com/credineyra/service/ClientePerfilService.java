package com.credineyra.service;

import com.credineyra.entity.Cliente;
import com.credineyra.entity.Documento;
import com.credineyra.entity.Usuario;
import com.credineyra.entity.enums.EstadoDocumento;
import com.credineyra.entity.enums.TipoDocumento;
import com.credineyra.exception.ReglaNegocioException;
import com.credineyra.repository.ClienteRepository;
import com.credineyra.repository.DocumentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

/** Gestiona la foto privada del propio cliente. */
@Service
@Transactional
public class ClientePerfilService {

    private static final long MAXIMO_FOTO_BYTES = 5L * 1024 * 1024;

    private final ClienteRepository clienteRepository;
    private final DocumentoRepository documentoRepository;
    private final DocumentoStorageService storageService;
    private final AuditoriaService auditoriaService;

    public ClientePerfilService(
            ClienteRepository clienteRepository,
            DocumentoRepository documentoRepository,
            DocumentoStorageService storageService,
            AuditoriaService auditoriaService
    ) {
        this.clienteRepository = clienteRepository;
        this.documentoRepository = documentoRepository;
        this.storageService = storageService;
        this.auditoriaService = auditoriaService;
    }

    public Documento guardarFotoPropia(Usuario usuario, MultipartFile archivo) {
        Cliente cliente = clientePropio(usuario);
        validarFoto(archivo);

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
        String extension = extension(contentType);
        String ruta = "clientes/" + cliente.getCodigoCliente()
                + "/perfil/" + marcaTiempo + extension;

        storageService.subir(contenido, ruta, contentType, false);

        documentoRepository
                .findFirstByCliente_IdClienteAndTipoDocumentoAndEstadoOrderByFechaSubidaDesc(
                        cliente.getIdCliente(), TipoDocumento.FOTO_PERFIL, EstadoDocumento.ACTIVO
                )
                .ifPresent(anterior -> anterior.setEstado(EstadoDocumento.ANULADO));

        Documento foto = new Documento();
        foto.setCliente(cliente);
        foto.setTipoDocumento(TipoDocumento.FOTO_PERFIL);
        foto.setCodigoDocumento("PERFIL-" + cliente.getIdCliente() + "-" + marcaTiempo);
        foto.setNombreArchivo("foto-perfil" + extension);
        foto.setRutaArchivo(ruta);
        foto.setVisibleCliente(false);
        foto.setEstado(EstadoDocumento.ACTIVO);
        foto.setObservaciones("Foto de perfil actualizada por el cliente");
        Documento guardada = documentoRepository.save(foto);

        auditoriaService.registrarEvento(
                usuario,
                "Foto de perfil actualizada",
                "PERFIL",
                "FOTO-" + guardada.getIdDocumento(),
                "El cliente actualizó su foto de perfil",
                null,
                "{\"estado\":\"ACTUALIZADA\"}",
                null
        );
        return guardada;
    }

    @Transactional(readOnly = true)
    public Optional<Documento> obtenerFotoPropia(Usuario usuario) {
        Cliente cliente = clientePropio(usuario);
        return documentoRepository
                .findFirstByCliente_IdClienteAndTipoDocumentoAndEstadoOrderByFechaSubidaDesc(
                        cliente.getIdCliente(), TipoDocumento.FOTO_PERFIL, EstadoDocumento.ACTIVO
                );
    }

    private Cliente clientePropio(Usuario usuario) {
        if (usuario == null || usuario.getPersona() == null) {
            throw new ReglaNegocioException("No se pudo identificar tu cuenta");
        }
        return clienteRepository.findByPersona_Dni(usuario.getPersona().getDni())
                .orElseThrow(() -> new ReglaNegocioException("Tu cuenta no tiene un cliente asociado"));
    }

    private void validarFoto(MultipartFile archivo) {
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
