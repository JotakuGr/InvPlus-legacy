package br.com.invplus.modules.produtos.entity;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.produtos.enums.TipoPrecoProduto;
import br.com.invplus.shared.domain.entity.SoftDeleteEntity;
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
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Historico de precos comerciais do produto com tipo, vigencia e empresa opcional.
 */
@Getter
@Setter
@Entity
@Table(
        name = "produto_preco",
        indexes = {
                @Index(name = "idx_produto_preco_produto", columnList = "produto_id"),
                @Index(name = "idx_produto_preco_empresa", columnList = "empresa_id"),
                @Index(name = "idx_produto_preco_tipo", columnList = "tipo_preco"),
                @Index(name = "idx_produto_preco_ativo", columnList = "ativo")
        }
)
@NoArgsConstructor
public class ProdutoPreco extends SoftDeleteEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_preco", nullable = false, length = 30)
    private TipoPrecoProduto tipoPreco = TipoPrecoProduto.VENDA;

    @NotNull
    @PositiveOrZero
    @Column(name = "preco", nullable = false, precision = 15, scale = 4)
    private BigDecimal preco = BigDecimal.ZERO;

    @NotNull
    @Column(name = "vigencia_inicio", nullable = false)
    private LocalDate vigenciaInicio;

    @Column(name = "vigencia_fim")
    private LocalDate vigenciaFim;

    @Column(name = "principal", nullable = false)
    private Boolean principal = false;

    @Column(name = "observacao")
    private String observacao;

    /**
     * Atualiza os dados editaveis do preco sem trocar os relacionamentos.
     */
    public void atualizar(
            TipoPrecoProduto tipoPreco,
            BigDecimal preco,
            LocalDate vigenciaInicio,
            LocalDate vigenciaFim,
            Boolean principal,
            String observacao
    ) {
        this.tipoPreco = tipoPreco == null ? TipoPrecoProduto.VENDA : tipoPreco;
        this.preco = preco == null ? BigDecimal.ZERO : preco;
        this.vigenciaInicio = vigenciaInicio;
        this.vigenciaFim = vigenciaFim;
        this.principal = Boolean.TRUE.equals(principal);
        this.observacao = normalizarTexto(observacao);
    }

    private String normalizarTexto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
