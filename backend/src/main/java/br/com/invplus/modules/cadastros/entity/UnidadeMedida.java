package br.com.invplus.modules.cadastros.entity;

import br.com.invplus.shared.domain.entity.SoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Define a unidade de medida usada para quantidades operacionais.
 */
@Getter
@Setter
@Entity
@Table(
        name = "unidade_medida",
        uniqueConstraints = @UniqueConstraint(name = "uk_unidade_medida_sigla", columnNames = "sigla")
)
@NoArgsConstructor
public class UnidadeMedida extends SoftDeleteEntity {

    @NotBlank
    @Size(max = 10)
    @Column(name = "sigla", nullable = false, length = 10)
    private String sigla;

    @NotBlank
    @Size(max = 80)
    @Column(name = "nome", nullable = false, length = 80)
    private String descricao;

    @NotNull
    @Min(0)
    @Max(6)
    @Column(name = "casas_decimais", nullable = false)
    private Short casasDecimais = 0;

    /**
     * Atualiza a unidade normalizando a sigla em caixa alta.
     */
    public void atualizar(String sigla, String descricao, Short casasDecimais) {
        this.sigla = sigla == null ? null : sigla.trim().toUpperCase();
        this.descricao = descricao == null ? null : descricao.trim();
        this.casasDecimais = casasDecimais == null ? 0 : casasDecimais;
    }
}
