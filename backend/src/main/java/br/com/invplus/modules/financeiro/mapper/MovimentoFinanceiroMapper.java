package br.com.invplus.modules.financeiro.mapper;

import br.com.invplus.modules.financeiro.dto.request.MovimentoFinanceiroRequest;
import br.com.invplus.modules.financeiro.dto.response.MovimentoFinanceiroResponse;
import br.com.invplus.modules.financeiro.entity.MovimentoFinanceiro;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Converte movimentos financeiros sem realizar consultas ao banco.
 */
@Component
public class MovimentoFinanceiroMapper
        implements CrudMapper<MovimentoFinanceiro, MovimentoFinanceiroRequest, MovimentoFinanceiroResponse> {

    @Override
    public MovimentoFinanceiro novaEntidade() {
        return new MovimentoFinanceiro();
    }

    @Override
    public void aplicar(MovimentoFinanceiro entity, MovimentoFinanceiroRequest request) {
        throw new UnsupportedOperationException("MovimentoFinanceiroService resolve os relacionamentos do movimento");
    }

    @Override
    public MovimentoFinanceiroResponse toResponse(MovimentoFinanceiro entity) {
        return new MovimentoFinanceiroResponse(
                entity.getId(),
                entity.getEmpresa().getId(),
                entity.getEmpresa().getNomeFantasia(),
                entity.getFilial() == null ? null : entity.getFilial().getId(),
                entity.getFilial() == null ? null : entity.getFilial().getNome(),
                entity.getTipoMovimento(),
                entity.getOrigem(),
                entity.getOrigemId(),
                entity.getFormaPagamento().getId(),
                entity.getFormaPagamento().getNome(),
                entity.getCliente() == null ? null : entity.getCliente().getId(),
                entity.getCliente() == null ? null : entity.getCliente().getPessoa().getNome(),
                entity.getFornecedor() == null ? null : entity.getFornecedor().getId(),
                entity.getFornecedor() == null ? null : entity.getFornecedor().getPessoa().getNome(),
                entity.getCentroCusto() == null ? null : entity.getCentroCusto().getId(),
                entity.getCentroCusto() == null ? null : entity.getCentroCusto().getNome(),
                entity.getCentroCusto() == null ? null : entity.getCentroCusto().getCodigo(),
                entity.getPlanoConta() == null ? null : entity.getPlanoConta().getId(),
                entity.getPlanoConta() == null ? null : entity.getPlanoConta().getCodigo(),
                entity.getPlanoConta() == null ? null : entity.getPlanoConta().getNome(),
                entity.getPlanoConta() == null ? null : entity.getPlanoConta().getNatureza(),
                entity.getValor(),
                entity.getDataMovimento(),
                entity.getDescricao(),
                entity.getStatus(),
                entity.getObservacao(),
                entity.getAtivo(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }
}
