package br.com.invplus.modules.produtos.validator;

import br.com.invplus.modules.produtos.dto.request.ProdutoFiscalRequest;
import br.com.invplus.modules.produtos.entity.ProdutoFiscal;
import br.com.invplus.modules.produtos.repository.ProdutoFiscalRepository;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ConflictException;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Valida regras fiscais basicas do produto antes de persistir.
 */
@Component
public class ProdutoFiscalValidator {

    private final ProdutoFiscalRepository repository;

    public ProdutoFiscalValidator(ProdutoFiscalRepository repository) {
        this.repository = repository;
    }

    public void validarCriacao(ProdutoFiscalRequest request) {
        validarCampos(request);
        validarDuplicidade(request, null);
    }

    public void validarAtualizacao(ProdutoFiscal atual, ProdutoFiscalRequest request) {
        validarCampos(request);
        validarDuplicidade(request, atual.getId());
    }

    private void validarCampos(ProdutoFiscalRequest request) {
        if (request.produtoId() == null) {
            throw new BusinessException("Produto e obrigatorio");
        }
        String ncm = somenteNumeros(request.ncm());
        if (ncm == null) {
            throw new BusinessException("NCM e obrigatorio");
        }
        if (ncm.length() != 8) {
            throw new BusinessException("NCM deve conter 8 digitos");
        }
        String cest = somenteNumeros(request.cest());
        if (cest != null && cest.length() != 7) {
            throw new BusinessException("CEST deve conter 7 digitos");
        }
        validarNaoNegativo(request.aliquotaIcms(), "Aliquota ICMS nao pode ser negativa");
        validarNaoNegativo(request.aliquotaIpi(), "Aliquota IPI nao pode ser negativa");
        validarNaoNegativo(request.aliquotaPis(), "Aliquota PIS nao pode ser negativa");
        validarNaoNegativo(request.aliquotaCofins(), "Aliquota COFINS nao pode ser negativa");
    }

    private void validarDuplicidade(ProdutoFiscalRequest request, UUID idAtual) {
        repository.findByProdutoIdAndAtivoTrueAndDeletedAtIsNull(request.produtoId())
                .map(ProdutoFiscal::getId)
                .filter(idEncontrado -> idAtual == null || !idEncontrado.equals(idAtual))
                .ifPresent(idEncontrado -> {
                    throw new ConflictException("Produto ja possui dados fiscais ativos");
                });
    }

    private void validarNaoNegativo(BigDecimal valor, String mensagem) {
        if (valor != null && valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(mensagem);
        }
    }

    private String somenteNumeros(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.replaceAll("\\D", "");
    }
}
