package br.com.invplus.modules.cadastros.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Resposta de unidade de medida para consultas REST.
 */
public record UnidadeMedidaResponse(
        UUID id,
        String sigla,
        String descricao,
        Short casasDecimais,
        Boolean ativo,
        Long version,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
