package br.com.invplus.modules.financeiro.mapper;

import br.com.invplus.modules.financeiro.dto.request.PagamentoRequest;
import br.com.invplus.modules.financeiro.dto.response.PagamentoResponse;
import br.com.invplus.modules.financeiro.entity.Pagamento;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Converte Pagamento entre entidade e DTO sem acessar repositories.
 */
@Component
public class PagamentoMapper implements CrudMapper<Pagamento, PagamentoRequest, PagamentoResponse> {

    @Override
    public Pagamento novaEntidade() {
        return new Pagamento();
    }

    @Override
    public void aplicar(Pagamento entity, PagamentoRequest request) {
        throw new UnsupportedOperationException("PagamentoService resolve ParcelaPagar e FormaPagamento antes de aplicar o request");
    }

    @Override
    public PagamentoResponse toResponse(Pagamento entity) {
        return new PagamentoResponse(
                entity.getId(),
                entity.getParcelaPagar().getId(),
                entity.getContaPagar().getId(),
                entity.getFormaPagamento().getId(),
                entity.getFormaPagamento().getNome(),
                entity.getEmpresa().getId(),
                entity.getEmpresa().getNomeFantasia(),
                entity.getFilial() == null ? null : entity.getFilial().getId(),
                entity.getFilial() == null ? null : entity.getFilial().getNome(),
                entity.getFornecedor().getId(),
                entity.getFornecedor().getPessoa().getNome(),
                entity.getValorPago(),
                entity.getJuros(),
                entity.getMulta(),
                entity.getDesconto(),
                entity.getValorLiquido(),
                entity.getDataPagamento(),
                entity.getStatus(),
                entity.getObservacao(),
                entity.getAtivo(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }
}
