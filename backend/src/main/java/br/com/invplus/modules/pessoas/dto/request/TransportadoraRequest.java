package br.com.invplus.modules.pessoas.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Dados de entrada para criar ou atualizar o papel Transportadora de uma Pessoa.
 */
public record TransportadoraRequest(
        @NotNull UUID pessoaId,
        String antt,
        String tipoVeiculoPrincipal,
        @PositiveOrZero BigDecimal capacidadeCargaKg,
        String observacao
) {
}
