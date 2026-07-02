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
 * Pais usado como raiz da hierarquia geografica do ERP.
 */
@Getter
@Setter
@Entity
@Table(
        name = "pais",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_pais_codigo_iso2", columnNames = "codigo_iso2"),
                @UniqueConstraint(name = "uk_pais_nome", columnNames = "nome")
        }
)
@NoArgsConstructor
public class Pais extends SoftDeleteEntity {

    @NotBlank
    @Size(max = 100)
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @NotBlank
    @Size(min = 2, max = 2)
    @Column(name = "codigo_iso2", nullable = false, length = 2)
    private String codigoIso2;

    @Size(min = 3, max = 3)
    @Column(name = "codigo_iso3", length = 3)
    private String codigoIso3;

    @Size(max = 10)
    @Column(name = "codigo_bacen", length = 10)
    private String codigoBacen;

    /**
     * Atualiza os dados principais e normaliza os codigos ISO em caixa alta.
     */
    public void atualizar(String nome, String codigoIso2, String codigoIso3, String codigoBacen) {
        this.nome = nome;
        this.codigoIso2 = normalizarCodigo(codigoIso2);
        this.codigoIso3 = normalizarCodigo(codigoIso3);
        this.codigoBacen = codigoBacen;
    }

    private String normalizarCodigo(String valor) {
        return valor == null ? null : valor.trim().toUpperCase();
    }
}
