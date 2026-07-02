package br.com.invplus.modules.cadastros.validator;

import br.com.invplus.modules.cadastros.dto.request.EstadoRequest;
import br.com.invplus.modules.cadastros.repository.EstadoRepository;
import br.com.invplus.shared.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Validacoes de regra de negocio para Estado.
 */
@Component
@RequiredArgsConstructor
public class EstadoValidator {

    private final EstadoRepository estadoRepository;

    /**
     * Valida duplicidade de UF dentro do pais.
     */
    public void validarCriacao(EstadoRequest request) {
        String uf = request.uf().trim().toUpperCase();
        if (estadoRepository.existsByPaisIdAndUfAndAtivoTrueAndDeletedAtIsNull(request.paisId(), uf)) {
            throw new BusinessException("Ja existe estado ativo com esta UF para o pais informado");
        }
    }
}
