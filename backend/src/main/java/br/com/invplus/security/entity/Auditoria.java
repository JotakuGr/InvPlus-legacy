package br.com.invplus.security.entity;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

import org.hibernate.Hibernate;

import br.com.invplus.security.enums.AcaoAuditoria;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
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
        name = "auditoria",
        indexes = {
                @Index(name = "idx_auditoria_tabela_data", columnList = "tabela, data"),
                @Index(name = "idx_auditoria_registro", columnList = "tabela, registro_id, data"),
                @Index(name = "idx_auditoria_usuario_data", columnList = "usuario_id, data")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Size(max = 45)
    @Column(name = "ip", columnDefinition = "inet")
    private String ip;

    @Size(max = 255)
    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @NotNull
    @Size(max = 80)
    @Column(name = "tabela", nullable = false, length = 80)
    private String tabela;

    @NotNull
    @Column(name = "registro_id", nullable = false)
    private UUID registroId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "acao", nullable = false, length = 20)
    private AcaoAuditoria acao;

    @Column(name = "dados_antes", columnDefinition = "jsonb")
    private String dadosAntes;

    @Column(name = "dados_depois", columnDefinition = "jsonb")
    private String dadosDepois;

    @Column(name = "data", nullable = false, updatable = false)
    private OffsetDateTime data;

    public Auditoria(String tabela, UUID registroId, AcaoAuditoria acao) {
        this.tabela = tabela;
        this.registroId = registroId;
        this.acao = acao;
    }

    @PrePersist
    protected void prePersist() {
        this.data = OffsetDateTime.now();
    }

    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Auditoria auditoria)) {
            return false;
        }
        if (Hibernate.getClass(this) != Hibernate.getClass(auditoria)) {
            return false;
        }
        return id != null && Objects.equals(id, auditoria.id);
    }

    @Override
    public final int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
