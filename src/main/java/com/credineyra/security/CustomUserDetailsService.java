package com.credineyra.security;

import com.credineyra.entity.Usuario;
import com.credineyra.entity.UsuarioRol;
import com.credineyra.entity.enums.EstadoRol;
import com.credineyra.entity.enums.EstadoUsuario;
import com.credineyra.repository.UsuarioRepository;
import com.credineyra.repository.UsuarioRolRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;

    public CustomUserDetailsService(
            UsuarioRepository usuarioRepository,
            UsuarioRolRepository usuarioRolRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioRolRepository = usuarioRolRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Usuario no encontrado"
                        )
                );

        List<UsuarioRol> usuarioRoles =
                usuarioRolRepository
                        .findByUsuario_IdUsuario(
                                usuario.getIdUsuario()
                        );

        List<SimpleGrantedAuthority> authorities =
                usuarioRoles.stream()
                        .filter(usuarioRol ->
                                usuarioRol.getRol() != null
                                        &&
                                        usuarioRol.getRol().getEstado()
                                                == EstadoRol.ACTIVO
                        )
                        .map(usuarioRol ->
                                new SimpleGrantedAuthority(
                                        "ROLE_" +
                                                usuarioRol
                                                        .getRol()
                                                        .getNombre()
                                                        .toUpperCase()
                                )
                        )
                        .distinct()
                        .toList();

        boolean bloqueado =
                usuario.getEstado() == EstadoUsuario.BLOQUEADA;

        return User.withUsername(
                        usuario.getUsername()
                )
                .password(
                        usuario.getPasswordHash()
                )
                .authorities(authorities)
                .disabled(bloqueado)
                .build();
    }
}