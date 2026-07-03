package br.com.invplus.modules.pessoas.validator;

import br.com.invplus.modules.pessoas.dto.request.ContatoPessoaRequest;
import br.com.invplus.shared.domain.exception.BusinessException;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Valida regras funcionais de ContatoPessoa.
 */
@Component
public class ContatoPessoaValidator {

    private static final Pattern EMAIL_SIMPLES = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /**
     * Valida os campos principais do contato antes de persistir.
     */
    public void validar(ContatoPessoaRequest request) {
        if (request.pessoaId() == null) {
            throw new BusinessException("Pessoa do contato e obrigatoria");
        }
        if (request.nome() == null || request.nome().isBlank()) {
            throw new BusinessException("Nome do contato e obrigatorio");
        }
        if (request.email() != null && !request.email().isBlank()
                && !EMAIL_SIMPLES.matcher(request.email().trim()).matches()) {
            throw new BusinessException("Email do contato invalido");
        }
    }
}
