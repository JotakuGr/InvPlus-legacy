package br.com.invplus.support.security;

import br.com.invplus.security.entity.Usuario;
import br.com.invplus.security.repository.PerfilRepository;
import br.com.invplus.security.repository.UsuarioRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
@RequiredArgsConstructor
public class UsuarioTestFactory {

    public static final String SENHA = "admin123";

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;

    public Usuario criarAtivoComPerfilAdmin() {
        String sufixo = UUID.randomUUID().toString();
        Usuario usuario = new Usuario(
                "Usuario Teste " + sufixo.substring(0, 8),
                "auth-" + sufixo + "@invplus.test",
                passwordEncoder.encode(SENHA));
        usuario.getPerfis().add(perfilRepository.findByNome("ADMIN").orElseThrow());
        return usuarioRepository.saveAndFlush(usuario);
    }

    public Usuario criarAtivoSemPerfil() {
        String sufixo = UUID.randomUUID().toString();
        return usuarioRepository.saveAndFlush(new Usuario(
                "Usuario Sem Perfil " + sufixo.substring(0, 8),
                "sem-perfil-" + sufixo + "@invplus.test",
                passwordEncoder.encode(SENHA)));
    }
}
