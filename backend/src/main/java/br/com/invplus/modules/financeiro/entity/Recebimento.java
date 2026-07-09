package br.com.invplus.modules.financeiro.entity;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.modules.financeiro.enums.StatusRecebimento;
import br.com.invplus.modules.pagamentos.entity.FormaPagamento;
import br.com.invplus.modules.pessoas.entity.Cliente;
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
 * Evento financeiro que registra valores recebidos de uma parcela a receber.
 */
@Getter
@Setter
@Entity
@Table(
        name = "recebimento",
        indexes = {
                @Index(name = "idx_recebimento_parcela_jpa", columnList = "parcela_receber_id"),
                @Index(name = "idx_recebimento_conta_jpa", columnList = "conta_receber_id"),
                @Index(name = "idx_recebimento_data_jpa", columnList = "data_recebimento"),
                @Index(name = "idx_recebimento_status_jpa", columnList = "status")
        }
)
@NoArgsConstructor
public class Recebimento extends SoftDeleteEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parcela_receber_id", nullable = false)
    private ParcelaReceber parcelaReceber;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conta_receber_id", nullable = false)
    private ContaReceber contaReceber;

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
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @NotNull
    @Positive
    @Column(name = "valor_recebido", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorRecebido;

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
    @Column(name = "data_recebimento", nullable = false)
    private LocalDate dataRecebimento;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private StatusRecebimento status = StatusRecebimento.ATIVO;

    @Column(name = "observacao")
    private String observacao;

    /**
     * Registra o recebimento copiando dados da parcela para facilitar consultas financeiras.
     */
    public void registrar(ParcelaReceber parcela, FormaPagamento formaPagamento, BigDecimal valorRecebido,
            BigDecimal juros, BigDecimal multa, BigDecimal desconto, LocalDate dataRecebimento, String observacao) {
        this.parcelaReceber = parcela;
        this.contaReceber = parcela.getContaReceber();
        this.formaPagamento = formaPagamento;
        this.empresa = parcela.getContaReceber().getEmpresa();
        this.filial = parcela.getContaReceber().getFilial();
        this.cliente = parcela.getContaReceber().getCliente();
        this.valorRecebido = valorRecebido;
        this.juros = zeroSeNulo(juros);
        this.multa = zeroSeNulo(multa);
        this.desconto = zeroSeNulo(desconto);
        this.valorLiquido = valorRecebido.add(this.juros).add(this.multa).subtract(this.desconto);
        this.dataRecebimento = dataRecebimento;
        this.status = StatusRecebimento.ATIVO;
        this.observacao = normalizar(observacao);
    }

    /**
     * Cancela o evento financeiro sem apagar o historico.
     */
    public void cancelar() {
        if (status == StatusRecebimento.CANCELADO) {
            throw new InvalidOperationException("Recebimento ja esta cancelado");
        }
        this.status = StatusRecebimento.CANCELADO;
    }

    private BigDecimal zeroSeNulo(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private String normalizar(String valor) {
        return valor == null ? null : valor.trim();
    }
}
