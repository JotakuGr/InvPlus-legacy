package br.com.invplus.modules.cadastros.validator;

import br.com.invplus.modules.cadastros.dto.request.FilialRequest;
import br.com.invplus.modules.cadastros.repository.FilialRepository;
import br.com.invplus.shared.domain.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Validacoes de regra de negocio para Filial.
 */
@Component
@RequiredArgsConstructor
public class FilialValidator {

    private final FilialRepository filialRepository;

    /**
     * Valida duplicidade de codigo dentro da empresa.
     */
    public void validarCriacao(FilialRequest request) {
        String codigo = request.codigo().trim().toUpperCase();
        if (filialRepository.existsByEmpresaIdAndCodigoAndAtivoTrueAndDeletedAtIsNull(request.empresaId(), codigo)) {
            throw new ConflictException("Ja existe filial ativa com este codigo para a empresa informada");
        }
    }
}
