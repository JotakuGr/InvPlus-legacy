package br.com.invplus.modules.produtos.mapper;

import br.com.invplus.modules.cadastros.entity.UnidadeMedida;
import br.com.invplus.modules.produtos.dto.request.ProdutoUnidadeRequest;
import br.com.invplus.modules.produtos.dto.response.ProdutoUnidadeResponse;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.entity.ProdutoUnidade;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Mapper de ProdutoUnidade.
 */
@Component
public class ProdutoUnidadeMapper implements CrudMapper<ProdutoUnidade, ProdutoUnidadeRequest, ProdutoUnidadeResponse> {

    @Override
    public ProdutoUnidade novaEntidade() {
        return new ProdutoUnidade();
    }

    public ProdutoUnidade toEntity(ProdutoUnidadeRequest request) {
        ProdutoUnidade entity = novaEntidade();
        updateEntity(entity, request);
        return entity;
    }

    @Override
    public ProdutoUnidadeResponse toResponse(ProdutoUnidade entity) {
        Produto produto = entity.getProduto();
        UnidadeMedida unidade = entity.getUnidadeMedida();
        return new ProdutoUnidadeResponse(
                entity.getId(),
                produto.getId(),
                produto.getCodigo(),
                produto.getNome(),
                unidade.getId(),
                unidade.getSigla(),
                unidade.getDescricao(),
                entity.getFatorConversao(),
                entity.getUnidadePadrao(),
                entity.getPermiteCompra(),
                entity.getPermiteVenda(),
                entity.getPermiteEstoque(),
                entity.getPermiteFracionamento(),
                entity.getObservacao(),
                entity.getAtivo(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }

    @Override
    public void aplicar(ProdutoUnidade entity, ProdutoUnidadeRequest request) {
        updateEntity(entity, request);
    }

    /**
     * Atualiza somente dados editaveis; relacionamentos sao resolvidos no service.
     */
    public void updateEntity(ProdutoUnidade entity, ProdutoUnidadeRequest request) {
        entity.atualizar(
                request.fatorConversao(),
                request.unidadePadrao(),
                request.permiteCompra(),
                request.permiteVenda(),
                request.permiteEstoque(),
                request.permiteFracionamento(),
                request.observacao()
        );
    }
}
