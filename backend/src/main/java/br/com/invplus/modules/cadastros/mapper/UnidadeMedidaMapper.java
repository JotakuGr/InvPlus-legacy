package br.com.invplus.modules.cadastros.mapper;

import br.com.invplus.modules.cadastros.dto.request.UnidadeMedidaRequest;
import br.com.invplus.modules.cadastros.dto.response.UnidadeMedidaResponse;
import br.com.invplus.modules.cadastros.entity.UnidadeMedida;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Converte dados de unidade de medida entre API e entidade JPA.
 */
@Component
public class UnidadeMedidaMapper implements CrudMapper<UnidadeMedida, UnidadeMedidaRequest, UnidadeMedidaResponse> {

    /**
     * Cria uma entidade vazia para o fluxo de cadastro.
     */
    @Override
    public UnidadeMedida novaEntidade() {
        return new UnidadeMedida();
    }

    /**
     * Aplica o payload normalizado na entidade.
     */
    @Override
    public void aplicar(UnidadeMedida unidade, UnidadeMedidaRequest request) {
        unidade.atualizar(request.sigla(), request.descricao(), request.casasDecimais());
    }

    /**
     * Converte a entidade para o contrato publico da API.
     */
    @Override
    public UnidadeMedidaResponse toResponse(UnidadeMedida unidade) {
        return new UnidadeMedidaResponse(
                unidade.getId(),
                unidade.getSigla(),
                unidade.getDescricao(),
                unidade.getCasasDecimais(),
                unidade.getAtivo(),
                unidade.getVersion(),
                unidade.getCriadoEm(),
                unidade.getAtualizadoEm()
        );
    }
}
