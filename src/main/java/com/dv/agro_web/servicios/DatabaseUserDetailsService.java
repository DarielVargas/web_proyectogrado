package com.dv.agro_web.servicios;

import com.dv.agro_web.entidades.Usuario;
import com.dv.agro_web.repositorios.UsuarioRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public DatabaseUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String usernameNormalizado = username == null ? "" : username.trim();

        Usuario usuario = usuarioRepository.findByUsernameIgnoreCase(usernameNormalizado)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        return toUserDetails(usuario);
    }

    private UserDetails toUserDetails(Usuario usuario) {
        String rol = usuario.getRol() == null ? "USER" : usuario.getRol().trim().toUpperCase();
        if (rol.startsWith("ROLE_")) {
            rol = rol.substring(5);
        }
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + rol));

        return User.withUsername(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(authorities)
                .disabled(Boolean.FALSE.equals(usuario.getActivo()))
                .build();
    }
}
