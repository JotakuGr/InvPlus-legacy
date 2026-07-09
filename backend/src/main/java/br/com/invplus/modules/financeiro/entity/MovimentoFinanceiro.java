package br.com.invplus.modules.financeiro.entity;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.modules.financeiro.enums.OrigemMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.StatusMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.TipoMovimentoFinanceiro;
import br.com.invplus.modules.pagamentos.entity.FormaPagamento;
import br.com.invplus.modules.pessoas.entity.Cliente;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Registro contabil do fluxo financeiro, criado manualmente ou por processos do ERP.
 */
@Getter
@Setter
@Entity
@Table(name = "movimento_financeiro", indexes = {
        @Index(name = "idx_mov_fin_empresa_jpa", columnList = "empresa_id"),
        @Index(name = "idx_mov_fin_origem_jpa", columnList = "origem, origem_id"),
        @Index(name = "idx_mov_fin_data_jpa", columnList = "data_movimento")
})
@NoArgsConstructor
public class MovimentoFinanceiro extends SoftDeleteEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filial_id")
    private Filial filial;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimento", nullable = false, length = 20)
    private TipoMovimentoFinanceiro tipoMovimento;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "origem", nullable = false, length = 30)
    private OrigemMovimentoFinanceiro origem;

    @Column(name = "origem_id")
    private UUID origemId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "forma_pagamento_id", nullable = false)
    private FormaPagamento formaPagamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "centro_custo_id")
    private CentroCusto centroCusto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plano_conta_id")
    private PlanoConta planoConta;

    @NotNull
    @Positive
    @Column(name = "valor", nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @NotNull
    @Column(name = "data_movimento", nullable = false)
    private LocalDate dataMovimento;

    @NotBlank
    @Column(name = "descricao", nullable = false, length = 255)
    private String descricao;

    @Column(name = "observacao")
    private String observacao;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusMovimentoFinanceiro status = StatusMovimentoFinanceiro.ATIVO;

    /**
     * Preenche um movimento ja validado, independentemente de sua origem.
     */
    public void registrar(Empresa empresa, Filial filial, TipoMovimentoFinanceiro tipoMovimento,
            OrigemMovimentoFinanceiro origem, UUID origemId, FormaPagamento formaPagamento,
            Cliente cliente, Fornecedor fornecedor, BigDecimal valor, LocalDate dataMovimento,
            String descricao, String observacao) {
        registrar(empresa, filial, tipoMovimento, origem, origemId, formaPagamento, cliente,
                fornecedor, null, valor, dataMovimento, descricao, observacao);
    }

    /**
     * Preenche um movimento incluindo sua classificacao gerencial opcional.
     */
    public void registrar(Empresa empresa, Filial filial, TipoMovimentoFinanceiro tipoMovimento,
            OrigemMovimentoFinanceiro origem, UUID origemId, FormaPagamento formaPagamento,
            Cliente cliente, Fornecedor fornecedor, CentroCusto centroCusto, BigDecimal valor,
            LocalDate dataMovimento, String descricao, String observacao) {
        registrar(empresa, filial, tipoMovimento, origem, origemId, formaPagamento, cliente,
                fornecedor, centroCusto, null, valor, dataMovimento, descricao, observacao);
    }

    /**
     * Preenche um movimento incluindo suas classificacoes gerencial e contabil opcionais.
     */
    public void registrar(Empresa empresa, Filial filial, TipoMovimentoFinanceiro tipoMovimento,
            OrigemMovimentoFinanceiro origem, UUID origemId, FormaPagamento formaPagamento,
            Cliente cliente, Fornecedor fornecedor, CentroCusto centroCusto, PlanoConta planoConta,
            BigDecimal valor, LocalDate dataMovimento, String descricao, String observacao) {
        this.empresa = empresa;
        this.filial = filial;
        this.tipoMovimento = tipoMovimento;
        this.origem = origem;
        this.origemId = origemId;
        this.formaPagamento = formaPagamento;
        this.cliente = cliente;
        this.fornecedor = fornecedor;
        this.centroCusto = centroCusto;
        this.planoConta = planoConta;
        this.valor = valor;
        this.dataMovimento = dataMovimento;
        this.descricao = descricao == null ? null : descricao.trim();
        this.observacao = observacao == null || observacao.isBlank() ? null : observacao.trim();
        this.status = StatusMovimentoFinanceiro.ATIVO;
    }

    /**
     * Cancela o movimento preservando seu historico e a origem.
     */
    public void cancelar() {
        if (status == StatusMovimentoFinanceiro.CANCELADO) {
            throw new InvalidOperationException("Movimento financeiro ja esta cancelado");
        }
        this.status = StatusMovimentoFinanceiro.CANCELADO;
    }
}
