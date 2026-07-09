package br.com.invplus.modules.financeiro.dto.response;

import br.com.invplus.modules.financeiro.enums.OrigemExtratoBancario;
import br.com.invplus.modules.financeiro.enums.StatusExtratoBancario;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Representacao de leitura de um extrato bancario importado.
 */
public record ExtratoBancarioResponse(
        UUID id,
        UUID contaBancariaId,
        String contaBancariaDescricao,
        LocalDate dataInicio,
        LocalDate dataFim,
        BigDecimal saldoInicial,
        BigDecimal saldoFinal,
        OrigemExtratoBancario origem,
        String arquivoNome,
        String arquivoHash,
        OffsetDateTime importadoEm,
        StatusExtratoBancario status,
        String observacao,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
