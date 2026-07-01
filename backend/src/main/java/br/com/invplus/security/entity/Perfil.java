package br.com.invplus.security.entity;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.hibernate.Hibernate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "perfil",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_perfil_nome", columnNames = "nome")
        },
        indexes = {
                @Index(name = "idx_perfil_ativo", columnList = "ativo")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @NotBlank
    @Size(max = 80)
    @Column(name = "nome", nullable = false, length = 80)
    private String nome;

    @Column(name = "descricao", columnDefinition = "text")
    private String descricao;

    @NotNull
    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "perfil_permissao",
            joinColumns = @JoinColumn(name = "perfil_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "permissao_id", nullable = false)
    )
    private Set<Permissao> permissoes = new LinkedHashSet<>();

    public Perfil(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    @PrePersist
    protected void prePersist() {
        this.criadoEm = OffsetDateTime.now();
    }

    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Perfil perfil)) {
            return false;
        }
        if (Hibernate.getClass(this) != Hibernate.getClass(perfil)) {
            return false;
        }
        return id != null && Objects.equals(id, perfil.id);
    }

    @Override
    public final int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
