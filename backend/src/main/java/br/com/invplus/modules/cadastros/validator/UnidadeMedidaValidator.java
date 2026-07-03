package br.com.invplus.modules.cadastros.validator;

import br.com.invplus.modules.cadastros.dto.request.UnidadeMedidaRequest;
import br.com.invplus.modules.cadastros.entity.UnidadeMedida;
import br.com.invplus.modules.cadastros.repository.UnidadeMedidaRepository;
import br.com.invplus.shared.domain.exception.ConflictException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Valida regras funcionais de unidades de medida antes da persistencia.
 */
@Component
@RequiredArgsConstructor
public class UnidadeMedidaValidator {

    private final UnidadeMedidaRepository unidadeMedidaRepository;

    /**
     * Garante que uma nova unidade nao reutilize sigla ativa.
     */
    public void validarCriacao(UnidadeMedidaRequest request) {
        validarSiglaUnica(null, request.sigla());
    }

    /**
     * Garante que a atualizacao nao gere duplicidade de sigla ativa.
     */
    public void validarAtualizacao(UUID id, UnidadeMedidaRequest request) {
        validarSiglaUnica(id, request.sigla());
    }

    private void validarSiglaUnica(UUID idAtual, String sigla) {
        String siglaNormalizada = sigla == null ? null : sigla.trim().toUpperCase();
        unidadeMedidaRepository.findBySiglaAndAtivoTrueAndDeletedAtIsNull(siglaNormalizada)
                .map(UnidadeMedida::getId)
                .filter(idEncontrado -> !idEncontrado.equals(idAtual))
                .ifPresent(id -> {
                    throw new ConflictException("Ja existe unidade de medida ativa com esta sigla");
                });
    }
}
