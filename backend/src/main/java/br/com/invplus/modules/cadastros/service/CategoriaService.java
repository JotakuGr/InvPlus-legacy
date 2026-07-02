package br.com.invplus.modules.cadastros.service;

import br.com.invplus.modules.cadastros.dto.request.CategoriaRequest;
import br.com.invplus.modules.cadastros.dto.response.CategoriaResponse;
import br.com.invplus.modules.cadastros.entity.Categoria;
import br.com.invplus.modules.cadastros.mapper.CategoriaMapper;
import br.com.invplus.modules.cadastros.repository.CategoriaRepository;
import br.com.invplus.modules.cadastros.validator.CategoriaValidator;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio para categorias.
 */
@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;
    private final CategoriaValidator categoriaValidator;

    /**
     * Lista categorias ativas em paginas e usa cache por pagina solicitada.
     */
    @Cacheable(cacheNames = "cadastros:categorias", key = "#pageable")
    @Transactional(readOnly = true)
    public Page<CategoriaResponse> listar(Pageable pageable) {
        return categoriaRepository.findByAtivoTrueAndDeletedAtIsNull(pageable).map(categoriaMapper::toResponse);
    }

    /**
     * Busca uma categoria pelo identificador.
     */
    @Transactional(readOnly = true)
    public CategoriaResponse buscar(UUID id) {
        return categoriaMapper.toResponse(buscarEntidade(id));
    }

    /**
     * Cria uma categoria base para classificacoes futuras.
     */
    @CacheEvict(cacheNames = "cadastros:categorias", allEntries = true)
    @Transactional
    public CategoriaResponse criar(CategoriaRequest request) {
        Categoria categoriaPai = buscarCategoriaPai(request.categoriaPaiId());
        categoriaValidator.validarCriacao(request, categoriaPai);

        Categoria categoria = new Categoria();
        categoriaMapper.aplicar(categoria, request, categoriaPai);
        return categoriaMapper.toResponse(categoriaRepository.save(categoria));
    }

    /**
     * Atualiza uma categoria existente.
     */
    @CacheEvict(cacheNames = "cadastros:categorias", allEntries = true)
    @Transactional
    public CategoriaResponse atualizar(UUID id, CategoriaRequest request) {
        Categoria categoria = buscarEntidade(id);
        Categoria categoriaPai = buscarCategoriaPai(request.categoriaPaiId());
        categoriaValidator.validarAtualizacao(id, request, categoriaPai);
        categoriaMapper.aplicar(categoria, request, categoriaPai);
        return categoriaMapper.toResponse(categoria);
    }

    /**
     * Realiza exclusao logica da categoria.
     */
    @CacheEvict(cacheNames = "cadastros:categorias", allEntries = true)
    @Transactional
    public void excluir(UUID id) {
        buscarEntidade(id).excluirLogicamente();
    }

    public Categoria buscarEntidade(UUID id) {
        return categoriaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria nao encontrada"));
    }

    private Categoria buscarCategoriaPai(UUID categoriaPaiId) {
        if (categoriaPaiId == null) {
            return null;
        }
        return buscarEntidade(categoriaPaiId);
    }
}
