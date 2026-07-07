package br.com.invplus.modules.estoque.entity;

import br.com.invplus.shared.domain.entity.SoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Posicao fisica ou logica dentro de um deposito.
 */
@Getter
@Setter
@Entity
@Table(name = "localizacao_estoque")
@NoArgsConstructor
public class LocalizacaoEstoque extends SoftDeleteEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deposito_id", nullable = false)
    private Deposito deposito;

    @NotBlank
    @Size(max = 30)
    @Column(name = "codigo", nullable = false, length = 30, columnDefinition = "citext")
    private String codigo;

    @Size(max = 160)
    @Column(name = "descricao", length = 160)
    private String descricao;

    @Size(max = 60)
    @Column(name = "rua", length = 60)
    private String rua;

    @Size(max = 60)
    @Column(name = "corredor", length = 60)
    private String corredor;

    @Size(max = 60)
    @Column(name = "prateleira", length = 60)
    private String prateleira;

    @Size(max = 60)
    @Column(name = "nivel", length = 60)
    private String nivel;

    @Size(max = 60)
    @Column(name = "posicao", length = 60)
    private String posicao;

    @Column(name = "principal", nullable = false)
    private Boolean principal = false;

    @Column(name = "bloqueada", nullable = false)
    private Boolean bloqueada = false;

    @Column(name = "observacao")
    private String observacao;

    /**
     * Atualiza os campos editaveis com normalizacao simples de texto.
     */
    public void atualizar(
            String codigo,
            String descricao,
            String rua,
            String corredor,
            String prateleira,
            String nivel,
            String posicao,
            Boolean principal,
            Boolean bloqueada,
            String observacao
    ) {
        this.codigo = normalizarCodigo(codigo);
        this.descricao = normalizarTexto(descricao);
        this.rua = normalizarTexto(rua);
        this.corredor = normalizarTexto(corredor);
        this.prateleira = normalizarTexto(prateleira);
        this.nivel = normalizarTexto(nivel);
        this.posicao = normalizarTexto(posicao);
        this.principal = Boolean.TRUE.equals(principal);
        this.bloqueada = Boolean.TRUE.equals(bloqueada);
        this.observacao = normalizarTexto(observacao);
    }

    private String normalizarCodigo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim().toUpperCase();
    }

    private String normalizarTexto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
