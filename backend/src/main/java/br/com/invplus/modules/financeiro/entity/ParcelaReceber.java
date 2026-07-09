package br.com.invplus.modules.financeiro.entity;

import br.com.invplus.modules.financeiro.enums.StatusParcelaReceber;
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
 * Vencimento individual de uma conta a receber.
 */
@Getter
@Setter
@Entity
@Table(
        name = "parcela_receber",
        indexes = {
                @Index(name = "idx_parcela_receber_conta_jpa", columnList = "conta_receber_id"),
                @Index(name = "idx_parcela_receber_status_jpa", columnList = "status"),
                @Index(name = "idx_parcela_receber_vencimento_jpa", columnList = "data_vencimento")
        }
)
@NoArgsConstructor
public class ParcelaReceber extends SoftDeleteEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conta_receber_id", nullable = false)
    private ContaReceber contaReceber;

    @NotNull
    @Positive
    @Column(name = "numero_parcela", nullable = false)
    private Integer numeroParcela;

    @NotNull
    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @NotNull
    @Positive
    @Column(name = "valor", nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @NotNull
    @PositiveOrZero
    @Column(name = "valor_pago", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorPago = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Column(name = "valor_aberto", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorAberto;

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
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private StatusParcelaReceber status = StatusParcelaReceber.ABERTA;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    @Column(name = "observacao")
    private String observacao;

    /**
     * Abre uma parcela nova sem pagamento registrado.
     */
    public void abrir(ContaReceber contaReceber, Integer numeroParcela, LocalDate dataVencimento,
            BigDecimal valor, String observacao) {
        this.contaReceber = contaReceber;
        this.numeroParcela = numeroParcela;
        this.dataVencimento = dataVencimento;
        this.valor = valor;
        this.valorPago = BigDecimal.ZERO;
        this.valorAberto = valor;
        this.juros = BigDecimal.ZERO;
        this.multa = BigDecimal.ZERO;
        this.desconto = BigDecimal.ZERO;
        this.status = StatusParcelaReceber.ABERTA;
        this.observacao = normalizar(observacao);
    }

    /**
     * Atualiza dados editaveis da parcela enquanto ela ainda nao foi encerrada.
     */
    public void atualizar(ContaReceber contaReceber, Integer numeroParcela, LocalDate dataVencimento,
            BigDecimal valor, String observacao) {
        validarEditavel();
        this.contaReceber = contaReceber;
        this.numeroParcela = numeroParcela;
        this.dataVencimento = dataVencimento;
        this.valor = valor;
        this.valorAberto = valor.subtract(valorPago == null ? BigDecimal.ZERO : valorPago);
        if (this.valorAberto.signum() < 0) {
            throw new InvalidOperationException("Valor da parcela nao pode ser menor que o valor ja pago");
        }
        this.observacao = normalizar(observacao);
    }

    /**
     * Registra uma baixa parcial ou total, separando principal de encargos.
     */
    public void baixar(BigDecimal valorPago, BigDecimal juros, BigDecimal multa, BigDecimal desconto,
            LocalDate dataPagamento, String observacao) {
        validarEditavel();
        BigDecimal jurosSeguro = zeroSeNulo(juros);
        BigDecimal multaSeguro = zeroSeNulo(multa);
        BigDecimal descontoSeguro = zeroSeNulo(desconto);
        BigDecimal limite = this.valorAberto.add(jurosSeguro).add(multaSeguro).subtract(descontoSeguro);
        if (valorPago.compareTo(limite) > 0) {
            throw new InvalidOperationException("Valor pago nao pode ultrapassar o saldo aberto com juros, multa e desconto");
        }
        BigDecimal principalBaixado = valorPago.subtract(jurosSeguro).subtract(multaSeguro).add(descontoSeguro);
        if (principalBaixado.signum() <= 0 || principalBaixado.compareTo(this.valorAberto) > 0) {
            throw new InvalidOperationException("Valor pago nao amortiza um principal valido da parcela");
        }
        this.valorPago = this.valorPago.add(principalBaixado);
        this.valorAberto = this.valorAberto.subtract(principalBaixado);
        this.juros = this.juros.add(jurosSeguro);
        this.multa = this.multa.add(multaSeguro);
        this.desconto = this.desconto.add(descontoSeguro);
        this.dataPagamento = dataPagamento;
        this.observacao = normalizar(observacao);
        this.status = this.valorAberto.signum() == 0 ? StatusParcelaReceber.PAGA : StatusParcelaReceber.PARCIALMENTE_PAGA;
    }

    /**
     * Estorna uma baixa gerada por recebimento cancelado, preservando o historico financeiro.
     */
    public void estornar(BigDecimal valorPago, BigDecimal juros, BigDecimal multa, BigDecimal desconto, String observacao) {
        if (status == StatusParcelaReceber.CANCELADA) {
            throw new InvalidOperationException("Parcela a receber cancelada nao permite estorno");
        }
        BigDecimal jurosSeguro = zeroSeNulo(juros);
        BigDecimal multaSeguro = zeroSeNulo(multa);
        BigDecimal descontoSeguro = zeroSeNulo(desconto);
        BigDecimal principalEstornado = valorPago.subtract(jurosSeguro).subtract(multaSeguro).add(descontoSeguro);
        if (principalEstornado.signum() <= 0 || principalEstornado.compareTo(this.valorPago) > 0) {
            throw new InvalidOperationException("Valor do estorno nao corresponde a um principal recebido valido");
        }
        this.valorPago = this.valorPago.subtract(principalEstornado);
        this.valorAberto = this.valorAberto.add(principalEstornado);
        this.juros = subtrairSemNegativar(this.juros, jurosSeguro, "Juros do estorno excedem os juros recebidos");
        this.multa = subtrairSemNegativar(this.multa, multaSeguro, "Multa do estorno excede a multa recebida");
        this.desconto = subtrairSemNegativar(this.desconto, descontoSeguro, "Desconto do estorno excede o desconto concedido");
        this.dataPagamento = this.valorPago.signum() == 0 ? null : this.dataPagamento;
        this.observacao = normalizar(observacao);
        this.status = this.valorPago.signum() == 0 ? StatusParcelaReceber.ABERTA : StatusParcelaReceber.PARCIALMENTE_PAGA;
    }

    /**
     * Cancela uma parcela sem remover o historico.
     */
    public void cancelar() {
        validarEditavel();
        this.status = StatusParcelaReceber.CANCELADA;
    }

    public void validarEditavel() {
        if (status == StatusParcelaReceber.PAGA) {
            throw new InvalidOperationException("Parcela a receber paga nao pode ser alterada");
        }
        if (status == StatusParcelaReceber.CANCELADA) {
            throw new InvalidOperationException("Parcela a receber cancelada nao pode ser alterada");
        }
    }

    private BigDecimal zeroSeNulo(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private BigDecimal subtrairSemNegativar(BigDecimal atual, BigDecimal valor, String mensagemErro) {
        BigDecimal resultado = zeroSeNulo(atual).subtract(valor);
        if (resultado.signum() < 0) {
            throw new InvalidOperationException(mensagemErro);
        }
        return resultado;
    }

    private String normalizar(String valor) {
        return valor == null ? null : valor.trim();
    }
}
