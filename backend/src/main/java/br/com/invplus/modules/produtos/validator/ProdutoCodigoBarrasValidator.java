package br.com.invplus.modules.produtos.validator;

import br.com.invplus.modules.produtos.dto.request.ProdutoCodigoBarrasRequest;
import br.com.invplus.modules.produtos.entity.ProdutoCodigoBarras;
import br.com.invplus.modules.produtos.repository.ProdutoCodigoBarrasRepository;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ConflictException;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Valida regras dos codigos de barras e codigos alternativos de Produto.
 */
@Component
public class ProdutoCodigoBarrasValidator {

    private final ProdutoCodigoBarrasRepository repository;

    public ProdutoCodigoBarrasValidator(ProdutoCodigoBarrasRepository repository) {
        this.repository = repository;
    }

    /**
     * Valida criacao, unicidade do codigo e codigo principal.
     */
    public void validarCriacao(ProdutoCodigoBarrasRequest request) {
        validarCampos(request);
        if (repository.existsByCodigoIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(request.codigo().trim())) {
            throw new ConflictException("Ja existe codigo de barras ativo com este codigo");
        }
        validarPrincipal(request.produtoId(), request.principal(), null);
    }

    /**
     * Valida atualizacao ignorando o proprio registro.
     */
    public void validarAtualizacao(ProdutoCodigoBarras atual, ProdutoCodigoBarrasRequest request) {
        validarCampos(request);
        repository.findByCodigoIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(request.codigo().trim())
                .filter(codigo -> !Objects.equals(codigo.getId(), atual.getId()))
                .ifPresent(codigo -> {
                    throw new ConflictException("Ja existe codigo de barras ativo com este codigo");
                });
        validarPrincipal(request.produtoId(), request.principal(), atual.getId());
    }

    private void validarCampos(ProdutoCodigoBarrasRequest request) {
        if (request.produtoId() == null) {
            throw new BusinessException("Produto e obrigatorio");
        }
        if (request.tipoCodigo() == null) {
            throw new BusinessException("Tipo do codigo e obrigatorio");
        }
        if (request.codigo() == null || request.codigo().isBlank()) {
            throw new BusinessException("Codigo de barras e obrigatorio");
        }
    }

    private void validarPrincipal(UUID produtoId, Boolean principal, UUID idAtual) {
        if (!Boolean.TRUE.equals(principal)) {
            return;
        }
        repository.findByProdutoIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(produtoId)
                .filter(codigo -> !Objects.equals(codigo.getId(), idAtual))
                .ifPresent(codigo -> {
                    throw new ConflictException("Produto ja possui codigo de barras principal ativo");
                });
    }
}
