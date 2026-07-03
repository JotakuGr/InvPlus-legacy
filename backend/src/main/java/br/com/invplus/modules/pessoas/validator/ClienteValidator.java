package br.com.invplus.modules.pessoas.validator;

import br.com.invplus.modules.pessoas.dto.request.ClienteRequest;
import br.com.invplus.modules.pessoas.entity.Cliente;
import br.com.invplus.modules.pessoas.repository.ClienteRepository;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ConflictException;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Valida regras funcionais de Cliente antes da persistencia.
 */
@Component
@RequiredArgsConstructor
public class ClienteValidator {

    private final ClienteRepository clienteRepository;

    /**
     * Valida criacao garantindo pessoa unica no papel de cliente ativo.
     */
    public void validarCriacao(ClienteRequest request) {
        validarCampos(request);
        validarPessoaSemClienteAtivo(null, request.pessoaId());
    }

    /**
     * Valida atualizacao permitindo manter o proprio cliente vinculado a pessoa atual.
     */
    public void validarAtualizacao(UUID id, ClienteRequest request) {
        validarCampos(request);
        validarPessoaSemClienteAtivo(id, request.pessoaId());
    }

    private void validarCampos(ClienteRequest request) {
        if (request.pessoaId() == null) {
            throw new BusinessException("Pessoa do cliente e obrigatoria");
        }
        if (request.limiteCredito() != null && request.limiteCredito().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Limite de credito nao pode ser negativo");
        }
    }

    private void validarPessoaSemClienteAtivo(UUID idAtual, UUID pessoaId) {
        clienteRepository.findByPessoaIdAndAtivoTrueAndDeletedAtIsNull(pessoaId)
                .map(Cliente::getId)
                .filter(idEncontrado -> !idEncontrado.equals(idAtual))
                .ifPresent(id -> {
                    throw new ConflictException("Pessoa ja possui cliente ativo");
                });
    }
}
