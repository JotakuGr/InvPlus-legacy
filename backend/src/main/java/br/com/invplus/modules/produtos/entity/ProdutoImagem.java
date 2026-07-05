package br.com.invplus.modules.produtos.entity;

import br.com.invplus.modules.cadastros.entity.Arquivo;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Vincula um Produto a um Arquivo usado como imagem ou anexo visual do catalogo.
 */
@Getter
@Setter
@Entity
@Table(
        name = "produto_imagem",
        indexes = {
                @Index(name = "idx_produto_imagem_produto", columnList = "produto_id"),
                @Index(name = "idx_produto_imagem_arquivo", columnList = "arquivo_id"),
                @Index(name = "idx_produto_imagem_ativo", columnList = "ativo")
        }
)
@NoArgsConstructor
public class ProdutoImagem extends SoftDeleteEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "arquivo_id", nullable = false)
    private Arquivo arquivo;

    @Column(name = "principal", nullable = false)
    private Boolean principal = false;

    @NotNull
    @PositiveOrZero
    @Column(name = "ordem", nullable = false)
    private Integer ordem = 0;

    @Column(name = "descricao")
    private String descricao;

    /**
     * Atualiza campos de exibicao da imagem do produto.
     */
    public void atualizar(Boolean principal, Integer ordem, String descricao) {
        this.principal = Boolean.TRUE.equals(principal);
        this.ordem = ordem == null ? 0 : ordem;
        this.descricao = descricao == null || descricao.isBlank() ? null : descricao.trim();
    }
}
