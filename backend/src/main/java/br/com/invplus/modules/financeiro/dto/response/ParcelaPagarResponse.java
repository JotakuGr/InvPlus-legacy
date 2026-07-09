package br.com.invplus.modules.financeiro.dto.response;

import br.com.invplus.modules.financeiro.enums.StatusParcelaPagar;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Dados retornados pela API de parcelas a pagar.
 */
public record ParcelaPagarResponse(
        UUID id,
        UUID contaPagarId,
        String numeroConta,
        Integer numeroParcela,
        LocalDate dataVencimento,
        BigDecimal valor,
        BigDecimal valorPago,
        BigDecimal valorAberto,
        BigDecimal juros,
        BigDecimal multa,
        BigDecimal desconto,
        StatusParcelaPagar status,
        LocalDate dataPagamento,
        String observacao,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
