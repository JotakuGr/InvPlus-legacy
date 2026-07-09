package br.com.invplus.modules.vendas.mapper;

import br.com.invplus.modules.vendas.dto.request.VendaFaturamentoRequest;
import br.com.invplus.modules.vendas.dto.response.VendaFaturamentoResponse;
import br.com.invplus.modules.vendas.entity.VendaFaturamento;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Converte VendaFaturamento entre entidade JPA, request e response.
 */
@Component
public class VendaFaturamentoMapper implements CrudMapper<VendaFaturamento, VendaFaturamentoRequest, VendaFaturamentoResponse> {

    @Override
    public VendaFaturamento novaEntidade() {
        return new VendaFaturamento();
    }

    @Override
    public void aplicar(VendaFaturamento entity, VendaFaturamentoRequest request) {
        entity.setNumero(request.numero() == null ? null : request.numero().trim());
        entity.setObservacao(request.observacao());
    }

    @Override
    public VendaFaturamentoResponse toResponse(VendaFaturamento entity) {
        return new VendaFaturamentoResponse(
                entity.getId(),
                entity.getPedidoVenda().getId(),
                entity.getEmpresa().getId(),
                entity.getEmpresa().getNomeFantasia(),
                entity.getFilial() == null ? null : entity.getFilial().getId(),
                entity.getFilial() == null ? null : entity.getFilial().getNome(),
                entity.getCliente().getId(),
                entity.getCliente().getPessoa().getNome(),
                entity.getNumero(),
                entity.getStatus(),
                entity.getDataFaturamento(),
                entity.getValorTotal(),
                entity.getContaReceber() == null ? null : entity.getContaReceber().getId(),
                entity.getContaReceber() == null ? null : entity.getContaReceber().getNumero(),
                entity.getObservacao(),
                entity.getAtivo(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }
}
