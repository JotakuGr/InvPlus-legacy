package br.com.invplus.modules.cadastros.entity;

import br.com.invplus.shared.domain.entity.SoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Classificacao generica reutilizada por produtos, pessoas e outros cadastros.
 */
@Getter
@Setter
@Entity
@Table(
        name = "categoria",
        indexes = {
                @Index(name = "idx_categoria_pai", columnList = "categoria_pai_id"),
                @Index(name = "idx_categoria_ativo_nome", columnList = "ativo, nome")
        }
)
@NoArgsConstructor
public class Categoria extends SoftDeleteEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_pai_id")
    private Categoria categoriaPai;

    @NotBlank
    @Size(max = 100)
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "descricao", columnDefinition = "text")
    private String descricao;

    @NotBlank
    @Size(max = 40)
    @Column(name = "tipo", nullable = false, length = 40)
    private String tipo = "GERAL";

    /**
     * Atualiza a categoria no contrato novo, preservando tipo como campo legado.
     */
    public void atualizar(Categoria categoriaPai, String nome, String descricao) {
        this.categoriaPai = categoriaPai;
        this.nome = nome == null ? null : nome.trim();
        this.descricao = descricao == null || descricao.isBlank() ? null : descricao.trim();
        this.tipo = "GERAL";
    }

    /**
     * Atualiza os dados principais e normaliza o tipo para facilitar filtros.
     */
    public void atualizar(String nome, String descricao, String tipo) {
        atualizar((Categoria) null, nome, descricao);
        this.tipo = tipo == null || tipo.isBlank() ? "GERAL" : tipo.trim().toUpperCase();
    }
}
