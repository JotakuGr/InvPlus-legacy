package br.com.invplus.modules.cadastros.service;

import br.com.invplus.modules.cadastros.dto.request.MarcaRequest;
import br.com.invplus.modules.cadastros.dto.response.MarcaResponse;
import br.com.invplus.modules.cadastros.entity.Marca;
import br.com.invplus.modules.cadastros.mapper.MarcaMapper;
import br.com.invplus.modules.cadastros.repository.MarcaRepository;
import br.com.invplus.modules.cadastros.validator.MarcaValidator;
import br.com.invplus.shared.domain.service.BaseCrudService;
import java.util.UUID;
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
public class MarcaService extends BaseCrudService<Marca, MarcaRequest, MarcaResponse> {

    private final MarcaValidator marcaValidator;

    public MarcaService(MarcaRepository marcaRepository, MarcaMapper marcaMapper, MarcaValidator marcaValidator) {
        super(marcaRepository, marcaMapper, "Marca");
        this.marcaValidator = marcaValidator;
    }

    /**
     * Lista marcas ativas em paginas e usa cache por pagina solicitada.
     */
    @Override
    @Cacheable(cacheNames = "cadastros:marcas", key = "#pageable")
    @Transactional(readOnly = true)
    public Page<MarcaResponse> listar(Pageable pageable) {
        return super.listar(pageable);
    }

    /**
     * Busca uma marca pelo identificador.
     */
    @Override
    @Transactional(readOnly = true)
    public MarcaResponse buscar(UUID id) {
        return super.buscar(id);
    }

    /**
     * Cria uma nova marca comercial.
     */
    @Override
    @CacheEvict(cacheNames = "cadastros:marcas", allEntries = true)
    @Transactional
    public MarcaResponse criar(MarcaRequest request) {
        marcaValidator.validarCriacao(request);
        return super.criar(request);
    }

    /**
     * Atualiza uma marca existente.
     */
    @Override
    @CacheEvict(cacheNames = "cadastros:marcas", allEntries = true)
    @Transactional
    public MarcaResponse atualizar(UUID id, MarcaRequest request) {
        marcaValidator.validarAtualizacao(id, request);
        return super.atualizar(id, request);
    }

    /**
     * Realiza exclusao logica da marca.
     */
    @Override
    @CacheEvict(cacheNames = "cadastros:marcas", allEntries = true)
    @Transactional
    public void excluir(UUID id) {
        super.excluir(id);
    }
}
