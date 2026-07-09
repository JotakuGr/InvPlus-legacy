package br.com.invplus.modules.financeiro.dto.response;

import br.com.invplus.modules.financeiro.enums.StatusContaReceber;
import br.com.invplus.modules.financeiro.enums.NaturezaPlanoConta;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Dados retornados pela API de contas a receber.
 */
public record ContaReceberResponse(
        UUID id,
        UUID empresaId,
        String empresaNome,
        UUID filialId,
        String filialNome,
        UUID clienteId,
        String clienteNome,
        UUID vendaFaturamentoId,
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
        StatusContaReceber status,
        String observacao,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
    /**
     * Mantem compatibilidade com testes anteriores a classificacao por centro de custo.
     */
    public ContaReceberResponse(UUID id, UUID empresaId, String empresaNome, UUID filialId,
            String filialNome, UUID clienteId, String clienteNome, UUID vendaFaturamentoId,
            String numero, String descricao, LocalDate dataEmissao, BigDecimal valorTotal,
            BigDecimal valorPago, BigDecimal valorAberto, StatusContaReceber status,
            String observacao, Boolean ativo, OffsetDateTime criadoEm, OffsetDateTime atualizadoEm) {
        this(id, empresaId, empresaNome, filialId, filialNome, clienteId, clienteNome,
                vendaFaturamentoId, null, null, null, null, null, null, null, numero, descricao, dataEmissao,
                valorTotal, valorPago, valorAberto, status, observacao, ativo, criadoEm, atualizadoEm);
    }
}
