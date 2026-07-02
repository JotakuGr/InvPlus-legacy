package br.com.invplus.modules.cadastros.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Resposta de filial para consultas REST.
 */
public record FilialResponse(
        UUID id,
        UUID empresaId,
        UUID enderecoId,
        String codigo,
        String nome,
        String cnpj,
        String inscricaoEstadual,
        String telefone,
        String email,
        Boolean matriz,
        Boolean ativo,
        Long version,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
