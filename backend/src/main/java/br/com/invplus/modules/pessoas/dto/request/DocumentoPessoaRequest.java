package br.com.invplus.modules.pessoas.dto.request;

import br.com.invplus.modules.pessoas.enums.TipoDocumentoPessoa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Dados de entrada para criar ou atualizar documento de Pessoa.
 */
public record DocumentoPessoaRequest(
        @NotNull UUID pessoaId,
        UUID arquivoId,
        @NotNull TipoDocumentoPessoa tipoDocumento,
        @NotBlank String numero,
        String orgaoEmissor,
        LocalDate dataEmissao,
        LocalDate dataValidade,
        String observacao
) {
}
