package br.com.invplus.modules.estoque.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Dados de saida da LocalizacaoEstoque.
 */
public record LocalizacaoEstoqueResponse(
        UUID id,
        UUID depositoId,
        String depositoCodigo,
        String depositoNome,
        String codigo,
        String descricao,
        String rua,
        String corredor,
        String prateleira,
        String nivel,
        String posicao,
        Boolean principal,
        Boolean bloqueada,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
