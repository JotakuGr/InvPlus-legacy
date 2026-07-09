package br.com.invplus.modules.financeiro.entity;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.financeiro.enums.NaturezaPlanoConta;
import br.com.invplus.modules.financeiro.enums.TipoPlanoConta;
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
 * Conta hierarquica usada para classificacao contabil e financeira.
 */
@Getter
@Setter
@Entity
@Table(name = "plano_conta", indexes = {
        @Index(name = "idx_plano_conta_empresa_jpa", columnList = "empresa_id"),
        @Index(name = "idx_plano_conta_pai_jpa", columnList = "plano_conta_pai_id"),
        @Index(name = "idx_plano_conta_tipo_jpa", columnList = "tipo"),
        @Index(name = "idx_plano_conta_natureza_jpa", columnList = "natureza")
})
@NoArgsConstructor
public class PlanoConta extends SoftDeleteEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plano_conta_pai_id")
    private PlanoConta planoContaPai;

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
    private TipoPlanoConta tipo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "natureza", nullable = false, length = 30)
    private NaturezaPlanoConta natureza;

    /**
     * Atualiza os dados depois que as referencias foram resolvidas pelo service.
     */
    public void atualizar(Empresa empresa, PlanoConta planoContaPai, String codigo,
            String nome, String descricao, TipoPlanoConta tipo, NaturezaPlanoConta natureza) {
        this.empresa = empresa;
        this.planoContaPai = planoContaPai;
        this.codigo = codigo == null ? null : codigo.trim().toUpperCase();
        this.nome = nome == null ? null : nome.trim();
        this.descricao = descricao == null || descricao.isBlank() ? null : descricao.trim();
        this.tipo = tipo;
        this.natureza = natureza;
    }

    /**
     * Indica se a conta pode receber lancamentos financeiros.
     */
    public boolean permiteLancamento() {
        return TipoPlanoConta.ANALITICA.equals(tipo);
    }

    /**
     * Retorna a trilha hierarquica exibida em seletores e relatorios.
     */
    public String caminhoCompleto() {
        return planoContaPai == null ? nome : planoContaPai.caminhoCompleto() + " > " + nome;
    }
}
