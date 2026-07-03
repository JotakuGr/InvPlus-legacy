package br.com.invplus.modules.cadastros.entity;

import br.com.invplus.shared.domain.entity.SoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa uma marca comercial usada no catalogo.
 */
@Getter
@Setter
@Entity
@Table(
        name = "marca",
        indexes = @Index(name = "idx_marca_ativo_nome", columnList = "ativo, nome")
)
@NoArgsConstructor
public class Marca extends SoftDeleteEntity {

    @NotBlank
    @Size(max = 100)
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "descricao", columnDefinition = "text")
    private String descricao;

    @Size(max = 255)
    @Column(name = "site", length = 255)
    private String site;

    @Size(max = 120)
    @Column(name = "fabricante", length = 120)
    private String fabricante;

    /**
     * Atualiza os dados exibidos nas telas de cadastro de marca.
     */
    public void atualizar(String nome, String descricao, String site, String fabricante) {
        this.nome = nome == null ? null : nome.trim();
        this.descricao = descricao == null || descricao.isBlank() ? null : descricao.trim();
        this.site = site == null || site.isBlank() ? null : site.trim();
        this.fabricante = fabricante == null || fabricante.isBlank() ? null : fabricante.trim();
    }

    /**
     * Mantem compatibilidade com chamadas antigas sem site/fabricante.
     */
    public void atualizar(String nome, String descricao) {
        atualizar(nome, descricao, null, null);
    }
}
