package br.com.invplus.modules.pessoas.entity;

import br.com.invplus.common.enums.PapelPessoa;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Indice auxiliar dos papeis ativos exercidos por uma Pessoa Aggregate Root.
 */
@Getter
@Setter
@Entity
@Table(
        name = "pessoa_role",
        indexes = {
                @Index(name = "idx_pessoa_role_pessoa", columnList = "pessoa_id"),
                @Index(name = "idx_pessoa_role_role", columnList = "role")
        }
)
@NoArgsConstructor
public class PessoaRole extends SoftDeleteEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 40)
    private PapelPessoa role;

    /**
     * Vincula uma Pessoa ao papel informado.
     */
    public void atualizar(Pessoa pessoa, PapelPessoa role) {
        this.pessoa = pessoa;
        this.role = role;
    }
}
