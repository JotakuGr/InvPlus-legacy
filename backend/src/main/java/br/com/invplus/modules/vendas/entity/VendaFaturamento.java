package br.com.invplus.modules.vendas.entity;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.modules.financeiro.entity.ContaReceber;
import br.com.invplus.modules.pessoas.entity.Cliente;
import br.com.invplus.modules.vendas.enums.StatusVendaFaturamento;
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
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Documento operacional que confirma o faturamento de um pedido de venda aprovado.
 */
@Getter
@Setter
@Entity
@Table(
        name = "venda_faturamento",
        indexes = {
                @Index(name = "idx_venda_faturamento_empresa", columnList = "empresa_id"),
                @Index(name = "idx_venda_faturamento_cliente", columnList = "cliente_id"),
                @Index(name = "idx_venda_faturamento_status", columnList = "status")
        }
)
@NoArgsConstructor
public class VendaFaturamento extends SoftDeleteEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_venda_id", nullable = false)
    private PedidoVenda pedidoVenda;

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

    @NotBlank
    @Column(name = "numero", nullable = false, length = 50)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_receber_id")
    private ContaReceber contaReceber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private StatusVendaFaturamento status = StatusVendaFaturamento.ABERTO;

    @Column(name = "data_faturamento")
    private OffsetDateTime dataFaturamento;

    @NotNull
    @PositiveOrZero
    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(name = "observacao")
    private String observacao;

    /**
     * Copia os vinculos do pedido e prepara o faturamento como documento finalizado.
     */
    public void faturar(PedidoVenda pedido, String numero, String observacao) {
        this.pedidoVenda = pedido;
        this.empresa = pedido.getEmpresa();
        this.filial = pedido.getFilial();
        this.cliente = pedido.getCliente();
        this.numero = numero == null ? null : numero.trim();
        this.valorTotal = pedido.getValorTotal();
        this.observacao = normalizar(observacao);
        this.status = StatusVendaFaturamento.FATURADO;
        this.dataFaturamento = OffsetDateTime.now();
    }

    /**
     * Vincula a conta financeira gerada pelo faturamento sem permitir duplicidade.
     */
    public void vincularContaReceber(ContaReceber contaReceber) {
        if (this.contaReceber != null) {
            throw new InvalidOperationException("Faturamento de venda ja possui conta a receber");
        }
        this.contaReceber = contaReceber;
    }

    /**
     * Cancela o documento de faturamento sem desfazer movimentos de estoque nesta versao.
     */
    public void cancelar(String observacao) {
        if (status == StatusVendaFaturamento.CANCELADO) {
            throw new InvalidOperationException("Faturamento de venda ja esta cancelado");
        }
        this.status = StatusVendaFaturamento.CANCELADO;
        this.observacao = normalizar(observacao);
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
