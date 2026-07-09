package br.com.invplus.modules.financeiro.entity;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.modules.financeiro.enums.StatusContaReceber;
import br.com.invplus.modules.pessoas.entity.Cliente;
import br.com.invplus.modules.vendas.entity.VendaFaturamento;
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
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Titulo financeiro gerado para representar um valor a receber de cliente.
 */
@Getter
@Setter
@Entity
@Table(
        name = "conta_receber",
        indexes = {
                @Index(name = "idx_conta_receber_empresa_jpa", columnList = "empresa_id"),
                @Index(name = "idx_conta_receber_cliente_jpa", columnList = "cliente_id"),
                @Index(name = "idx_conta_receber_status_jpa", columnList = "status")
        }
)
@NoArgsConstructor
public class ContaReceber extends SoftDeleteEntity {

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
    @JoinColumn(name = "venda_faturamento_id")
    private VendaFaturamento vendaFaturamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "centro_custo_id")
    private CentroCusto centroCusto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plano_conta_id")
    private PlanoConta planoConta;

    @NotBlank
    @Column(name = "numero", nullable = false, length = 50)
    private String numero;

    @NotBlank
    @Column(name = "descricao", nullable = false, length = 255)
    private String descricao;

    @NotNull
    @Column(name = "data_emissao", nullable = false)
    private LocalDate dataEmissao;

    @NotNull
    @Positive
    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTotal;

    @NotNull
    @PositiveOrZero
    @Column(name = "valor_pago", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorPago = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Column(name = "valor_aberto", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorAberto;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private StatusContaReceber status = StatusContaReceber.ABERTA;

    @Column(name = "observacao")
    private String observacao;

    /**
     * Prepara uma conta nova sempre aberta e sem valor pago.
     */
    public void abrir(Empresa empresa, Filial filial, Cliente cliente, VendaFaturamento vendaFaturamento,
            String numero, String descricao, LocalDate dataEmissao, BigDecimal valorTotal, String observacao) {
        abrir(empresa, filial, cliente, vendaFaturamento, null, numero, descricao, dataEmissao, valorTotal, observacao);
    }

    /**
     * Prepara uma conta nova e associa opcionalmente sua classificacao gerencial.
     */
    public void abrir(Empresa empresa, Filial filial, Cliente cliente, VendaFaturamento vendaFaturamento,
            CentroCusto centroCusto, String numero, String descricao, LocalDate dataEmissao,
            BigDecimal valorTotal, String observacao) {
        abrir(empresa, filial, cliente, vendaFaturamento, centroCusto, null, numero, descricao,
                dataEmissao, valorTotal, observacao);
    }

    /**
     * Prepara uma conta nova e associa suas classificacoes gerencial e contabil.
     */
    public void abrir(Empresa empresa, Filial filial, Cliente cliente, VendaFaturamento vendaFaturamento,
            CentroCusto centroCusto, PlanoConta planoConta, String numero, String descricao,
            LocalDate dataEmissao, BigDecimal valorTotal, String observacao) {
        this.empresa = empresa;
        this.filial = filial;
        this.cliente = cliente;
        this.vendaFaturamento = vendaFaturamento;
        this.centroCusto = centroCusto;
        this.planoConta = planoConta;
        this.numero = normalizar(numero);
        this.descricao = normalizar(descricao);
        this.dataEmissao = dataEmissao;
        this.valorTotal = valorTotal;
        this.valorPago = BigDecimal.ZERO;
        this.valorAberto = valorTotal;
        this.status = StatusContaReceber.ABERTA;
        this.observacao = normalizar(observacao);
    }

    /**
     * Atualiza dados editaveis sem permitir alterar contas encerradas.
     */
    public void atualizar(Empresa empresa, Filial filial, Cliente cliente, VendaFaturamento vendaFaturamento,
            String numero, String descricao, LocalDate dataEmissao, BigDecimal valorTotal, String observacao) {
        atualizar(empresa, filial, cliente, vendaFaturamento, null, numero, descricao, dataEmissao,
                valorTotal, observacao);
    }

    /**
     * Atualiza a conta e sua classificacao gerencial preservando valores pagos.
     */
    public void atualizar(Empresa empresa, Filial filial, Cliente cliente, VendaFaturamento vendaFaturamento,
            CentroCusto centroCusto, String numero, String descricao, LocalDate dataEmissao,
            BigDecimal valorTotal, String observacao) {
        atualizar(empresa, filial, cliente, vendaFaturamento, centroCusto, null, numero, descricao,
                dataEmissao, valorTotal, observacao);
    }

    /**
     * Atualiza a conta e suas classificacoes preservando valores pagos.
     */
    public void atualizar(Empresa empresa, Filial filial, Cliente cliente, VendaFaturamento vendaFaturamento,
            CentroCusto centroCusto, PlanoConta planoConta, String numero, String descricao,
            LocalDate dataEmissao, BigDecimal valorTotal, String observacao) {
        validarEditavel();
        this.empresa = empresa;
        this.filial = filial;
        this.cliente = cliente;
        this.vendaFaturamento = vendaFaturamento;
        this.centroCusto = centroCusto;
        this.planoConta = planoConta;
        this.numero = normalizar(numero);
        this.descricao = normalizar(descricao);
        this.dataEmissao = dataEmissao;
        this.valorTotal = valorTotal;
        recalcularAberto();
        this.observacao = normalizar(observacao);
    }

    /**
     * Cancela a conta sem apagar o registro financeiro.
     */
    public void cancelar() {
        validarEditavel();
        this.status = StatusContaReceber.CANCELADA;
    }

    /**
     * Atualiza os totais consolidados a partir das parcelas recebidas.
     */
    public void atualizarRecebimento(BigDecimal valorPago, BigDecimal valorAberto, StatusContaReceber status) {
        if (this.status == StatusContaReceber.CANCELADA) {
            throw new InvalidOperationException("Conta a receber cancelada nao pode receber baixa");
        }
        this.valorPago = valorPago;
        this.valorAberto = valorAberto;
        this.status = status;
    }

    public void validarEditavel() {
        if (status == StatusContaReceber.PAGA) {
            throw new InvalidOperationException("Conta a receber paga nao pode ser alterada");
        }
        if (status == StatusContaReceber.CANCELADA) {
            throw new InvalidOperationException("Conta a receber cancelada nao pode ser alterada");
        }
    }

    private void recalcularAberto() {
        this.valorAberto = valorTotal.subtract(valorPago == null ? BigDecimal.ZERO : valorPago);
        if (valorAberto.signum() < 0) {
            throw new InvalidOperationException("Valor pago nao pode ser maior que valor total");
        }
    }

    private String normalizar(String valor) {
        return valor == null ? null : valor.trim();
    }
}
