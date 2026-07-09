package br.com.invplus.modules.financeiro.validator;

import br.com.invplus.modules.financeiro.dto.request.RecebimentoRequest;
import br.com.invplus.modules.financeiro.entity.ParcelaReceber;
import br.com.invplus.modules.financeiro.entity.Recebimento;
import br.com.invplus.modules.financeiro.enums.StatusParcelaReceber;
import br.com.invplus.modules.financeiro.enums.StatusRecebimento;
import br.com.invplus.modules.pagamentos.entity.FormaPagamento;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/**
 * Valida as regras para registro e cancelamento de recebimentos financeiros.
 */
@Component
public class RecebimentoValidator {

    public void validarCriacao(ParcelaReceber parcela, FormaPagamento formaPagamento, RecebimentoRequest request) {
        validarParcelaRecebivel(parcela);
        validarFormaPagamento(formaPagamento);
        validarValores(parcela, request);
    }

    public void validarCancelamento(Recebimento recebimento) {
        if (recebimento.getStatus() == StatusRecebimento.CANCELADO) {
            throw new InvalidOperationException("Recebimento ja esta cancelado");
        }
    }

    private void validarParcelaRecebivel(ParcelaReceber parcela) {
        if (parcela.getStatus() == StatusParcelaReceber.PAGA) {
            throw new InvalidOperationException("Parcela a receber paga nao permite novo recebimento");
        }
        if (parcela.getStatus() == StatusParcelaReceber.CANCELADA) {
            throw new InvalidOperationException("Parcela a receber cancelada nao permite recebimento");
        }
    }

    private void validarFormaPagamento(FormaPagamento formaPagamento) {
        if (!Boolean.TRUE.equals(formaPagamento.getAtivo()) || formaPagamento.getDeletedAt() != null) {
            throw new InvalidOperationException("Forma de pagamento inativa nao pode ser usada em recebimento");
        }
    }

    private void validarValores(ParcelaReceber parcela, RecebimentoRequest request) {
        if (request.valorRecebido() == null || request.valorRecebido().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Valor recebido deve ser maior que zero");
        }
        BigDecimal juros = zeroSeNulo(request.juros());
        BigDecimal multa = zeroSeNulo(request.multa());
        BigDecimal desconto = zeroSeNulo(request.desconto());
        if (juros.signum() < 0 || multa.signum() < 0 || desconto.signum() < 0) {
            throw new InvalidOperationException("Juros, multa e desconto nao podem ser negativos");
        }
        if (request.dataRecebimento() == null) {
            throw new InvalidOperationException("Data de recebimento e obrigatoria");
        }
        BigDecimal limite = parcela.getValorAberto().add(juros).add(multa).subtract(desconto);
        if (request.valorRecebido().compareTo(limite) > 0) {
            throw new InvalidOperationException("Valor recebido nao pode ultrapassar o saldo aberto com juros, multa e desconto");
        }
    }

    private BigDecimal zeroSeNulo(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
