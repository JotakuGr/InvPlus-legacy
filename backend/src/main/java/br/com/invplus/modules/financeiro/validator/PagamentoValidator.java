package br.com.invplus.modules.financeiro.validator;

import br.com.invplus.modules.financeiro.dto.request.PagamentoRequest;
import br.com.invplus.modules.financeiro.entity.ParcelaPagar;
import br.com.invplus.modules.financeiro.entity.Pagamento;
import br.com.invplus.modules.financeiro.enums.StatusParcelaPagar;
import br.com.invplus.modules.financeiro.enums.StatusPagamentoFinanceiro;
import br.com.invplus.modules.pagamentos.entity.FormaPagamento;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/**
 * Valida as regras para registro e cancelamento de pagamentos financeiros.
 */
@Component
public class PagamentoValidator {

    public void validarCriacao(ParcelaPagar parcela, FormaPagamento formaPagamento, PagamentoRequest request) {
        validarParcelaPagavel(parcela);
        validarFormaPagamento(formaPagamento);
        validarValores(parcela, request);
    }

    public void validarCancelamento(Pagamento pagamento) {
        if (pagamento.getStatus() == StatusPagamentoFinanceiro.CANCELADO) {
            throw new InvalidOperationException("Pagamento ja esta cancelado");
        }
    }

    private void validarParcelaPagavel(ParcelaPagar parcela) {
        if (parcela.getStatus() == StatusParcelaPagar.PAGA) {
            throw new InvalidOperationException("Parcela a pagar paga nao permite novo pagamento");
        }
        if (parcela.getStatus() == StatusParcelaPagar.CANCELADA) {
            throw new InvalidOperationException("Parcela a pagar cancelada nao permite pagamento");
        }
    }

    private void validarFormaPagamento(FormaPagamento formaPagamento) {
        if (!Boolean.TRUE.equals(formaPagamento.getAtivo()) || formaPagamento.getDeletedAt() != null) {
            throw new InvalidOperationException("Forma de pagamento inativa nao pode ser usada em pagamento");
        }
    }

    private void validarValores(ParcelaPagar parcela, PagamentoRequest request) {
        if (request.valorPago() == null || request.valorPago().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Valor pago deve ser maior que zero");
        }
        BigDecimal juros = zeroSeNulo(request.juros());
        BigDecimal multa = zeroSeNulo(request.multa());
        BigDecimal desconto = zeroSeNulo(request.desconto());
        if (juros.signum() < 0 || multa.signum() < 0 || desconto.signum() < 0) {
            throw new InvalidOperationException("Juros, multa e desconto nao podem ser negativos");
        }
        if (request.dataPagamento() == null) {
            throw new InvalidOperationException("Data de pagamento e obrigatoria");
        }
        BigDecimal limite = parcela.getValorAberto().add(juros).add(multa).subtract(desconto);
        if (request.valorPago().compareTo(limite) > 0) {
            throw new InvalidOperationException("Valor pago nao pode ultrapassar o saldo aberto com juros, multa e desconto");
        }
    }

    private BigDecimal zeroSeNulo(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
