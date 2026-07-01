package br.com.invplus.security.service;

import br.com.invplus.security.entity.Perfil;
import br.com.invplus.security.entity.Permissao;
import br.com.invplus.security.entity.Usuario;
import br.com.invplus.security.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
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
                .filter(user -> Boolean.TRUE.equals(user.getAtivo()))
                .filter(user -> user.getDeletedAt() == null)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado"));

        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getSenhaHash())
                .authorities(toAuthorities(usuario))
                .accountLocked(false)
                .disabled(!Boolean.TRUE.equals(usuario.getAtivo()) || usuario.getDeletedAt() != null)
                .build();
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
                    authorities.add(new SimpleGrantedAuthority(permissao.getRecurso() + "_" + permissao.getAcao()));
                }
            }
        }

        return authorities;
    }

    private String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
