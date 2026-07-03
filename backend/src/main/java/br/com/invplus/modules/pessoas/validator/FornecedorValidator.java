package br.com.invplus.modules.pessoas.validator;

import br.com.invplus.modules.pessoas.dto.request.FornecedorRequest;
import br.com.invplus.modules.pessoas.entity.Fornecedor;
import br.com.invplus.modules.pessoas.repository.FornecedorRepository;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ConflictException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Valida regras funcionais de Fornecedor antes da persistencia.
 */
@Component
@RequiredArgsConstructor
public class FornecedorValidator {

    private final FornecedorRepository fornecedorRepository;

    /**
     * Valida criacao garantindo pessoa unica no papel de fornecedor ativo.
     */
    public void validarCriacao(FornecedorRequest request) {
        validarCampos(request);
        validarPessoaSemFornecedorAtivo(null, request.pessoaId());
    }

    /**
     * Valida atualizacao permitindo manter o proprio fornecedor vinculado a pessoa atual.
     */
    public void validarAtualizacao(UUID id, FornecedorRequest request) {
        validarCampos(request);
        validarPessoaSemFornecedorAtivo(id, request.pessoaId());
    }

    private void validarCampos(FornecedorRequest request) {
        if (request.pessoaId() == null) {
            throw new BusinessException("Pessoa do fornecedor e obrigatoria");
        }
        if (request.prazoMedioEntrega() != null && request.prazoMedioEntrega() < 0) {
            throw new BusinessException("Prazo medio de entrega nao pode ser negativo");
        }
    }

    private void validarPessoaSemFornecedorAtivo(UUID idAtual, UUID pessoaId) {
        fornecedorRepository.findByPessoaIdAndAtivoTrueAndDeletedAtIsNull(pessoaId)
                .map(Fornecedor::getId)
                .filter(idEncontrado -> !idEncontrado.equals(idAtual))
                .ifPresent(id -> {
                    throw new ConflictException("Pessoa ja possui fornecedor ativo");
                });
    }
}
