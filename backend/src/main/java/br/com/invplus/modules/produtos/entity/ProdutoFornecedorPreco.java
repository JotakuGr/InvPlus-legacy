package br.com.invplus.modules.produtos.entity;

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
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Historico de custo do produto em um fornecedor com controle de vigencia.
 */
@Getter
@Setter
@Entity
@Table(
        name = "produto_fornecedor_preco",
        indexes = {
                @Index(name = "idx_produto_fornecedor_preco_vinculo", columnList = "produto_fornecedor_id"),
                @Index(name = "idx_produto_fornecedor_preco_ativo", columnList = "ativo")
        }
)
@NoArgsConstructor
public class ProdutoFornecedorPreco extends SoftDeleteEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_fornecedor_id", nullable = false)
    private ProdutoFornecedor produtoFornecedor;

    @NotNull
    @PositiveOrZero
    @Column(name = "preco", nullable = false, precision = 15, scale = 4)
    private BigDecimal preco = BigDecimal.ZERO;

    @NotNull
    @Column(name = "vigencia_inicio", nullable = false)
    private LocalDate vigenciaInicio;

    @Column(name = "vigencia_fim")
    private LocalDate vigenciaFim;

    /**
     * Atualiza o preco e sua janela de validade.
     */
    public void atualizar(BigDecimal preco, LocalDate vigenciaInicio, LocalDate vigenciaFim) {
        this.preco = preco == null ? BigDecimal.ZERO : preco;
        this.vigenciaInicio = vigenciaInicio;
        this.vigenciaFim = vigenciaFim;
    }
}
