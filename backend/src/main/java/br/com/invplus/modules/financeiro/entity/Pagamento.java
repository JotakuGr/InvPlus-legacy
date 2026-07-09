package br.com.invplus.modules.financeiro.entity;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.modules.financeiro.enums.StatusPagamentoFinanceiro;
import br.com.invplus.modules.pagamentos.entity.FormaPagamento;
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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Evento financeiro que registra valores pagos para uma parcela a pagar.
 */
@Getter
@Setter
@Entity
@Table(
        name = "pagamento",
        indexes = {
                @Index(name = "idx_pagamento_parcela_jpa", columnList = "parcela_pagar_id"),
                @Index(name = "idx_pagamento_conta_jpa", columnList = "conta_pagar_id"),
                @Index(name = "idx_pagamento_data_jpa", columnList = "data_pagamento"),
                @Index(name = "idx_pagamento_status_jpa", columnList = "status")
        }
)
@NoArgsConstructor
public class Pagamento extends SoftDeleteEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parcela_pagar_id", nullable = false)
    private ParcelaPagar parcelaPagar;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conta_pagar_id", nullable = false)
    private ContaPagar contaPagar;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "forma_pagamento_id", nullable = false)
    private FormaPagamento formaPagamento;

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

    @NotNull
    @Positive
    @Column(name = "valor_pago", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorPago;

    @NotNull
    @PositiveOrZero
    @Column(name = "juros", nullable = false, precision = 15, scale = 2)
    private BigDecimal juros = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Column(name = "multa", nullable = false, precision = 15, scale = 2)
    private BigDecimal multa = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Column(name = "desconto", nullable = false, precision = 15, scale = 2)
    private BigDecimal desconto = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Column(name = "valor_liquido", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorLiquido;

    @NotNull
    @Column(name = "data_pagamento", nullable = false)
    private LocalDate dataPagamento;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private StatusPagamentoFinanceiro status = StatusPagamentoFinanceiro.ATIVO;

    @Column(name = "observacao")
    private String observacao;

    /**
     * Registra o pagamento copiando dados da parcela para facilitar consultas financeiras.
     */
    public void registrar(ParcelaPagar parcela, FormaPagamento formaPagamento, BigDecimal valorPago,
            BigDecimal juros, BigDecimal multa, BigDecimal desconto, LocalDate dataPagamento, String observacao) {
        this.parcelaPagar = parcela;
        this.contaPagar = parcela.getContaPagar();
        this.formaPagamento = formaPagamento;
        this.empresa = parcela.getContaPagar().getEmpresa();
        this.filial = parcela.getContaPagar().getFilial();
        this.fornecedor = parcela.getContaPagar().getFornecedor();
        this.valorPago = valorPago;
        this.juros = zeroSeNulo(juros);
        this.multa = zeroSeNulo(multa);
        this.desconto = zeroSeNulo(desconto);
        this.valorLiquido = valorPago.add(this.juros).add(this.multa).subtract(this.desconto);
        this.dataPagamento = dataPagamento;
        this.status = StatusPagamentoFinanceiro.ATIVO;
        this.observacao = normalizar(observacao);
    }

    /**
     * Cancela o evento financeiro sem apagar o historico.
     */
    public void cancelar() {
        if (status == StatusPagamentoFinanceiro.CANCELADO) {
            throw new InvalidOperationException("Pagamento ja esta cancelado");
        }
        this.status = StatusPagamentoFinanceiro.CANCELADO;
    }

    private BigDecimal zeroSeNulo(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private String normalizar(String valor) {
        return valor == null ? null : valor.trim();
    }
}
