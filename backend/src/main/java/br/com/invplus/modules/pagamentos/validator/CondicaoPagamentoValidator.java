package br.com.invplus.modules.pagamentos.validator;

import br.com.invplus.modules.pagamentos.dto.request.CondicaoPagamentoRequest;
import br.com.invplus.modules.pagamentos.dto.request.CondicaoPagamentoParcelaRequest;
import br.com.invplus.modules.pagamentos.entity.CondicaoPagamento;
import br.com.invplus.modules.pagamentos.repository.CondicaoPagamentoRepository;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Valida regras funcionais de CondicaoPagamento antes da persistencia.
 */
@Component
@RequiredArgsConstructor
public class CondicaoPagamentoValidator {

    private final CondicaoPagamentoRepository repository;

    public void validarCriacao(CondicaoPagamentoRequest request) {
        validarCodigoUnico(null, request.codigo());
        validarNumerosEParcelas(request);
    }

    public void validarAtualizacao(UUID id, CondicaoPagamentoRequest request) {
        validarCodigoUnico(id, request.codigo());
        validarNumerosEParcelas(request);
    }

    private void validarCodigoUnico(UUID idAtual, String codigo) {
        String normalizado = codigo == null ? null : codigo.trim();
        repository.findByCodigoIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(normalizado)
                .map(CondicaoPagamento::getId)
                .filter(idEncontrado -> !idEncontrado.equals(idAtual))
                .ifPresent(id -> {
                    throw new ConflictException("Ja existe condicao de pagamento ativa com este codigo");
                });
    }

    private void validarNumerosEParcelas(CondicaoPagamentoRequest request) {
        if (request.regraVencimento() == null) {
            throw new InvalidOperationException("Regra de vencimento e obrigatoria");
        }
        validarPercentual("Percentual de juros", request.jurosPercentual());
        validarPercentual("Percentual de multa", request.multaPercentual());
        validarPercentual("Percentual de desconto", request.descontoPercentual());
        validarParcelas(request.parcelas());
    }

    private void validarPercentual(String nome, BigDecimal valor) {
        if (valor != null && valor.signum() < 0) {
            throw new InvalidOperationException(nome + " nao pode ser negativo");
        }
    }

    private void validarParcelas(List<CondicaoPagamentoParcelaRequest> parcelas) {
        if (parcelas == null || parcelas.isEmpty()) {
            throw new InvalidOperationException("Condicao de pagamento deve possuir pelo menos uma parcela");
        }

        Set<Integer> numeros = new HashSet<>();
        BigDecimal soma = BigDecimal.ZERO;
        for (CondicaoPagamentoParcelaRequest parcela : parcelas) {
            if (parcela.numeroParcela() == null || parcela.numeroParcela() < 1) {
                throw new InvalidOperationException("Numero da parcela deve ser maior ou igual a 1");
            }
            if (!numeros.add(parcela.numeroParcela())) {
                throw new InvalidOperationException("Numero da parcela nao pode repetir");
            }
            if (parcela.dias() == null || parcela.dias() < 0) {
                throw new InvalidOperationException("Dias da parcela nao pode ser negativo");
            }
            if (parcela.percentual() == null || parcela.percentual().compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidOperationException("Percentual da parcela deve ser maior que zero");
            }
            if (parcela.percentual().compareTo(new BigDecimal("100")) > 0) {
                throw new InvalidOperationException("Percentual da parcela deve ser menor ou igual a 100");
            }
            soma = soma.add(parcela.percentual());
        }

        if (soma.compareTo(new BigDecimal("99.99")) < 0 || soma.compareTo(new BigDecimal("100.01")) > 0) {
            throw new InvalidOperationException("Soma dos percentuais das parcelas deve fechar 100%");
        }
    }
}
