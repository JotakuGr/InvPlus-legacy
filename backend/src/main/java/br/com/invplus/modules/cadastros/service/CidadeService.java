package br.com.invplus.modules.cadastros.service;

import br.com.invplus.modules.cadastros.dto.request.CidadeRequest;
import br.com.invplus.modules.cadastros.dto.response.CidadeResponse;
import br.com.invplus.modules.cadastros.entity.Cidade;
import br.com.invplus.modules.cadastros.entity.Estado;
import br.com.invplus.modules.cadastros.mapper.CidadeMapper;
import br.com.invplus.modules.cadastros.repository.CidadeRepository;
import br.com.invplus.modules.cadastros.validator.CidadeValidator;
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
 * Regras de negocio para cadastro de cidades.
 */
@Service
@RequiredArgsConstructor
public class CidadeService {

    private final CidadeRepository cidadeRepository;
    private final EstadoService estadoService;
    private final CidadeMapper cidadeMapper;
    private final CidadeValidator cidadeValidator;

    /**
     * Lista cidades ativas, opcionalmente por estado, sempre com paginacao.
     */
    @Cacheable(cacheNames = "cadastros:cidades", key = "{#estadoId, #pageable}")
    @Transactional(readOnly = true)
    public Page<CidadeResponse> listar(UUID estadoId, Pageable pageable) {
        Page<Cidade> cidades = estadoId == null
                ? cidadeRepository.findByAtivoTrueAndDeletedAtIsNull(pageable)
                : cidadeRepository.findByEstadoIdAndAtivoTrueAndDeletedAtIsNull(estadoId, pageable);
        return cidades.map(cidadeMapper::toResponse);
    }

    /**
     * Busca cidade ativa pelo identificador.
     */
    @Transactional(readOnly = true)
    public CidadeResponse buscar(UUID id) {
        return cidadeMapper.toResponse(buscarEntidade(id));
    }

    /**
     * Cria uma cidade vinculada a estado ativo.
     */
    @CacheEvict(cacheNames = "cadastros:cidades", allEntries = true)
    @Transactional
    public CidadeResponse criar(CidadeRequest request) {
        cidadeValidator.validarCriacao(request);
        Cidade cidade = new Cidade();
        aplicar(cidade, request);
        return cidadeMapper.toResponse(cidadeRepository.save(cidade));
    }

    /**
     * Atualiza uma cidade existente.
     */
    @CacheEvict(cacheNames = "cadastros:cidades", allEntries = true)
    @Transactional
    public CidadeResponse atualizar(UUID id, CidadeRequest request) {
        Cidade cidade = buscarEntidade(id);
        aplicar(cidade, request);
        return cidadeMapper.toResponse(cidade);
    }

    /**
     * Exclui logicamente a cidade.
     */
    @CacheEvict(cacheNames = "cadastros:cidades", allEntries = true)
    @Transactional
    public void excluir(UUID id) {
        buscarEntidade(id).excluirLogicamente();
    }

    private void aplicar(Cidade cidade, CidadeRequest request) {
        Estado estado = estadoService.buscarEntidade(request.estadoId());
        cidade.atualizar(estado, request.nome(), request.codigoIbge());
    }

    /**
     * Recupera a entidade ativa para uso por outros services.
     */
    @Transactional(readOnly = true)
    public Cidade buscarEntidade(UUID id) {
        return cidadeRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cidade nao encontrada"));
    }
}
