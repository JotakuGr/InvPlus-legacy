package br.com.invplus.modules.pessoas.entity;

import br.com.invplus.shared.domain.entity.SoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Papel de transportadora associado a uma pessoa ja cadastrada.
 */
@Getter
@Setter
@Entity
@Table(
        name = "transportadora",
        indexes = {
                @Index(name = "idx_transportadora_pessoa", columnList = "pessoa_id"),
                @Index(name = "idx_transportadora_ativo", columnList = "ativo")
        }
)
@NoArgsConstructor
public class Transportadora extends SoftDeleteEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;

    @Size(max = 40)
    @Column(name = "antt", length = 40)
    private String antt;

    @Size(max = 80)
    @Column(name = "tipo_veiculo_principal", length = 80)
    private String tipoVeiculoPrincipal;

    @NotNull
    @PositiveOrZero
    @Column(name = "capacidade_carga_kg", nullable = false, precision = 15, scale = 3)
    private BigDecimal capacidadeCargaKg = BigDecimal.ZERO;

    @Column(name = "observacao")
    private String observacao;

    /**
     * Atualiza dados logisticos da transportadora sem alterar os dados cadastrais da pessoa.
     */
    public void atualizar(
            String antt,
            String tipoVeiculoPrincipal,
            BigDecimal capacidadeCargaKg,
            String observacao
    ) {
        this.antt = normalizarTexto(antt);
        this.tipoVeiculoPrincipal = normalizarTexto(tipoVeiculoPrincipal);
        this.capacidadeCargaKg = capacidadeCargaKg == null ? BigDecimal.ZERO : capacidadeCargaKg;
        this.observacao = normalizarTexto(observacao);
    }

    private String normalizarTexto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
