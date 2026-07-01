package br.com.invplus.shared.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class SoftDeleteEntity extends BaseEntity {

    @NotNull
    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    public void ativar() {
        this.ativo = true;
        this.deletedAt = null;
    }

    public void desativar() {
        this.ativo = false;
    }

    public void excluirLogicamente() {
        this.ativo = false;
        this.deletedAt = OffsetDateTime.now();
    }

    public boolean estaExcluido() {
        return this.deletedAt != null;
    }
}
