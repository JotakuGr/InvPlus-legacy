package br.com.invplus.modules.pessoas.dto.request;

import br.com.invplus.common.enums.TipoPessoa;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Payload para criacao e atualizacao de pessoas.
 */
public record PessoaRequest(
        @NotNull TipoPessoa tipoPessoa,
        @NotBlank @Size(max = 160) String nome,
        @Size(max = 160) String nomeFantasia,
        @NotBlank @Size(max = 18) String cpfCnpj,
        @Size(max = 30) String rgIe,
        @Size(max = 30) String telefone,
        @Size(max = 30) String celular,
        @Email @Size(max = 255) String email,
        String observacao
) {
}
