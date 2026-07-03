package br.com.invplus.modules.cadastros.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Resposta de categoria para consultas REST, incluindo resumo do pai.
 */
public record CategoriaResponse(
        UUID id,
        UUID categoriaPaiId,
        String categoriaPaiNome,
        String nome,
        String caminhoCompleto,
        String descricao,
        Integer ordem,
        Boolean ativo,
        Long version,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
