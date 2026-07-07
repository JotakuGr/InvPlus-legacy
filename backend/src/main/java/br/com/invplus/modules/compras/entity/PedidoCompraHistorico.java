package br.com.invplus.modules.compras.entity;

import br.com.invplus.modules.compras.enums.StatusPedidoCompra;
import br.com.invplus.shared.domain.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Registra cada transicao de status do pedido de compra para auditoria operacional.
 */
@Getter
@Setter
@Entity
@Table(
        name = "pedido_compra_historico",
        indexes = @Index(name = "idx_pedido_compra_historico_pedido", columnList = "pedido_compra_id,data_alteracao")
)
@NoArgsConstructor
public class PedidoCompraHistorico extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_compra_id", nullable = false)
    private PedidoCompra pedidoCompra;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior", length = 30)
    private StatusPedidoCompra statusAnterior;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "novo_status", nullable = false, length = 30)
    private StatusPedidoCompra novoStatus;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @NotNull
    @Column(name = "data_alteracao", nullable = false)
    private OffsetDateTime dataAlteracao = OffsetDateTime.now();

    @Column(name = "observacao")
    private String observacao;
}
