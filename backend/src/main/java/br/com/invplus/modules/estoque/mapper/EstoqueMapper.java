package br.com.invplus.modules.estoque.mapper;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.modules.estoque.dto.request.EstoqueRequest;
import br.com.invplus.modules.estoque.dto.response.EstoqueResponse;
import br.com.invplus.modules.estoque.entity.Deposito;
import br.com.invplus.modules.estoque.entity.Estoque;
import br.com.invplus.modules.estoque.entity.LocalizacaoEstoque;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Converte Estoque entre entidade e DTOs.
 */
@Component
public class EstoqueMapper implements CrudMapper<Estoque, EstoqueRequest, EstoqueResponse> {

    @Override
    public Estoque novaEntidade() {
        return new Estoque();
    }

    public Estoque toEntity(EstoqueRequest request) {
        Estoque entity = novaEntidade();
        updateEntity(entity, request);
        return entity;
    }

    @Override
    public EstoqueResponse toResponse(Estoque entity) {
        Produto produto = entity.getProduto();
        Empresa empresa = entity.getEmpresa();
        Filial filial = entity.getFilial();
        Deposito deposito = entity.getDeposito();
        LocalizacaoEstoque localizacao = entity.getLocalizacao();
        return new EstoqueResponse(
                entity.getId(),
                produto.getId(),
                produto.getCodigo(),
                produto.getNome(),
                empresa.getId(),
                empresa.getRazaoSocial(),
                filial == null ? null : filial.getId(),
                filial == null ? null : filial.getNome(),
                deposito.getId(),
                deposito.getCodigo(),
                deposito.getNome(),
                localizacao.getId(),
                localizacao.getCodigo(),
                localizacao.getDescricao(),
                entity.getQuantidade(),
                entity.getQuantidadeReservada(),
                entity.getQuantidadeDisponivel(),
                entity.getQuantidadeAvariada(),
                entity.getQuantidadeTransito(),
                entity.getEstoqueMinimo(),
                entity.getEstoqueMaximo(),
                entity.getUltimoMovimentoEm(),
                entity.getUltimoMovimentoId(),
                entity.getAtivo(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }

    @Override
    public void aplicar(Estoque entity, EstoqueRequest request) {
        updateEntity(entity, request);
    }

    /**
     * Atualiza saldos simples; relacionamentos sao resolvidos no service.
     */
    public void updateEntity(Estoque entity, EstoqueRequest request) {
        entity.atualizarSaldos(
                request.quantidade(),
                request.quantidadeReservada(),
                request.quantidadeAvariada(),
                request.quantidadeTransito(),
                request.estoqueMinimo(),
                request.estoqueMaximo(),
                request.ultimoMovimentoEm(),
                request.ultimoMovimentoId()
        );
    }
}
