package br.com.invplus.modules.compras.dto.response;

import br.com.invplus.modules.compras.enums.StatusEntradaMercadoria;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entrada de mercadoria retornada pela API.
 */
public record EntradaMercadoriaResponse(
        UUID id,
        UUID empresaId,
        String empresaNome,
        UUID filialId,
        String filialNome,
        UUID fornecedorId,
        String fornecedorNome,
        UUID pedidoCompraId,
        String numero,
        StatusEntradaMercadoria status,
        LocalDate dataEntrada,
        String observacao,
        BigDecimal valorTotal,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
