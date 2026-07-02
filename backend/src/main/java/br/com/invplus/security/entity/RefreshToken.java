package br.com.invplus.security.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

import br.com.invplus.shared.domain.entity.BaseEntity;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "refresh_token",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_refresh_token_hash", columnNames = "token_hash")
        },
        indexes = {
                @Index(name = "idx_refresh_token_usuario_ativo", columnList = "usuario_id, expira_em"),
                @Index(name = "idx_refresh_token_expira_em", columnList = "expira_em")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotBlank
    @Size(max = 64)
    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @NotNull
    @Column(name = "expira_em", nullable = false)
    private OffsetDateTime expiraEm;

    @NotNull
    @Column(name = "revogado", nullable = false)
    private Boolean revogado = false;

    @Column(name = "revogado_em")
    private OffsetDateTime revogadoEm;

    @Size(max = 45)
    @Column(name = "ip", length = 45)
    private String ip;

    @Size(max = 255)
    @Column(name = "user_agent", length = 255)
    private String userAgent;

    public RefreshToken(Usuario usuario, String tokenHash, OffsetDateTime expiraEm, String ip, String userAgent) {
        this.usuario = usuario;
        this.tokenHash = tokenHash;
        this.expiraEm = expiraEm;
        this.ip = ip;
        this.userAgent = userAgent;
    }

    public void revogar() {
        this.revogado = true;
        this.revogadoEm = OffsetDateTime.now();
    }

    public boolean estaExpirado() {
        return !OffsetDateTime.now().isBefore(expiraEm);
    }

    @PrePersist
    @Override
    protected void prePersist() {
        super.prePersist();
        if (revogado == null) {
            revogado = false;
        }
    }
}
