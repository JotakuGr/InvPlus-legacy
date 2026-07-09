package br.com.invplus.modules.financeiro.entity;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.modules.compras.entity.EntradaMercadoria;
import br.com.invplus.modules.financeiro.enums.StatusContaPagar;
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
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Titulo financeiro que representa uma obrigacao com fornecedor.
 */
@Getter
@Setter
@Entity
@Table(
        name = "conta_pagar",
        indexes = {
                @Index(name = "idx_conta_pagar_empresa_jpa", columnList = "empresa_id"),
                @Index(name = "idx_conta_pagar_fornecedor_jpa", columnList = "fornecedor_id"),
                @Index(name = "idx_conta_pagar_status_jpa", columnList = "status")
        }
)
@NoArgsConstructor
public class ContaPagar extends SoftDeleteEntity {

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
    @JoinColumn(name = "entrada_mercadoria_id")
    private EntradaMercadoria entradaMercadoria;

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
    private StatusContaPagar status = StatusContaPagar.ABERTA;

    @Column(name = "observacao")
    private String observacao;

    /**
     * Inicializa uma obrigacao aberta, sem pagamentos realizados.
     */
    public void abrir(Empresa empresa, Filial filial, Fornecedor fornecedor, EntradaMercadoria entradaMercadoria,
            String numero, String descricao, LocalDate dataEmissao, BigDecimal valorTotal, String observacao) {
        abrir(empresa, filial, fornecedor, entradaMercadoria, null, numero, descricao, dataEmissao,
                valorTotal, observacao);
    }

    /**
     * Inicializa uma obrigacao com classificacao gerencial opcional.
     */
    public void abrir(Empresa empresa, Filial filial, Fornecedor fornecedor, EntradaMercadoria entradaMercadoria,
            CentroCusto centroCusto, String numero, String descricao, LocalDate dataEmissao,
            BigDecimal valorTotal, String observacao) {
        abrir(empresa, filial, fornecedor, entradaMercadoria, centroCusto, null, numero, descricao,
                dataEmissao, valorTotal, observacao);
    }

    /**
     * Inicializa uma obrigacao com classificacoes gerencial e contabil opcionais.
     */
    public void abrir(Empresa empresa, Filial filial, Fornecedor fornecedor, EntradaMercadoria entradaMercadoria,
            CentroCusto centroCusto, PlanoConta planoConta, String numero, String descricao,
            LocalDate dataEmissao, BigDecimal valorTotal, String observacao) {
        this.empresa = empresa;
        this.filial = filial;
        this.fornecedor = fornecedor;
        this.entradaMercadoria = entradaMercadoria;
        this.centroCusto = centroCusto;
        this.planoConta = planoConta;
        this.numero = normalizar(numero);
        this.descricao = normalizar(descricao);
        this.dataEmissao = dataEmissao;
        this.valorTotal = valorTotal;
        this.valorPago = BigDecimal.ZERO;
        this.valorAberto = valorTotal;
        this.status = StatusContaPagar.ABERTA;
        this.observacao = normalizar(observacao);
    }

    /**
     * Atualiza os dados editaveis e preserva os valores ja pagos.
     */
    public void atualizar(Empresa empresa, Filial filial, Fornecedor fornecedor,
            EntradaMercadoria entradaMercadoria, String numero, String descricao,
            LocalDate dataEmissao, BigDecimal valorTotal, String observacao) {
        atualizar(empresa, filial, fornecedor, entradaMercadoria, null, numero, descricao,
                dataEmissao, valorTotal, observacao);
    }

    /**
     * Atualiza a obrigacao e sua classificacao gerencial.
     */
    public void atualizar(Empresa empresa, Filial filial, Fornecedor fornecedor,
            EntradaMercadoria entradaMercadoria, CentroCusto centroCusto, String numero, String descricao,
            LocalDate dataEmissao, BigDecimal valorTotal, String observacao) {
        atualizar(empresa, filial, fornecedor, entradaMercadoria, centroCusto, null, numero, descricao,
                dataEmissao, valorTotal, observacao);
    }

    /**
     * Atualiza a obrigacao e suas classificacoes.
     */
    public void atualizar(Empresa empresa, Filial filial, Fornecedor fornecedor,
            EntradaMercadoria entradaMercadoria, CentroCusto centroCusto, PlanoConta planoConta,
            String numero, String descricao, LocalDate dataEmissao, BigDecimal valorTotal, String observacao) {
        validarEditavel();
        this.empresa = empresa;
        this.filial = filial;
        this.fornecedor = fornecedor;
        this.entradaMercadoria = entradaMercadoria;
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
     * Cancela a obrigacao sem remover seu historico.
     */
    public void cancelar() {
        validarEditavel();
        this.status = StatusContaPagar.CANCELADA;
    }

    /**
     * Consolida os pagamentos das parcelas sem permitir baixa em conta cancelada.
     */
    public void atualizarPagamento(BigDecimal valorPago, BigDecimal valorAberto, StatusContaPagar status) {
        if (this.status == StatusContaPagar.CANCELADA) {
            throw new InvalidOperationException("Conta a pagar cancelada nao pode receber baixa");
        }
        this.valorPago = valorPago;
        this.valorAberto = valorAberto;
        this.status = status;
    }

    public void validarEditavel() {
        if (status == StatusContaPagar.PAGA) {
            throw new InvalidOperationException("Conta a pagar paga nao pode ser alterada");
        }
        if (status == StatusContaPagar.CANCELADA) {
            throw new InvalidOperationException("Conta a pagar cancelada nao pode ser alterada");
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
