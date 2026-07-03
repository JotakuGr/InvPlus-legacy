package br.com.invplus.modules.pessoas.dto.response;

import br.com.invplus.common.enums.TipoPessoa;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Resposta publica do cadastro de pessoa.
 */
public record PessoaResponse(
        UUID id,
        TipoPessoa tipoPessoa,
        String nome,
        String nomeFantasia,
        String cpfCnpj,
        String rgIe,
        String telefone,
        String celular,
        String email,
        String observacao,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
