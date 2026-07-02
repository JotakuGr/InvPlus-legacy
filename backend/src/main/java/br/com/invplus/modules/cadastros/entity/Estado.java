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
 * Estado ou unidade federativa vinculada a um pais.
 */
@Getter
@Setter
@Entity
@Table(
        name = "estado",
        uniqueConstraints = @UniqueConstraint(name = "uk_estado_pais_uf", columnNames = {"pais_id", "uf"})
)
@NoArgsConstructor
public class Estado extends SoftDeleteEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pais_id", nullable = false)
    private Pais pais;

    @NotBlank
    @Size(max = 100)
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @NotBlank
    @Size(min = 2, max = 2)
    @Column(name = "uf", nullable = false, length = 2)
    private String uf;

    @Size(max = 10)
    @Column(name = "codigo_ibge", length = 10)
    private String codigoIbge;

    /**
     * Atualiza a unidade federativa mantendo o pais resolvido pelo service.
     */
    public void atualizar(Pais pais, String nome, String uf, String codigoIbge) {
        this.pais = pais;
        this.nome = nome;
        this.uf = uf == null ? null : uf.trim().toUpperCase();
        this.codigoIbge = codigoIbge;
    }
}
