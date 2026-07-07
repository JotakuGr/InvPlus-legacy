package br.com.invplus.modules.compras.mapper;

import br.com.invplus.modules.compras.dto.response.EntradaMercadoriaHistoricoResponse;
import br.com.invplus.modules.compras.entity.EntradaMercadoriaHistorico;
import org.springframework.stereotype.Component;

/**
 * Converte historico da entrada para DTO de leitura.
 */
@Component
public class EntradaMercadoriaHistoricoMapper {

    public EntradaMercadoriaHistoricoResponse toResponse(EntradaMercadoriaHistorico entity) {
        return new EntradaMercadoriaHistoricoResponse(
                entity.getId(),
                entity.getEntradaMercadoria().getId(),
                entity.getStatusAnterior(),
                entity.getNovoStatus(),
                entity.getUsuarioId(),
                entity.getDataAlteracao(),
                entity.getObservacao()
        );
    }
}
