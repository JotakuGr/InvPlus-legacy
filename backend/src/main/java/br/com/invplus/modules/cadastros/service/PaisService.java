package br.com.invplus.modules.cadastros.service;

import br.com.invplus.modules.cadastros.dto.request.PaisRequest;
import br.com.invplus.modules.cadastros.dto.response.PaisResponse;
import br.com.invplus.modules.cadastros.entity.Pais;
import br.com.invplus.modules.cadastros.mapper.PaisMapper;
import br.com.invplus.modules.cadastros.repository.PaisRepository;
import br.com.invplus.modules.cadastros.validator.PaisValidator;
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
 * Regras de negocio para cadastro de paises.
 */
@Service
@RequiredArgsConstructor
public class PaisService {

    private final PaisRepository paisRepository;
    private final PaisMapper paisMapper;
    private final PaisValidator paisValidator;

    /**
     * Lista paises ativos em paginas e usa cache por pagina solicitada.
     */
    @Cacheable(cacheNames = "cadastros:paises", key = "#pageable")
    @Transactional(readOnly = true)
    public Page<PaisResponse> listar(Pageable pageable) {
        return paisRepository.findByAtivoTrueAndDeletedAtIsNull(pageable).map(paisMapper::toResponse);
    }

    /**
     * Busca pais ativo pelo identificador.
     */
    @Transactional(readOnly = true)
    public PaisResponse buscar(UUID id) {
        return paisMapper.toResponse(buscarEntidade(id));
    }

    /**
     * Cria um pais depois das validacoes funcionais.
     */
    @CacheEvict(cacheNames = "cadastros:paises", allEntries = true)
    @Transactional
    public PaisResponse criar(PaisRequest request) {
        paisValidator.validarCriacao(request);
        Pais pais = new Pais();
        paisMapper.aplicar(pais, request);
        return paisMapper.toResponse(paisRepository.save(pais));
    }

    /**
     * Atualiza um pais existente.
     */
    @CacheEvict(cacheNames = "cadastros:paises", allEntries = true)
    @Transactional
    public PaisResponse atualizar(UUID id, PaisRequest request) {
        Pais pais = buscarEntidade(id);
        paisMapper.aplicar(pais, request);
        return paisMapper.toResponse(pais);
    }

    /**
     * Exclui logicamente o pais.
     */
    @CacheEvict(cacheNames = "cadastros:paises", allEntries = true)
    @Transactional
    public void excluir(UUID id) {
        buscarEntidade(id).excluirLogicamente();
    }

    /**
     * Recupera a entidade ativa para uso por outros services.
     */
    @Transactional(readOnly = true)
    public Pais buscarEntidade(UUID id) {
        return paisRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pais nao encontrado"));
    }
}
