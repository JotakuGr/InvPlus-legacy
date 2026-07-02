package br.com.invplus.modules.cadastros.entity;

import br.com.invplus.shared.domain.entity.SoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
        uniqueConstraints = @UniqueConstraint(name = "uk_marca_nome", columnNames = "nome")
)
@NoArgsConstructor
public class Marca extends SoftDeleteEntity {

    @NotBlank
    @Size(max = 100)
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "descricao", columnDefinition = "text")
    private String descricao;

    /**
     * Atualiza os dados exibidos nas telas de cadastro de marca.
     */
    public void atualizar(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }
}
