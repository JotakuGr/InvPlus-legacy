package br.com.invplus.modules.financeiro.entity;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.financeiro.enums.TipoCentroCusto;
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
 * Classificacao hierarquica de receitas e despesas dentro de uma empresa.
 */
@Getter
@Setter
@Entity
@Table(name = "centro_custo", indexes = {
        @Index(name = "idx_centro_custo_empresa_jpa", columnList = "empresa_id"),
        @Index(name = "idx_centro_custo_pai_jpa", columnList = "centro_custo_pai_id"),
        @Index(name = "idx_centro_custo_tipo_jpa", columnList = "tipo")
})
@NoArgsConstructor
public class CentroCusto extends SoftDeleteEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "centro_custo_pai_id")
    private CentroCusto centroCustoPai;

    @NotBlank
    @Size(max = 40)
    @Column(name = "codigo", nullable = false, length = 40)
    private String codigo;

    @NotBlank
    @Size(max = 120)
    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Column(name = "descricao")
    private String descricao;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoCentroCusto tipo;

    /**
     * Atualiza os dados depois que empresa e pai foram resolvidos pelo service.
     */
    public void atualizar(Empresa empresa, CentroCusto centroCustoPai, String codigo,
            String nome, String descricao, TipoCentroCusto tipo) {
        this.empresa = empresa;
        this.centroCustoPai = centroCustoPai;
        this.codigo = codigo == null ? null : codigo.trim().toUpperCase();
        this.nome = nome == null ? null : nome.trim();
        this.descricao = descricao == null || descricao.isBlank() ? null : descricao.trim();
        this.tipo = tipo;
    }

    /**
     * Retorna a trilha hierarquica usada em seletores, filtros e relatorios.
     */
    public String caminhoCompleto() {
        return centroCustoPai == null ? nome : centroCustoPai.caminhoCompleto() + " > " + nome;
    }
}
