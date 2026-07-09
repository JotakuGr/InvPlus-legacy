package br.com.invplus.modules.financeiro.dto.response;

import br.com.invplus.modules.financeiro.enums.TipoContaBancaria;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Representacao de leitura de uma conta bancaria.
 */
public record ContaBancariaResponse(
        UUID id,
        UUID empresaId,
        String empresaNome,
        UUID filialId,
        String filialNome,
        String bancoCodigo,
        String bancoNome,
        String agencia,
        String agenciaDigito,
        String numeroConta,
        String contaDigito,
        TipoContaBancaria tipoConta,
        String titular,
        String documentoTitular,
        String pixChave,
        BigDecimal saldoInicial,
        BigDecimal saldoAtual,
        LocalDate dataSaldoInicial,
        Boolean principal,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
