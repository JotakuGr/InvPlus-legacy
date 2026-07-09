package br.com.invplus.modules.financeiro.mapper;

import br.com.invplus.modules.financeiro.dto.request.ParcelaReceberRequest;
import br.com.invplus.modules.financeiro.dto.response.ParcelaReceberResponse;
import br.com.invplus.modules.financeiro.entity.ParcelaReceber;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Converte ParcelaReceber entre entidade e DTO.
 */
@Component
public class ParcelaReceberMapper implements CrudMapper<ParcelaReceber, ParcelaReceberRequest, ParcelaReceberResponse> {

    @Override
    public ParcelaReceber novaEntidade() {
        return new ParcelaReceber();
    }

    @Override
    public void aplicar(ParcelaReceber entity, ParcelaReceberRequest request) {
        throw new UnsupportedOperationException("ParcelaReceberService resolve a ContaReceber antes de aplicar o request");
    }

    @Override
    public ParcelaReceberResponse toResponse(ParcelaReceber entity) {
        return new ParcelaReceberResponse(
                entity.getId(),
                entity.getContaReceber().getId(),
                entity.getContaReceber().getNumero(),
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
