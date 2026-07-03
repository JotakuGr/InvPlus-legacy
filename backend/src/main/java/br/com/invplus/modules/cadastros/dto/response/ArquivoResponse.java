package br.com.invplus.modules.cadastros.dto.response;

import br.com.invplus.modules.cadastros.entity.Arquivo;
import br.com.invplus.modules.cadastros.enums.StorageArquivo;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Resposta de metadados de arquivo para consultas REST.
 */
public record ArquivoResponse(
        UUID id,
        String nomeOriginal,
        String nomeArmazenado,
        String tipoMime,
        String extensao,
        Long tamanhoBytes,
        String hashSha256,
        String url,
        StorageArquivo storage,
        String entidade,
        UUID entidadeId,
        Boolean ativo,
        Long version,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
    /**
     * Converte a entidade JPA para o contrato publico da API.
     */
    public static ArquivoResponse from(Arquivo arquivo) {
        return new ArquivoResponse(
                arquivo.getId(),
                arquivo.getNomeOriginal(),
                arquivo.getNomeArmazenado(),
                arquivo.getTipoMime(),
                arquivo.getExtensao(),
                arquivo.getTamanhoBytes(),
                arquivo.getHashSha256(),
                arquivo.getUrl(),
                arquivo.getStorage(),
                arquivo.getEntidade(),
                arquivo.getEntidadeId(),
                arquivo.getAtivo(),
                arquivo.getVersion(),
                arquivo.getCriadoEm(),
                arquivo.getAtualizadoEm()
        );
    }
}
