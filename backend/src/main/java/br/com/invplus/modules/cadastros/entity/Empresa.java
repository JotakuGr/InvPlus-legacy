package br.com.invplus.modules.cadastros.entity;

import br.com.invplus.shared.domain.entity.SoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
        indexes = {
                @Index(name = "idx_empresa_cnpj", columnList = "cnpj"),
                @Index(name = "idx_empresa_ativo", columnList = "ativo")
        }
)
@NoArgsConstructor
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
    @Column(name = "inscricao_municipal", length = 30)
    private String inscricaoMunicipal;

    @Size(max = 60)
    @Column(name = "regime_tributario", length = 60)
    private String regimeTributario;

    @NotNull
    @Column(name = "crt", nullable = false)
    private Integer crt = 3;

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
        atualizar(razaoSocial, nomeFantasia, cnpj, inscricaoEstadual, null, null, 3, telefone, email);
    }

    /**
     * Atualiza os campos editaveis mantendo a mesma regra de normalizacao do construtor.
     */
    public void atualizar(String razaoSocial, String nomeFantasia, String cnpj, String inscricaoEstadual, String telefone, String email) {
        atualizar(razaoSocial, nomeFantasia, cnpj, inscricaoEstadual, null, null, 3, telefone, email);
    }

    /**
     * Atualiza todos os dados principais da empresa com normalizacao.
     */
    public void atualizar(
            String razaoSocial,
            String nomeFantasia,
            String cnpj,
            String inscricaoEstadual,
            String inscricaoMunicipal,
            String regimeTributario,
            Integer crt,
            String telefone,
            String email
    ) {
        this.razaoSocial = normalizarTexto(razaoSocial);
        this.nomeFantasia = normalizarTexto(nomeFantasia);
        this.cnpj = normalizarDocumento(cnpj);
        this.inscricaoEstadual = normalizarTexto(inscricaoEstadual);
        this.inscricaoMunicipal = normalizarTexto(inscricaoMunicipal);
        this.regimeTributario = normalizarTexto(regimeTributario);
        this.crt = crt;
        this.telefone = normalizarTexto(telefone);
        this.email = normalizarEmail(email);
    }

    private String normalizarDocumento(String valor) {
        return valor == null ? null : valor.replaceAll("\\D", "");
    }

    private String normalizarEmail(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim().toLowerCase();
    }

    private String normalizarTexto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
