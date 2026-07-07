package br.com.invplus.modules.estoque.entity;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.shared.domain.entity.SoftDeleteEntity;
import br.com.invplus.shared.domain.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Saldo fisico atual de um produto em uma localizacao especifica.
 */
@Getter
@Setter
@Entity
@Table(name = "estoque")
@NoArgsConstructor
public class Estoque extends SoftDeleteEntity {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filial_id")
    private Filial filial;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deposito_id", nullable = false)
    private Deposito deposito;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "localizacao_id", nullable = false)
    private LocalizacaoEstoque localizacao;

    @NotNull
    @DecimalMin("0.000")
    @Column(name = "quantidade", nullable = false, precision = 14, scale = 3)
    private BigDecimal quantidade = ZERO;

    @NotNull
    @DecimalMin("0.000")
    @Column(name = "quantidade_reservada", nullable = false, precision = 14, scale = 3)
    private BigDecimal quantidadeReservada = ZERO;

    @NotNull
    @DecimalMin("0.000")
    @Column(name = "quantidade_disponivel", nullable = false, precision = 14, scale = 3)
    private BigDecimal quantidadeDisponivel = ZERO;

    @NotNull
    @DecimalMin("0.000")
    @Column(name = "quantidade_avariada", nullable = false, precision = 14, scale = 3)
    private BigDecimal quantidadeAvariada = ZERO;

    @NotNull
    @DecimalMin("0.000")
    @Column(name = "quantidade_transito", nullable = false, precision = 14, scale = 3)
    private BigDecimal quantidadeTransito = ZERO;

    @NotNull
    @DecimalMin("0.000")
    @Column(name = "estoque_minimo", nullable = false, precision = 14, scale = 3)
    private BigDecimal estoqueMinimo = ZERO;

    @DecimalMin("0.000")
    @Column(name = "estoque_maximo", precision = 14, scale = 3)
    private BigDecimal estoqueMaximo;

    @Column(name = "ultimo_movimento_em")
    private OffsetDateTime ultimoMovimentoEm;

    @Column(name = "ultimo_movimento_id")
    private UUID ultimoMovimentoId;

    /**
     * Atualiza os saldos iniciais e recalcula a disponibilidade.
     */
    public void atualizarSaldos(
            BigDecimal quantidade,
            BigDecimal quantidadeReservada,
            BigDecimal quantidadeAvariada,
            BigDecimal quantidadeTransito,
            BigDecimal estoqueMinimo,
            BigDecimal estoqueMaximo,
            OffsetDateTime ultimoMovimentoEm,
            UUID ultimoMovimentoId
    ) {
        this.quantidade = valorOuZero(quantidade);
        this.quantidadeReservada = valorOuZero(quantidadeReservada);
        this.quantidadeAvariada = valorOuZero(quantidadeAvariada);
        this.quantidadeTransito = valorOuZero(quantidadeTransito);
        this.estoqueMinimo = valorOuZero(estoqueMinimo);
        this.estoqueMaximo = estoqueMaximo;
        this.ultimoMovimentoEm = ultimoMovimentoEm;
        this.ultimoMovimentoId = ultimoMovimentoId;
        recalcularDisponivel();
    }

    /**
     * Adiciona saldo fisico ao estoque atual.
     */
    public void adicionar(BigDecimal valor) {
        validarPositivo(valor, "Quantidade para adicionar deve ser maior que zero");
        quantidade = quantidade.add(valor);
        recalcularDisponivel();
        marcarMovimento();
    }

    /**
     * Retira saldo fisico disponivel do estoque atual.
     */
    public void retirar(BigDecimal valor) {
        validarPositivo(valor, "Quantidade para retirar deve ser maior que zero");
        if (quantidadeDisponivel.compareTo(valor) < 0) {
            throw new BusinessException("Quantidade disponivel insuficiente");
        }
        quantidade = quantidade.subtract(valor);
        recalcularDisponivel();
        marcarMovimento();
    }

    /**
     * Reserva quantidade disponivel para venda, separacao ou outro processo.
     */
    public void reservar(BigDecimal valor) {
        validarPositivo(valor, "Quantidade para reservar deve ser maior que zero");
        if (quantidadeDisponivel.compareTo(valor) < 0) {
            throw new BusinessException("Quantidade disponivel insuficiente para reserva");
        }
        quantidadeReservada = quantidadeReservada.add(valor);
        recalcularDisponivel();
        marcarMovimento();
    }

    /**
     * Cancela uma reserva e devolve o saldo para disponibilidade.
     */
    public void cancelarReserva(BigDecimal valor) {
        validarPositivo(valor, "Quantidade para cancelar reserva deve ser maior que zero");
        if (quantidadeReservada.compareTo(valor) < 0) {
            throw new BusinessException("Quantidade reservada insuficiente");
        }
        quantidadeReservada = quantidadeReservada.subtract(valor);
        recalcularDisponivel();
        marcarMovimento();
    }

    /**
     * Baixa uma reserva, reduzindo a quantidade fisica e a reservada ao mesmo tempo.
     */
    public void baixarReserva(BigDecimal valor) {
        validarPositivo(valor, "Quantidade para baixar reserva deve ser maior que zero");
        if (quantidadeReservada.compareTo(valor) < 0) {
            throw new BusinessException("Quantidade reservada insuficiente");
        }
        quantidadeReservada = quantidadeReservada.subtract(valor);
        quantidade = quantidade.subtract(valor);
        recalcularDisponivel();
        marcarMovimento();
    }

    /**
     * Move parte do saldo para quantidade avariada.
     */
    public void adicionarAvaria(BigDecimal valor) {
        validarPositivo(valor, "Quantidade avariada deve ser maior que zero");
        if (quantidadeDisponivel.compareTo(valor) < 0) {
            throw new BusinessException("Quantidade disponivel insuficiente para avaria");
        }
        quantidadeAvariada = quantidadeAvariada.add(valor);
        quantidade = quantidade.subtract(valor);
        recalcularDisponivel();
        marcarMovimento();
    }

    /**
     * Remove saldo avariado e devolve para quantidade fisica disponivel.
     */
    public void removerAvaria(BigDecimal valor) {
        validarPositivo(valor, "Quantidade para remover avaria deve ser maior que zero");
        if (quantidadeAvariada.compareTo(valor) < 0) {
            throw new BusinessException("Quantidade avariada insuficiente");
        }
        quantidadeAvariada = quantidadeAvariada.subtract(valor);
        quantidade = quantidade.add(valor);
        recalcularDisponivel();
        marcarMovimento();
    }

    /**
     * Mantem quantidade_disponivel derivada de quantidade - quantidade_reservada.
     */
    public void recalcularDisponivel() {
        if (quantidadeReservada.compareTo(quantidade) > 0) {
            throw new BusinessException("Quantidade reservada nao pode ser maior que a quantidade fisica");
        }
        quantidadeDisponivel = quantidade.subtract(quantidadeReservada);
    }

    private void marcarMovimento() {
        ultimoMovimentoEm = OffsetDateTime.now();
    }

    private BigDecimal valorOuZero(BigDecimal valor) {
        return valor == null ? ZERO : valor;
    }

    private void validarPositivo(BigDecimal valor, String mensagem) {
        if (valor == null || valor.compareTo(ZERO) <= 0) {
            throw new BusinessException(mensagem);
        }
    }
}
