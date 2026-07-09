package br.com.invplus.modules.financeiro.dto.response;

import br.com.invplus.modules.financeiro.enums.TipoCentroCusto;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Representacao completa de leitura de um centro de custo.
 */
public record CentroCustoResponse(
        UUID id,
        UUID empresaId,
        String empresaNome,
        UUID centroCustoPaiId,
        String centroCustoPaiNome,
        String codigo,
        String nome,
        String descricao,
        TipoCentroCusto tipo,
        String caminhoCompleto,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
