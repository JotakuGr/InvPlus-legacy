package br.com.invplus.modules.cadastros.mapper;

import br.com.invplus.modules.cadastros.dto.response.CidadeResponse;
import br.com.invplus.modules.cadastros.entity.Cidade;
import org.springframework.stereotype.Component;

/**
 * Conversoes entre entidade Cidade e DTOs da API.
 */
@Component
public class CidadeMapper {

    /**
     * Monta a resposta publica da cidade.
     */
    public CidadeResponse toResponse(Cidade cidade) {
        return new CidadeResponse(cidade.getId(), cidade.getEstado().getId(), cidade.getEstado().getNome(),
                cidade.getEstado().getUf(), cidade.getNome(), cidade.getCodigoIbge(), cidade.getAtivo(),
                cidade.getVersion(), cidade.getCriadoEm(), cidade.getAtualizadoEm());
    }
}
