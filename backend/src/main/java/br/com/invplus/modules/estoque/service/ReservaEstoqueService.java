package br.com.invplus.modules.estoque.service;

import br.com.invplus.modules.estoque.dto.request.ReservaEstoqueRequest;
import br.com.invplus.modules.estoque.dto.response.ReservaEstoqueResponse;
import br.com.invplus.modules.estoque.entity.Estoque;
import br.com.invplus.modules.estoque.entity.ReservaEstoque;
import br.com.invplus.modules.estoque.enums.MovimentacaoOrigem;
import br.com.invplus.modules.estoque.enums.StatusReservaEstoque;
import br.com.invplus.modules.estoque.mapper.ReservaEstoqueMapper;
import br.com.invplus.modules.estoque.repository.EstoqueRepository;
import br.com.invplus.modules.estoque.repository.ReservaEstoqueRepository;
import br.com.invplus.modules.estoque.specification.ReservaEstoqueSpecification;
import br.com.invplus.modules.estoque.validator.ReservaEstoqueValidator;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import br.com.invplus.shared.domain.service.BaseCrudService;
import br.com.invplus.shared.domain.service.EstoqueDomainService;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fluxo de estado de reservas de estoque.
 */
@Service
public class ReservaEstoqueService extends BaseCrudService<ReservaEstoque, ReservaEstoqueRequest, ReservaEstoqueResponse> {

    private final ReservaEstoqueRepository repository;
    private final EstoqueRepository estoqueRepository;
    private final ReservaEstoqueMapper mapper;
    private final ReservaEstoqueValidator validator;
    private final EstoqueDomainService estoqueDomainService;

    public ReservaEstoqueService(
            ReservaEstoqueRepository repository,
            EstoqueRepository estoqueRepository,
            ReservaEstoqueMapper mapper,
            ReservaEstoqueValidator validator,
            EstoqueDomainService estoqueDomainService
    ) {
        super(repository, mapper, "ReservaEstoque");
        this.repository = repository;
        this.estoqueRepository = estoqueRepository;
        this.mapper = mapper;
        this.validator = validator;
        this.estoqueDomainService = estoqueDomainService;
    }

    @Transactional(readOnly = true)
    public Page<ReservaEstoqueResponse> listarComFiltros(
            UUID estoqueId,
            UUID produtoId,
            UUID empresaId,
            UUID filialId,
            UUID depositoId,
            UUID localizacaoId,
            StatusReservaEstoque status,
            MovimentacaoOrigem origem,
            UUID origemId,
            Boolean ativo,
            Pageable pageable
    ) {
        Specification<ReservaEstoque> specification = ReservaEstoqueSpecification.naoDeletado()
                .and(ReservaEstoqueSpecification.ativo(ativo))
                .and(ReservaEstoqueSpecification.estoqueIgual(estoqueId))
                .and(ReservaEstoqueSpecification.produtoIgual(produtoId))
                .and(ReservaEstoqueSpecification.empresaIgual(empresaId))
                .and(ReservaEstoqueSpecification.filialIgual(filialId))
                .and(ReservaEstoqueSpecification.depositoIgual(depositoId))
                .and(ReservaEstoqueSpecification.localizacaoIgual(localizacaoId))
                .and(ReservaEstoqueSpecification.statusIgual(status))
                .and(ReservaEstoqueSpecification.origemIgual(origem))
                .and(ReservaEstoqueSpecification.origemIdIgual(origemId));
        return repository.findAll(specification, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public ReservaEstoqueResponse criar(ReservaEstoqueRequest request) {
        Estoque estoque = buscarEstoqueAtivo(request.estoqueId());
        validator.validarCriacao(request, estoque);

        ReservaEstoque entity = mapper.toEntity(request);
        entity.setStatus(StatusReservaEstoque.ATIVA);
        entity.vincularEstoque(estoque);
        ReservaEstoque salvo = repository.save(entity);
        estoqueDomainService.reservar(estoque, salvo.getQuantidade(), salvo.getId(), null, "Reserva de estoque");
        return mapper.toResponse(salvo);
    }

    @Transactional
    public ReservaEstoqueResponse cancelar(UUID id) {
        ReservaEstoque reserva = buscarEntidade(id);
        validator.validarCancelamento(reserva);
        estoqueDomainService.cancelarReserva(reserva.getEstoque(), reserva.getQuantidade(), reserva.getId(),
                null, "Cancelamento de reserva de estoque");
        reserva.cancelar();
        return mapper.toResponse(reserva);
    }

    @Transactional
    public ReservaEstoqueResponse baixar(UUID id) {
        ReservaEstoque reserva = buscarEntidade(id);
        validator.validarBaixa(reserva);
        estoqueDomainService.baixarReserva(reserva.getEstoque(), reserva.getQuantidade(), reserva.getId(),
                null, "Baixa de reserva de estoque");
        reserva.baixar();
        return mapper.toResponse(reserva);
    }

    private Estoque buscarEstoqueAtivo(UUID id) {
        return estoqueRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estoque nao encontrado"));
    }
}
