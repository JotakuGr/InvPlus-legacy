package br.com.invplus.modules.pagamentos.validator;

import br.com.invplus.modules.pagamentos.dto.request.FormaPagamentoRequest;
import br.com.invplus.modules.pagamentos.entity.FormaPagamento;
import br.com.invplus.modules.pagamentos.enums.TipoFormaPagamento;
import br.com.invplus.modules.pagamentos.repository.FormaPagamentoRepository;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import java.util.EnumSet;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Valida regras funcionais de FormaPagamento antes da persistencia.
 */
@Component
@RequiredArgsConstructor
public class FormaPagamentoValidator {

    private static final EnumSet<TipoFormaPagamento> TIPOS_PARCELAVEIS = EnumSet.of(
            TipoFormaPagamento.CARTAO_CREDITO,
            TipoFormaPagamento.BOLETO,
            TipoFormaPagamento.CREDIARIO
    );

    private final FormaPagamentoRepository repository;

    public void validarCriacao(FormaPagamentoRequest request) {
        validarCodigoUnico(null, request.codigo());
        validarRegrasOperacionais(request);
    }

    public void validarAtualizacao(UUID id, FormaPagamentoRequest request) {
        validarCodigoUnico(id, request.codigo());
        validarRegrasOperacionais(request);
    }

    private void validarCodigoUnico(UUID idAtual, String codigo) {
        String normalizado = codigo == null ? null : codigo.trim();
        repository.findByCodigoIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(normalizado)
                .map(FormaPagamento::getId)
                .filter(idEncontrado -> !idEncontrado.equals(idAtual))
                .ifPresent(id -> {
                    throw new ConflictException("Ja existe forma de pagamento ativa com este codigo");
                });
    }

    private void validarRegrasOperacionais(FormaPagamentoRequest request) {
        if (Boolean.TRUE.equals(request.permiteTroco()) && request.tipo() != TipoFormaPagamento.DINHEIRO) {
            throw new InvalidOperationException("Troco so e permitido para forma de pagamento DINHEIRO");
        }
        if (Boolean.TRUE.equals(request.permiteParcelamento()) && !TIPOS_PARCELAVEIS.contains(request.tipo())) {
            throw new InvalidOperationException("Parcelamento so e permitido para CARTAO_CREDITO, BOLETO ou CREDIARIO");
        }
    }
}
