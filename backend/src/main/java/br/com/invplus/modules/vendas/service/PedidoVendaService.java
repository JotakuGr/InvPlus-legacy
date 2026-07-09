package br.com.invplus.modules.vendas.service;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.cadastros.repository.FilialRepository;
import br.com.invplus.modules.estoque.dto.request.ReservaEstoqueRequest;
import br.com.invplus.modules.estoque.entity.Estoque;
import br.com.invplus.modules.estoque.enums.MovimentacaoOrigem;
import br.com.invplus.modules.estoque.repository.EstoqueRepository;
import br.com.invplus.modules.estoque.service.ReservaEstoqueService;
import br.com.invplus.modules.pessoas.entity.Cliente;
import br.com.invplus.modules.pessoas.entity.Funcionario;
import br.com.invplus.modules.pessoas.repository.ClienteRepository;
import br.com.invplus.modules.pessoas.repository.FuncionarioRepository;
import br.com.invplus.modules.vendas.dto.request.PedidoVendaRequest;
import br.com.invplus.modules.vendas.dto.response.PedidoVendaResponse;
import br.com.invplus.modules.vendas.entity.PedidoVenda;
import br.com.invplus.modules.vendas.enums.StatusPedidoVenda;
import br.com.invplus.modules.vendas.events.PedidoVendaAprovadoEvent;
import br.com.invplus.modules.vendas.events.PedidoVendaCanceladoEvent;
import br.com.invplus.modules.vendas.events.PedidoVendaCriadoEvent;
import br.com.invplus.modules.vendas.events.PedidoVendaReservadoEvent;
import br.com.invplus.modules.vendas.mapper.PedidoVendaMapper;
import br.com.invplus.modules.vendas.repository.PedidoVendaItemRepository;
import br.com.invplus.modules.vendas.repository.PedidoVendaRepository;
import br.com.invplus.modules.vendas.specification.PedidoVendaSpecification;
import br.com.invplus.modules.vendas.validator.PedidoVendaValidator;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import br.com.invplus.shared.domain.service.BaseCrudService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orquestra o ciclo de vida do pedido de venda e suas transicoes de status.
 */
@Service
public class PedidoVendaService extends BaseCrudService<PedidoVenda, PedidoVendaRequest, PedidoVendaResponse> {

    private final PedidoVendaRepository repository;
    private final PedidoVendaMapper mapper;
    private final PedidoVendaValidator validator;
    private final EmpresaRepository empresaRepository;
    private final FilialRepository filialRepository;
    private final ClienteRepository clienteRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final PedidoVendaItemRepository itemRepository;
    private final EstoqueRepository estoqueRepository;
    private final ReservaEstoqueService reservaEstoqueService;
    private final VendaHistoricoService historicoService;
    private final ApplicationEventPublisher eventPublisher;

    public PedidoVendaService(
            PedidoVendaRepository repository,
            PedidoVendaMapper mapper,
            PedidoVendaValidator validator,
            EmpresaRepository empresaRepository,
            FilialRepository filialRepository,
            ClienteRepository clienteRepository,
            FuncionarioRepository funcionarioRepository,
            PedidoVendaItemRepository itemRepository,
            EstoqueRepository estoqueRepository,
            ReservaEstoqueService reservaEstoqueService,
            VendaHistoricoService historicoService,
            ApplicationEventPublisher eventPublisher
    ) {
        super(repository, mapper, "Pedido de venda");
        this.repository = repository;
        this.mapper = mapper;
        this.validator = validator;
        this.empresaRepository = empresaRepository;
        this.filialRepository = filialRepository;
        this.clienteRepository = clienteRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.itemRepository = itemRepository;
        this.estoqueRepository = estoqueRepository;
        this.reservaEstoqueService = reservaEstoqueService;
        this.historicoService = historicoService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public Page<PedidoVendaResponse> listarComFiltros(UUID empresaId, UUID filialId, UUID clienteId, UUID vendedorId,
                                                      String numero, StatusPedidoVenda status,
                                                      LocalDate dataEmissaoDe, LocalDate dataEmissaoAte,
                                                      Boolean ativo, Pageable pageable) {
        Specification<PedidoVenda> specification = PedidoVendaSpecification.naoDeletado();
        specification = and(specification, PedidoVendaSpecification.ativo(ativo));
        specification = and(specification, PedidoVendaSpecification.empresaIgual(empresaId));
        specification = and(specification, PedidoVendaSpecification.filialIgual(filialId));
        specification = and(specification, PedidoVendaSpecification.clienteIgual(clienteId));
        specification = and(specification, PedidoVendaSpecification.vendedorIgual(vendedorId));
        specification = and(specification, PedidoVendaSpecification.numeroContem(numero));
        specification = and(specification, PedidoVendaSpecification.statusIgual(status));
        specification = and(specification, PedidoVendaSpecification.dataEmissaoMaiorOuIgual(dataEmissaoDe));
        specification = and(specification, PedidoVendaSpecification.dataEmissaoMenorOuIgual(dataEmissaoAte));
        return repository.findAll(specification, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public PedidoVendaResponse criar(PedidoVendaRequest request) {
        validator.validarCriacao(request);
        PedidoVenda entity = mapper.toEntity(request);
        entity.setStatus(StatusPedidoVenda.ABERTO);
        entity.atualizarValorTotal(null);
        associar(entity, request);
        PedidoVenda salvo = repository.save(entity);
        historicoService.registrarPedido(salvo, null, salvo.getStatus(), "Pedido de venda criado");
        eventPublisher.publishEvent(new PedidoVendaCriadoEvent(
                salvo.getId(),
                salvo.getEmpresa().getId(),
                salvo.getCliente().getId(),
                salvo.getValorTotal()
        ));
        return mapper.toResponse(salvo);
    }

    @Override
    @Transactional
    public PedidoVendaResponse atualizar(UUID id, PedidoVendaRequest request) {
        PedidoVenda entity = buscarEntidade(id);
        validator.validarAtualizacao(entity, request);
        mapper.updateEntity(entity, request);
        associar(entity, request);
        return mapper.toResponse(entity);
    }

    @Transactional
    public PedidoVendaResponse aprovar(UUID id) {
        PedidoVenda entity = buscarEntidade(id);
        StatusPedidoVenda anterior = entity.getStatus();
        entity.aprovar(historicoService.usuarioAtual().orElse(null));
        reservarItens(entity);
        UUID usuarioId = historicoService.usuarioAtual().orElse(null);
        entity.setAprovadoPor(usuarioId);
        historicoService.registrarPedido(entity, anterior, entity.getStatus(), "Pedido de venda aprovado");
        historicoService.registrarPedido(entity, entity.getStatus(), entity.getStatus(), "Reserva criada automaticamente");
        eventPublisher.publishEvent(new PedidoVendaReservadoEvent(
                entity.getId(),
                entity.getEmpresa().getId(),
                entity.getCliente().getId()
        ));
        eventPublisher.publishEvent(new PedidoVendaAprovadoEvent(
                entity.getId(),
                entity.getEmpresa().getId(),
                entity.getCliente().getId(),
                entity.getAprovadoPor()
        ));
        return mapper.toResponse(entity);
    }

    @Transactional
    public PedidoVendaResponse cancelar(UUID id) {
        PedidoVenda entity = buscarEntidade(id);
        StatusPedidoVenda anterior = entity.getStatus();
        if (entity.getStatus() == StatusPedidoVenda.APROVADO || entity.getStatus() == StatusPedidoVenda.SEPARANDO) {
            reservaEstoqueService.cancelarPorOrigem(MovimentacaoOrigem.VENDA, entity.getId());
        }
        entity.cancelar();
        historicoService.registrarPedido(entity, anterior, entity.getStatus(), "Pedido de venda cancelado");
        eventPublisher.publishEvent(new PedidoVendaCanceladoEvent(
                entity.getId(),
                entity.getEmpresa().getId(),
                entity.getCliente().getId()
        ));
        return mapper.toResponse(entity);
    }

    @Transactional
    public PedidoVendaResponse faturar(UUID id) {
        throw new InvalidOperationException("Use o fluxo de faturamento de venda para faturar o pedido");
    }

    @Override
    @Transactional
    public void excluir(UUID id) {
        PedidoVenda entity = buscarEntidade(id);
        if (entity.getStatus() == StatusPedidoVenda.FATURADO) {
            throw new InvalidOperationException("Pedido faturado nao pode ser excluido");
        }
        entity.excluirLogicamente();
    }

    @Transactional
    public void recalcularTotal(PedidoVenda pedido) {
        BigDecimal total = itemRepository.somarValorTotal(pedido.getId());
        pedido.atualizarValorTotal(total);
    }

    private void associar(PedidoVenda entity, PedidoVendaRequest request) {
        Empresa empresa = buscarEmpresa(request.empresaId());
        Filial filial = buscarFilial(request.filialId());
        validarFilialDaEmpresa(filial, empresa);
        entity.setEmpresa(empresa);
        entity.setFilial(filial);
        entity.setCliente(buscarCliente(request.clienteId()));
        entity.setVendedor(buscarVendedor(request.vendedorId()));
    }

    private Empresa buscarEmpresa(UUID id) {
        return empresaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa nao encontrada"));
    }

    private Filial buscarFilial(UUID id) {
        if (id == null) {
            return null;
        }
        return filialRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Filial nao encontrada"));
    }

    private Cliente buscarCliente(UUID id) {
        return clienteRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente nao encontrado"));
    }

    private Funcionario buscarVendedor(UUID id) {
        if (id == null) {
            return null;
        }
        return funcionarioRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendedor nao encontrado"));
    }

    private void validarFilialDaEmpresa(Filial filial, Empresa empresa) {
        if (filial != null && !filial.getEmpresa().getId().equals(empresa.getId())) {
            throw new InvalidOperationException("Filial nao pertence a empresa informada");
        }
    }

    private void reservarItens(PedidoVenda pedido) {
        var itens = itemRepository.findByPedidoVendaIdAndAtivoTrueAndDeletedAtIsNull(pedido.getId());
        if (itens.isEmpty()) {
            throw new InvalidOperationException("Pedido de venda precisa possuir itens para ser aprovado");
        }

        Map<UUID, ReservaPlanejada> reservas = new LinkedHashMap<>();
        itens.forEach(item -> {
            BigDecimal quantidadeBase = item.getQuantidade().multiply(item.getProdutoUnidade().getFatorConversao());
            Estoque estoque = buscarEstoqueDisponivel(pedido, item.getProduto().getId(), quantidadeBase);
            reservas.compute(estoque.getId(), (id, existente) -> {
                if (existente == null) {
                    return new ReservaPlanejada(estoque, quantidadeBase);
                }
                return new ReservaPlanejada(estoque, existente.quantidade().add(quantidadeBase));
            });
        });

        reservas.values().forEach(reserva -> reservaEstoqueService.criar(new ReservaEstoqueRequest(
                reserva.estoque().getId(),
                reserva.quantidade(),
                MovimentacaoOrigem.VENDA,
                pedido.getId(),
                null,
                "Reserva automatica do pedido de venda " + pedido.getNumero()
        )));
    }

    private Estoque buscarEstoqueDisponivel(PedidoVenda pedido, UUID produtoId, BigDecimal quantidade) {
        List<Estoque> estoques = estoqueRepository.findDisponiveisParaReserva(
                produtoId,
                pedido.getEmpresa().getId(),
                quantidade
        );
        if (estoques.isEmpty()) {
            throw new InvalidOperationException("Produto sem estoque disponivel para reserva");
        }
        return estoques.getFirst();
    }

    private Specification<PedidoVenda> and(Specification<PedidoVenda> base, Specification<PedidoVenda> filtro) {
        return filtro == null ? base : base.and(filtro);
    }

    private record ReservaPlanejada(Estoque estoque, BigDecimal quantidade) {
    }
}
