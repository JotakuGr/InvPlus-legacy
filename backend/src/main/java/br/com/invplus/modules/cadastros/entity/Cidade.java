package br.com.invplus.modules.cadastros.entity;

import br.com.invplus.shared.domain.entity.SoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Cidade vinculada a um estado, usada em enderecos e documentos fiscais.
 */
@Getter
@Setter
@Entity
@Table(
        name = "cidade",
        uniqueConstraints = @UniqueConstraint(name = "uk_cidade_estado_nome", columnNames = {"estado_id", "nome"})
)
@NoArgsConstructor
public class Cidade extends SoftDeleteEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estado_id", nullable = false)
    private Estado estado;

    @NotBlank
    @Size(max = 120)
    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Size(max = 10)
    @Column(name = "codigo_ibge", length = 10)
    private String codigoIbge;

    /**
     * Atualiza a cidade mantendo o estado resolvido pelo service.
     */
    public void atualizar(Estado estado, String nome, String codigoIbge) {
        this.estado = estado;
        this.nome = nome;
        this.codigoIbge = codigoIbge;
    }
}
