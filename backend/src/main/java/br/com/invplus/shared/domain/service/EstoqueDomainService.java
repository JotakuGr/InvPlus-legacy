package br.com.invplus.shared.domain.service;

import br.com.invplus.modules.estoque.entity.Estoque;
import br.com.invplus.modules.estoque.entity.MovimentacaoEstoque;
import br.com.invplus.modules.estoque.enums.MovimentacaoOrigem;
import br.com.invplus.modules.estoque.enums.TipoMovimentacaoEstoque;
import br.com.invplus.modules.estoque.service.MovimentacaoEstoqueService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Ponto unico para alteracoes de saldo, disponibilidade e movimentacoes de estoque.
 */
@Service
public class EstoqueDomainService {

    private final MovimentacaoEstoqueService movimentacaoService;

    public EstoqueDomainService(MovimentacaoEstoqueService movimentacaoService) {
        this.movimentacaoService = movimentacaoService;
    }

    public void entrada(Estoque estoque, BigDecimal quantidade, MovimentacaoOrigem origem, UUID origemId, String documento, String observacao) {
        BigDecimal saldoAnterior = estoque.getQuantidade();
        estoque.adicionar(quantidade);
        registrar(estoque, TipoMovimentacaoEstoque.ENTRADA, quantidade, saldoAnterior, estoque.getQuantidade(), origem, origemId, documento, observacao);
    }

    public void saida(Estoque estoque, BigDecimal quantidade, MovimentacaoOrigem origem, UUID origemId, String documento, String observacao) {
        BigDecimal saldoAnterior = estoque.getQuantidade();
        estoque.retirar(quantidade);
        registrar(estoque, TipoMovimentacaoEstoque.SAIDA, quantidade, saldoAnterior, estoque.getQuantidade(), origem, origemId, documento, observacao);
    }

    public void ajuste(Estoque estoque, BigDecimal saldoAnterior, MovimentacaoOrigem origem, UUID origemId, String documento, String observacao) {
        BigDecimal saldoPosterior = estoque.getQuantidade();
        int comparacao = saldoPosterior.compareTo(saldoAnterior);
        if (comparacao == 0) {
            return;
        }
        TipoMovimentacaoEstoque tipo = comparacao > 0
                ? TipoMovimentacaoEstoque.AJUSTE_ENTRADA
                : TipoMovimentacaoEstoque.AJUSTE_SAIDA;
        registrar(estoque, tipo, saldoPosterior.subtract(saldoAnterior).abs(), saldoAnterior,
                saldoPosterior, origem, origemId, documento, observacao);
    }

    public void transferencia(Estoque origem, Estoque destino, BigDecimal quantidade, UUID origemId, String documento, String observacao) {
        BigDecimal saldoOrigem = origem.getQuantidade();
        origem.retirar(quantidade);
        registrar(origem, TipoMovimentacaoEstoque.TRANSFERENCIA_SAIDA, quantidade, saldoOrigem,
                origem.getQuantidade(), MovimentacaoOrigem.TRANSFERENCIA, origemId, documento, observacao);

        BigDecimal saldoDestino = destino.getQuantidade();
        destino.adicionar(quantidade);
        registrar(destino, TipoMovimentacaoEstoque.TRANSFERENCIA_ENTRADA, quantidade, saldoDestino,
                destino.getQuantidade(), MovimentacaoOrigem.TRANSFERENCIA, origemId, documento, observacao);
    }

    public void reservar(Estoque estoque, BigDecimal quantidade, UUID origemId, String documento, String observacao) {
        reservar(estoque, quantidade, MovimentacaoOrigem.RESERVA, origemId, documento, observacao);
    }

    public void reservar(Estoque estoque, BigDecimal quantidade, MovimentacaoOrigem origem, UUID origemId, String documento, String observacao) {
        BigDecimal saldoAnterior = estoque.getQuantidade();
        estoque.reservar(quantidade);
        registrar(estoque, TipoMovimentacaoEstoque.RESERVA, quantidade, saldoAnterior, estoque.getQuantidade(),
                origem, origemId, documento, observacao);
    }

    public void cancelarReserva(Estoque estoque, BigDecimal quantidade, UUID origemId, String documento, String observacao) {
        cancelarReserva(estoque, quantidade, MovimentacaoOrigem.RESERVA, origemId, documento, observacao);
    }

    public void cancelarReserva(Estoque estoque, BigDecimal quantidade, MovimentacaoOrigem origem, UUID origemId, String documento, String observacao) {
        BigDecimal saldoAnterior = estoque.getQuantidade();
        estoque.cancelarReserva(quantidade);
        registrar(estoque, TipoMovimentacaoEstoque.CANCELAMENTO_RESERVA, quantidade, saldoAnterior, estoque.getQuantidade(),
                origem, origemId, documento, observacao);
    }

    public void baixarReserva(Estoque estoque, BigDecimal quantidade, UUID origemId, String documento, String observacao) {
        baixarReserva(estoque, quantidade, MovimentacaoOrigem.RESERVA, origemId, documento, observacao);
    }

    public void baixarReserva(Estoque estoque, BigDecimal quantidade, MovimentacaoOrigem origem, UUID origemId, String documento, String observacao) {
        BigDecimal saldoAnterior = estoque.getQuantidade();
        estoque.baixarReserva(quantidade);
        registrar(estoque, TipoMovimentacaoEstoque.BAIXA_RESERVA, quantidade, saldoAnterior, estoque.getQuantidade(),
                origem, origemId, documento, observacao);
    }

    /**
     * Registra a entrada inicial quando o saldo ja foi informado no cadastro de estoque.
     */
    public void registrarEntradaInicial(Estoque estoque) {
        if (estoque.getQuantidade().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        registrar(estoque, TipoMovimentacaoEstoque.ENTRADA, estoque.getQuantidade(), BigDecimal.ZERO,
                estoque.getQuantidade(), MovimentacaoOrigem.API, estoque.getId(), null, "Saldo inicial do estoque");
    }

    private void registrar(
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
        MovimentacaoEstoque movimentacao = movimentacaoService.registrarInterna(
                estoque,
                tipo,
                quantidade,
                saldoAnterior,
                saldoPosterior,
                origem,
                origemId,
                documento,
                observacao
        );
        estoque.setUltimoMovimentoId(movimentacao.getId());
        estoque.setUltimoMovimentoEm(OffsetDateTime.now());
    }
}
