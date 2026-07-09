package br.com.invplus.modules.pagamentos.mapper;

import br.com.invplus.modules.pagamentos.dto.request.FormaPagamentoRequest;
import br.com.invplus.modules.pagamentos.dto.response.FormaPagamentoResponse;
import br.com.invplus.modules.pagamentos.entity.FormaPagamento;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Converte FormaPagamento entre entidade JPA e DTOs da API.
 */
@Component
public class FormaPagamentoMapper implements CrudMapper<FormaPagamento, FormaPagamentoRequest, FormaPagamentoResponse> {

    @Override
    public FormaPagamento novaEntidade() {
        return new FormaPagamento();
    }

    @Override
    public void aplicar(FormaPagamento entity, FormaPagamentoRequest request) {
        entity.atualizar(
                request.codigo(),
                request.nome(),
                request.tipo(),
                request.permiteTroco(),
                request.permiteParcelamento()
        );
    }

    @Override
    public FormaPagamentoResponse toResponse(FormaPagamento entity) {
        return new FormaPagamentoResponse(
                entity.getId(),
                entity.getCodigo(),
                entity.getNome(),
                entity.getTipo(),
                entity.getPermiteTroco(),
                entity.getPermiteParcelamento(),
                entity.getAtivo(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }
}
