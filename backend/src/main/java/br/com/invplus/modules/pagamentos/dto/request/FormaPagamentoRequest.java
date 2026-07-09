package br.com.invplus.modules.pagamentos.dto.request;

import br.com.invplus.modules.pagamentos.enums.TipoFormaPagamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Payload para criar ou atualizar uma forma de pagamento.
 */
public record FormaPagamentoRequest(
        @NotBlank(message = "Codigo e obrigatorio")
        String codigo,
        @NotBlank(message = "Nome e obrigatorio")
        String nome,
        @NotNull(message = "Tipo e obrigatorio")
        TipoFormaPagamento tipo,
        Boolean permiteTroco,
        Boolean permiteParcelamento
) {
}
