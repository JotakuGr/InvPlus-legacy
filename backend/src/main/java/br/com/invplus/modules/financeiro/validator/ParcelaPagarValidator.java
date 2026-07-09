package br.com.invplus.modules.financeiro.validator;

import br.com.invplus.modules.financeiro.dto.request.ParcelaPagarBaixaRequest;
import br.com.invplus.modules.financeiro.dto.request.ParcelaPagarRequest;
import br.com.invplus.modules.financeiro.entity.ContaPagar;
import br.com.invplus.modules.financeiro.entity.ParcelaPagar;
import br.com.invplus.modules.financeiro.enums.StatusContaPagar;
import br.com.invplus.modules.financeiro.repository.ParcelaPagarRepository;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Valida regras de criacao, alteracao, baixa e cancelamento de parcelas a pagar.
 */
@Component
@RequiredArgsConstructor
public class ParcelaPagarValidator {

    private final ParcelaPagarRepository repository;

    public void validarCriacao(ContaPagar conta, ParcelaPagarRequest request) {
        validarContaEditavel(conta);
        validarCampos(request);
        validarNumeroUnico(null, request.contaPagarId(), request.numeroParcela());
    }

    public void validarAtualizacao(UUID id, ParcelaPagar parcela, ContaPagar conta, ParcelaPagarRequest request) {
        parcela.validarEditavel();
        validarContaEditavel(conta);
        validarCampos(request);
        validarNumeroUnico(id, request.contaPagarId(), request.numeroParcela());
    }

    public void validarBaixa(ParcelaPagar parcela, ParcelaPagarBaixaRequest request) {
        parcela.validarEditavel();
        if (request.valorPago() == null || request.valorPago().signum() <= 0) {
            throw new InvalidOperationException("Valor pago deve ser maior que zero");
        }
    }

    public void validarCancelamento(ParcelaPagar parcela) {
        parcela.validarEditavel();
    }

    public void validarContaEditavel(ContaPagar conta) {
        if (conta.getStatus() == StatusContaPagar.PAGA) {
            throw new InvalidOperationException("Conta a pagar paga nao permite parcelas");
        }
        if (conta.getStatus() == StatusContaPagar.CANCELADA) {
            throw new InvalidOperationException("Conta a pagar cancelada nao permite parcelas");
        }
    }

    private void validarCampos(ParcelaPagarRequest request) {
        if (request.numeroParcela() == null || request.numeroParcela() <= 0) {
            throw new InvalidOperationException("Numero da parcela deve ser maior que zero");
        }
        if (request.valor() == null || request.valor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Valor da parcela deve ser maior que zero");
        }
    }

    private void validarNumeroUnico(UUID idAtual, UUID contaPagarId, Integer numeroParcela) {
        repository.findByContaPagarIdAndNumeroParcelaAndAtivoTrueAndDeletedAtIsNull(contaPagarId, numeroParcela)
                .filter(existente -> idAtual == null || !existente.getId().equals(idAtual))
                .ifPresent(existente -> {
                    throw new ConflictException("Ja existe parcela ativa com este numero para a conta a pagar");
                });
    }
}
