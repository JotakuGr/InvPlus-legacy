package br.com.invplus.modules.estoque.dto.response;

import br.com.invplus.modules.estoque.enums.MovimentacaoOrigem;
import br.com.invplus.modules.estoque.enums.TipoMovimentacaoEstoque;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Dados retornados para consultas do historico de movimentacoes.
 */
public record MovimentacaoEstoqueResponse(
        UUID id,
        UUID estoqueId,
        UUID produtoId,
        String produtoCodigo,
        String produtoNome,
        UUID empresaId,
        String empresaNome,
        UUID filialId,
        String filialNome,
        UUID depositoId,
        String depositoCodigo,
        String depositoNome,
        UUID localizacaoId,
        String localizacaoCodigo,
        TipoMovimentacaoEstoque tipoMovimentacao,
        BigDecimal quantidade,
        BigDecimal saldoAnterior,
        BigDecimal saldoPosterior,
        MovimentacaoOrigem origem,
        UUID origemId,
        String documento,
        String observacao,
        OffsetDateTime dataMovimentacao,
        OffsetDateTime criadoEm,
        UUID criadoPor
) {
}
