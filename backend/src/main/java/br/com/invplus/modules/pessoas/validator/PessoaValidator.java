package br.com.invplus.modules.pessoas.validator;

import br.com.invplus.common.enums.TipoPessoa;
import br.com.invplus.modules.pessoas.dto.request.PessoaRequest;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.pessoas.repository.PessoaRepository;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ConflictException;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Valida regras funcionais de Pessoa antes da persistencia.
 */
@Component
@RequiredArgsConstructor
public class PessoaValidator {

    private static final Pattern EMAIL_SIMPLES = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final PessoaRepository pessoaRepository;

    /**
     * Valida criacao com documento consistente e unico.
     */
    public void validarCriacao(PessoaRequest request) {
        validarCampos(request);
        validarCpfCnpjUnico(null, request.cpfCnpj());
    }

    /**
     * Valida atualizacao ignorando o proprio registro na checagem de duplicidade.
     */
    public void validarAtualizacao(UUID id, PessoaRequest request) {
        validarCampos(request);
        validarCpfCnpjUnico(id, request.cpfCnpj());
    }

    private void validarCampos(PessoaRequest request) {
        String documento = normalizarDocumento(request.cpfCnpj());
        if (documento == null || !documento.matches("\\d{11}|\\d{14}")) {
            throw new BusinessException("CPF/CNPJ invalido");
        }
        if (request.tipoPessoa() == TipoPessoa.FISICA && documento.length() != 11) {
            throw new BusinessException("CPF invalido para pessoa fisica");
        }
        if (request.tipoPessoa() == TipoPessoa.JURIDICA && documento.length() != 14) {
            throw new BusinessException("CNPJ invalido para pessoa juridica");
        }
        if (request.email() != null && !request.email().isBlank()
                && !EMAIL_SIMPLES.matcher(request.email().trim()).matches()) {
            throw new BusinessException("Email invalido");
        }
    }

    private void validarCpfCnpjUnico(UUID idAtual, String cpfCnpj) {
        pessoaRepository.findByCpfCnpjAndAtivoTrueAndDeletedAtIsNull(normalizarDocumento(cpfCnpj))
                .map(Pessoa::getId)
                .filter(idEncontrado -> !idEncontrado.equals(idAtual))
                .ifPresent(id -> {
                    throw new ConflictException("Ja existe pessoa ativa com este CPF/CNPJ");
                });
    }

    private String normalizarDocumento(String cpfCnpj) {
        return cpfCnpj == null ? null : cpfCnpj.replaceAll("\\D", "");
    }
}
