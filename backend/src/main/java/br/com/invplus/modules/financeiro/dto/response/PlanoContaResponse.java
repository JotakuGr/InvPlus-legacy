package br.com.invplus.modules.financeiro.dto.response;

import br.com.invplus.modules.financeiro.enums.NaturezaPlanoConta;
import br.com.invplus.modules.financeiro.enums.TipoPlanoConta;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Representacao de leitura de uma conta do plano.
 */
public record PlanoContaResponse(
        UUID id,
        UUID empresaId,
        String empresaNome,
        UUID planoContaPaiId,
        String planoContaPaiNome,
        String codigo,
        String nome,
        String descricao,
        TipoPlanoConta tipo,
        NaturezaPlanoConta natureza,
        String caminhoCompleto,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
