package br.com.invplus.modules.estoque.service;

import br.com.invplus.modules.estoque.dto.response.EstoqueCompletoResponse;
import br.com.invplus.modules.estoque.dto.response.EstoqueDisponivelResponse;
import br.com.invplus.modules.estoque.dto.response.ReservaEstoqueResponse;
import br.com.invplus.modules.estoque.entity.Estoque;
import br.com.invplus.modules.estoque.enums.StatusReservaEstoque;
import br.com.invplus.modules.estoque.mapper.EstoqueMapper;
import br.com.invplus.modules.estoque.mapper.MovimentacaoEstoqueMapper;
import br.com.invplus.modules.estoque.mapper.ReservaEstoqueMapper;
import br.com.invplus.modules.estoque.repository.EstoqueRepository;
import br.com.invplus.modules.estoque.repository.MovimentacaoEstoqueRepository;
import br.com.invplus.modules.estoque.repository.ReservaEstoqueRepository;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Monta uma visao consolidada do saldo, movimentacoes recentes e reservas ativas.
 */
@Service
public class EstoqueAggregateService {

    private final EstoqueRepository estoqueRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final ReservaEstoqueRepository reservaRepository;
    private final EstoqueMapper estoqueMapper;
    private final MovimentacaoEstoqueMapper movimentacaoMapper;
    private final ReservaEstoqueMapper reservaMapper;

    public EstoqueAggregateService(
            EstoqueRepository estoqueRepository,
            MovimentacaoEstoqueRepository movimentacaoRepository,
            ReservaEstoqueRepository reservaRepository,
            EstoqueMapper estoqueMapper,
            MovimentacaoEstoqueMapper movimentacaoMapper,
            ReservaEstoqueMapper reservaMapper
    ) {
        this.estoqueRepository = estoqueRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.reservaRepository = reservaRepository;
        this.estoqueMapper = estoqueMapper;
        this.movimentacaoMapper = movimentacaoMapper;
        this.reservaMapper = reservaMapper;
    }

    /**
     * Carrega o estoque com sua ultima movimentacao, reservas ativas e disponibilidade calculada.
     */
    @Transactional(readOnly = true)
    public EstoqueCompletoResponse buscar(UUID estoqueId) {
        Estoque estoque = buscarEstoqueAtivo(estoqueId);
        List<ReservaEstoqueResponse> reservasAtivas = reservaRepository
                .findByEstoqueIdAndStatusAndAtivoTrueAndDeletedAtIsNull(estoqueId, StatusReservaEstoque.ATIVA)
                .stream()
                .map(reservaMapper::toResponse)
                .toList();

        return new EstoqueCompletoResponse(
                estoqueMapper.toResponse(estoque),
                movimentacaoRepository.findTopByEstoqueIdOrderByDataMovimentacaoDesc(estoqueId)
                        .map(movimentacaoMapper::toResponse)
                        .orElse(null),
                reservasAtivas,
                toDisponivel(estoque)
        );
    }

    /**
     * Retorna apenas os numeros de saldo para chamadas frequentes de PDV e Vendas.
     */
    @Transactional(readOnly = true)
    public EstoqueDisponivelResponse buscarDisponivel(UUID produtoId, UUID empresaId, UUID depositoId, UUID localizacaoId) {
        Estoque estoque = estoqueRepository
                .findByProdutoIdAndEmpresaIdAndDepositoIdAndLocalizacaoIdAndAtivoTrueAndDeletedAtIsNull(
                        produtoId,
                        empresaId,
                        depositoId,
                        localizacaoId
                )
                .orElseThrow(() -> new ResourceNotFoundException("Saldo de estoque nao encontrado"));
        return toDisponivel(estoque);
    }

    private Estoque buscarEstoqueAtivo(UUID estoqueId) {
        return estoqueRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(estoqueId)
                .orElseThrow(() -> new ResourceNotFoundException("Estoque nao encontrado"));
    }

    private EstoqueDisponivelResponse toDisponivel(Estoque estoque) {
        return new EstoqueDisponivelResponse(
                estoque.getId(),
                estoque.getQuantidade(),
                estoque.getQuantidadeReservada(),
                estoque.getQuantidadeDisponivel()
        );
    }
}
