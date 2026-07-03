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
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Papel comercial de fornecedor associado a uma pessoa ja cadastrada.
 */
@Getter
@Setter
@Entity
@Table(
        name = "fornecedor",
        indexes = {
                @Index(name = "idx_fornecedor_pessoa", columnList = "pessoa_id"),
                @Index(name = "idx_fornecedor_ativo", columnList = "ativo")
        }
)
@NoArgsConstructor
public class Fornecedor extends SoftDeleteEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;

    @Column(name = "condicao_pagamento_id")
    private UUID condicaoPagamentoId;

    @NotNull
    @PositiveOrZero
    @Column(name = "prazo_medio_entrega", nullable = false)
    private Integer prazoMedioEntrega = 0;

    @Column(name = "observacao")
    private String observacao;

    /**
     * Atualiza dados comerciais do fornecedor sem alterar os dados cadastrais da pessoa.
     */
    public void atualizar(UUID condicaoPagamentoId, Integer prazoMedioEntrega, String observacao) {
        this.condicaoPagamentoId = condicaoPagamentoId;
        this.prazoMedioEntrega = prazoMedioEntrega == null ? 0 : prazoMedioEntrega;
        this.observacao = observacao == null || observacao.isBlank() ? null : observacao.trim();
    }
}
