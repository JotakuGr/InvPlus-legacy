package br.com.invplus.modules.cadastros.mapper;

import br.com.invplus.modules.cadastros.dto.request.CategoriaRequest;
import br.com.invplus.modules.cadastros.dto.response.CategoriaResponse;
import br.com.invplus.modules.cadastros.entity.Categoria;
import org.springframework.stereotype.Component;

/**
 * Converte Categoria entre entidade JPA e contratos REST.
 */
@Component
public class CategoriaMapper {

    /**
     * Aplica os dados de entrada e o pai ja validado na entidade.
     */
    public void aplicar(Categoria categoria, CategoriaRequest request, Categoria categoriaPai) {
        categoria.atualizar(categoriaPai, request.nome(), request.descricao());
    }

    /**
     * Monta o DTO de resposta com dados resumidos do pai quando existir.
     */
    public CategoriaResponse toResponse(Categoria categoria) {
        Categoria pai = categoria.getCategoriaPai();
        return new CategoriaResponse(
                categoria.getId(),
                pai == null ? null : pai.getId(),
                pai == null ? null : pai.getNome(),
                categoria.getNome(),
                categoria.getDescricao(),
                categoria.getAtivo(),
                categoria.getVersion(),
                categoria.getCriadoEm(),
                categoria.getAtualizadoEm()
        );
    }
}
