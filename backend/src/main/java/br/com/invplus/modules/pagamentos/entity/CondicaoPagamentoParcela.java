package br.com.invplus.modules.pagamentos.entity;

import java.math.BigDecimal;

import br.com.invplus.shared.domain.entity.SoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Parcela configurada dentro de uma condicao de pagamento.
 */
@Getter
@Setter
@Entity
@Table(name = "condicao_pagamento_parcela")
@NoArgsConstructor
public class CondicaoPagamentoParcela extends SoftDeleteEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "condicao_pagamento_id", nullable = false)
    private CondicaoPagamento condicaoPagamento;

    @NotNull
    @Min(1)
    @Column(name = "numero_parcela", nullable = false)
    private Integer numeroParcela;

    @NotNull
    @Min(0)
    @Column(name = "dias", nullable = false)
    private Integer dias;

    @NotNull
    @DecimalMin(value = "0.0001")
    @DecimalMax(value = "100.0000")
    @Column(name = "percentual", nullable = false, precision = 7, scale = 4)
    private BigDecimal percentual;

    /**
     * Atualiza os dados simples da parcela mantendo o vinculo com a condicao.
     */
    public void atualizar(CondicaoPagamento condicaoPagamento, Integer numeroParcela, Integer dias, BigDecimal percentual) {
        this.condicaoPagamento = condicaoPagamento;
        this.numeroParcela = numeroParcela;
        this.dias = dias;
        this.percentual = percentual;
    }
}
