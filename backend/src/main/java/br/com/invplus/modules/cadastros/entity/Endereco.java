package br.com.invplus.modules.cadastros.entity;

import br.com.invplus.shared.domain.entity.SoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Guarda enderecos fisicos que podem ser vinculados a empresas.
 */
@Getter
@Setter
@Entity
@Table(name = "endereco")
@NoArgsConstructor
public class Endereco extends SoftDeleteEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cidade_id")
    private Cidade cidadeCadastro;

    @NotBlank
    @Size(max = 40)
    @Column(name = "tipo", nullable = false, length = 40)
    private String tipo = "PRINCIPAL";

    @NotBlank
    @Size(max = 160)
    @Column(name = "logradouro", nullable = false, length = 160)
    private String logradouro;

    @Size(max = 20)
    @Column(name = "numero", length = 20)
    private String numero;

    @Size(max = 120)
    @Column(name = "complemento", length = 120)
    private String complemento;

    @Size(max = 100)
    @Column(name = "bairro", length = 100)
    private String bairro;

    @NotBlank
    @Size(max = 100)
    @Column(name = "cidade", nullable = false, length = 100)
    private String cidade;

    @NotBlank
    @Size(min = 2, max = 2)
    @Column(name = "uf", nullable = false, length = 2)
    private String uf;

    @Size(max = 12)
    @Column(name = "cep", length = 12)
    private String cep;

    @Column(name = "principal", nullable = false)
    private Boolean principal = false;

    /**
     * Atualiza o vinculo com a empresa e os dados postais em uma unica operacao.
     */
    public void atualizar(Empresa empresa, Cidade cidadeCadastro, String tipo, String logradouro, String numero, String complemento,
            String bairro, String cidade, String uf, String cep, Boolean principal) {
        this.empresa = empresa;
        this.cidadeCadastro = cidadeCadastro;
        this.tipo = normalizarTexto(tipo, "PRINCIPAL");
        this.logradouro = logradouro;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
        this.cidade = cidade;
        this.uf = uf == null ? null : uf.trim().toUpperCase();
        this.cep = cep == null ? null : cep.replaceAll("\\D", "");
        this.principal = Boolean.TRUE.equals(principal);
    }

    private String normalizarTexto(String valor, String padrao) {
        return valor == null || valor.isBlank() ? padrao : valor.trim().toUpperCase();
    }
}
