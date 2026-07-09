package br.com.invplus.modules.financeiro.dto.response;

import br.com.invplus.modules.financeiro.enums.StatusRecebimento;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Dados retornados apos registrar ou consultar um recebimento.
 */
public record RecebimentoResponse(
        UUID id,
        UUID parcelaReceberId,
        UUID contaReceberId,
        UUID formaPagamentoId,
        String formaPagamentoNome,
        UUID empresaId,
        UUID filialId,
        UUID clienteId,
        String clienteNome,
        BigDecimal valorRecebido,
        BigDecimal juros,
        BigDecimal multa,
        BigDecimal desconto,
        BigDecimal valorLiquido,
        LocalDate dataRecebimento,
        StatusRecebimento status,
        String observacao,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
