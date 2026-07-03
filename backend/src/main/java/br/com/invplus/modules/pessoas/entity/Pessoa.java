package br.com.invplus.modules.pessoas.entity;

import br.com.invplus.common.enums.TipoPessoa;
import br.com.invplus.shared.domain.entity.SoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Cadastro base de pessoa fisica ou juridica compartilhado pelos proximos subtipos.
 */
@Getter
@Setter
@Entity
@Table(
        name = "pessoa",
        indexes = {
                @Index(name = "idx_pessoa_cpf_cnpj", columnList = "cpf_cnpj"),
                @Index(name = "idx_pessoa_ativo", columnList = "ativo")
        }
)
@NoArgsConstructor
public class Pessoa extends SoftDeleteEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pessoa", nullable = false, length = 20)
    private TipoPessoa tipoPessoa;

    @NotBlank
    @Size(max = 160)
    @Column(name = "nome", nullable = false, length = 160)
    private String nome;

    @Size(max = 160)
    @Column(name = "nome_fantasia", length = 160)
    private String nomeFantasia;

    @NotBlank
    @Size(max = 14)
    @Column(name = "cpf_cnpj", nullable = false, length = 14)
    private String cpfCnpj;

    @Size(max = 30)
    @Column(name = "rg_ie", length = 30)
    private String rgIe;

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

    @Column(name = "observacao")
    private String observacao;

    /**
     * Atualiza os dados principais e normaliza campos usados para busca e unicidade.
     */
    public void atualizar(
            TipoPessoa tipoPessoa,
            String nome,
            String nomeFantasia,
            String cpfCnpj,
            String rgIe,
            String telefone,
            String celular,
            String email,
            String observacao
    ) {
        this.tipoPessoa = tipoPessoa;
        this.nome = normalizarTexto(nome);
        this.nomeFantasia = normalizarTexto(nomeFantasia);
        this.cpfCnpj = normalizarDocumento(cpfCnpj);
        this.rgIe = normalizarTexto(rgIe);
        this.telefone = normalizarTexto(telefone);
        this.celular = normalizarTexto(celular);
        this.email = normalizarTexto(email);
        this.observacao = normalizarTexto(observacao);
    }

    private String normalizarTexto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private String normalizarDocumento(String valor) {
        return valor == null ? null : valor.replaceAll("\\D", "");
    }
}
