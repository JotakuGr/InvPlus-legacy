package br.com.invplus.modules.cadastros.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Resposta de marca para consultas REST.
 */
public record MarcaResponse(
        UUID id,
        String nome,
        String descricao,
        String site,
        String fabricante,
        Boolean ativo,
        Long version,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
