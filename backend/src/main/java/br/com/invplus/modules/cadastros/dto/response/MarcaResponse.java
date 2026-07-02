package br.com.invplus.modules.cadastros.dto.response;

import br.com.invplus.modules.cadastros.entity.Marca;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Resposta de marca para consultas REST.
 */
public record MarcaResponse(
        UUID id,
        String nome,
        String descricao,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
    /**
     * Converte a entidade JPA para o contrato publico da API.
     */
    public static MarcaResponse from(Marca marca) {
        return new MarcaResponse(
                marca.getId(),
                marca.getNome(),
                marca.getDescricao(),
                marca.getAtivo(),
                marca.getCriadoEm(),
                marca.getAtualizadoEm()
        );
    }
}
