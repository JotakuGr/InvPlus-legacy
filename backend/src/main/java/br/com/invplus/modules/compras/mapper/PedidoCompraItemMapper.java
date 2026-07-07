package br.com.invplus.modules.compras.mapper;

import br.com.invplus.modules.compras.dto.request.PedidoCompraItemRequest;
import br.com.invplus.modules.compras.dto.response.PedidoCompraItemResponse;
import br.com.invplus.modules.compras.entity.PedidoCompraItem;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Mapper de itens de pedido de compra.
 */
@Component
public class PedidoCompraItemMapper implements CrudMapper<PedidoCompraItem, PedidoCompraItemRequest, PedidoCompraItemResponse> {

    @Override
    public PedidoCompraItem novaEntidade() {
        return new PedidoCompraItem();
    }

    @Override
    public void aplicar(PedidoCompraItem entity, PedidoCompraItemRequest request) {
        entity.atualizar(request.quantidade(), request.valorUnitario(), request.observacao());
    }

    public PedidoCompraItem toEntity(PedidoCompraItemRequest request) {
        PedidoCompraItem entity = novaEntidade();
        aplicar(entity, request);
        return entity;
    }

    public void updateEntity(PedidoCompraItem entity, PedidoCompraItemRequest request) {
        aplicar(entity, request);
    }

    @Override
    public PedidoCompraItemResponse toResponse(PedidoCompraItem entity) {
        return new PedidoCompraItemResponse(
                entity.getId(),
                entity.getPedidoCompra().getId(),
                entity.getProduto().getId(),
                entity.getProduto().getCodigo(),
                entity.getProduto().getNome(),
                entity.getProdutoUnidade() == null ? null : entity.getProdutoUnidade().getId(),
                entity.getProdutoUnidade() == null ? null : entity.getProdutoUnidade().getUnidadeMedida().getSigla(),
                entity.getQuantidade(),
                entity.getValorUnitario(),
                entity.getValorTotal(),
                entity.getObservacao(),
                entity.getAtivo(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }
}
