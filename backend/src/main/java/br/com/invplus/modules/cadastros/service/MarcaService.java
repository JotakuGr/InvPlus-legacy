package br.com.invplus.modules.cadastros.service;

import br.com.invplus.modules.cadastros.dto.request.MarcaRequest;
import br.com.invplus.modules.cadastros.dto.response.MarcaResponse;
import br.com.invplus.modules.cadastros.entity.Marca;
import br.com.invplus.modules.cadastros.repository.MarcaRepository;
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
 * Regras de negocio para marcas.
 */
@Service
@RequiredArgsConstructor
public class MarcaService {

    private final MarcaRepository marcaRepository;

    /**
     * Lista marcas ativas em paginas e usa cache por pagina solicitada.
     */
    @Cacheable(cacheNames = "cadastros:marcas", key = "#pageable")
    @Transactional(readOnly = true)
    public Page<MarcaResponse> listar(Pageable pageable) {
        return marcaRepository.findByAtivoTrueAndDeletedAtIsNull(pageable).map(MarcaResponse::from);
    }

    /**
     * Busca uma marca pelo identificador.
     */
    @Transactional(readOnly = true)
    public MarcaResponse buscar(UUID id) {
        return MarcaResponse.from(buscarEntidade(id));
    }

    /**
     * Cria uma nova marca comercial.
     */
    @CacheEvict(cacheNames = "cadastros:marcas", allEntries = true)
    @Transactional
    public MarcaResponse criar(MarcaRequest request) {
        Marca marca = new Marca();
        marca.atualizar(request.nome(), request.descricao());
        return MarcaResponse.from(marcaRepository.save(marca));
    }

    /**
     * Atualiza uma marca existente.
     */
    @CacheEvict(cacheNames = "cadastros:marcas", allEntries = true)
    @Transactional
    public MarcaResponse atualizar(UUID id, MarcaRequest request) {
        Marca marca = buscarEntidade(id);
        marca.atualizar(request.nome(), request.descricao());
        return MarcaResponse.from(marca);
    }

    /**
     * Realiza exclusao logica da marca.
     */
    @CacheEvict(cacheNames = "cadastros:marcas", allEntries = true)
    @Transactional
    public void excluir(UUID id) {
        buscarEntidade(id).excluirLogicamente();
    }

    private Marca buscarEntidade(UUID id) {
        return marcaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Marca nao encontrada"));
    }
}
