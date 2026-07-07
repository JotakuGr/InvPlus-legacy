package br.com.invplus.modules.compras.mapper;

import br.com.invplus.modules.compras.dto.request.EntradaMercadoriaItemRequest;
import br.com.invplus.modules.compras.dto.response.EntradaMercadoriaItemResponse;
import br.com.invplus.modules.compras.entity.EntradaMercadoriaItem;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Mapper dos itens recebidos na entrada de mercadoria.
 */
@Component
public class EntradaMercadoriaItemMapper implements CrudMapper<EntradaMercadoriaItem, EntradaMercadoriaItemRequest, EntradaMercadoriaItemResponse> {

    @Override
    public EntradaMercadoriaItem novaEntidade() {
        return new EntradaMercadoriaItem();
    }

    @Override
    public void aplicar(EntradaMercadoriaItem entity, EntradaMercadoriaItemRequest request) {
        entity.atualizar(request.quantidade(), request.valorUnitario(), request.observacao());
    }

    public EntradaMercadoriaItem toEntity(EntradaMercadoriaItemRequest request) {
        EntradaMercadoriaItem entity = novaEntidade();
        aplicar(entity, request);
        return entity;
    }

    public void updateEntity(EntradaMercadoriaItem entity, EntradaMercadoriaItemRequest request) {
        aplicar(entity, request);
    }

    @Override
    public EntradaMercadoriaItemResponse toResponse(EntradaMercadoriaItem entity) {
        return new EntradaMercadoriaItemResponse(
                entity.getId(),
                entity.getEntradaMercadoria().getId(),
                entity.getPedidoCompraItem() == null ? null : entity.getPedidoCompraItem().getId(),
                entity.getProduto().getId(),
                entity.getProduto().getCodigo(),
                entity.getProduto().getNome(),
                entity.getProdutoUnidade() == null ? null : entity.getProdutoUnidade().getId(),
                entity.getProdutoUnidade() == null ? null : entity.getProdutoUnidade().getUnidadeMedida().getSigla(),
                entity.getDeposito().getId(),
                entity.getDeposito().getNome(),
                entity.getLocalizacao().getId(),
                entity.getLocalizacao().getCodigo(),
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
