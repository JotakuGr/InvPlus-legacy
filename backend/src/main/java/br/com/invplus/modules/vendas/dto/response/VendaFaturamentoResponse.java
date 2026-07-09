package br.com.invplus.modules.vendas.dto.response;

import br.com.invplus.modules.vendas.enums.StatusVendaFaturamento;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Dados retornados do faturamento de venda.
 */
public record VendaFaturamentoResponse(
        UUID id,
        UUID pedidoVendaId,
        UUID empresaId,
        String empresaNome,
        UUID filialId,
        String filialNome,
        UUID clienteId,
        String clienteNome,
        String numero,
        StatusVendaFaturamento status,
        OffsetDateTime dataFaturamento,
        BigDecimal valorTotal,
        UUID contaReceberId,
        String numeroContaReceber,
        String observacao,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
