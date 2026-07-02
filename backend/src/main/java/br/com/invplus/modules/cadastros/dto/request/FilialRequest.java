package br.com.invplus.modules.cadastros.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Payload para criar ou atualizar filiais.
 */
public record FilialRequest(
        @NotNull UUID empresaId,
        UUID enderecoId,
        @NotBlank @Size(max = 30) String codigo,
        @NotBlank @Size(max = 120) String nome,
        @Size(max = 18) String cnpj,
        @Size(max = 30) String inscricaoEstadual,
        @Size(max = 30) String telefone,
        @Email @Size(max = 320) String email,
        Boolean matriz
) {
}
