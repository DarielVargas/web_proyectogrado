package com.dv.agro_web.config;

import com.dv.agro_web.entidades.Usuario;
import com.dv.agro_web.repositorios.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordBootstrapConfig {

    @Bean
    public CommandLineRunner ensureKnownUsersPasswords(UsuarioRepository usuarioRepository,
                                                       PasswordEncoder passwordEncoder) {
        return args -> {
            actualizarPasswordSiNoCoincide(usuarioRepository, passwordEncoder, "Dariel", "Dariel1234");
            actualizarPasswordSiNoCoincide(usuarioRepository, passwordEncoder, "Carlos", "Carlos1234");
        };
    }

    private void actualizarPasswordSiNoCoincide(UsuarioRepository usuarioRepository,
                                                PasswordEncoder passwordEncoder,
                                                String username,
                                                String plainPassword) {
        usuarioRepository.findByUsernameIgnoreCase(username).ifPresent(usuario -> {
            String hashActual = usuario.getPassword();
            if (hashActual == null || !passwordEncoder.matches(plainPassword, hashActual)) {
                usuario.setPassword(passwordEncoder.encode(plainPassword));
                usuarioRepository.save(usuario);
            }
            activarSiEsNecesario(usuario, usuarioRepository);
        });
    }

    private void activarSiEsNecesario(Usuario usuario, UsuarioRepository usuarioRepository) {
        if (Boolean.FALSE.equals(usuario.getActivo())) {
            usuario.setActivo(true);
            usuarioRepository.save(usuario);
        }
    }
}
