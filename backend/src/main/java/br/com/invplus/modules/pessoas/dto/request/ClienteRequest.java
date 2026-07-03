package br.com.invplus.modules.pessoas.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Payload para criacao e atualizacao do papel Cliente.
 */
public record ClienteRequest(
        @NotNull UUID pessoaId,
        @PositiveOrZero BigDecimal limiteCredito,
        UUID condicaoPagamentoId,
        UUID tabelaPrecoId,
        String observacao
) {
}
