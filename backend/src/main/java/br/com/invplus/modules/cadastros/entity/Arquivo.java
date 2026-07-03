package br.com.invplus.modules.cadastros.entity;

import java.util.UUID;

import br.com.invplus.modules.cadastros.enums.StorageArquivo;
import br.com.invplus.shared.domain.entity.SoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
@Table(
        name = "arquivo",
        indexes = {
                @Index(name = "idx_arquivo_entidade", columnList = "entidade, entidade_id"),
                @Index(name = "idx_arquivo_hash_sha256", columnList = "hash_sha256")
        }
)
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
    @Column(name = "tipo_mime", nullable = false, length = 120)
    private String tipoMime;

    @NotBlank
    @Size(max = 20)
    @Column(name = "extensao", nullable = false, length = 20)
    private String extensao;

    @NotNull
    @Positive
    @Column(name = "tamanho_bytes", nullable = false)
    private Long tamanhoBytes;

    @NotBlank
    @Size(max = 64)
    @Column(name = "hash_sha256", nullable = false, length = 64)
    private String hashSha256;

    @NotBlank
    @Size(max = 500)
    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "storage", nullable = false, length = 30)
    private StorageArquivo storage = StorageArquivo.LOCAL;

    @NotBlank
    @Size(max = 80)
    @Column(name = "entidade", nullable = false, length = 80)
    private String entidade;

    @NotNull
    @Column(name = "entidade_id")
    private UUID entidadeId;

    /**
     * Atualiza metadados de armazenamento e o vinculo generico com outra entidade.
     */
    public void atualizar(
            String nomeOriginal,
            String nomeArmazenado,
            String tipoMime,
            String extensao,
            Long tamanhoBytes,
            String hashSha256,
            String url,
            StorageArquivo storage,
            String entidade,
            UUID entidadeId
    ) {
        this.nomeOriginal = normalizarTexto(nomeOriginal);
        this.nomeArmazenado = normalizarTexto(nomeArmazenado);
        this.tipoMime = normalizarTexto(tipoMime);
        this.extensao = normalizarExtensao(extensao);
        this.tamanhoBytes = tamanhoBytes;
        this.hashSha256 = hashSha256 == null ? null : hashSha256.trim().toLowerCase();
        this.url = normalizarTexto(url);
        this.storage = storage == null ? StorageArquivo.LOCAL : storage;
        this.entidade = entidade == null ? null : entidade.trim().toUpperCase();
        this.entidadeId = entidadeId;
    }

    private String normalizarTexto(String valor) {
        return valor == null ? null : valor.trim();
    }

    private String normalizarExtensao(String valor) {
        if (valor == null) {
            return null;
        }
        String extensaoNormalizada = valor.trim().toLowerCase();
        return extensaoNormalizada.startsWith(".") ? extensaoNormalizada.substring(1) : extensaoNormalizada;
    }
}
