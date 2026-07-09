package br.com.invplus.modules.financeiro.dto.response;

import br.com.invplus.modules.financeiro.enums.OrigemMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.NaturezaPlanoConta;
import br.com.invplus.modules.financeiro.enums.StatusMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.TipoMovimentoFinanceiro;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Representacao de leitura de um movimento financeiro.
 */
public record MovimentoFinanceiroResponse(
        UUID id,
        UUID empresaId,
        String empresaNome,
        UUID filialId,
        String filialNome,
        TipoMovimentoFinanceiro tipoMovimento,
        OrigemMovimentoFinanceiro origem,
        UUID origemId,
        UUID formaPagamentoId,
        String formaPagamentoNome,
        UUID clienteId,
        String clienteNome,
        UUID fornecedorId,
        String fornecedorNome,
        UUID centroCustoId,
        String centroCustoNome,
        String centroCustoCodigo,
        UUID planoContaId,
        String planoContaCodigo,
        String planoContaNome,
        NaturezaPlanoConta planoContaNatureza,
        BigDecimal valor,
        LocalDate dataMovimento,
        String descricao,
        StatusMovimentoFinanceiro status,
        String observacao,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
