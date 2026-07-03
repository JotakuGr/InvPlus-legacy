package br.com.invplus.modules.cadastros.service;

import br.com.invplus.modules.cadastros.dto.request.EmpresaRequest;
import br.com.invplus.modules.cadastros.dto.response.EmpresaResponse;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.mapper.EmpresaMapper;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.cadastros.validator.EmpresaValidator;
import br.com.invplus.shared.domain.service.BaseCrudService;
import java.util.UUID;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio para cadastro de empresas.
 */
@Service
public class EmpresaService extends BaseCrudService<Empresa, EmpresaRequest, EmpresaResponse> {

    private final EmpresaValidator empresaValidator;

    public EmpresaService(EmpresaRepository empresaRepository, EmpresaMapper empresaMapper, EmpresaValidator empresaValidator) {
        super(empresaRepository, empresaMapper, "Empresa");
        this.empresaValidator = empresaValidator;
    }

    /**
     * Lista empresas ativas em paginas para evitar consultas sem limite.
     */
    @Override
    @Cacheable(cacheNames = "cadastros:empresas", key = "#pageable")
    @Transactional(readOnly = true)
    public Page<EmpresaResponse> listar(Pageable pageable) {
        return super.listar(pageable);
    }

    /**
     * Busca uma empresa pelo identificador.
     */
    @Override
    @Transactional(readOnly = true)
    public EmpresaResponse buscar(UUID id) {
        return super.buscar(id);
    }

    /**
     * Cria uma nova empresa com dados normalizados.
     */
    @Override
    @CacheEvict(cacheNames = "cadastros:empresas", allEntries = true)
    @Transactional
    public EmpresaResponse criar(EmpresaRequest request) {
        empresaValidator.validarCriacao(request);
        return super.criar(request);
    }

    /**
     * Atualiza uma empresa existente.
     */
    @Override
    @CacheEvict(cacheNames = "cadastros:empresas", allEntries = true)
    @Transactional
    public EmpresaResponse atualizar(UUID id, EmpresaRequest request) {
        empresaValidator.validarAtualizacao(id, request);
        return super.atualizar(id, request);
    }

    /**
     * Realiza exclusao logica mantendo historico do cadastro.
     */
    @Override
    @CacheEvict(cacheNames = "cadastros:empresas", allEntries = true)
    @Transactional
    public void excluir(UUID id) {
        super.excluir(id);
    }
}
