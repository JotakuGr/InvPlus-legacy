package br.com.invplus.modules.vendas.mapper;

import br.com.invplus.modules.vendas.dto.request.PedidoVendaItemRequest;
import br.com.invplus.modules.vendas.dto.response.PedidoVendaItemResponse;
import br.com.invplus.modules.vendas.entity.PedidoVendaItem;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Mapper de itens de pedido de venda sem acesso a repositories.
 */
@Component
public class PedidoVendaItemMapper implements CrudMapper<PedidoVendaItem, PedidoVendaItemRequest, PedidoVendaItemResponse> {

    @Override
    public PedidoVendaItem novaEntidade() {
        return new PedidoVendaItem();
    }

    @Override
    public void aplicar(PedidoVendaItem entity, PedidoVendaItemRequest request) {
        entity.atualizar(request.quantidade(), entity.getValorUnitario(),
                request.desconto(), request.acrescimo(), request.observacao());
    }

    public PedidoVendaItem toEntity(PedidoVendaItemRequest request) {
        PedidoVendaItem entity = novaEntidade();
        aplicar(entity, request);
        return entity;
    }

    public void updateEntity(PedidoVendaItem entity, PedidoVendaItemRequest request) {
        aplicar(entity, request);
    }

    @Override
    public PedidoVendaItemResponse toResponse(PedidoVendaItem entity) {
        return new PedidoVendaItemResponse(
                entity.getId(),
                entity.getPedidoVenda().getId(),
                entity.getProduto().getId(),
                entity.getProduto().getCodigo(),
                entity.getProduto().getNome(),
                entity.getProduto().getDescricao(),
                entity.getProdutoPreco().getId(),
                entity.getValorUnitario(),
                entity.getProdutoUnidade().getId(),
                entity.getProdutoUnidade().getUnidadeMedida().getSigla(),
                entity.getProdutoUnidade().getUnidadeMedida().getDescricao(),
                entity.getQuantidade(),
                entity.subtotal(),
                entity.getDesconto(),
                entity.getAcrescimo(),
                entity.getValorTotal(),
                entity.getObservacao(),
                entity.getAtivo(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }
}
