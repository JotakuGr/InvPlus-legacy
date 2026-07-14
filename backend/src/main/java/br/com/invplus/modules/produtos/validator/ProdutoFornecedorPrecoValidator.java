package br.com.invplus.modules.produtos.validator;

import br.com.invplus.modules.produtos.dto.request.ProdutoFornecedorPrecoRequest;
import br.com.invplus.modules.produtos.entity.ProdutoFornecedorPreco;
import br.com.invplus.modules.produtos.repository.ProdutoFornecedorPrecoRepository;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ConflictException;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Valida regras do historico de preco de ProdutoFornecedor.
 */
@Component
public class ProdutoFornecedorPrecoValidator {

    private final ProdutoFornecedorPrecoRepository repository;

    public ProdutoFornecedorPrecoValidator(ProdutoFornecedorPrecoRepository repository) {
        this.repository = repository;
    }

    public void validarCriacao(ProdutoFornecedorPrecoRequest request) {
        validarCampos(request);
        validarDuplicidade(request, null);
    }

    public void validarAtualizacao(ProdutoFornecedorPreco atual, ProdutoFornecedorPrecoRequest request) {
        validarCampos(request);
        validarDuplicidade(request, atual.getId());
    }

    private void validarCampos(ProdutoFornecedorPrecoRequest request) {
        if (request.produtoFornecedorId() == null) {
            throw new BusinessException("ProdutoFornecedor e obrigatorio");
        }
        if (request.preco() == null) {
            throw new BusinessException("Preco e obrigatorio");
        }
        if (request.preco().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Preco nao pode ser negativo");
        }
        if (request.vigenciaInicio() == null) {
            throw new BusinessException("Vigencia inicial e obrigatoria");
        }
        if (request.vigenciaFim() != null && request.vigenciaFim().isBefore(request.vigenciaInicio())) {
            throw new BusinessException("Vigencia final nao pode ser anterior a inicial");
        }
    }

    private void validarDuplicidade(ProdutoFornecedorPrecoRequest request, UUID idAtual) {
        repository.findByProdutoFornecedorIdAndVigenciaInicioAndAtivoTrueAndDeletedAtIsNull(
                        request.produtoFornecedorId(),
                        request.vigenciaInicio()
                )
                .filter(preco -> !Objects.equals(preco.getId(), idAtual))
                .ifPresent(preco -> {
                    throw new ConflictException("Ja existe preco ativo para este vinculo nesta vigencia inicial");
                });
    }
}
