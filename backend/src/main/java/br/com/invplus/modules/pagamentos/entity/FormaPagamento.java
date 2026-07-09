package br.com.invplus.modules.pagamentos.entity;

import br.com.invplus.modules.pagamentos.enums.TipoFormaPagamento;
import br.com.invplus.shared.domain.entity.SoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Meio de pagamento usado em vendas, compras e recebimentos financeiros.
 */
@Getter
@Setter
@Entity
@Table(
        name = "forma_pagamento",
        indexes = {
                @Index(name = "idx_forma_pagamento_tipo", columnList = "tipo"),
                @Index(name = "idx_forma_pagamento_codigo", columnList = "codigo")
        }
)
@NoArgsConstructor
public class FormaPagamento extends SoftDeleteEntity {

    @NotBlank
    @Column(name = "codigo", nullable = false, length = 30)
    private String codigo;

    @NotBlank
    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoFormaPagamento tipo;

    @Column(name = "permite_troco", nullable = false)
    private Boolean permiteTroco = false;

    @Column(name = "permite_parcelamento", nullable = false)
    private Boolean permiteParcelamento = false;

    /**
     * Atualiza os dados normalizando textos e aplicando defaults booleanos.
     */
    public void atualizar(String codigo, String nome, TipoFormaPagamento tipo, Boolean permiteTroco, Boolean permiteParcelamento) {
        this.codigo = normalizar(codigo);
        this.nome = normalizar(nome);
        this.tipo = tipo;
        this.permiteTroco = Boolean.TRUE.equals(permiteTroco);
        this.permiteParcelamento = Boolean.TRUE.equals(permiteParcelamento);
    }

    private String normalizar(String valor) {
        return valor == null ? null : valor.trim();
    }
}
