package br.com.invplus.modules.cadastros.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Resposta de cidade para consultas REST.
 */
public record CidadeResponse(
        UUID id,
        UUID estadoId,
        String estadoNome,
        String uf,
        String nome,
        String codigoIbge,
        Boolean ativo,
        Long version,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
