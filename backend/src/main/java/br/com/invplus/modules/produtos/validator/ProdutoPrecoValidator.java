package br.com.invplus.modules.produtos.validator;

import br.com.invplus.modules.produtos.dto.request.ProdutoPrecoRequest;
import br.com.invplus.modules.produtos.entity.ProdutoPreco;
import br.com.invplus.modules.produtos.repository.ProdutoPrecoRepository;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ConflictException;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Valida regras comerciais do historico de precos de Produto.
 */
@Component
public class ProdutoPrecoValidator {

    private final ProdutoPrecoRepository repository;

    public ProdutoPrecoValidator(ProdutoPrecoRepository repository) {
        this.repository = repository;
    }

    public void validarCriacao(ProdutoPrecoRequest request) {
        validarCampos(request);
        validarDuplicidade(request, null);
        validarPrincipal(request, null);
    }

    public void validarAtualizacao(ProdutoPreco atual, ProdutoPrecoRequest request) {
        validarCampos(request);
        validarDuplicidade(request, atual.getId());
        validarPrincipal(request, atual.getId());
    }

    private void validarCampos(ProdutoPrecoRequest request) {
        if (request.produtoId() == null) {
            throw new BusinessException("Produto e obrigatorio");
        }
        if (request.tipoPreco() == null) {
            throw new BusinessException("Tipo de preco e obrigatorio");
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

    private void validarDuplicidade(ProdutoPrecoRequest request, UUID idAtual) {
        Optional<ProdutoPreco> existente = request.empresaId() == null
                ? repository.findByProdutoIdAndTipoPrecoAndEmpresaIsNullAndVigenciaInicioAndAtivoTrueAndDeletedAtIsNull(
                        request.produtoId(), request.tipoPreco(), request.vigenciaInicio())
                : repository.findByProdutoIdAndTipoPrecoAndEmpresaIdAndVigenciaInicioAndAtivoTrueAndDeletedAtIsNull(
                        request.produtoId(), request.tipoPreco(), request.empresaId(), request.vigenciaInicio());

        existente.map(ProdutoPreco::getId)
                .filter(idEncontrado -> idAtual == null || !idEncontrado.equals(idAtual))
                .ifPresent(idEncontrado -> {
                    throw new ConflictException("Ja existe preco ativo para este produto, tipo, empresa e vigencia inicial");
                });
    }

    private void validarPrincipal(ProdutoPrecoRequest request, UUID idAtual) {
        if (!Boolean.TRUE.equals(request.principal())) {
            return;
        }

        Optional<ProdutoPreco> existente = request.empresaId() == null
                ? repository.findByProdutoIdAndTipoPrecoAndEmpresaIsNullAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(
                        request.produtoId(), request.tipoPreco())
                : repository.findByProdutoIdAndTipoPrecoAndEmpresaIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(
                        request.produtoId(), request.tipoPreco(), request.empresaId());

        existente.map(ProdutoPreco::getId)
                .filter(idEncontrado -> idAtual == null || !idEncontrado.equals(idAtual))
                .ifPresent(idEncontrado -> {
                    throw new ConflictException("Produto ja possui preco principal ativo para este tipo e empresa");
                });
    }
}
