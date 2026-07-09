package br.com.invplus.modules.financeiro.mapper;

import br.com.invplus.modules.financeiro.dto.request.RecebimentoRequest;
import br.com.invplus.modules.financeiro.dto.response.RecebimentoResponse;
import br.com.invplus.modules.financeiro.entity.Recebimento;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Converte Recebimento entre entidade e DTO sem acessar repositories.
 */
@Component
public class RecebimentoMapper implements CrudMapper<Recebimento, RecebimentoRequest, RecebimentoResponse> {

    @Override
    public Recebimento novaEntidade() {
        return new Recebimento();
    }

    @Override
    public void aplicar(Recebimento entity, RecebimentoRequest request) {
        throw new UnsupportedOperationException("RecebimentoService resolve ParcelaReceber e FormaPagamento antes de aplicar o request");
    }

    @Override
    public RecebimentoResponse toResponse(Recebimento entity) {
        return new RecebimentoResponse(
                entity.getId(),
                entity.getParcelaReceber().getId(),
                entity.getContaReceber().getId(),
                entity.getFormaPagamento().getId(),
                entity.getFormaPagamento().getNome(),
                entity.getEmpresa().getId(),
                entity.getFilial() == null ? null : entity.getFilial().getId(),
                entity.getCliente().getId(),
                entity.getCliente().getPessoa().getNome(),
                entity.getValorRecebido(),
                entity.getJuros(),
                entity.getMulta(),
                entity.getDesconto(),
                entity.getValorLiquido(),
                entity.getDataRecebimento(),
                entity.getStatus(),
                entity.getObservacao(),
                entity.getAtivo(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }
}
