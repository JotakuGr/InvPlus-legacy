package br.com.invplus.security.entity;

import java.util.Objects;
import java.util.UUID;

import org.hibernate.Hibernate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "permissao",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_permissao_recurso_acao", columnNames = {"recurso", "acao"})
        },
        indexes = {
                @Index(name = "idx_permissao_ativo", columnList = "recurso, acao")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Permissao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @NotBlank
    @Size(max = 80)
    @Column(name = "nome", nullable = false, length = 80)
    private String nome;

    @NotBlank
    @Size(max = 80)
    @Column(name = "recurso", nullable = false, length = 80)
    private String recurso;

    @NotBlank
    @Size(max = 20)
    @Pattern(regexp = "CRIAR|VISUALIZAR|EDITAR|EXCLUIR|EXPORTAR|APROVAR|\\*")
    @Column(name = "acao", nullable = false, length = 20)
    private String acao;

    @NotNull
    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    public Permissao(String nome, String recurso, String acao) {
        this.nome = nome;
        this.recurso = recurso;
        this.acao = acao;
    }

    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Permissao permissao)) {
            return false;
        }
        if (Hibernate.getClass(this) != Hibernate.getClass(permissao)) {
            return false;
        }
        return id != null && Objects.equals(id, permissao.id);
    }

    @Override
    public final int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
