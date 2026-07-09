package br.com.invplus.modules.financeiro.dto.response;

import br.com.invplus.modules.financeiro.enums.StatusParcelaReceber;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Resposta REST com os dados financeiros calculados da parcela.
 */
public record ParcelaReceberResponse(
        UUID id,
        UUID contaReceberId,
        String numeroConta,
        Integer numeroParcela,
        LocalDate dataVencimento,
        BigDecimal valor,
        BigDecimal valorPago,
        BigDecimal valorAberto,
        BigDecimal juros,
        BigDecimal multa,
        BigDecimal desconto,
        StatusParcelaReceber status,
        LocalDate dataPagamento,
        String observacao,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
