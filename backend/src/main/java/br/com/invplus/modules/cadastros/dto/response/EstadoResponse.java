package br.com.invplus.modules.cadastros.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Resposta de estado para consultas REST.
 */
public record EstadoResponse(
        UUID id,
        UUID paisId,
        String paisNome,
        String nome,
        String uf,
        String codigoIbge,
        Boolean ativo,
        Long version,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
