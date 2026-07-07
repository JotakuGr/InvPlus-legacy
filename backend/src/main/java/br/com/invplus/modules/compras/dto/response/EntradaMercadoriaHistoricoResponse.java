package br.com.invplus.modules.compras.dto.response;

import br.com.invplus.modules.compras.enums.StatusEntradaMercadoria;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Linha de historico de status da entrada de mercadoria.
 */
public record EntradaMercadoriaHistoricoResponse(
        UUID id,
        UUID entradaMercadoriaId,
        StatusEntradaMercadoria statusAnterior,
        StatusEntradaMercadoria novoStatus,
        UUID usuarioId,
        OffsetDateTime dataAlteracao,
        String observacao
) {
}
