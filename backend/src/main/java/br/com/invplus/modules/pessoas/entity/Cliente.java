package br.com.invplus.modules.pessoas.entity;

import br.com.invplus.shared.domain.entity.SoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Papel comercial de cliente associado a uma pessoa ja cadastrada.
 */
@Getter
@Setter
@Entity
@Table(
        name = "cliente",
        indexes = {
                @Index(name = "idx_cliente_pessoa", columnList = "pessoa_id"),
                @Index(name = "idx_cliente_ativo", columnList = "ativo")
        }
)
@NoArgsConstructor
public class Cliente extends SoftDeleteEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;

    @NotNull
    @PositiveOrZero
    @Column(name = "limite_credito", nullable = false, precision = 15, scale = 2)
    private BigDecimal limiteCredito = BigDecimal.ZERO;

    @Column(name = "condicao_pagamento_id")
    private UUID condicaoPagamentoId;

    @Column(name = "tabela_preco_id")
    private UUID tabelaPrecoId;

    @Column(name = "observacao")
    private String observacao;

    /**
     * Atualiza os dados comerciais do cliente sem alterar os dados cadastrais da pessoa.
     */
    public void atualizar(
            BigDecimal limiteCredito,
            UUID condicaoPagamentoId,
            UUID tabelaPrecoId,
            String observacao
    ) {
        this.limiteCredito = limiteCredito == null ? BigDecimal.ZERO : limiteCredito;
        this.condicaoPagamentoId = condicaoPagamentoId;
        this.tabelaPrecoId = tabelaPrecoId;
        this.observacao = observacao == null || observacao.isBlank() ? null : observacao.trim();
    }
}
