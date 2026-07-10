package br.com.invplus.modules.financeiro.importacao.dto;

import br.com.invplus.modules.financeiro.importacao.enums.StatusImportacaoExtrato;
import java.util.List;
import java.util.UUID;

/** Resultado detalhado devolvido imediatamente apos a importacao. */
public record ImportacaoExtratoResponse(
        UUID importacaoId,
        UUID extratoBancarioId,
        UUID contaBancariaId,
        String arquivoNome,
        String arquivoHash,
        StatusImportacaoExtrato status,
        Integer quantidadeLida,
        Integer quantidadeImportada,
        Integer quantidadeIgnorada,
        List<String> alertas
) {
}
