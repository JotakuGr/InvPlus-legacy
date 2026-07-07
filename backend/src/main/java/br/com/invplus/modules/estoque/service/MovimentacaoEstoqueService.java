package br.com.invplus.modules.estoque.service;

import br.com.invplus.modules.estoque.dto.request.MovimentacaoEstoqueRequest;
import br.com.invplus.modules.estoque.dto.response.MovimentacaoEstoqueResponse;
import br.com.invplus.modules.estoque.entity.Estoque;
import br.com.invplus.modules.estoque.entity.MovimentacaoEstoque;
import br.com.invplus.modules.estoque.enums.MovimentacaoOrigem;
import br.com.invplus.modules.estoque.enums.TipoMovimentacaoEstoque;
import br.com.invplus.modules.estoque.mapper.MovimentacaoEstoqueMapper;
import br.com.invplus.modules.estoque.repository.EstoqueRepository;
import br.com.invplus.modules.estoque.repository.MovimentacaoEstoqueRepository;
import br.com.invplus.modules.estoque.specification.MovimentacaoEstoqueSpecification;
import br.com.invplus.modules.estoque.validator.MovimentacaoEstoqueValidator;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consulta e registro interno do historico imutavel de movimentacoes.
 */
@Service
public class MovimentacaoEstoqueService {

    private final MovimentacaoEstoqueRepository repository;
    private final EstoqueRepository estoqueRepository;
    private final MovimentacaoEstoqueMapper mapper;
    private final MovimentacaoEstoqueValidator validator;

    public MovimentacaoEstoqueService(
            MovimentacaoEstoqueRepository repository,
            EstoqueRepository estoqueRepository,
            MovimentacaoEstoqueMapper mapper,
            MovimentacaoEstoqueValidator validator
    ) {
        this.repository = repository;
        this.estoqueRepository = estoqueRepository;
        this.mapper = mapper;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public Page<MovimentacaoEstoqueResponse> listarComFiltros(
            UUID estoqueId,
            UUID produtoId,
            UUID empresaId,
            UUID depositoId,
            UUID localizacaoId,
            TipoMovimentacaoEstoque tipoMovimentacao,
            MovimentacaoOrigem origem,
            OffsetDateTime dataInicial,
            OffsetDateTime dataFinal,
            Pageable pageable
    ) {
        Specification<MovimentacaoEstoque> specification = MovimentacaoEstoqueSpecification.estoqueIgual(estoqueId)
                .and(MovimentacaoEstoqueSpecification.produtoIgual(produtoId))
                .and(MovimentacaoEstoqueSpecification.empresaIgual(empresaId))
                .and(MovimentacaoEstoqueSpecification.depositoIgual(depositoId))
                .and(MovimentacaoEstoqueSpecification.localizacaoIgual(localizacaoId))
                .and(MovimentacaoEstoqueSpecification.tipoIgual(tipoMovimentacao))
                .and(MovimentacaoEstoqueSpecification.origemIgual(origem))
                .and(MovimentacaoEstoqueSpecification.periodoDe(dataInicial))
                .and(MovimentacaoEstoqueSpecification.periodoAte(dataFinal));
        return repository.findAll(specification, pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public MovimentacaoEstoqueResponse buscar(UUID id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("MovimentacaoEstoque nao encontrada"));
    }

    @Transactional
    public MovimentacaoEstoque registrarInterna(MovimentacaoEstoqueRequest request) {
        validator.validarRegistro(request);
        Estoque estoque = estoqueRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(request.estoqueId())
                .orElseThrow(() -> new ResourceNotFoundException("Estoque nao encontrado"));
        MovimentacaoEstoque entity = mapper.toEntity(request);
        entity.vincularEstoque(estoque);
        return repository.save(entity);
    }

    @Transactional
    public MovimentacaoEstoque registrarInterna(
            Estoque estoque,
            TipoMovimentacaoEstoque tipo,
            BigDecimal quantidade,
            BigDecimal saldoAnterior,
            BigDecimal saldoPosterior,
            MovimentacaoOrigem origem,
            UUID origemId,
            String documento,
            String observacao
    ) {
        MovimentacaoEstoqueRequest request = new MovimentacaoEstoqueRequest(
                estoque.getId(),
                tipo,
                quantidade,
                saldoAnterior,
                saldoPosterior,
                origem,
                origemId,
                documento,
                observacao,
                OffsetDateTime.now()
        );
        validator.validarRegistro(request);
        MovimentacaoEstoque entity = mapper.toEntity(request);
        entity.vincularEstoque(estoque);
        return repository.save(entity);
    }

    /**
     * Movimentacoes sao imutaveis; alteracao funcional nao e permitida.
     */
    public void bloquearAlteracao() {
        throw new InvalidOperationException("Movimentacao de estoque nao pode ser alterada");
    }

    /**
     * Movimentacoes sao imutaveis; exclusao funcional nao e permitida.
     */
    public void bloquearExclusao() {
        throw new InvalidOperationException("Movimentacao de estoque nao pode ser excluida");
    }
}
