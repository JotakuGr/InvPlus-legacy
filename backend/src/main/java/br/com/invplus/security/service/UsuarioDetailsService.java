package br.com.invplus.security.service;

import br.com.invplus.security.entity.Perfil;
import br.com.invplus.security.entity.Permissao;
import br.com.invplus.security.entity.Usuario;
import br.com.invplus.security.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        Usuario usuario = usuarioRepository.findByEmailWithPerfisAndPermissoes(normalizarEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado"));

        boolean enabled = Boolean.TRUE.equals(usuario.getAtivo()) && usuario.getDeletedAt() == null;
        return new AuthenticatedUserDetails(usuario.getId(), usuario.getEmail(), usuario.getSenhaHash(),
                toAuthorities(usuario), enabled);
    }

    public Collection<GrantedAuthority> toAuthorities(Usuario usuario) {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();

        for (Perfil perfil : usuario.getPerfis()) {
            if (!Boolean.TRUE.equals(perfil.getAtivo())) {
                continue;
            }

            authorities.add(new SimpleGrantedAuthority("ROLE_" + perfil.getNome()));

            for (Permissao permissao : perfil.getPermissoes()) {
                if (Boolean.TRUE.equals(permissao.getAtivo())) {
                    authorities.add(new SimpleGrantedAuthority(permissao.getRecurso() + ":" + permissao.getAcao()));
                }
            }
        }

        return authorities;
    }

    private String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
