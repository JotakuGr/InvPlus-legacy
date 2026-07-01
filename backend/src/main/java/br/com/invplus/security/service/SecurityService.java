package br.com.invplus.security.service;

import br.com.invplus.security.dto.LoginRequest;
import br.com.invplus.security.dto.LoginResponse;
import br.com.invplus.security.entity.Perfil;
import br.com.invplus.security.entity.Permissao;
import br.com.invplus.security.entity.Usuario;
import br.com.invplus.security.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SecurityService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String email = normalizarEmail(request.email());

        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.senha())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.gerarToken(userDetails);
        OffsetDateTime expiresAt = jwtService.calcularExpiracao();

        Usuario usuario = usuarioRepository.findByEmailWithPerfisAndPermissoes(email).orElseThrow();
        usuario.setUltimoLogin(OffsetDateTime.now());

        return new LoginResponse(
                "Bearer",
                token,
                expiresAt,
                new LoginResponse.UsuarioAutenticadoResponse(
                        usuario.getId().toString(),
                        usuario.getNome(),
                        usuario.getEmail(),
                        perfis(usuario),
                        permissoes(usuario)
                )
        );
    }

    private List<String> perfis(Usuario usuario) {
        return usuario.getPerfis()
                .stream()
                .filter(perfil -> Boolean.TRUE.equals(perfil.getAtivo()))
                .map(Perfil::getNome)
                .sorted()
                .toList();
    }

    private List<String> permissoes(Usuario usuario) {
        return usuario.getPerfis()
                .stream()
                .filter(perfil -> Boolean.TRUE.equals(perfil.getAtivo()))
                .flatMap(perfil -> perfil.getPermissoes().stream())
                .filter(permissao -> Boolean.TRUE.equals(permissao.getAtivo()))
                .map(this::permissao)
                .distinct()
                .sorted()
                .toList();
    }

    private String permissao(Permissao permissao) {
        return permissao.getRecurso() + "_" + permissao.getAcao();
    }

    private String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
