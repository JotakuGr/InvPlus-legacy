package br.com.invplus.modules.vendas.entity;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.modules.pessoas.entity.Cliente;
import br.com.invplus.modules.pessoas.entity.Funcionario;
import br.com.invplus.modules.vendas.enums.StatusPedidoVenda;
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
 * Pedido comercial emitido para um cliente antes de separacao, faturamento e entrega.
 */
@Getter
@Setter
@Entity
@Table(
        name = "pedido_venda",
        indexes = {
                @Index(name = "idx_pedido_venda_empresa", columnList = "empresa_id"),
                @Index(name = "idx_pedido_venda_cliente", columnList = "cliente_id"),
                @Index(name = "idx_pedido_venda_status", columnList = "status")
        }
)
@NoArgsConstructor
public class PedidoVenda extends SoftDeleteEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filial_id")
    private Filial filial;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendedor_id")
    private Funcionario vendedor;

    @NotBlank
    @Column(name = "numero", nullable = false, length = 50)
    private String numero;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private StatusPedidoVenda status = StatusPedidoVenda.ABERTO;

    @NotNull
    @Column(name = "data_emissao", nullable = false)
    private LocalDate dataEmissao;

    @Column(name = "data_entrega_prevista")
    private LocalDate dataEntregaPrevista;

    @NotNull
    @PositiveOrZero
    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Column(name = "desconto", nullable = false, precision = 15, scale = 2)
    private BigDecimal desconto = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Column(name = "acrescimo", nullable = false, precision = 15, scale = 2)
    private BigDecimal acrescimo = BigDecimal.ZERO;

    @Column(name = "observacao")
    private String observacao;

    @NotNull
    @PositiveOrZero
    @Column(name = "nivel_aprovacao", nullable = false)
    private Integer nivelAprovacao = 0;

    @Column(name = "aprovado_por")
    private UUID aprovadoPor;

    @Column(name = "aprovado_em")
    private OffsetDateTime aprovadoEm;

    @Column(name = "cancelado_em")
    private OffsetDateTime canceladoEm;

    /**
     * Aplica campos editaveis do cabecalho sem alterar relacionamentos ja resolvidos pelo service.
     */
    public void atualizar(String numero, LocalDate dataEmissao, LocalDate dataEntregaPrevista,
                          BigDecimal desconto, BigDecimal acrescimo, String observacao) {
        validarEditavel();
        this.numero = normalizarObrigatorio(numero);
        this.dataEmissao = dataEmissao;
        this.dataEntregaPrevista = dataEntregaPrevista;
        this.desconto = desconto == null ? BigDecimal.ZERO : desconto;
        this.acrescimo = acrescimo == null ? BigDecimal.ZERO : acrescimo;
        this.observacao = normalizarTexto(observacao);
    }

    public void aprovar(UUID usuarioId) {
        if (status != StatusPedidoVenda.ABERTO) {
            throw new InvalidOperationException("Somente pedido aberto pode ser aprovado");
        }
        status = StatusPedidoVenda.APROVADO;
        aprovadoPor = usuarioId;
        aprovadoEm = OffsetDateTime.now();
    }

    public void cancelar() {
        if (status == StatusPedidoVenda.FATURADO) {
            throw new InvalidOperationException("Pedido faturado nao pode ser cancelado");
        }
        if (status == StatusPedidoVenda.CANCELADO) {
            throw new InvalidOperationException("Pedido ja esta cancelado");
        }
        status = StatusPedidoVenda.CANCELADO;
        canceladoEm = OffsetDateTime.now();
    }

    public void atualizarValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal == null ? BigDecimal.ZERO : valorTotal;
    }

    public void validarEditavel() {
        if (status == StatusPedidoVenda.CANCELADO || status == StatusPedidoVenda.FATURADO) {
            throw new InvalidOperationException("Pedido cancelado ou faturado nao pode ser alterado");
        }
    }

    private String normalizarObrigatorio(String valor) {
        return valor == null ? null : valor.trim();
    }

    private String normalizarTexto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
