package br.com.invplus.modules.pagamentos.dto.request;

import br.com.invplus.modules.financeiro.enums.RegraVencimento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;

/**
 * Payload para criar ou atualizar condicoes de pagamento.
 */
public record CondicaoPagamentoRequest(
        @NotBlank(message = "Codigo e obrigatorio")
        String codigo,
        @NotBlank(message = "Nome e obrigatorio")
        String nome,
        @NotNull(message = "Percentual de juros e obrigatorio")
        @PositiveOrZero(message = "Percentual de juros nao pode ser negativo")
        BigDecimal jurosPercentual,
        @NotNull(message = "Percentual de multa e obrigatorio")
        @PositiveOrZero(message = "Percentual de multa nao pode ser negativo")
        BigDecimal multaPercentual,
        @NotNull(message = "Percentual de desconto e obrigatorio")
        @PositiveOrZero(message = "Percentual de desconto nao pode ser negativo")
        BigDecimal descontoPercentual,
        @NotNull(message = "Regra de vencimento e obrigatoria")
        RegraVencimento regraVencimento,
        @NotNull(message = "Considera feriados e obrigatorio")
        Boolean consideraFeriados,
        @Valid
        @NotNull(message = "Parcelas sao obrigatorias")
        List<CondicaoPagamentoParcelaRequest> parcelas
) {
}
