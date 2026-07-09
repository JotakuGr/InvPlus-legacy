package br.com.invplus.modules.financeiro.validator;

import br.com.invplus.modules.financeiro.dto.request.BaixaParcelaReceberRequest;
import br.com.invplus.modules.financeiro.dto.request.ParcelaReceberRequest;
import br.com.invplus.modules.financeiro.entity.ContaReceber;
import br.com.invplus.modules.financeiro.entity.ParcelaReceber;
import br.com.invplus.modules.financeiro.enums.StatusContaReceber;
import br.com.invplus.modules.financeiro.repository.ParcelaReceberRepository;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Valida regras financeiras de parcelas a receber.
 */
@Component
public class ParcelaReceberValidator {

    private final ParcelaReceberRepository repository;

    public ParcelaReceberValidator(ParcelaReceberRepository repository) {
        this.repository = repository;
    }

    public void validarCriacao(ContaReceber conta, ParcelaReceberRequest request) {
        validarContaEditavel(conta);
        validarCampos(request);
        validarNumeroUnico(null, request.contaReceberId(), request.numeroParcela());
    }

    public void validarAtualizacao(UUID id, ParcelaReceber parcela, ContaReceber conta, ParcelaReceberRequest request) {
        parcela.validarEditavel();
        validarContaEditavel(conta);
        validarCampos(request);
        validarNumeroUnico(id, request.contaReceberId(), request.numeroParcela());
    }

    public void validarBaixa(ParcelaReceber parcela, BaixaParcelaReceberRequest request) {
        parcela.validarEditavel();
        if (request.valorPago() == null || request.valorPago().signum() <= 0) {
            throw new InvalidOperationException("Valor pago deve ser maior que zero");
        }
    }

    public void validarCancelamento(ParcelaReceber parcela) {
        parcela.validarEditavel();
    }

    public void validarContaEditavel(ContaReceber conta) {
        if (conta.getStatus() == StatusContaReceber.PAGA) {
            throw new InvalidOperationException("Conta a receber paga nao permite parcelas");
        }
        if (conta.getStatus() == StatusContaReceber.CANCELADA) {
            throw new InvalidOperationException("Conta a receber cancelada nao permite parcelas");
        }
    }

    private void validarCampos(ParcelaReceberRequest request) {
        if (request.numeroParcela() == null || request.numeroParcela() <= 0) {
            throw new InvalidOperationException("Numero da parcela deve ser maior que zero");
        }
        if (request.valor() == null || request.valor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Valor da parcela deve ser maior que zero");
        }
    }

    private void validarNumeroUnico(UUID idAtual, UUID contaReceberId, Integer numeroParcela) {
        repository.findByContaReceberIdAndNumeroParcelaAndAtivoTrueAndDeletedAtIsNull(contaReceberId, numeroParcela)
                .filter(existente -> idAtual == null || !existente.getId().equals(idAtual))
                .ifPresent(existente -> {
                    throw new ConflictException("Ja existe parcela ativa com este numero para a conta a receber");
                });
    }
}
