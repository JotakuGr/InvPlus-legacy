package br.com.invplus.modules.compras.dto.response;

import br.com.invplus.modules.compras.enums.StatusPedidoCompra;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Representacao de pedido de compra retornada pela API.
 */
public record PedidoCompraResponse(
        UUID id,
        UUID empresaId,
        String empresaNome,
        UUID filialId,
        String filialNome,
        UUID fornecedorId,
        String fornecedorNome,
        String fornecedorCpfCnpj,
        String numero,
        StatusPedidoCompra status,
        LocalDate dataEmissao,
        LocalDate dataPrevistaEntrega,
        String observacao,
        BigDecimal valorTotal,
        Integer nivelAprovacao,
        UUID aprovadoPor,
        OffsetDateTime aprovadoEm,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
