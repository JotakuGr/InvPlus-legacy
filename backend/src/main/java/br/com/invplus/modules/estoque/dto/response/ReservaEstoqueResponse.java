package br.com.invplus.modules.estoque.dto.response;

import br.com.invplus.modules.estoque.enums.MovimentacaoOrigem;
import br.com.invplus.modules.estoque.enums.StatusReservaEstoque;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Dados retornados pela API para reservas de estoque.
 */
public record ReservaEstoqueResponse(
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
        String depositoNome,
        UUID localizacaoId,
        String localizacaoCodigo,
        BigDecimal quantidade,
        StatusReservaEstoque status,
        MovimentacaoOrigem origem,
        UUID origemId,
        OffsetDateTime expiraEm,
        OffsetDateTime baixadaEm,
        OffsetDateTime canceladaEm,
        String observacao,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
