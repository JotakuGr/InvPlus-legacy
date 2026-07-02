package br.com.invplus.modules.cadastros.entity;

import br.com.invplus.shared.domain.entity.SoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa a empresa ou filial usada como base operacional do ERP.
 */
@Getter
@Setter
@Entity
@Table(
        name = "empresa",
        uniqueConstraints = @UniqueConstraint(name = "uk_empresa_cnpj", columnNames = "cnpj")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Empresa extends SoftDeleteEntity {

    @NotBlank
    @Size(max = 160)
    @Column(name = "razao_social", nullable = false, length = 160)
    private String razaoSocial;

    @Size(max = 160)
    @Column(name = "nome_fantasia", length = 160)
    private String nomeFantasia;

    @NotBlank
    @Size(max = 18)
    @Column(name = "cnpj", nullable = false, length = 18, columnDefinition = "citext")
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

    /**
     * Cria uma empresa normalizando os principais documentos para persistencia.
     */
    public Empresa(String razaoSocial, String nomeFantasia, String cnpj, String inscricaoEstadual, String telefone, String email) {
        this.razaoSocial = razaoSocial;
        this.nomeFantasia = nomeFantasia;
        this.cnpj = normalizarDocumento(cnpj);
        this.inscricaoEstadual = inscricaoEstadual;
        this.telefone = telefone;
        this.email = normalizarEmail(email);
    }

    /**
     * Atualiza os campos editaveis mantendo a mesma regra de normalizacao do construtor.
     */
    public void atualizar(String razaoSocial, String nomeFantasia, String cnpj, String inscricaoEstadual, String telefone, String email) {
        this.razaoSocial = razaoSocial;
        this.nomeFantasia = nomeFantasia;
        this.cnpj = normalizarDocumento(cnpj);
        this.inscricaoEstadual = inscricaoEstadual;
        this.telefone = telefone;
        this.email = normalizarEmail(email);
    }

    private String normalizarDocumento(String valor) {
        return valor == null ? null : valor.replaceAll("\\D", "");
    }

    private String normalizarEmail(String valor) {
        return valor == null ? null : valor.trim().toLowerCase();
    }
}
