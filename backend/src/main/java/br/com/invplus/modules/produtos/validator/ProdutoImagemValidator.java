package br.com.invplus.modules.produtos.validator;

import br.com.invplus.modules.produtos.dto.request.ProdutoImagemRequest;
import br.com.invplus.modules.produtos.entity.ProdutoImagem;
import br.com.invplus.modules.produtos.repository.ProdutoImagemRepository;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ConflictException;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Valida regras do vinculo entre Produto e Arquivo de imagem.
 */
@Component
public class ProdutoImagemValidator {

    private final ProdutoImagemRepository repository;

    public ProdutoImagemValidator(ProdutoImagemRepository repository) {
        this.repository = repository;
    }

    /**
     * Valida criacao, duplicidade e imagem principal.
     */
    public void validarCriacao(ProdutoImagemRequest request) {
        validarCampos(request);
        if (repository.existsByProdutoIdAndArquivoIdAndAtivoTrueAndDeletedAtIsNull(
                request.produtoId(),
                request.arquivoId()
        )) {
            throw new ConflictException("Produto ja possui vinculo ativo com este arquivo");
        }
        validarPrincipal(request.produtoId(), request.principal(), null);
    }

    /**
     * Valida atualizacao ignorando o proprio registro.
     */
    public void validarAtualizacao(ProdutoImagem atual, ProdutoImagemRequest request) {
        validarCampos(request);
        repository.findByProdutoIdAndArquivoIdAndAtivoTrueAndDeletedAtIsNull(request.produtoId(), request.arquivoId())
                .map(ProdutoImagem::getId)
                .filter(idEncontrado -> !idEncontrado.equals(atual.getId()))
                .ifPresent(idEncontrado -> {
                    throw new ConflictException("Produto ja possui vinculo ativo com este arquivo");
                });
        validarPrincipal(request.produtoId(), request.principal(), atual.getId());
    }

    private void validarCampos(ProdutoImagemRequest request) {
        if (request.produtoId() == null) {
            throw new BusinessException("Produto e obrigatorio");
        }
        if (request.arquivoId() == null) {
            throw new BusinessException("Arquivo e obrigatorio");
        }
        if (request.ordem() != null && request.ordem() < 0) {
            throw new BusinessException("Ordem nao pode ser negativa");
        }
    }

    private void validarPrincipal(UUID produtoId, Boolean principal, UUID idAtual) {
        if (!Boolean.TRUE.equals(principal)) {
            return;
        }
        repository.findByProdutoIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(produtoId)
                .map(ProdutoImagem::getId)
                .filter(idEncontrado -> idAtual == null || !idEncontrado.equals(idAtual))
                .ifPresent(idEncontrado -> {
                    throw new ConflictException("Produto ja possui imagem principal ativa");
                });
    }
}
