package com.credineyra.service;

import com.credineyra.dto.UsuarioRegistroDto;
import com.credineyra.entity.Persona;
import com.credineyra.entity.Rol;
import com.credineyra.entity.Usuario;
import com.credineyra.entity.UsuarioRol;
import com.credineyra.entity.enums.EstadoRol;
import com.credineyra.entity.enums.EstadoUsuario;
import com.credineyra.exception.DatoDuplicadoException;
import com.credineyra.exception.OperacionNoPermitidaException;
import com.credineyra.exception.RecursoNoEncontradoException;
import com.credineyra.exception.ReglaNegocioException;
import com.credineyra.repository.PersonaRepository;
import com.credineyra.repository.RolRepository;
import com.credineyra.repository.UsuarioRepository;
import com.credineyra.repository.UsuarioRolRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;
    private final RolRepository rolRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PersonaRepository personaRepository,
            RolRepository rolRepository,
            UsuarioRolRepository usuarioRolRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.personaRepository = personaRepository;
        this.rolRepository = rolRepository;
        this.usuarioRolRepository = usuarioRolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /*
     * REGISTRO COMPLETO:
     *
     * UsuarioRegistroDto
     *      ↓
     * Persona
     *      ↓
     * Usuario + BCrypt
     *      ↓
     * Rol
     *      ↓
     * UsuarioRol
     *
     * Todo ocurre dentro de una sola transacción.
     */
    public Usuario registrar(
            UsuarioRegistroDto dto
    ) {

        if (dto == null) {
            throw new ReglaNegocioException(
                    "Los datos del usuario son obligatorios"
            );
        }

        if (dto.getIdPersona() == null) {
            throw new ReglaNegocioException(
                    "Debe seleccionar una persona"
            );
        }

        if (dto.getIdRol() == null) {
            throw new ReglaNegocioException(
                    "Debe seleccionar un rol"
            );
        }

        validarPassword(
                dto.getPassword(),
                dto.getConfirmarPassword()
        );

        Persona persona =
                personaRepository
                        .findById(dto.getIdPersona())
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "Persona no encontrada"
                                )
                        );

        Rol rol =
                rolRepository
                        .findById(dto.getIdRol())
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "Rol no encontrado"
                                )
                        );

        if (rol.getEstado() != EstadoRol.ACTIVO) {
            throw new OperacionNoPermitidaException(
                    "No se puede asignar un rol inactivo"
            );
        }

        String username = dto.getUsername();

        if (username == null
                || username.isBlank()) {

            throw new ReglaNegocioException(
                    "El nombre de usuario es obligatorio"
            );
        }

        username = username.trim();

        if (usuarioRepository.existsByUsername(
                username
        )) {
            throw new DatoDuplicadoException(
                    "El nombre de usuario ya está registrado"
            );
        }

        usuarioRepository
                .findByPersona_Dni(
                        persona.getDni()
                )
                .ifPresent(existente -> {
                    throw new DatoDuplicadoException(
                            "La persona ya tiene una cuenta de usuario"
                    );
                });

        Usuario usuario = new Usuario();

        usuario.setPersona(persona);
        usuario.setUsername(username);

        usuario.setPasswordHash(
                passwordEncoder.encode(
                        dto.getPassword()
                )
        );

        usuario.setEstado(
                EstadoUsuario.ACTIVA
        );

        usuario.setCambioPasswordObligatorio(
                true
        );

        Usuario usuarioGuardado =
                usuarioRepository.save(usuario);

        /*
         * usuario_rol tiene una llave compuesta:
         * id_usuario + id_rol
         */
        UsuarioRol usuarioRol =
                new UsuarioRol();

        UsuarioRol.UsuarioRolId id =
                new UsuarioRol.UsuarioRolId(
                        usuarioGuardado.getIdUsuario(),
                        rol.getIdRol()
                );

        usuarioRol.setId(id);
        usuarioRol.setUsuario(usuarioGuardado);
        usuarioRol.setRol(rol);

        usuarioRolRepository.save(usuarioRol);

        return usuarioGuardado;
    }

    /**
     * Crea una cuenta destinada exclusivamente al portal del cliente.
     * El rol no se recibe del navegador para evitar que una cuenta de cliente
     * pueda obtener privilegios administrativos por una petición manipulada.
     */
    public Usuario registrarCliente(
            UsuarioRegistroDto dto
    ) {

        Rol rolCliente = rolRepository
                .findByNombre("CLIENTE")
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el rol CLIENTE"
                ));

        dto.setIdRol(rolCliente.getIdRol());

        return registrar(dto);
    }

    public Usuario actualizarUsername(
            Long idUsuario,
            String nuevoUsername
    ) {

        if (nuevoUsername == null
                || nuevoUsername.isBlank()) {

            throw new ReglaNegocioException(
                    "El nombre de usuario es obligatorio"
            );
        }

        nuevoUsername =
                nuevoUsername.trim();

        Usuario usuario =
                buscarPorId(idUsuario);

        usuarioRepository
                .findByUsername(nuevoUsername)
                .ifPresent(encontrado -> {

                    if (!encontrado
                            .getIdUsuario()
                            .equals(idUsuario)) {

                        throw new DatoDuplicadoException(
                                "El nombre de usuario ya está registrado"
                        );
                    }
                });

        usuario.setUsername(
                nuevoUsername
        );

        return usuarioRepository.save(usuario);
    }

    public Usuario cambiarPassword(
            Long idUsuario,
            String nuevaPassword,
            String confirmarPassword
    ) {

        validarPassword(
                nuevaPassword,
                confirmarPassword
        );

        Usuario usuario =
                buscarPorId(idUsuario);

        usuario.setPasswordHash(
                passwordEncoder.encode(
                        nuevaPassword
                )
        );

        usuario.setCambioPasswordObligatorio(
                false
        );

        return usuarioRepository.save(usuario);
    }

    /**
     * El cliente decidió conservar por ahora la contraseña inicial entregada
     * por el administrador. No modifica la contraseña, solo evita mostrar de
     * nuevo el aviso de primer ingreso.
     */
    public Usuario conservarPasswordInicial(
            Long idUsuario
    ) {

        Usuario usuario =
                buscarPorId(idUsuario);

        usuario.setCambioPasswordObligatorio(false);

        return usuarioRepository.save(usuario);
    }

    public Usuario cambiarEstado(
            Long idUsuario,
            EstadoUsuario nuevoEstado
    ) {

        if (nuevoEstado == null) {
            throw new ReglaNegocioException(
                    "El estado es obligatorio"
            );
        }

        Usuario usuario =
                buscarPorId(idUsuario);

        usuario.setEstado(nuevoEstado);

        return usuarioRepository.save(usuario);
    }

    public Usuario activar(
            Long idUsuario
    ) {

        return cambiarEstado(
                idUsuario,
                EstadoUsuario.ACTIVA
        );
    }

    public Usuario restringir(
            Long idUsuario
    ) {

        return cambiarEstado(
                idUsuario,
                EstadoUsuario.RESTRINGIDA
        );
    }

    public Usuario bloquear(
            Long idUsuario
    ) {

        return cambiarEstado(
                idUsuario,
                EstadoUsuario.BLOQUEADA
        );
    }

    public Usuario registrarAcceso(
            Long idUsuario
    ) {

        Usuario usuario =
                buscarPorId(idUsuario);

        usuario.setUltimoAcceso(
                OffsetDateTime.now()
        );

        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(
            Long idUsuario
    ) {

        return usuarioRepository
                .findById(idUsuario)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Usuario no encontrado"
                        )
                );
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorUsername(
            String username
    ) {

        return usuarioRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Usuario no encontrado"
                        )
                );
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorDni(
            String dni
    ) {

        return usuarioRepository
                .findByPersona_Dni(dni)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "No existe un usuario con DNI "
                                        + dni
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {

        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarActivos() {

        return usuarioRepository.findByEstado(
                EstadoUsuario.ACTIVA
        );
    }

    @Transactional(readOnly = true)
    public List<UsuarioRol> listarRolesUsuario(
            Long idUsuario
    ) {

        buscarPorId(idUsuario);

        return usuarioRolRepository
                .findByUsuario_IdUsuario(
                        idUsuario
                );
    }

    private void validarPassword(
            String password,
            String confirmarPassword
    ) {

        if (password == null
                || password.isBlank()) {

            throw new ReglaNegocioException(
                    "La contraseña es obligatoria"
            );
        }

        if (password.length() < 8) {
            throw new ReglaNegocioException(
                    "La contraseña debe tener como mínimo 8 caracteres"
            );
        }

        if (confirmarPassword == null
                || !password.equals(
                confirmarPassword
        )) {

            throw new ReglaNegocioException(
                    "Las contraseñas no coinciden"
            );
        }
    }
}
