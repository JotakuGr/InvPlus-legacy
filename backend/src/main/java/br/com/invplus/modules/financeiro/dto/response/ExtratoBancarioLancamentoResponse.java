package br.com.invplus.modules.financeiro.dto.response;

import br.com.invplus.modules.financeiro.enums.CategoriaLancamentoBancario;
import br.com.invplus.modules.financeiro.enums.TipoLancamentoBancario;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Representacao de leitura de um lancamento de extrato bancario.
 */
public record ExtratoBancarioLancamentoResponse(
        UUID id,
        UUID extratoBancarioId,
        UUID contaBancariaId,
        String contaBancariaDescricao,
        LocalDate dataLancamento,
        String descricao,
        String documento,
        TipoLancamentoBancario tipo,
        BigDecimal valor,
        BigDecimal saldoAposLancamento,
        String identificadorBancario,
        CategoriaLancamentoBancario categoriaBancaria,
        Boolean conciliado,
        String observacao,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
