package br.com.invplus.modules.financeiro.dto.request;

import br.com.invplus.modules.financeiro.enums.CategoriaLancamentoBancario;
import br.com.invplus.modules.financeiro.enums.TipoLancamentoBancario;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Dados aceitos para criar ou atualizar uma linha do extrato bancario.
 */
public record ExtratoBancarioLancamentoRequest(
        @NotNull UUID extratoBancarioId,
        @NotNull LocalDate dataLancamento,
        @NotBlank @Size(max = 255) String descricao,
        @Size(max = 80) String documento,
        @NotNull TipoLancamentoBancario tipo,
        @NotNull @DecimalMin(value = "0.01") BigDecimal valor,
        BigDecimal saldoAposLancamento,
        @Size(max = 120) String identificadorBancario,
        CategoriaLancamentoBancario categoriaBancaria,
        String observacao
) {
}
