package br.com.invplus.modules.cadastros.service;

import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.invplus.modules.cadastros.dto.request.UnidadeMedidaRequest;
import br.com.invplus.modules.cadastros.dto.response.UnidadeMedidaResponse;
import br.com.invplus.modules.cadastros.entity.UnidadeMedida;
import br.com.invplus.modules.cadastros.mapper.UnidadeMedidaMapper;
import br.com.invplus.modules.cadastros.repository.UnidadeMedidaRepository;
import br.com.invplus.modules.cadastros.validator.UnidadeMedidaValidator;
import br.com.invplus.shared.domain.service.BaseCrudService;

/**
 * Regras de negocio para unidades de medida.
 */
@Service
public class UnidadeMedidaService extends BaseCrudService<UnidadeMedida, UnidadeMedidaRequest, UnidadeMedidaResponse> {

    private final UnidadeMedidaValidator unidadeMedidaValidator;

    public UnidadeMedidaService(
            UnidadeMedidaRepository unidadeMedidaRepository,
            UnidadeMedidaMapper unidadeMedidaMapper,
            UnidadeMedidaValidator unidadeMedidaValidator
    ) {
        super(unidadeMedidaRepository, unidadeMedidaMapper, "Unidade de medida");
        this.unidadeMedidaValidator = unidadeMedidaValidator;
    }

    /**
     * Lista unidades ativas em paginas e usa cache por pagina solicitada.
     */
    @Override
    @Cacheable(cacheNames = "cadastros:unidades-medida", key = "#pageable")
    @Transactional(readOnly = true)
    public Page<UnidadeMedidaResponse> listar(Pageable pageable) {
        return super.listar(pageable);
    }

    /**
     * Busca uma unidade pelo identificador.
     */
    @Override
    @Transactional(readOnly = true)
    public UnidadeMedidaResponse buscar(UUID id) {
        return super.buscar(id);
    }

    /**
     * Cria uma nova unidade de medida.
     */
    @Override
    @CacheEvict(cacheNames = "cadastros:unidades-medida", allEntries = true)
    @Transactional
    public UnidadeMedidaResponse criar(UnidadeMedidaRequest request) {
        unidadeMedidaValidator.validarCriacao(request);
        return super.criar(request);
    }

    /**
     * Atualiza uma unidade de medida existente.
     */
    @Override
    @CacheEvict(cacheNames = "cadastros:unidades-medida", allEntries = true)
    @Transactional
    public UnidadeMedidaResponse atualizar(UUID id, UnidadeMedidaRequest request) {
        unidadeMedidaValidator.validarAtualizacao(id, request);
        return super.atualizar(id, request);
    }

    /**
     * Realiza exclusao logica da unidade.
     */
    @Override
    @CacheEvict(cacheNames = "cadastros:unidades-medida", allEntries = true)
    @Transactional
    public void excluir(UUID id) {
        super.excluir(id);
    }
}
