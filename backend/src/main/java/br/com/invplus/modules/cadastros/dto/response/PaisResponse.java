package br.com.invplus.modules.cadastros.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Resposta de pais para consultas REST.
 */
public record PaisResponse(
        UUID id,
        String nome,
        String codigoIso2,
        String codigoIso3,
        String codigoBacen,
        Boolean ativo,
        Long version,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
