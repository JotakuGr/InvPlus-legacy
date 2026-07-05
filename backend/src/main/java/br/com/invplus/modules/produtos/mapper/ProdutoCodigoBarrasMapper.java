package br.com.invplus.modules.produtos.mapper;

import br.com.invplus.modules.produtos.dto.request.ProdutoCodigoBarrasRequest;
import br.com.invplus.modules.produtos.dto.response.ProdutoCodigoBarrasResponse;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.entity.ProdutoCodigoBarras;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Converte ProdutoCodigoBarras entre entidade JPA e DTOs sem acessar repositories.
 */
@Component
public class ProdutoCodigoBarrasMapper implements CrudMapper<ProdutoCodigoBarras, ProdutoCodigoBarrasRequest, ProdutoCodigoBarrasResponse> {

    @Override
    public ProdutoCodigoBarras novaEntidade() {
        return new ProdutoCodigoBarras();
    }

    /**
     * Cria entidade sem relacionamento; o service associa Produto.
     */
    public ProdutoCodigoBarras toEntity(ProdutoCodigoBarrasRequest request) {
        ProdutoCodigoBarras entity = novaEntidade();
        updateEntity(entity, request);
        return entity;
    }

    @Override
    public ProdutoCodigoBarrasResponse toResponse(ProdutoCodigoBarras entity) {
        Produto produto = entity.getProduto();
        return new ProdutoCodigoBarrasResponse(
                entity.getId(),
                produto.getId(),
                produto.getCodigo(),
                produto.getNome(),
                entity.getTipoCodigo(),
                entity.getCodigo(),
                entity.getPrincipal(),
                entity.getObservacao(),
                entity.getAtivo(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }

    @Override
    public void aplicar(ProdutoCodigoBarras entity, ProdutoCodigoBarrasRequest request) {
        updateEntity(entity, request);
    }

    /**
     * Atualiza somente campos do codigo alternativo.
     */
    public void updateEntity(ProdutoCodigoBarras entity, ProdutoCodigoBarrasRequest request) {
        entity.atualizar(request.tipoCodigo(), request.codigo(), request.principal(), request.observacao());
    }
}
