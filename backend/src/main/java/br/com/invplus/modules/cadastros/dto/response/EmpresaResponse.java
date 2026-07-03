package br.com.invplus.modules.cadastros.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Resposta resumida de empresa para a API.
 */
public record EmpresaResponse(
        UUID id,
        String razaoSocial,
        String nomeFantasia,
        String cnpj,
        String regimeTributario,
        Integer crt,
        String telefone,
        String email,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
