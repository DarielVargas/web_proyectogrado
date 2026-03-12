package com.dv.agro_web.config;

import com.dv.agro_web.entidades.Usuario;
import com.dv.agro_web.repositorios.UsuarioRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Locale;

@ControllerAdvice
public class AuthenticatedUserModelAdvice {

    private final UsuarioRepository usuarioRepository;

    public AuthenticatedUserModelAdvice(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @ModelAttribute("sidebarUsuarioNombre")
    public String sidebarUsuarioNombre() {
        return obtenerUsuarioActual()
                .map(usuario -> {
                    String nombre = usuario.getNombre();
                    return (nombre == null || nombre.isBlank()) ? usuario.getUsername() : nombre;
                })
                .orElse("Usuario");
    }

    @ModelAttribute("sidebarUsuarioRol")
    public String sidebarUsuarioRol() {
        return obtenerUsuarioActual()
                .map(Usuario::getRol)
                .map(this::etiquetaRol)
                .orElse("Usuario");
    }

    private java.util.Optional<Usuario> obtenerUsuarioActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return java.util.Optional.empty();
        }

        String username = authentication.getName();
        if (username == null || username.isBlank()) {
            return java.util.Optional.empty();
        }

        return usuarioRepository.findByUsernameIgnoreCase(username.trim());
    }

    private String etiquetaRol(String rol) {
        if (rol == null || rol.isBlank()) {
            return "Usuario";
        }

        String rolNormalizado = rol.trim().toUpperCase(Locale.ROOT);
        if (rolNormalizado.startsWith("ROLE_")) {
            rolNormalizado = rolNormalizado.substring(5);
        }

        if ("ADMIN".equals(rolNormalizado)) {
            return "Administrador";
        }

        return rolNormalizado.charAt(0) + rolNormalizado.substring(1).toLowerCase(Locale.ROOT);
    }
}
