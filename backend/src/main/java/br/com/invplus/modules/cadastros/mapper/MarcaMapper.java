package br.com.invplus.modules.cadastros.mapper;

import br.com.invplus.modules.cadastros.dto.request.MarcaRequest;
import br.com.invplus.modules.cadastros.dto.response.MarcaResponse;
import br.com.invplus.modules.cadastros.entity.Marca;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Converte Marca entre entidade JPA e contratos REST.
 */
@Component
public class MarcaMapper implements CrudMapper<Marca, MarcaRequest, MarcaResponse> {

    /**
     * Cria uma entidade vazia para o fluxo de cadastro.
     */
    @Override
    public Marca novaEntidade() {
        return new Marca();
    }

    /**
     * Aplica os dados de entrada na entidade com normalizacao simples.
     */
    @Override
    public void aplicar(Marca marca, MarcaRequest request) {
        marca.atualizar(request.nome(), request.descricao(), request.site(), request.fabricante());
    }

    /**
     * Converte a entidade para o contrato publico da API.
     */
    @Override
    public MarcaResponse toResponse(Marca marca) {
        return new MarcaResponse(
                marca.getId(),
                marca.getNome(),
                marca.getDescricao(),
                marca.getSite(),
                marca.getFabricante(),
                marca.getAtivo(),
                marca.getVersion(),
                marca.getCriadoEm(),
                marca.getAtualizadoEm()
        );
    }
}
