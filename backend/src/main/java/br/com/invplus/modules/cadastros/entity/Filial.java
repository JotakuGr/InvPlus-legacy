package br.com.invplus.modules.cadastros.entity;

import br.com.invplus.shared.domain.entity.SoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Unidade operacional vinculada a uma empresa.
 */
@Getter
@Setter
@Entity
@Table(
        name = "filial",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_filial_empresa_codigo", columnNames = {"empresa_id", "codigo"}),
                @UniqueConstraint(name = "uk_filial_cnpj", columnNames = "cnpj")
        }
)
@NoArgsConstructor
public class Filial extends SoftDeleteEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endereco_id")
    private Endereco endereco;

    @NotBlank
    @Size(max = 30)
    @Column(name = "codigo", nullable = false, length = 30)
    private String codigo;

    @NotBlank
    @Size(max = 120)
    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Size(max = 18)
    @Column(name = "cnpj", length = 18, columnDefinition = "citext")
    private String cnpj;

    @Size(max = 30)
    @Column(name = "inscricao_estadual", length = 30)
    private String inscricaoEstadual;

    @Size(max = 30)
    @Column(name = "telefone", length = 30)
    private String telefone;

    @Email
    @Size(max = 320)
    @Column(name = "email", length = 320, columnDefinition = "citext")
    private String email;

    @Column(name = "matriz", nullable = false)
    private Boolean matriz = false;

    /**
     * Atualiza a filial com empresa/endereco ja resolvidos pelos services.
     */
    public void atualizar(Empresa empresa, Endereco endereco, String codigo, String nome, String cnpj,
            String inscricaoEstadual, String telefone, String email, Boolean matriz) {
        this.empresa = empresa;
        this.endereco = endereco;
        this.codigo = codigo == null ? null : codigo.trim().toUpperCase();
        this.nome = nome;
        this.cnpj = normalizarDocumento(cnpj);
        this.inscricaoEstadual = inscricaoEstadual;
        this.telefone = telefone;
        this.email = normalizarEmail(email);
        this.matriz = Boolean.TRUE.equals(matriz);
    }

    private String normalizarDocumento(String valor) {
        return valor == null ? null : valor.replaceAll("\\D", "");
    }

    private String normalizarEmail(String valor) {
        return valor == null ? null : valor.trim().toLowerCase();
    }
}
