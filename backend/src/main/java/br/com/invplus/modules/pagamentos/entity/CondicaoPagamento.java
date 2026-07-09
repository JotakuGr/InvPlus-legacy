package br.com.invplus.modules.pagamentos.entity;

import br.com.invplus.modules.financeiro.enums.RegraVencimento;
import br.com.invplus.shared.domain.entity.SoftDeleteEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Regra de vencimento, parcelamento e encargos usada por compras, vendas e financeiro.
 */
@Getter
@Setter
@Entity
@Table(
        name = "condicao_pagamento",
        indexes = {
                @Index(name = "idx_condicao_pagamento_codigo", columnList = "codigo")
        }
)
@NoArgsConstructor
public class CondicaoPagamento extends SoftDeleteEntity {

    @NotBlank
    @Column(name = "codigo", nullable = false, length = 30)
    private String codigo;

    @NotBlank
    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @NotNull
    @PositiveOrZero
    @Column(name = "juros_percentual", nullable = false, precision = 7, scale = 4)
    private BigDecimal jurosPercentual = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Column(name = "multa_percentual", nullable = false, precision = 7, scale = 4)
    private BigDecimal multaPercentual = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Column(name = "desconto_percentual", nullable = false, precision = 7, scale = 4)
    private BigDecimal descontoPercentual = BigDecimal.ZERO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "regra_vencimento", nullable = false, length = 30)
    private RegraVencimento regraVencimento = RegraVencimento.PROXIMO_DIA_UTIL;

    @NotNull
    @Column(name = "considera_feriados", nullable = false)
    private Boolean consideraFeriados = true;

    @OneToMany(mappedBy = "condicaoPagamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CondicaoPagamentoParcela> parcelas = new ArrayList<>();

    /**
     * Atualiza a regra aplicando defaults para campos opcionais.
     */
    public void atualizar(
            String codigo,
            String nome,
            BigDecimal jurosPercentual,
            BigDecimal multaPercentual,
            BigDecimal descontoPercentual,
            RegraVencimento regraVencimento,
            Boolean consideraFeriados
    ) {
        this.codigo = normalizar(codigo);
        this.nome = normalizar(nome);
        this.jurosPercentual = zeroSeNulo(jurosPercentual);
        this.multaPercentual = zeroSeNulo(multaPercentual);
        this.descontoPercentual = zeroSeNulo(descontoPercentual);
        this.regraVencimento = regraVencimento == null ? RegraVencimento.PROXIMO_DIA_UTIL : regraVencimento;
        this.consideraFeriados = consideraFeriados == null || consideraFeriados;
    }

    /**
     * Substitui a grade de parcelas mantendo o relacionamento bidirecional consistente.
     */
    public void substituirParcelas(List<CondicaoPagamentoParcela> novasParcelas) {
        parcelas.clear();
        if (novasParcelas != null) {
            novasParcelas.forEach(this::adicionarParcela);
        }
    }

    private void adicionarParcela(CondicaoPagamentoParcela parcela) {
        parcela.setCondicaoPagamento(this);
        parcelas.add(parcela);
    }

    private String normalizar(String valor) {
        return valor == null ? null : valor.trim();
    }

    private BigDecimal zeroSeNulo(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
