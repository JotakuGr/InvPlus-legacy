package br.com.invplus.modules.cadastros.entity;

import java.util.UUID;

import br.com.invplus.shared.domain.entity.SoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Mantem os metadados de arquivos anexados sem armazenar o binario no banco.
 */
@Getter
@Setter
@Entity
@Table(name = "arquivo")
@NoArgsConstructor
public class Arquivo extends SoftDeleteEntity {

    @NotBlank
    @Size(max = 255)
    @Column(name = "nome_original", nullable = false, length = 255)
    private String nomeOriginal;

    @NotBlank
    @Size(max = 255)
    @Column(name = "nome_armazenado", nullable = false, length = 255)
    private String nomeArmazenado;

    @NotBlank
    @Size(max = 120)
    @Column(name = "content_type", nullable = false, length = 120)
    private String contentType;

    @NotNull
    @Min(0)
    @Column(name = "tamanho_bytes", nullable = false)
    private Long tamanhoBytes;

    @NotBlank
    @Size(max = 500)
    @Column(name = "caminho", nullable = false, length = 500)
    private String caminho;

    @Size(max = 64)
    @Column(name = "checksum_sha256", length = 64)
    private String checksumSha256;

    @Size(max = 80)
    @Column(name = "entidade", length = 80)
    private String entidade;

    @Column(name = "entidade_id")
    private UUID entidadeId;

    /**
     * Atualiza metadados de armazenamento e o vinculo opcional com outra entidade.
     */
    public void atualizar(String nomeOriginal, String nomeArmazenado, String contentType, Long tamanhoBytes,
            String caminho, String checksumSha256, String entidade, UUID entidadeId) {
        this.nomeOriginal = nomeOriginal;
        this.nomeArmazenado = nomeArmazenado;
        this.contentType = contentType;
        this.tamanhoBytes = tamanhoBytes;
        this.caminho = caminho;
        this.checksumSha256 = checksumSha256;
        this.entidade = entidade == null ? null : entidade.trim().toUpperCase();
        this.entidadeId = entidadeId;
    }
}
