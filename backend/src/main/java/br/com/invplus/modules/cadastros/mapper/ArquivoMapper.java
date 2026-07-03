package br.com.invplus.modules.cadastros.mapper;

import br.com.invplus.modules.cadastros.dto.request.ArquivoRequest;
import br.com.invplus.modules.cadastros.dto.response.ArquivoResponse;
import br.com.invplus.modules.cadastros.entity.Arquivo;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Converte Arquivo entre entidade JPA e contratos REST.
 */
@Component
public class ArquivoMapper implements CrudMapper<Arquivo, ArquivoRequest, ArquivoResponse> {

    /**
     * Cria uma entidade vazia para o fluxo padrao de cadastro.
     */
    @Override
    public Arquivo novaEntidade() {
        return new Arquivo();
    }

    /**
     * Aplica o payload na entidade, delegando normalizacoes para o dominio.
     */
    @Override
    public void aplicar(Arquivo arquivo, ArquivoRequest request) {
        arquivo.atualizar(
                request.nomeOriginal(),
                request.nomeArmazenado(),
                request.tipoMime(),
                request.extensao(),
                request.tamanhoBytes(),
                request.hashSha256(),
                request.url(),
                request.storage(),
                request.entidade(),
                request.entidadeId()
        );
    }

    /**
     * Converte a entidade persistida para o contrato publico da API.
     */
    @Override
    public ArquivoResponse toResponse(Arquivo arquivo) {
        return ArquivoResponse.from(arquivo);
    }
}
