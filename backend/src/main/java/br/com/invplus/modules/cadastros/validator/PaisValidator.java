package br.com.invplus.modules.cadastros.validator;

import br.com.invplus.modules.cadastros.dto.request.PaisRequest;
import br.com.invplus.modules.cadastros.repository.PaisRepository;
import br.com.invplus.shared.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Validacoes de regra de negocio para Pais.
 */
@Component
@RequiredArgsConstructor
public class PaisValidator {

    private final PaisRepository paisRepository;

    /**
     * Valida duplicidade funcional antes da criacao.
     */
    public void validarCriacao(PaisRequest request) {
        String iso2 = request.codigoIso2().trim().toUpperCase();
        if (paisRepository.existsByCodigoIso2AndAtivoTrueAndDeletedAtIsNull(iso2)) {
            throw new BusinessException("Ja existe pais ativo com este codigo ISO2");
        }
    }
}
