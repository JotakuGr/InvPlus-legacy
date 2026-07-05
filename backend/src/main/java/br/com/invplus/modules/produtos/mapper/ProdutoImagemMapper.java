package br.com.invplus.modules.produtos.mapper;

import br.com.invplus.modules.cadastros.entity.Arquivo;
import br.com.invplus.modules.produtos.dto.request.ProdutoImagemRequest;
import br.com.invplus.modules.produtos.dto.response.ProdutoImagemResponse;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.entity.ProdutoImagem;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Converte ProdutoImagem entre entidade JPA e DTOs sem acessar repositories.
 */
@Component
public class ProdutoImagemMapper implements CrudMapper<ProdutoImagem, ProdutoImagemRequest, ProdutoImagemResponse> {

    @Override
    public ProdutoImagem novaEntidade() {
        return new ProdutoImagem();
    }

    /**
     * Cria entidade sem relacionamentos; o service associa Produto e Arquivo.
     */
    public ProdutoImagem toEntity(ProdutoImagemRequest request) {
        ProdutoImagem entity = novaEntidade();
        updateEntity(entity, request);
        return entity;
    }

    @Override
    public ProdutoImagemResponse toResponse(ProdutoImagem entity) {
        Produto produto = entity.getProduto();
        Arquivo arquivo = entity.getArquivo();
        return new ProdutoImagemResponse(
                entity.getId(),
                produto.getId(),
                produto.getCodigo(),
                produto.getNome(),
                arquivo.getId(),
                arquivo.getNomeOriginal(),
                arquivo.getUrl(),
                arquivo.getTipoMime(),
                entity.getPrincipal(),
                entity.getOrdem(),
                entity.getDescricao(),
                entity.getAtivo(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }

    @Override
    public void aplicar(ProdutoImagem entity, ProdutoImagemRequest request) {
        updateEntity(entity, request);
    }

    /**
     * Atualiza somente campos de apresentacao da imagem.
     */
    public void updateEntity(ProdutoImagem entity, ProdutoImagemRequest request) {
        entity.atualizar(request.principal(), request.ordem(), request.descricao());
    }
}
