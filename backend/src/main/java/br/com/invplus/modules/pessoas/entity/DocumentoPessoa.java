package br.com.invplus.modules.pessoas.entity;

import br.com.invplus.modules.cadastros.entity.Arquivo;
import br.com.invplus.modules.pessoas.enums.TipoDocumentoPessoa;
import br.com.invplus.shared.domain.entity.SoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Documento cadastral de uma Pessoa, com anexo digital opcional via Arquivo.
 */
@Getter
@Setter
@Entity
@Table(
        name = "documento_pessoa",
        indexes = {
                @Index(name = "idx_documento_pessoa_pessoa", columnList = "pessoa_id"),
                @Index(name = "idx_documento_pessoa_arquivo", columnList = "arquivo_id")
        }
)
@NoArgsConstructor
public class DocumentoPessoa extends SoftDeleteEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arquivo_id")
    private Arquivo arquivo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 40)
    private TipoDocumentoPessoa tipoDocumento;

    @NotBlank
    @Size(max = 120)
    @Column(name = "numero", nullable = false, length = 120)
    private String numero;

    @Size(max = 120)
    @Column(name = "orgao_emissor", length = 120)
    private String orgaoEmissor;

    @Column(name = "data_emissao")
    private LocalDate dataEmissao;

    @Column(name = "data_validade")
    private LocalDate dataValidade;

    @Column(name = "observacao")
    private String observacao;

    /**
     * Atualiza somente os dados do documento, sem alterar Pessoa ou Arquivo vinculados.
     */
    public void atualizar(
            TipoDocumentoPessoa tipoDocumento,
            String numero,
            String orgaoEmissor,
            LocalDate dataEmissao,
            LocalDate dataValidade,
            String observacao
    ) {
        this.tipoDocumento = tipoDocumento;
        this.numero = normalizarTexto(numero);
        this.orgaoEmissor = normalizarTexto(orgaoEmissor);
        this.dataEmissao = dataEmissao;
        this.dataValidade = dataValidade;
        this.observacao = normalizarTexto(observacao);
    }

    private String normalizarTexto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
