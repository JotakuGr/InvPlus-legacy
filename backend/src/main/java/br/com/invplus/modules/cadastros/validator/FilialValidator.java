package br.com.invplus.modules.cadastros.validator;

import br.com.invplus.modules.cadastros.dto.request.FilialRequest;
import br.com.invplus.modules.cadastros.repository.FilialRepository;
import br.com.invplus.shared.domain.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.UUID;

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
    public void validarCriacao(UUID empresaId, FilialRequest request) {
        String codigo = request.codigo().trim().toUpperCase();
        if (filialRepository.existsByEmpresaIdAndCodigoAndAtivoTrueAndDeletedAtIsNull(empresaId, codigo)) {
            throw new ConflictException("Ja existe filial ativa com este codigo para a empresa informada");
        }
        validarMatriz(empresaId, null, request.matriz());
    }

    public void validarAtualizacao(UUID empresaId, UUID filialId, FilialRequest request) {
        String codigo = request.codigo().trim().toUpperCase();
        if (filialRepository.existsByEmpresaIdAndCodigoAndAtivoTrueAndDeletedAtIsNullAndIdNot(
                empresaId, codigo, filialId)) {
            throw new ConflictException("Ja existe filial ativa com este codigo para a empresa informada");
        }
        validarMatriz(empresaId, filialId, request.matriz());
    }

    private void validarMatriz(UUID empresaId, UUID filialId, Boolean matriz) {
        if (!Boolean.TRUE.equals(matriz)) {
            return;
        }
        boolean existe = filialId == null
                ? filialRepository.existsByEmpresaIdAndMatrizTrueAndAtivoTrueAndDeletedAtIsNull(empresaId)
                : filialRepository.existsByEmpresaIdAndMatrizTrueAndAtivoTrueAndDeletedAtIsNullAndIdNot(
                        empresaId, filialId);
        if (existe) {
            throw new ConflictException("Ja existe filial matriz ativa para a empresa");
        }
    }
}
