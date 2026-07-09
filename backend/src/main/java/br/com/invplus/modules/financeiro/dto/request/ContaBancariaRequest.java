package br.com.invplus.modules.financeiro.dto.request;

import br.com.invplus.modules.financeiro.enums.TipoContaBancaria;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Dados para cadastrar ou atualizar uma conta bancaria.
 */
public record ContaBancariaRequest(
        @NotNull UUID empresaId,
        UUID filialId,
        @NotBlank @Size(max = 20) String bancoCodigo,
        @NotBlank @Size(max = 120) String bancoNome,
        @NotBlank @Size(max = 20) String agencia,
        @Size(max = 5) String agenciaDigito,
        @NotBlank @Size(max = 30) String numeroConta,
        @Size(max = 5) String contaDigito,
        @NotNull TipoContaBancaria tipoConta,
        @NotBlank @Size(max = 160) String titular,
        @Size(max = 18) String documentoTitular,
        @Size(max = 160) String pixChave,
        @DecimalMin(value = "0.00") BigDecimal saldoInicial,
        LocalDate dataSaldoInicial,
        Boolean principal
) {
}
