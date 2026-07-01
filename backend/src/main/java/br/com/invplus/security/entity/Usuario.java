package br.com.invplus.security.entity;

import br.com.invplus.shared.domain.entity.SoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(
        name = "usuario",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_usuario_email", columnNames = "email")
        },
        indexes = {
                @Index(name = "idx_usuario_ativo_deleted", columnList = "ativo, deleted_at"),
                @Index(name = "idx_usuario_deleted_at", columnList = "deleted_at"),
                @Index(name = "idx_usuario_ultimo_login", columnList = "ultimo_login")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Usuario extends SoftDeleteEntity {

    @NotBlank
    @Size(max = 120)
    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @NotBlank
    @Email
    @Size(max = 320)
    @Column(name = "email", nullable = false, length = 320, columnDefinition = "citext")
    private String email;

    @NotBlank
    @Size(max = 255)
    @Column(name = "senha_hash", nullable = false, length = 255)
    private String senhaHash;

    @Column(name = "ultimo_login")
    private OffsetDateTime ultimoLogin;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "usuario_perfil",
            joinColumns = @JoinColumn(name = "usuario_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "perfil_id", nullable = false)
    )
    private Set<Perfil> perfis = new LinkedHashSet<>();

    public Usuario(String nome, String email, String senhaHash) {
        this.nome = nome;
        this.email = normalizarEmail(email);
        this.senhaHash = senhaHash;
    }

    public void alterarEmail(String email) {
        this.email = normalizarEmail(email);
    }

    private String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
