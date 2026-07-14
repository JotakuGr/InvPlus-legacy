package br.com.invplus.modules.produtos.validator;

import br.com.invplus.modules.produtos.dto.request.ProdutoFornecedorRequest;
import br.com.invplus.modules.produtos.entity.ProdutoFornecedor;
import br.com.invplus.modules.produtos.repository.ProdutoFornecedorRepository;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ConflictException;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Valida regras comerciais do vinculo entre Produto e Fornecedor.
 */
@Component
public class ProdutoFornecedorValidator {

    private final ProdutoFornecedorRepository repository;

    public ProdutoFornecedorValidator(ProdutoFornecedorRepository repository) {
        this.repository = repository;
    }

    /**
     * Valida criacao do vinculo, incluindo duplicidade e fornecedor principal.
     */
    public void validarCriacao(ProdutoFornecedorRequest request) {
        validarCampos(request);
        if (repository.existsByProdutoIdAndFornecedorIdAndAtivoTrueAndDeletedAtIsNull(
                request.produtoId(),
                request.fornecedorId()
        )) {
            throw new ConflictException("Produto ja possui vinculo ativo com este fornecedor");
        }
        validarPrincipal(request.produtoId(), request.principal(), null);
    }

    /**
     * Valida atualizacao ignorando o proprio registro encontrado.
     */
    public void validarAtualizacao(ProdutoFornecedor atual, ProdutoFornecedorRequest request) {
        validarCampos(request);
        repository.findByProdutoIdAndFornecedorIdAndAtivoTrueAndDeletedAtIsNull(
                        request.produtoId(),
                        request.fornecedorId()
                )
                .filter(vinculo -> !Objects.equals(vinculo.getId(), atual.getId()))
                .ifPresent(vinculo -> {
                    throw new ConflictException("Produto ja possui vinculo ativo com este fornecedor");
                });
        validarPrincipal(request.produtoId(), request.principal(), atual.getId());
    }

    private void validarCampos(ProdutoFornecedorRequest request) {
        if (request.produtoId() == null) {
            throw new BusinessException("Produto e obrigatorio");
        }
        if (request.fornecedorId() == null) {
            throw new BusinessException("Fornecedor e obrigatorio");
        }
        if (request.leadTimeDias() != null && request.leadTimeDias() < 0) {
            throw new BusinessException("Lead time nao pode ser negativo");
        }
        if (request.prioridade() != null && request.prioridade() < 0) {
            throw new BusinessException("Prioridade nao pode ser negativa");
        }
    }

    private void validarPrincipal(UUID produtoId, Boolean principal, UUID idAtual) {
        if (!Boolean.TRUE.equals(principal)) {
            return;
        }
        repository.findByProdutoIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(produtoId)
                .filter(vinculo -> !Objects.equals(vinculo.getId(), idAtual))
                .ifPresent(vinculo -> {
                    throw new ConflictException("Produto ja possui fornecedor principal ativo");
                });
    }
}
