package br.com.invplus.modules.estoque.mapper;

import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.modules.estoque.dto.request.MovimentacaoEstoqueRequest;
import br.com.invplus.modules.estoque.dto.response.MovimentacaoEstoqueResponse;
import br.com.invplus.modules.estoque.entity.MovimentacaoEstoque;
import org.springframework.stereotype.Component;

/**
 * Converte movimentacoes de estoque entre entidade e DTOs.
 */
@Component
public class MovimentacaoEstoqueMapper {

    public MovimentacaoEstoque toEntity(MovimentacaoEstoqueRequest request) {
        MovimentacaoEstoque entity = new MovimentacaoEstoque();
        entity.setTipoMovimentacao(request.tipoMovimentacao());
        entity.setQuantidade(request.quantidade());
        entity.setSaldoAnterior(request.saldoAnterior());
        entity.setSaldoPosterior(request.saldoPosterior());
        entity.setOrigem(request.origem());
        entity.setOrigemId(request.origemId());
        entity.setDocumento(normalizarTexto(request.documento()));
        entity.setObservacao(normalizarTexto(request.observacao()));
        entity.setDataMovimentacao(request.dataMovimentacao());
        return entity;
    }

    public MovimentacaoEstoqueResponse toResponse(MovimentacaoEstoque entity) {
        Filial filial = entity.getFilial();
        return new MovimentacaoEstoqueResponse(
                entity.getId(),
                entity.getEstoque().getId(),
                entity.getProduto().getId(),
                entity.getProduto().getCodigo(),
                entity.getProduto().getNome(),
                entity.getEmpresa().getId(),
                entity.getEmpresa().getRazaoSocial(),
                filial == null ? null : filial.getId(),
                filial == null ? null : filial.getNome(),
                entity.getDeposito().getId(),
                entity.getDeposito().getCodigo(),
                entity.getDeposito().getNome(),
                entity.getLocalizacao().getId(),
                entity.getLocalizacao().getCodigo(),
                entity.getTipoMovimentacao(),
                entity.getQuantidade(),
                entity.getSaldoAnterior(),
                entity.getSaldoPosterior(),
                entity.getOrigem(),
                entity.getOrigemId(),
                entity.getDocumento(),
                entity.getObservacao(),
                entity.getDataMovimentacao(),
                entity.getCriadoEm(),
                entity.getCriadoPor()
        );
    }

    private String normalizarTexto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
