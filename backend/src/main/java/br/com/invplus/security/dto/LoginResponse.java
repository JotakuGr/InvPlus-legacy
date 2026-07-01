package br.com.invplus.security.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record LoginResponse(
        String tokenType,
        String accessToken,
        OffsetDateTime expiresAt,
        UsuarioAutenticadoResponse usuario
) {
    public record UsuarioAutenticadoResponse(
            String id,
            String nome,
            String email,
            List<String> perfis,
            List<String> permissoes
    ) {
    }
}
