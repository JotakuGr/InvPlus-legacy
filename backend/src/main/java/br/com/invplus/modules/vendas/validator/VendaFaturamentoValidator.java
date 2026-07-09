package br.com.invplus.modules.vendas.validator;

import br.com.invplus.modules.estoque.enums.MovimentacaoOrigem;
import br.com.invplus.modules.estoque.enums.StatusReservaEstoque;
import br.com.invplus.modules.estoque.repository.ReservaEstoqueRepository;
import br.com.invplus.modules.vendas.dto.request.VendaFaturamentoRequest;
import br.com.invplus.modules.vendas.entity.PedidoVenda;
import br.com.invplus.modules.vendas.entity.VendaFaturamento;
import br.com.invplus.modules.vendas.enums.StatusPedidoVenda;
import br.com.invplus.modules.vendas.enums.StatusVendaFaturamento;
import br.com.invplus.modules.vendas.repository.VendaFaturamentoRepository;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import org.springframework.stereotype.Component;

/**
 * Valida regras de faturamento antes de baixar reservas e alterar o pedido.
 */
@Component
public class VendaFaturamentoValidator {

    private final VendaFaturamentoRepository faturamentoRepository;
    private final ReservaEstoqueRepository reservaRepository;

    public VendaFaturamentoValidator(
            VendaFaturamentoRepository faturamentoRepository,
            ReservaEstoqueRepository reservaRepository
    ) {
        this.faturamentoRepository = faturamentoRepository;
        this.reservaRepository = reservaRepository;
    }

    public void validarFaturamento(VendaFaturamentoRequest request, PedidoVenda pedido) {
        if (request.condicaoPagamentoId() == null) {
            throw new InvalidOperationException("Condicao de pagamento e obrigatoria para faturar venda");
        }
        if (pedido.getStatus() != StatusPedidoVenda.APROVADO) {
            throw new InvalidOperationException("Somente pedido aprovado pode ser faturado");
        }
        if (faturamentoRepository.existsByPedidoVendaIdAndAtivoTrueAndDeletedAtIsNull(pedido.getId())) {
            throw new ConflictException("Pedido de venda ja possui faturamento ativo");
        }
        if (faturamentoRepository.existsByEmpresaIdAndNumeroIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(
                pedido.getEmpresa().getId(),
                request.numero()
        )) {
            throw new ConflictException("Ja existe faturamento ativo com este numero para a empresa");
        }
        boolean possuiReservaAtiva = reservaRepository.existsByOrigemAndOrigemIdAndStatusAndAtivoTrueAndDeletedAtIsNull(
                MovimentacaoOrigem.VENDA,
                pedido.getId(),
                StatusReservaEstoque.ATIVA
        );
        if (!possuiReservaAtiva) {
            throw new InvalidOperationException("Pedido de venda precisa possuir reserva ativa para ser faturado");
        }
    }

    public void validarCancelamento(VendaFaturamento faturamento) {
        if (faturamento.getStatus() == StatusVendaFaturamento.CANCELADO) {
            throw new InvalidOperationException("Faturamento de venda ja esta cancelado");
        }
    }
}
