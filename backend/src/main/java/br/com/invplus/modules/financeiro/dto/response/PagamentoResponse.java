package br.com.invplus.modules.financeiro.dto.response;

import br.com.invplus.modules.financeiro.enums.StatusPagamentoFinanceiro;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Dados retornados apos registrar ou consultar um pagamento.
 */
public record PagamentoResponse(
        UUID id,
        UUID parcelaPagarId,
        UUID contaPagarId,
        UUID formaPagamentoId,
        String formaPagamentoNome,
        UUID empresaId,
        String empresaNome,
        UUID filialId,
        String filialNome,
        UUID fornecedorId,
        String fornecedorNome,
        BigDecimal valorPago,
        BigDecimal juros,
        BigDecimal multa,
        BigDecimal desconto,
        BigDecimal valorLiquido,
        LocalDate dataPagamento,
        StatusPagamentoFinanceiro status,
        String observacao,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
