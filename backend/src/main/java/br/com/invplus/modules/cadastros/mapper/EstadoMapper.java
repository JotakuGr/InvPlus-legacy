package br.com.invplus.modules.cadastros.mapper;

import br.com.invplus.modules.cadastros.dto.response.EstadoResponse;
import br.com.invplus.modules.cadastros.entity.Estado;
import org.springframework.stereotype.Component;

/**
 * Conversoes entre entidade Estado e DTOs da API.
 */
@Component
public class EstadoMapper {

    /**
     * Monta a resposta publica do estado.
     */
    public EstadoResponse toResponse(Estado estado) {
        return new EstadoResponse(estado.getId(), estado.getPais().getId(), estado.getPais().getNome(),
                estado.getNome(), estado.getUf(), estado.getCodigoIbge(), estado.getAtivo(), estado.getVersion(),
                estado.getCriadoEm(), estado.getAtualizadoEm());
    }
}
