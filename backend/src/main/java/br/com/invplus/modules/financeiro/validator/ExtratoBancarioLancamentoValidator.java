package br.com.invplus.modules.financeiro.validator;

import br.com.invplus.modules.financeiro.dto.request.ExtratoBancarioLancamentoRequest;
import br.com.invplus.modules.financeiro.entity.ExtratoBancario;
import br.com.invplus.modules.financeiro.entity.ExtratoBancarioLancamento;
import br.com.invplus.modules.financeiro.enums.StatusExtratoBancario;
import br.com.invplus.modules.financeiro.repository.ExtratoBancarioLancamentoRepository;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Centraliza validacoes de vinculo, duplicidade e conciliacao de lancamentos.
 */
@Component
@RequiredArgsConstructor
public class ExtratoBancarioLancamentoValidator {

    private final ExtratoBancarioLancamentoRepository repository;

    public void validarCriacao(ExtratoBancarioLancamentoRequest request, ExtratoBancario extrato) {
        validarBasico(request, extrato);
        validarIdentificadorUnico(null, request.extratoBancarioId(), request.identificadorBancario());
    }

    public void validarAtualizacao(UUID id, ExtratoBancarioLancamentoRequest request, ExtratoBancario extrato,
            ExtratoBancarioLancamento entity) {
        entity.validarNaoConciliado("editado");
        validarBasico(request, extrato);
        validarIdentificadorUnico(id, request.extratoBancarioId(), request.identificadorBancario());
    }

    public void validarExclusao(ExtratoBancarioLancamento entity) {
        entity.validarNaoConciliado("excluido");
    }

    private void validarBasico(ExtratoBancarioLancamentoRequest request, ExtratoBancario extrato) {
        if (StatusExtratoBancario.CANCELADO.equals(extrato.getStatus())) {
            throw new InvalidOperationException("Extrato bancario cancelado nao pode receber lancamentos");
        }
        if (request.valor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Valor do lancamento deve ser maior que zero");
        }
    }

    private void validarIdentificadorUnico(UUID idAtual, UUID extratoId, String identificador) {
        if (identificador == null || identificador.isBlank()) {
            return;
        }
        repository.findByExtratoBancarioIdAndIdentificadorBancarioIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(
                        extratoId, identificador.trim())
                .map(ExtratoBancarioLancamento::getId)
                .filter(id -> !id.equals(idAtual))
                .ifPresent(id -> {
                    throw new ConflictException("Ja existe lancamento ativo com este identificador no extrato");
                });
    }
}
