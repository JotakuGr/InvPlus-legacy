package br.com.invplus.modules.vendas.dto.response;

import br.com.invplus.modules.vendas.enums.StatusPedidoVenda;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Representacao resumida do pedido de venda retornada pela API.
 */
public record PedidoVendaResponse(
        UUID id,
        UUID empresaId,
        String empresaNome,
        UUID filialId,
        String filialNome,
        UUID clienteId,
        String clienteNome,
        UUID vendedorId,
        String vendedorNome,
        String numero,
        StatusPedidoVenda status,
        LocalDate dataEmissao,
        LocalDate dataEntregaPrevista,
        BigDecimal valorTotal,
        BigDecimal desconto,
        BigDecimal acrescimo,
        String observacao,
        Integer nivelAprovacao,
        UUID aprovadoPor,
        OffsetDateTime aprovadoEm,
        OffsetDateTime canceladoEm,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
