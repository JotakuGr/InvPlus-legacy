package br.com.invplus.modules.pessoas.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Dados retornados para contatos de uma Pessoa.
 */
public record ContatoPessoaResponse(
        UUID id,
        UUID pessoaId,
        String pessoaNome,
        String nome,
        String telefone,
        String celular,
        String email,
        String cargo,
        String tipo,
        Boolean principal,
        String observacao,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
