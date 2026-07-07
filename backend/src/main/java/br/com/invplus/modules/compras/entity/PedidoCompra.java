package br.com.invplus.modules.compras.entity;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.modules.compras.enums.StatusPedidoCompra;
import br.com.invplus.modules.pessoas.entity.Fornecedor;
import br.com.invplus.shared.domain.entity.SoftDeleteEntity;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Intencao de compra negociada com um fornecedor antes do recebimento fisico.
 */
@Getter
@Setter
@Entity
@Table(
        name = "pedido_compra",
        indexes = {
                @Index(name = "idx_pedido_compra_empresa", columnList = "empresa_id"),
                @Index(name = "idx_pedido_compra_fornecedor", columnList = "fornecedor_id"),
                @Index(name = "idx_pedido_compra_status", columnList = "status")
        }
)
@NoArgsConstructor
public class PedidoCompra extends SoftDeleteEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filial_id")
    private Filial filial;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fornecedor_id", nullable = false)
    private Fornecedor fornecedor;

    @NotBlank
    @Column(name = "numero", nullable = false, length = 50)
    private String numero;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private StatusPedidoCompra status = StatusPedidoCompra.ABERTO;

    @NotNull
    @Column(name = "data_emissao", nullable = false)
    private LocalDate dataEmissao;

    @Column(name = "data_prevista_entrega")
    private LocalDate dataPrevistaEntrega;

    @Column(name = "observacao")
    private String observacao;

    @NotNull
    @PositiveOrZero
    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Column(name = "nivel_aprovacao", nullable = false)
    private Integer nivelAprovacao = 0;

    @Column(name = "aprovado_por")
    private UUID aprovadoPor;

    @Column(name = "aprovado_em")
    private OffsetDateTime aprovadoEm;

    /**
     * Atualiza os campos editaveis enquanto o pedido ainda aceita manutencao.
     */
    public void atualizar(String numero, LocalDate dataEmissao, LocalDate dataPrevistaEntrega, String observacao) {
        validarEditavel();
        this.numero = normalizarObrigatorio(numero);
        this.dataEmissao = dataEmissao;
        this.dataPrevistaEntrega = dataPrevistaEntrega;
        this.observacao = normalizarTexto(observacao);
    }

    public void aprovar(UUID usuarioId) {
        if (status != StatusPedidoCompra.ABERTO) {
            throw new InvalidOperationException("Somente pedido aberto pode ser aprovado");
        }
        status = StatusPedidoCompra.APROVADO;
        aprovadoPor = usuarioId;
        aprovadoEm = OffsetDateTime.now();
    }

    public void cancelar() {
        if (status == StatusPedidoCompra.RECEBIDO) {
            throw new InvalidOperationException("Pedido recebido nao pode ser cancelado");
        }
        status = StatusPedidoCompra.CANCELADO;
    }

    public void marcarRecebido(boolean parcial) {
        status = parcial ? StatusPedidoCompra.PARCIALMENTE_RECEBIDO : StatusPedidoCompra.RECEBIDO;
    }

    public void atualizarValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal == null ? BigDecimal.ZERO : valorTotal;
    }

    public void validarEditavel() {
        if (status == StatusPedidoCompra.CANCELADO || status == StatusPedidoCompra.RECEBIDO) {
            throw new InvalidOperationException("Pedido cancelado ou recebido nao pode ser alterado");
        }
    }

    private String normalizarObrigatorio(String valor) {
        return valor == null ? null : valor.trim();
    }

    private String normalizarTexto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
