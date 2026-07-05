package br.com.invplus.modules.produtos.entity;

import br.com.invplus.modules.cadastros.entity.UnidadeMedida;
import br.com.invplus.shared.domain.entity.SoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Unidade alternativa do produto para compra, venda, estoque e conversao.
 */
@Getter
@Setter
@Entity
@Table(
        name = "produto_unidade",
        indexes = {
                @Index(name = "idx_produto_unidade_produto", columnList = "produto_id"),
                @Index(name = "idx_produto_unidade_unidade", columnList = "unidade_medida_id"),
                @Index(name = "idx_produto_unidade_ativo", columnList = "ativo")
        }
)
@NoArgsConstructor
public class ProdutoUnidade extends SoftDeleteEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unidade_medida_id", nullable = false)
    private UnidadeMedida unidadeMedida;

    @NotNull
    @DecimalMin(value = "0.000001")
    @Column(name = "fator_conversao", nullable = false, precision = 18, scale = 6)
    private BigDecimal fatorConversao = BigDecimal.ONE;

    @Column(name = "unidade_padrao", nullable = false)
    private Boolean unidadePadrao = false;

    @Column(name = "permite_compra", nullable = false)
    private Boolean permiteCompra = false;

    @Column(name = "permite_venda", nullable = false)
    private Boolean permiteVenda = false;

    @Column(name = "permite_estoque", nullable = false)
    private Boolean permiteEstoque = false;

    @Column(name = "permite_fracionamento", nullable = false)
    private Boolean permiteFracionamento = false;

    @Column(name = "observacao")
    private String observacao;

    /**
     * Atualiza os dados de conversao e permissao operacional da unidade.
     */
    public void atualizar(
            BigDecimal fatorConversao,
            Boolean unidadePadrao,
            Boolean permiteCompra,
            Boolean permiteVenda,
            Boolean permiteEstoque,
            Boolean permiteFracionamento,
            String observacao
    ) {
        this.fatorConversao = fatorConversao == null ? BigDecimal.ONE : fatorConversao;
        this.unidadePadrao = Boolean.TRUE.equals(unidadePadrao);
        this.permiteCompra = Boolean.TRUE.equals(permiteCompra);
        this.permiteVenda = Boolean.TRUE.equals(permiteVenda);
        this.permiteEstoque = Boolean.TRUE.equals(permiteEstoque);
        this.permiteFracionamento = Boolean.TRUE.equals(permiteFracionamento);
        this.observacao = normalizarTexto(observacao);
    }

    private String normalizarTexto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
