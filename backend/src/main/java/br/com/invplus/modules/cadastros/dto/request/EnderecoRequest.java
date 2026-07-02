package br.com.invplus.modules.cadastros.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Payload para criar ou atualizar enderecos.
 */
public record EnderecoRequest(
        UUID empresaId,
        UUID cidadeId,
        @Size(max = 40) String tipo,
        @NotBlank @Size(max = 160) String logradouro,
        @Size(max = 20) String numero,
        @Size(max = 120) String complemento,
        @Size(max = 100) String bairro,
        @NotBlank @Size(max = 100) String cidade,
        @NotBlank @Size(min = 2, max = 2) String uf,
        @Size(max = 12) String cep,
        Boolean principal
) {
}
