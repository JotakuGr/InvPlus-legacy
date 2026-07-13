package br.com.invplus.support.security;

import br.com.invplus.security.service.AuthenticatedUserDetails;
import br.com.invplus.security.service.JwtService;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

/** Fabrica central de tokens com os claims usados pela seguranca real. */
@Component
@Profile("test")
@RequiredArgsConstructor
public class JwtTestFactory {

    private final JwtService jwtService;

    public String accessToken(UUID usuarioId, String email, UUID empresaId, UUID filialId,
            Collection<String> permissoes) {
        return jwtService.gerarAccessToken(
                principal(usuarioId, email, empresaId, filialId, permissoes), empresaId, filialId);
    }

    public String accessTokenSemEmpresa(UUID usuarioId, String email, Collection<String> permissoes) {
        return jwtService.gerarAccessToken(
                principal(usuarioId, email, null, null, permissoes), null, null);
    }

    public String selectionToken(UUID usuarioId) {
        return jwtService.gerarSelectionToken(usuarioId);
    }

    private AuthenticatedUserDetails principal(UUID usuarioId, String email, UUID empresaId, UUID filialId,
            Collection<String> permissoes) {
        List<SimpleGrantedAuthority> authorities = permissoes.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
        return new AuthenticatedUserDetails(
                usuarioId, email, "", authorities, true, empresaId, filialId);
    }
}
