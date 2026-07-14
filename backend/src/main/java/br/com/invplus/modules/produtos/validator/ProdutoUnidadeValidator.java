package br.com.invplus.modules.produtos.validator;

import br.com.invplus.modules.produtos.dto.request.ProdutoUnidadeRequest;
import br.com.invplus.modules.produtos.entity.ProdutoUnidade;
import br.com.invplus.modules.produtos.repository.ProdutoUnidadeRepository;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ConflictException;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Valida regras de unidade alternativa do produto.
 */
@Component
public class ProdutoUnidadeValidator {

    private final ProdutoUnidadeRepository repository;

    public ProdutoUnidadeValidator(ProdutoUnidadeRepository repository) {
        this.repository = repository;
    }

    public void validarCriacao(ProdutoUnidadeRequest request) {
        validarCampos(request);
        validarDuplicidade(request, null);
        validarUnidadePadrao(request, null);
    }

    public void validarAtualizacao(ProdutoUnidade atual, ProdutoUnidadeRequest request) {
        validarCampos(request);
        validarDuplicidade(request, atual.getId());
        validarUnidadePadrao(request, atual.getId());
    }

    private void validarCampos(ProdutoUnidadeRequest request) {
        if (request.produtoId() == null) {
            throw new BusinessException("Produto e obrigatorio");
        }
        if (request.unidadeMedidaId() == null) {
            throw new BusinessException("Unidade de medida e obrigatoria");
        }
        if (request.fatorConversao() == null) {
            throw new BusinessException("Fator de conversao e obrigatorio");
        }
        if (request.fatorConversao().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Fator de conversao deve ser maior que zero");
        }
        if (Boolean.TRUE.equals(request.unidadePadrao()) && request.fatorConversao().compareTo(BigDecimal.ONE) != 0) {
            throw new BusinessException("Unidade padrao deve ter fator de conversao igual a 1");
        }
    }

    private void validarDuplicidade(ProdutoUnidadeRequest request, UUID idAtual) {
        repository.findByProdutoIdAndUnidadeMedidaIdAndAtivoTrueAndDeletedAtIsNull(
                        request.produtoId(), request.unidadeMedidaId())
                .filter(unidade -> !Objects.equals(unidade.getId(), idAtual))
                .ifPresent(unidade -> {
                    throw new ConflictException("Ja existe unidade ativa para este produto");
                });
    }

    private void validarUnidadePadrao(ProdutoUnidadeRequest request, UUID idAtual) {
        if (!Boolean.TRUE.equals(request.unidadePadrao())) {
            return;
        }
        repository.findByProdutoIdAndUnidadePadraoTrueAndAtivoTrueAndDeletedAtIsNull(request.produtoId())
                .filter(unidade -> !Objects.equals(unidade.getId(), idAtual))
                .ifPresent(unidade -> {
                    throw new ConflictException("Produto ja possui unidade padrao ativa");
                });
    }
}
