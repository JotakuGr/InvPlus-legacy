package br.com.invplus.modules.produtos.mapper;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.produtos.dto.request.ProdutoPrecoRequest;
import br.com.invplus.modules.produtos.dto.response.ProdutoPrecoResponse;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.entity.ProdutoPreco;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Mapper de ProdutoPreco.
 */
@Component
public class ProdutoPrecoMapper implements CrudMapper<ProdutoPreco, ProdutoPrecoRequest, ProdutoPrecoResponse> {

    @Override
    public ProdutoPreco novaEntidade() {
        return new ProdutoPreco();
    }

    public ProdutoPreco toEntity(ProdutoPrecoRequest request) {
        ProdutoPreco entity = novaEntidade();
        updateEntity(entity, request);
        return entity;
    }

    @Override
    public ProdutoPrecoResponse toResponse(ProdutoPreco entity) {
        Produto produto = entity.getProduto();
        Empresa empresa = entity.getEmpresa();
        return new ProdutoPrecoResponse(
                entity.getId(),
                produto.getId(),
                produto.getCodigo(),
                produto.getNome(),
                empresa == null ? null : empresa.getId(),
                empresa == null ? null : empresa.getRazaoSocial(),
                entity.getTipoPreco(),
                entity.getPreco(),
                entity.getVigenciaInicio(),
                entity.getVigenciaFim(),
                entity.getPrincipal(),
                entity.getObservacao(),
                entity.getAtivo(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }

    @Override
    public void aplicar(ProdutoPreco entity, ProdutoPrecoRequest request) {
        updateEntity(entity, request);
    }

    /**
     * Atualiza apenas dados de preco; relacionamentos sao resolvidos no service.
     */
    public void updateEntity(ProdutoPreco entity, ProdutoPrecoRequest request) {
        entity.atualizar(
                request.tipoPreco(),
                request.preco(),
                request.vigenciaInicio(),
                request.vigenciaFim(),
                request.principal(),
                request.observacao()
        );
    }
}
