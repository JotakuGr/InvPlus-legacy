package br.com.invplus.modules.financeiro.dto.response;

import br.com.invplus.modules.financeiro.enums.StatusContaPagar;
import br.com.invplus.modules.financeiro.enums.NaturezaPlanoConta;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Dados retornados pela API de contas a pagar.
 */
public record ContaPagarResponse(
        UUID id,
        UUID empresaId,
        String empresaNome,
        UUID filialId,
        String filialNome,
        UUID fornecedorId,
        String fornecedorNome,
        UUID entradaMercadoriaId,
        UUID centroCustoId,
        String centroCustoNome,
        String centroCustoCodigo,
        UUID planoContaId,
        String planoContaCodigo,
        String planoContaNome,
        NaturezaPlanoConta planoContaNatureza,
        String numero,
        String descricao,
        LocalDate dataEmissao,
        BigDecimal valorTotal,
        BigDecimal valorPago,
        BigDecimal valorAberto,
        StatusContaPagar status,
        String observacao,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
