package br.com.invplus.shared.domain.service;

import br.com.invplus.shared.domain.entity.SoftDeleteEntity;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service base para CRUDs simples com soft delete, paginacao e mapeamento padronizado.
 */
public abstract class BaseCrudService<T extends SoftDeleteEntity, REQ, RES> {

    private final BaseRepository<T> repository;
    private final CrudMapper<T, REQ, RES> mapper;
    private final String nomeRecurso;

    protected BaseCrudService(BaseRepository<T> repository, CrudMapper<T, REQ, RES> mapper, String nomeRecurso) {
        this.repository = repository;
        this.mapper = mapper;
        this.nomeRecurso = nomeRecurso;
    }

    /**
     * Lista registros ativos em paginas para impedir consultas sem limite.
     */
    @Transactional(readOnly = true)
    public Page<RES> listar(Pageable pageable) {
        return repository.findByAtivoTrueAndDeletedAtIsNull(pageable).map(mapper::toResponse);
    }

    /**
     * Busca um registro ativo pelo identificador.
     */
    @Transactional(readOnly = true)
    public RES buscar(UUID id) {
        return mapper.toResponse(buscarEntidade(id));
    }

    /**
     * Cria um novo registro usando o mapper especifico do dominio.
     */
    @Transactional
    public RES criar(REQ request) {
        T entity = mapper.novaEntidade();
        mapper.aplicar(entity, request);
        return mapper.toResponse(repository.save(entity));
    }

    /**
     * Atualiza um registro existente sem expor registros excluidos logicamente.
     */
    @Transactional
    public RES atualizar(UUID id, REQ request) {
        T entity = buscarEntidade(id);
        mapper.aplicar(entity, request);
        return mapper.toResponse(entity);
    }

    /**
     * Remove logicamente um registro, preservando historico e referencias.
     */
    @Transactional
    public void excluir(UUID id) {
        buscarEntidade(id).excluirLogicamente();
    }

    /**
     * Recupera a entidade ativa para uso por services especializados.
     */
    @Transactional(readOnly = true)
    public T buscarEntidade(UUID id) {
        return repository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException(nomeRecurso + " nao encontrado"));
    }
}
