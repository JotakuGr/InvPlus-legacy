package br.com.invplus.modules.pessoas.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Dados de entrada para criar ou atualizar o papel Funcionario de uma Pessoa.
 */
public record FuncionarioRequest(
        @NotNull UUID pessoaId,
        UUID usuarioId,
        @NotBlank String cargo,
        @PositiveOrZero BigDecimal salario,
        @NotNull LocalDate dataAdmissao,
        LocalDate dataDemissao,
        String observacao
) {
}
