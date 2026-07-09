package br.com.invplus.modules.financeiro.mapper;

import br.com.invplus.modules.financeiro.dto.request.ParcelaPagarRequest;
import br.com.invplus.modules.financeiro.dto.response.ParcelaPagarResponse;
import br.com.invplus.modules.financeiro.entity.ParcelaPagar;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Converte ParcelaPagar entre entidade e DTO.
 */
@Component
public class ParcelaPagarMapper implements CrudMapper<ParcelaPagar, ParcelaPagarRequest, ParcelaPagarResponse> {

    @Override
    public ParcelaPagar novaEntidade() {
        return new ParcelaPagar();
    }

    @Override
    public void aplicar(ParcelaPagar entity, ParcelaPagarRequest request) {
        throw new UnsupportedOperationException("ParcelaPagarService resolve a ContaPagar antes de aplicar o request");
    }

    @Override
    public ParcelaPagarResponse toResponse(ParcelaPagar entity) {
        return new ParcelaPagarResponse(
                entity.getId(),
                entity.getContaPagar().getId(),
                entity.getContaPagar().getNumero(),
                entity.getNumeroParcela(),
                entity.getDataVencimento(),
                entity.getValor(),
                entity.getValorPago(),
                entity.getValorAberto(),
                entity.getJuros(),
                entity.getMulta(),
                entity.getDesconto(),
                entity.getStatus(),
                entity.getDataPagamento(),
                entity.getObservacao(),
                entity.getAtivo(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }
}
