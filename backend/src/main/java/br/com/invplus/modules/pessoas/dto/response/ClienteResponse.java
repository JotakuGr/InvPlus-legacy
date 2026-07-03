package br.com.invplus.modules.pessoas.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Resposta publica do cliente com dados basicos da pessoa vinculada.
 */
public record ClienteResponse(
        UUID id,
        UUID pessoaId,
        String nome,
        String cpfCnpj,
        String email,
        String telefone,
        BigDecimal limiteCredito,
        UUID condicaoPagamentoId,
        UUID tabelaPrecoId,
        String observacao,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
