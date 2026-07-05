package br.com.invplus.modules.produtos.mapper;

import br.com.invplus.modules.pessoas.entity.Fornecedor;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.produtos.dto.request.ProdutoFornecedorRequest;
import br.com.invplus.modules.produtos.dto.response.ProdutoFornecedorResponse;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.entity.ProdutoFornecedor;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Converte ProdutoFornecedor entre entidade JPA e DTOs sem acessar repositories.
 */
@Component
public class ProdutoFornecedorMapper implements CrudMapper<ProdutoFornecedor, ProdutoFornecedorRequest, ProdutoFornecedorResponse> {

    @Override
    public ProdutoFornecedor novaEntidade() {
        return new ProdutoFornecedor();
    }

    /**
     * Cria entidade sem relacionamentos; o service associa Produto e Fornecedor.
     */
    public ProdutoFornecedor toEntity(ProdutoFornecedorRequest request) {
        ProdutoFornecedor entity = novaEntidade();
        updateEntity(entity, request);
        return entity;
    }

    @Override
    public ProdutoFornecedorResponse toResponse(ProdutoFornecedor entity) {
        Produto produto = entity.getProduto();
        Fornecedor fornecedor = entity.getFornecedor();
        Pessoa pessoa = fornecedor.getPessoa();
        return new ProdutoFornecedorResponse(
                entity.getId(),
                produto.getId(),
                produto.getCodigo(),
                produto.getNome(),
                fornecedor.getId(),
                pessoa.getNome(),
                pessoa.getCpfCnpj(),
                entity.getCodigoFornecedor(),
                entity.getLeadTimeDias(),
                entity.getPrincipal(),
                entity.getPrioridade(),
                entity.getStatus(),
                entity.getObservacao(),
                entity.getAtivo(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }

    @Override
    public void aplicar(ProdutoFornecedor entity, ProdutoFornecedorRequest request) {
        updateEntity(entity, request);
    }

    /**
     * Atualiza somente os campos comerciais do vinculo.
     */
    public void updateEntity(ProdutoFornecedor entity, ProdutoFornecedorRequest request) {
        entity.atualizar(
                request.codigoFornecedor(),
                request.leadTimeDias(),
                request.principal(),
                request.prioridade(),
                request.status(),
                request.observacao()
        );
    }
}
