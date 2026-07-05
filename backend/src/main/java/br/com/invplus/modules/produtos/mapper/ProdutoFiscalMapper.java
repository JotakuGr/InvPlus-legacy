package br.com.invplus.modules.produtos.mapper;

import br.com.invplus.modules.produtos.dto.request.ProdutoFiscalRequest;
import br.com.invplus.modules.produtos.dto.response.ProdutoFiscalResponse;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.entity.ProdutoFiscal;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Mapper dos dados fiscais de Produto; relacionamentos sao resolvidos pelo service.
 */
@Component
public class ProdutoFiscalMapper implements CrudMapper<ProdutoFiscal, ProdutoFiscalRequest, ProdutoFiscalResponse> {

    @Override
    public ProdutoFiscal novaEntidade() {
        return new ProdutoFiscal();
    }

    public ProdutoFiscal toEntity(ProdutoFiscalRequest request) {
        ProdutoFiscal entity = novaEntidade();
        updateEntity(entity, request);
        return entity;
    }

    @Override
    public ProdutoFiscalResponse toResponse(ProdutoFiscal entity) {
        Produto produto = entity.getProduto();
        return new ProdutoFiscalResponse(
                entity.getId(),
                produto.getId(),
                produto.getCodigo(),
                produto.getNome(),
                entity.getNcm(),
                entity.getCest(),
                entity.getOrigemMercadoria(),
                entity.getCfopPadrao(),
                entity.getCstIcms(),
                entity.getCsosn(),
                entity.getCstPis(),
                entity.getCstCofins(),
                entity.getAliquotaIcms(),
                entity.getAliquotaIpi(),
                entity.getAliquotaPis(),
                entity.getAliquotaCofins(),
                entity.getObservacao(),
                entity.getAtivo(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }

    @Override
    public void aplicar(ProdutoFiscal entity, ProdutoFiscalRequest request) {
        updateEntity(entity, request);
    }

    /**
     * Atualiza apenas os dados fiscais; a associacao com Produto fica no service.
     */
    public void updateEntity(ProdutoFiscal entity, ProdutoFiscalRequest request) {
        entity.atualizar(
                request.ncm(),
                request.cest(),
                request.origemMercadoria(),
                request.cfopPadrao(),
                request.cstIcms(),
                request.csosn(),
                request.cstPis(),
                request.cstCofins(),
                request.aliquotaIcms(),
                request.aliquotaIpi(),
                request.aliquotaPis(),
                request.aliquotaCofins(),
                request.observacao()
        );
    }
}
