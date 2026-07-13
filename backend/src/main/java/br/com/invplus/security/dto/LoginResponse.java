package br.com.invplus.security.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record LoginResponse(
        String tokenType,
        String accessToken,
        String refreshToken,
        OffsetDateTime expiresAt,
        UsuarioAutenticadoResponse usuario,
        String status,
        String selectionToken,
        ContextoAtualResponse contexto,
        List<EmpresaDisponivelResponse> empresas
) {
    public LoginResponse(String tokenType, String accessToken, String refreshToken,
            OffsetDateTime expiresAt, UsuarioAutenticadoResponse usuario) {
        this(tokenType, accessToken, refreshToken, expiresAt, usuario, "AUTENTICADO", null, null, List.of());
    }
    public record UsuarioAutenticadoResponse(
            String id,
            String nome,
            String email,
            List<String> perfis,
            List<String> permissoes
    ) {
    }
}
