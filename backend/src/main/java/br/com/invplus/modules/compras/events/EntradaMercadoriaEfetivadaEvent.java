package br.com.invplus.modules.compras.events;

import br.com.invplus.shared.events.AbstractDomainEvent;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Evento publicado quando a entrada de mercadoria gera saldo no estoque.
 */
public class EntradaMercadoriaEfetivadaEvent extends AbstractDomainEvent {

    private final UUID entradaMercadoriaId;
    private final UUID pedidoCompraId;
    private final UUID empresaId;
    private final UUID fornecedorId;
    private final BigDecimal valorTotal;

    public EntradaMercadoriaEfetivadaEvent(UUID entradaMercadoriaId, UUID pedidoCompraId, UUID empresaId,
                                           UUID fornecedorId, BigDecimal valorTotal) {
        this.entradaMercadoriaId = entradaMercadoriaId;
        this.pedidoCompraId = pedidoCompraId;
        this.empresaId = empresaId;
        this.fornecedorId = fornecedorId;
        this.valorTotal = valorTotal;
    }

    public UUID entradaMercadoriaId() {
        return entradaMercadoriaId;
    }

    public UUID pedidoCompraId() {
        return pedidoCompraId;
    }

    public UUID empresaId() {
        return empresaId;
    }

    public UUID fornecedorId() {
        return fornecedorId;
    }

    public BigDecimal valorTotal() {
        return valorTotal;
    }
}
