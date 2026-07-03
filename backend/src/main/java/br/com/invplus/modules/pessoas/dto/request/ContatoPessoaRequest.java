package br.com.invplus.modules.pessoas.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Dados de entrada para criar ou atualizar um contato operacional de Pessoa.
 */
public record ContatoPessoaRequest(
        @NotNull UUID pessoaId,
        @NotBlank String nome,
        String telefone,
        String celular,
        @Email String email,
        String cargo,
        String tipo,
        Boolean principal,
        String observacao
) {
}
