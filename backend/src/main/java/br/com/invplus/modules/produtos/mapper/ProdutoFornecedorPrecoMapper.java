package br.com.invplus.modules.produtos.mapper;

import br.com.invplus.modules.pessoas.entity.Fornecedor;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.produtos.dto.request.ProdutoFornecedorPrecoRequest;
import br.com.invplus.modules.produtos.dto.response.ProdutoFornecedorPrecoResponse;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.entity.ProdutoFornecedor;
import br.com.invplus.modules.produtos.entity.ProdutoFornecedorPreco;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Mapper do historico de custo de ProdutoFornecedor.
 */
@Component
public class ProdutoFornecedorPrecoMapper implements CrudMapper<ProdutoFornecedorPreco, ProdutoFornecedorPrecoRequest, ProdutoFornecedorPrecoResponse> {

    @Override
    public ProdutoFornecedorPreco novaEntidade() {
        return new ProdutoFornecedorPreco();
    }

    public ProdutoFornecedorPreco toEntity(ProdutoFornecedorPrecoRequest request) {
        ProdutoFornecedorPreco entity = novaEntidade();
        updateEntity(entity, request);
        return entity;
    }

    @Override
    public ProdutoFornecedorPrecoResponse toResponse(ProdutoFornecedorPreco entity) {
        ProdutoFornecedor vinculo = entity.getProdutoFornecedor();
        Produto produto = vinculo.getProduto();
        Fornecedor fornecedor = vinculo.getFornecedor();
        Pessoa pessoa = fornecedor.getPessoa();
        return new ProdutoFornecedorPrecoResponse(
                entity.getId(),
                vinculo.getId(),
                produto.getId(),
                produto.getCodigo(),
                produto.getNome(),
                fornecedor.getId(),
                pessoa.getNome(),
                entity.getPreco(),
                entity.getVigenciaInicio(),
                entity.getVigenciaFim(),
                entity.getAtivo(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }

    @Override
    public void aplicar(ProdutoFornecedorPreco entity, ProdutoFornecedorPrecoRequest request) {
        updateEntity(entity, request);
    }

    /**
     * Atualiza somente preco e vigencia; o service associa o vinculo.
     */
    public void updateEntity(ProdutoFornecedorPreco entity, ProdutoFornecedorPrecoRequest request) {
        entity.atualizar(request.preco(), request.vigenciaInicio(), request.vigenciaFim());
    }
}
