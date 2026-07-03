package br.com.invplus.modules.cadastros.service;

import br.com.invplus.modules.cadastros.dto.request.ArquivoRequest;
import br.com.invplus.modules.cadastros.dto.response.ArquivoResponse;
import br.com.invplus.modules.cadastros.entity.Arquivo;
import br.com.invplus.modules.cadastros.mapper.ArquivoMapper;
import br.com.invplus.modules.cadastros.repository.ArquivoRepository;
import br.com.invplus.modules.cadastros.validator.ArquivoValidator;
import br.com.invplus.shared.domain.service.BaseCrudService;
import java.util.UUID;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio para metadados de arquivos anexados.
 */
@Service
public class ArquivoService extends BaseCrudService<Arquivo, ArquivoRequest, ArquivoResponse> {

    private final ArquivoRepository arquivoRepository;
    private final ArquivoValidator arquivoValidator;
    private final ArquivoMapper arquivoMapper;

    public ArquivoService(
            ArquivoRepository arquivoRepository,
            ArquivoMapper arquivoMapper,
            ArquivoValidator arquivoValidator
    ) {
        super(arquivoRepository, arquivoMapper, "Arquivo");
        this.arquivoRepository = arquivoRepository;
        this.arquivoMapper = arquivoMapper;
        this.arquivoValidator = arquivoValidator;
    }

    /**
     * Lista arquivos ativos em paginas e usa cache por pagina solicitada.
     */
    @Override
    @Cacheable(cacheNames = "cadastros:arquivos", key = "#pageable")
    @Transactional(readOnly = true)
    public Page<ArquivoResponse> listar(Pageable pageable) {
        return super.listar(pageable);
    }

    /**
     * Lista arquivos, com filtro opcional por entidade vinculada e paginacao.
     */
    @Cacheable(cacheNames = "cadastros:arquivos", key = "{#entidade, #entidadeId, #pageable}")
    @Transactional(readOnly = true)
    public Page<ArquivoResponse> listar(String entidade, UUID entidadeId, Pageable pageable) {
        if (entidade == null || entidadeId == null) {
            return listar(pageable);
        }
        return arquivoRepository.findByEntidadeAndEntidadeIdAndAtivoTrueAndDeletedAtIsNull(
                entidade.trim().toUpperCase(),
                entidadeId,
                pageable
        ).map(arquivoMapper::toResponse);
    }

    /**
     * Busca metadados de arquivo pelo identificador.
     */
    @Override
    @Transactional(readOnly = true)
    public ArquivoResponse buscar(UUID id) {
        return super.buscar(id);
    }

    /**
     * Registra um novo arquivo sem armazenar o binario no banco.
     */
    @Override
    @CacheEvict(cacheNames = "cadastros:arquivos", allEntries = true)
    @Transactional
    public ArquivoResponse criar(ArquivoRequest request) {
        arquivoValidator.validarCriacao(request);
        return super.criar(request);
    }

    /**
     * Atualiza metadados de arquivo existentes.
     */
    @Override
    @CacheEvict(cacheNames = "cadastros:arquivos", allEntries = true)
    @Transactional
    public ArquivoResponse atualizar(UUID id, ArquivoRequest request) {
        arquivoValidator.validarAtualizacao(id, request);
        return super.atualizar(id, request);
    }

    /**
     * Realiza exclusao logica dos metadados do arquivo.
     */
    @Override
    @CacheEvict(cacheNames = "cadastros:arquivos", allEntries = true)
    @Transactional
    public void excluir(UUID id) {
        super.excluir(id);
    }
}
