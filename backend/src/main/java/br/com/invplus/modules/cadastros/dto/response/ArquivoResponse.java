package br.com.invplus.modules.cadastros.dto.response;

import br.com.invplus.modules.cadastros.entity.Arquivo;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Resposta de metadados de arquivo para consultas REST.
 */
public record ArquivoResponse(
        UUID id,
        String nomeOriginal,
        String nomeArmazenado,
        String contentType,
        Long tamanhoBytes,
        String caminho,
        String checksumSha256,
        String entidade,
        UUID entidadeId,
        Boolean ativo,
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
                arquivo.getContentType(),
                arquivo.getTamanhoBytes(),
                arquivo.getCaminho(),
                arquivo.getChecksumSha256(),
                arquivo.getEntidade(),
                arquivo.getEntidadeId(),
                arquivo.getAtivo(),
                arquivo.getCriadoEm(),
                arquivo.getAtualizadoEm()
        );
    }
}
