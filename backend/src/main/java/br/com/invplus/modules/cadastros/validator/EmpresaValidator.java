package br.com.invplus.modules.cadastros.validator;

import br.com.invplus.modules.cadastros.dto.request.EmpresaRequest;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ConflictException;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Valida regras funcionais de Empresa antes da persistencia.
 */
@Component
@RequiredArgsConstructor
public class EmpresaValidator {

    private static final Set<Integer> CRT_VALIDOS = Set.of(1, 2, 3, 4);
    private static final Pattern EMAIL_SIMPLES = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final EmpresaRepository empresaRepository;

    /**
     * Valida criacao com documento, CRT, email opcional e CNPJ unico.
     */
    public void validarCriacao(EmpresaRequest request) {
        validarCampos(request);
        validarCnpjUnico(null, request.cnpj());
    }

    /**
     * Valida atualizacao ignorando a propria empresa ao checar duplicidade.
     */
    public void validarAtualizacao(UUID id, EmpresaRequest request) {
        validarCampos(request);
        validarCnpjUnico(id, request.cnpj());
    }

    private void validarCampos(EmpresaRequest request) {
        String cnpj = normalizarCnpj(request.cnpj());
        if (cnpj == null || !cnpj.matches("\\d{14}")) {
            throw new BusinessException("CNPJ invalido");
        }
        if (!CRT_VALIDOS.contains(request.crt())) {
            throw new BusinessException("CRT invalido");
        }
        if (request.email() != null && !request.email().isBlank()
                && !EMAIL_SIMPLES.matcher(request.email().trim()).matches()) {
            throw new BusinessException("Email invalido");
        }
    }

    private void validarCnpjUnico(UUID idAtual, String cnpj) {
        empresaRepository.findByCnpjAndAtivoTrueAndDeletedAtIsNull(normalizarCnpj(cnpj))
                .map(Empresa::getId)
                .filter(idEncontrado -> !idEncontrado.equals(idAtual))
                .ifPresent(id -> {
                    throw new ConflictException("Ja existe empresa ativa com este CNPJ");
                });
    }

    private String normalizarCnpj(String cnpj) {
        return cnpj == null ? null : cnpj.replaceAll("\\D", "");
    }
}
