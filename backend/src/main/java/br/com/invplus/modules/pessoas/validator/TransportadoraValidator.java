package br.com.invplus.modules.pessoas.validator;

import br.com.invplus.modules.pessoas.dto.request.TransportadoraRequest;
import br.com.invplus.modules.pessoas.repository.TransportadoraRepository;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ConflictException;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Valida regras funcionais de Transportadora antes da persistencia.
 */
@Component
@RequiredArgsConstructor
public class TransportadoraValidator {

    private final TransportadoraRepository transportadoraRepository;

    /**
     * Valida criacao garantindo pessoa unica no papel de transportadora ativa.
     */
    public void validarCriacao(TransportadoraRequest request) {
        validarCampos(request);
        validarPessoaSemTransportadoraAtiva(null, request.pessoaId());
    }

    /**
     * Valida atualizacao permitindo manter a propria transportadora vinculada a pessoa atual.
     */
    public void validarAtualizacao(UUID id, TransportadoraRequest request) {
        validarCampos(request);
        validarPessoaSemTransportadoraAtiva(id, request.pessoaId());
    }

    private void validarCampos(TransportadoraRequest request) {
        if (request.pessoaId() == null) {
            throw new BusinessException("Pessoa da transportadora e obrigatoria");
        }
        if (request.capacidadeCargaKg() != null && request.capacidadeCargaKg().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Capacidade de carga nao pode ser negativa");
        }
    }

    private void validarPessoaSemTransportadoraAtiva(UUID idAtual, UUID pessoaId) {
        transportadoraRepository.findByPessoaIdAndAtivoTrueAndDeletedAtIsNull(pessoaId)
                .filter(transportadora -> !Objects.equals(transportadora.getId(), idAtual))
                .ifPresent(transportadora -> {
                    throw new ConflictException("Pessoa ja possui transportadora ativa");
                });
    }
}
