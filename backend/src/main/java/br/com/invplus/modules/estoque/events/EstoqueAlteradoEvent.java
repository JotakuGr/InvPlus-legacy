package br.com.invplus.modules.estoque.events;

import br.com.invplus.modules.estoque.enums.MovimentacaoOrigem;
import br.com.invplus.modules.estoque.enums.TipoMovimentacaoEstoque;
import br.com.invplus.shared.events.AbstractDomainEvent;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Evento de dominio preparado para auditoria, cache e mensageria quando saldo mudar.
 */
public class EstoqueAlteradoEvent extends AbstractDomainEvent {

    private final UUID estoqueId;
    private final UUID produtoId;
    private final UUID empresaId;
    private final TipoMovimentacaoEstoque tipoMovimentacao;
    private final MovimentacaoOrigem origem;
    private final UUID origemId;
    private final BigDecimal saldoAnterior;
    private final BigDecimal saldoPosterior;

    public EstoqueAlteradoEvent(UUID estoqueId, UUID produtoId, UUID empresaId,
                                TipoMovimentacaoEstoque tipoMovimentacao, MovimentacaoOrigem origem, UUID origemId,
                                BigDecimal saldoAnterior, BigDecimal saldoPosterior) {
        this.estoqueId = estoqueId;
        this.produtoId = produtoId;
        this.empresaId = empresaId;
        this.tipoMovimentacao = tipoMovimentacao;
        this.origem = origem;
        this.origemId = origemId;
        this.saldoAnterior = saldoAnterior;
        this.saldoPosterior = saldoPosterior;
    }

    public UUID estoqueId() {
        return estoqueId;
    }

    public UUID produtoId() {
        return produtoId;
    }

    public UUID empresaId() {
        return empresaId;
    }

    public TipoMovimentacaoEstoque tipoMovimentacao() {
        return tipoMovimentacao;
    }

    public MovimentacaoOrigem origem() {
        return origem;
    }

    public UUID origemId() {
        return origemId;
    }

    public BigDecimal saldoAnterior() {
        return saldoAnterior;
    }

    public BigDecimal saldoPosterior() {
        return saldoPosterior;
    }
}
