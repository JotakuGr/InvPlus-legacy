package br.com.invplus.modules.cadastros.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Payload para criar ou atualizar empresas.
 */
public record EmpresaRequest(
        @NotBlank @Size(max = 160) String razaoSocial,
        @Size(max = 160) String nomeFantasia,
        @NotBlank @Size(max = 18) String cnpj,
        @Size(max = 30) String inscricaoEstadual,
        @Size(max = 30) String inscricaoMunicipal,
        @Size(max = 60) String regimeTributario,
        @NotNull Integer crt,
        @Size(max = 30) String telefone,
        @Email @Size(max = 320) String email
) {
}
