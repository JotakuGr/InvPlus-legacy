package br.com.invplus.modules.cadastros.service;

import br.com.invplus.modules.cadastros.dto.request.EstadoRequest;
import br.com.invplus.modules.cadastros.dto.response.EstadoResponse;
import br.com.invplus.modules.cadastros.entity.Estado;
import br.com.invplus.modules.cadastros.entity.Pais;
import br.com.invplus.modules.cadastros.mapper.EstadoMapper;
import br.com.invplus.modules.cadastros.repository.EstadoRepository;
import br.com.invplus.modules.cadastros.validator.EstadoValidator;
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
 * Regras de negocio para cadastro de estados.
 */
@Service
@RequiredArgsConstructor
public class EstadoService {

    private final EstadoRepository estadoRepository;
    private final PaisService paisService;
    private final EstadoMapper estadoMapper;
    private final EstadoValidator estadoValidator;

    /**
     * Lista estados ativos, opcionalmente por pais, sempre com paginacao.
     */
    @Cacheable(cacheNames = "cadastros:estados", key = "{#paisId, #pageable}")
    @Transactional(readOnly = true)
    public Page<EstadoResponse> listar(UUID paisId, Pageable pageable) {
        Page<Estado> estados = paisId == null
                ? estadoRepository.findByAtivoTrueAndDeletedAtIsNull(pageable)
                : estadoRepository.findByPaisIdAndAtivoTrueAndDeletedAtIsNull(paisId, pageable);
        return estados.map(estadoMapper::toResponse);
    }

    /**
     * Busca estado ativo pelo identificador.
     */
    @Transactional(readOnly = true)
    public EstadoResponse buscar(UUID id) {
        return estadoMapper.toResponse(buscarEntidade(id));
    }

    /**
     * Cria um estado vinculado a pais ativo.
     */
    @CacheEvict(cacheNames = {"cadastros:estados", "cadastros:cidades"}, allEntries = true)
    @Transactional
    public EstadoResponse criar(EstadoRequest request) {
        estadoValidator.validarCriacao(request);
        Estado estado = new Estado();
        aplicar(estado, request);
        return estadoMapper.toResponse(estadoRepository.save(estado));
    }

    /**
     * Atualiza um estado existente.
     */
    @CacheEvict(cacheNames = {"cadastros:estados", "cadastros:cidades"}, allEntries = true)
    @Transactional
    public EstadoResponse atualizar(UUID id, EstadoRequest request) {
        Estado estado = buscarEntidade(id);
        aplicar(estado, request);
        return estadoMapper.toResponse(estado);
    }

    /**
     * Exclui logicamente o estado.
     */
    @CacheEvict(cacheNames = {"cadastros:estados", "cadastros:cidades"}, allEntries = true)
    @Transactional
    public void excluir(UUID id) {
        buscarEntidade(id).excluirLogicamente();
    }

    private void aplicar(Estado estado, EstadoRequest request) {
        Pais pais = paisService.buscarEntidade(request.paisId());
        estado.atualizar(pais, request.nome(), request.uf(), request.codigoIbge());
    }

    /**
     * Recupera a entidade ativa para uso por outros services.
     */
    @Transactional(readOnly = true)
    public Estado buscarEntidade(UUID id) {
        return estadoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estado nao encontrado"));
    }
}
