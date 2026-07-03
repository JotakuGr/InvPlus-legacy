package br.com.invplus.modules.pessoas.dto.response;

import br.com.invplus.modules.pessoas.enums.TipoDocumentoPessoa;
import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Dados retornados pela API para documentos de Pessoa.
 */
public record DocumentoPessoaResponse(
        UUID id,
        UUID pessoaId,
        String nomePessoa,
        String cpfCnpj,
        UUID arquivoId,
        String nomeArquivo,
        TipoDocumentoPessoa tipoDocumento,
        String numero,
        String orgaoEmissor,
        LocalDate dataEmissao,
        LocalDate dataValidade,
        String observacao,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
