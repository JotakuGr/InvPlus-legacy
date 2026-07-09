package br.com.invplus.modules.pagamentos.dto.response;

import br.com.invplus.modules.financeiro.enums.RegraVencimento;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Resposta da API para condicoes de pagamento.
 */
public record CondicaoPagamentoResponse(
        UUID id,
        String codigo,
        String nome,
        RegraVencimento regraVencimento,
        Boolean consideraFeriados,
        BigDecimal jurosPercentual,
        BigDecimal multaPercentual,
        BigDecimal descontoPercentual,
        List<CondicaoPagamentoParcelaResponse> parcelas,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
