package br.com.invplus.modules.pessoas.entity;

import br.com.invplus.shared.domain.entity.SoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Contato operacional de uma Pessoa Aggregate Root, como Financeiro, Compras ou Comercial.
 */
@Getter
@Setter
@Entity
@Table(
        name = "contato_pessoa",
        indexes = {
                @Index(name = "idx_contato_pessoa_pessoa", columnList = "pessoa_id"),
                @Index(name = "idx_contato_pessoa_email", columnList = "email")
        }
)
@NoArgsConstructor
public class ContatoPessoa extends SoftDeleteEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;

    @NotBlank
    @Size(max = 160)
    @Column(name = "nome", nullable = false, length = 160)
    private String nome;

    @Size(max = 30)
    @Column(name = "telefone", length = 30)
    private String telefone;

    @Size(max = 30)
    @Column(name = "celular", length = 30)
    private String celular;

    @Email
    @Size(max = 255)
    @Column(name = "email", columnDefinition = "citext")
    private String email;

    @Size(max = 120)
    @Column(name = "cargo", length = 120)
    private String cargo;

    @Size(max = 60)
    @Column(name = "tipo", length = 60)
    private String tipo;

    @Column(name = "principal", nullable = false)
    private Boolean principal = false;

    @Column(name = "observacao")
    private String observacao;

    /**
     * Atualiza os dados do contato sem alterar os dados cadastrais da Pessoa.
     */
    public void atualizar(
            String nome,
            String telefone,
            String celular,
            String email,
            String cargo,
            String tipo,
            Boolean principal,
            String observacao
    ) {
        this.nome = normalizarTexto(nome);
        this.telefone = normalizarTexto(telefone);
        this.celular = normalizarTexto(celular);
        this.email = email == null || email.isBlank() ? null : email.trim().toLowerCase();
        this.cargo = normalizarTexto(cargo);
        this.tipo = tipo == null || tipo.isBlank() ? null : tipo.trim().toUpperCase();
        this.principal = Boolean.TRUE.equals(principal);
        this.observacao = normalizarTexto(observacao);
    }

    private String normalizarTexto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
