package br.com.invplus.modules.financeiro.importacao.dto;

import br.com.invplus.modules.financeiro.importacao.enums.FormatoArquivoExtrato;
import br.com.invplus.modules.financeiro.importacao.enums.StatusImportacaoExtrato;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Visao persistida para consulta do processamento de arquivos bancarios. */
public record ImportacaoExtratoResumoResponse(
        UUID id,
        UUID contaBancariaId,
        UUID extratoBancarioId,
        String arquivoNome,
        String arquivoHash,
        FormatoArquivoExtrato formato,
        StatusImportacaoExtrato status,
        Integer quantidadeLida,
        Integer quantidadeImportada,
        Integer quantidadeIgnorada,
        String mensagemErro,
        OffsetDateTime iniciadaEm,
        OffsetDateTime finalizadaEm,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
