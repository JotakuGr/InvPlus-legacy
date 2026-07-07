package br.com.invplus.modules.estoque.mapper;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.modules.estoque.dto.request.ReservaEstoqueRequest;
import br.com.invplus.modules.estoque.dto.response.ReservaEstoqueResponse;
import br.com.invplus.modules.estoque.entity.Deposito;
import br.com.invplus.modules.estoque.entity.LocalizacaoEstoque;
import br.com.invplus.modules.estoque.entity.ReservaEstoque;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Converte ReservaEstoque entre entidade e DTOs.
 */
@Component
public class ReservaEstoqueMapper implements CrudMapper<ReservaEstoque, ReservaEstoqueRequest, ReservaEstoqueResponse> {

    @Override
    public ReservaEstoque novaEntidade() {
        return new ReservaEstoque();
    }

    public ReservaEstoque toEntity(ReservaEstoqueRequest request) {
        ReservaEstoque entity = novaEntidade();
        updateEntity(entity, request);
        return entity;
    }

    @Override
    public ReservaEstoqueResponse toResponse(ReservaEstoque entity) {
        Produto produto = entity.getProduto();
        Empresa empresa = entity.getEmpresa();
        Filial filial = entity.getFilial();
        Deposito deposito = entity.getDeposito();
        LocalizacaoEstoque localizacao = entity.getLocalizacao();
        return new ReservaEstoqueResponse(
                entity.getId(),
                entity.getEstoque().getId(),
                produto.getId(),
                produto.getCodigo(),
                produto.getNome(),
                empresa.getId(),
                empresa.getRazaoSocial(),
                filial == null ? null : filial.getId(),
                filial == null ? null : filial.getNome(),
                deposito.getId(),
                deposito.getNome(),
                localizacao.getId(),
                localizacao.getCodigo(),
                entity.getQuantidade(),
                entity.getStatus(),
                entity.getOrigem(),
                entity.getOrigemId(),
                entity.getExpiraEm(),
                entity.getBaixadaEm(),
                entity.getCanceladaEm(),
                entity.getObservacao(),
                entity.getAtivo(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }

    @Override
    public void aplicar(ReservaEstoque entity, ReservaEstoqueRequest request) {
        updateEntity(entity, request);
    }

    /**
     * Atualiza somente os dados simples; o estoque e sua fotografia sao resolvidos no service.
     */
    public void updateEntity(ReservaEstoque entity, ReservaEstoqueRequest request) {
        entity.atualizarDados(
                request.quantidade(),
                request.origem(),
                request.origemId(),
                request.expiraEm(),
                request.observacao()
        );
    }
}
