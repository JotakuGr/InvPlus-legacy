package br.com.invplus.modules.produtos.entity;

import br.com.invplus.modules.pessoas.entity.Fornecedor;
import br.com.invplus.modules.produtos.enums.FornecedorProdutoStatus;
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
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Vinculo comercial entre um Produto e um Fornecedor usado por compras e cotacoes.
 */
@Getter
@Setter
@Entity
@Table(
        name = "produto_fornecedor",
        indexes = {
                @Index(name = "idx_produto_fornecedor_produto", columnList = "produto_id"),
                @Index(name = "idx_produto_fornecedor_fornecedor", columnList = "fornecedor_id"),
                @Index(name = "idx_produto_fornecedor_ativo", columnList = "ativo")
        }
)
@NoArgsConstructor
public class ProdutoFornecedor extends SoftDeleteEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fornecedor_id", nullable = false)
    private Fornecedor fornecedor;

    @Size(max = 120)
    @Column(name = "codigo_fornecedor", length = 120)
    private String codigoFornecedor;

    @NotNull
    @PositiveOrZero
    @Column(name = "lead_time_dias", nullable = false)
    private Integer leadTimeDias = 0;

    @Column(name = "principal", nullable = false)
    private Boolean principal = false;

    @NotNull
    @PositiveOrZero
    @Column(name = "prioridade", nullable = false)
    private Integer prioridade = 0;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private FornecedorProdutoStatus status = FornecedorProdutoStatus.EVENTUAL;

    @Column(name = "observacao")
    private String observacao;

    /**
     * Atualiza as condicoes comerciais do fornecedor para este produto.
     */
    public void atualizar(
            String codigoFornecedor,
            Integer leadTimeDias,
            Boolean principal,
            Integer prioridade,
            FornecedorProdutoStatus status,
            String observacao
    ) {
        this.codigoFornecedor = normalizarTexto(codigoFornecedor);
        this.leadTimeDias = leadTimeDias == null ? 0 : leadTimeDias;
        this.principal = Boolean.TRUE.equals(principal);
        this.prioridade = prioridade == null ? 0 : prioridade;
        this.status = status == null ? FornecedorProdutoStatus.EVENTUAL : status;
        this.observacao = normalizarTexto(observacao);
    }

    private String normalizarTexto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
