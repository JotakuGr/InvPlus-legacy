package br.com.invplus.modules.pagamentos.dto.response;

import br.com.invplus.modules.pagamentos.enums.TipoFormaPagamento;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Resposta da API para formas de pagamento.
 */
public record FormaPagamentoResponse(
        UUID id,
        String codigo,
        String nome,
        TipoFormaPagamento tipo,
        Boolean permiteTroco,
        Boolean permiteParcelamento,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
