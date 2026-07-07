package br.com.invplus.modules.compras.entity;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.modules.compras.enums.StatusEntradaMercadoria;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Recebimento fisico de mercadorias, origem da entrada de estoque.
 */
@Getter
@Setter
@Entity
@Table(
        name = "entrada_mercadoria",
        indexes = {
                @Index(name = "idx_entrada_mercadoria_empresa", columnList = "empresa_id"),
                @Index(name = "idx_entrada_mercadoria_fornecedor", columnList = "fornecedor_id"),
                @Index(name = "idx_entrada_mercadoria_status", columnList = "status")
        }
)
@NoArgsConstructor
public class EntradaMercadoria extends SoftDeleteEntity {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_compra_id")
    private PedidoCompra pedidoCompra;

    @NotBlank
    @Column(name = "numero", nullable = false, length = 50)
    private String numero;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private StatusEntradaMercadoria status = StatusEntradaMercadoria.ABERTA;

    @NotNull
    @Column(name = "data_entrada", nullable = false)
    private LocalDate dataEntrada;

    @Column(name = "observacao")
    private String observacao;

    @NotNull
    @PositiveOrZero
    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    public void atualizar(String numero, LocalDate dataEntrada, String observacao) {
        validarEditavel();
        this.numero = numero == null ? null : numero.trim();
        this.dataEntrada = dataEntrada;
        this.observacao = observacao == null || observacao.isBlank() ? null : observacao.trim();
    }

    public void conferir() {
        validarEditavel();
        status = StatusEntradaMercadoria.CONFERIDA;
    }

    public void efetivar() {
        validarEditavel();
        status = StatusEntradaMercadoria.EFETIVADA;
    }

    public void cancelar() {
        validarEditavel();
        status = StatusEntradaMercadoria.CANCELADA;
    }

    public void atualizarValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal == null ? BigDecimal.ZERO : valorTotal;
    }

    public void validarEditavel() {
        if (status == StatusEntradaMercadoria.EFETIVADA || status == StatusEntradaMercadoria.CANCELADA) {
            throw new InvalidOperationException("Entrada efetivada ou cancelada nao pode ser alterada");
        }
    }
}
