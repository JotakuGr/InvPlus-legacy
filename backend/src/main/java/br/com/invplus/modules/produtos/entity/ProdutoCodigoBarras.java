package br.com.invplus.modules.produtos.entity;

import br.com.invplus.modules.produtos.enums.TipoCodigoBarras;
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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Codigo de barras ou codigo alternativo associado a um Produto.
 */
@Getter
@Setter
@Entity
@Table(
        name = "produto_codigo_barras",
        indexes = {
                @Index(name = "idx_produto_codigo_barras_produto", columnList = "produto_id"),
                @Index(name = "idx_produto_codigo_barras_tipo", columnList = "tipo_codigo"),
                @Index(name = "idx_produto_codigo_barras_ativo", columnList = "ativo")
        }
)
@NoArgsConstructor
public class ProdutoCodigoBarras extends SoftDeleteEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_codigo", nullable = false, length = 40)
    private TipoCodigoBarras tipoCodigo = TipoCodigoBarras.EAN_UNIDADE;

    @NotBlank
    @Size(max = 120)
    @Column(name = "codigo", nullable = false, length = 120)
    private String codigo;

    @Column(name = "principal", nullable = false)
    private Boolean principal = false;

    @Column(name = "observacao")
    private String observacao;

    /**
     * Atualiza os dados do codigo alternativo do produto.
     */
    public void atualizar(TipoCodigoBarras tipoCodigo, String codigo, Boolean principal, String observacao) {
        this.tipoCodigo = tipoCodigo == null ? TipoCodigoBarras.EAN_UNIDADE : tipoCodigo;
        this.codigo = codigo == null ? null : codigo.trim();
        this.principal = Boolean.TRUE.equals(principal);
        this.observacao = observacao == null || observacao.isBlank() ? null : observacao.trim();
    }
}
