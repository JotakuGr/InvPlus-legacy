package br.com.invplus.modules.estoque.service;

import br.com.invplus.modules.estoque.dto.request.ReservaEstoqueRequest;
import br.com.invplus.modules.estoque.dto.response.ReservaEstoqueResponse;
import br.com.invplus.modules.estoque.entity.Estoque;
import br.com.invplus.modules.estoque.enums.MovimentacaoOrigem;
import br.com.invplus.modules.estoque.repository.EstoqueRepository;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import br.com.invplus.shared.domain.service.EstoqueDomainService;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * API interna para que Compras, Vendas, Inventario e PDV operem estoque sem acessar repositories diretamente.
 */
@Service
public class EstoqueFacade {

    private final EstoqueRepository estoqueRepository;
    private final EstoqueDomainService estoqueDomainService;
    private final ReservaEstoqueService reservaEstoqueService;

    public EstoqueFacade(
            EstoqueRepository estoqueRepository,
            EstoqueDomainService estoqueDomainService,
            ReservaEstoqueService reservaEstoqueService
    ) {
        this.estoqueRepository = estoqueRepository;
        this.estoqueDomainService = estoqueDomainService;
        this.reservaEstoqueService = reservaEstoqueService;
    }

    @Transactional
    public void entrada(UUID estoqueId, BigDecimal quantidade, MovimentacaoOrigem origem, UUID origemId,
                        String documento, String observacao) {
        estoqueDomainService.entrada(buscarEstoque(estoqueId), quantidade, origem, origemId, documento, observacao);
    }

    @Transactional
    public void saida(UUID estoqueId, BigDecimal quantidade, MovimentacaoOrigem origem, UUID origemId,
                      String documento, String observacao) {
        estoqueDomainService.saida(buscarEstoque(estoqueId), quantidade, origem, origemId, documento, observacao);
    }

    @Transactional
    public void transferencia(UUID estoqueOrigemId, UUID estoqueDestinoId, BigDecimal quantidade, UUID origemId,
                              String documento, String observacao) {
        estoqueDomainService.transferencia(buscarEstoque(estoqueOrigemId), buscarEstoque(estoqueDestinoId),
                quantidade, origemId, documento, observacao);
    }

    /**
     * Ajusta o saldo fisico para um novo valor e registra a diferenca como movimentacao.
     */
    @Transactional
    public void ajuste(UUID estoqueId, BigDecimal novaQuantidade, MovimentacaoOrigem origem, UUID origemId,
                       String documento, String observacao) {
        Estoque estoque = buscarEstoque(estoqueId);
        BigDecimal saldoAnterior = estoque.getQuantidade();
        estoque.atualizarSaldos(novaQuantidade, estoque.getQuantidadeReservada(), estoque.getQuantidadeAvariada(),
                estoque.getQuantidadeTransito(), estoque.getEstoqueMinimo(), estoque.getEstoqueMaximo(),
                estoque.getUltimoMovimentoEm(), estoque.getUltimoMovimentoId());
        estoqueDomainService.ajuste(estoque, saldoAnterior, origem, origemId, documento, observacao);
    }

    @Transactional
    public ReservaEstoqueResponse reserva(ReservaEstoqueRequest request) {
        return reservaEstoqueService.criar(request);
    }

    @Transactional
    public ReservaEstoqueResponse cancelamento(UUID reservaId) {
        return reservaEstoqueService.cancelar(reservaId);
    }

    @Transactional
    public ReservaEstoqueResponse baixa(UUID reservaId) {
        return reservaEstoqueService.baixar(reservaId);
    }

    @Transactional
    public void inventario(UUID estoqueId, BigDecimal novaQuantidade, UUID origemId, String documento, String observacao) {
        ajuste(estoqueId, novaQuantidade, MovimentacaoOrigem.INVENTARIO, origemId, documento, observacao);
    }

    private Estoque buscarEstoque(UUID estoqueId) {
        return estoqueRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(estoqueId)
                .orElseThrow(() -> new ResourceNotFoundException("Estoque nao encontrado"));
    }
}
