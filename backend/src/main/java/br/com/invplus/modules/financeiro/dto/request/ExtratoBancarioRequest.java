package br.com.invplus.modules.financeiro.dto.request;

import br.com.invplus.modules.financeiro.enums.OrigemExtratoBancario;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Dados aceitos para registrar uma importacao de extrato bancario.
 */
public record ExtratoBancarioRequest(
        @NotNull UUID contaBancariaId,
        @NotNull LocalDate dataInicio,
        @NotNull LocalDate dataFim,
        @NotNull BigDecimal saldoInicial,
        @NotNull BigDecimal saldoFinal,
        @NotNull OrigemExtratoBancario origem,
        @Size(max = 255) String arquivoNome,
        @Size(max = 64) String arquivoHash,
        String observacao
) {
}
