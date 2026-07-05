package br.com.invplus.modules.produtos.validator;

import br.com.invplus.modules.produtos.dto.request.ProdutoRequest;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.repository.ProdutoRepository;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ConflictException;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/**
 * Valida regras funcionais de Produto.
 */
@Component
public class ProdutoValidator {

    private final ProdutoRepository produtoRepository;

    public ProdutoValidator(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    /**
     * Valida produto novo, incluindo duplicidades ativas.
     */
    public void validarCriacao(ProdutoRequest request) {
        validarCampos(request);
        validarDuplicidades(request);
    }

    /**
     * Valida atualizacao permitindo manter os identificadores do proprio produto.
     */
    public void validarAtualizacao(Produto produto, ProdutoRequest request) {
        validarCampos(request);
        if (!produto.getCodigo().equals(request.codigo().trim())
                && produtoRepository.existsByCodigoAndAtivoTrueAndDeletedAtIsNull(request.codigo().trim())) {
            throw new ConflictException("Ja existe produto ativo com este codigo");
        }
        if (mudouTexto(produto.getSku(), request.sku())
                && textoInformado(request.sku())
                && produtoRepository.existsBySkuAndAtivoTrueAndDeletedAtIsNull(request.sku().trim())) {
            throw new ConflictException("Ja existe produto ativo com este SKU");
        }
        if (mudouTexto(produto.getCodigoBarras(), request.codigoBarras())
                && textoInformado(request.codigoBarras())
                && produtoRepository.existsByCodigoBarrasAndAtivoTrueAndDeletedAtIsNull(request.codigoBarras().trim())) {
            throw new ConflictException("Ja existe produto ativo com este codigo de barras");
        }
    }

    private void validarCampos(ProdutoRequest request) {
        if (request.categoriaId() == null) {
            throw new BusinessException("Categoria do produto e obrigatoria");
        }
        if (request.unidadeMedidaId() == null) {
            throw new BusinessException("Unidade de medida do produto e obrigatoria");
        }
        if (!textoInformado(request.codigo())) {
            throw new BusinessException("Codigo do produto e obrigatorio");
        }
        if (!textoInformado(request.nome())) {
            throw new BusinessException("Nome do produto e obrigatorio");
        }
        validarNaoNegativo(request.precoCusto(), "Preco de custo nao pode ser negativo");
        validarNaoNegativo(request.precoVenda(), "Preco de venda nao pode ser negativo");
        validarNaoNegativo(request.pesoLiquido(), "Peso liquido nao pode ser negativo");
        validarNaoNegativo(request.pesoBruto(), "Peso bruto nao pode ser negativo");
        validarNaoNegativo(request.alturaCm(), "Altura nao pode ser negativa");
        validarNaoNegativo(request.larguraCm(), "Largura nao pode ser negativa");
        validarNaoNegativo(request.comprimentoCm(), "Comprimento nao pode ser negativo");
        validarNaoNegativo(request.estoqueMinimo(), "Estoque minimo nao pode ser negativo");
        validarCodigoBarras(request.codigoBarras());
    }

    private void validarDuplicidades(ProdutoRequest request) {
        if (produtoRepository.existsByCodigoAndAtivoTrueAndDeletedAtIsNull(request.codigo().trim())) {
            throw new ConflictException("Ja existe produto ativo com este codigo");
        }
        if (textoInformado(request.sku())
                && produtoRepository.existsBySkuAndAtivoTrueAndDeletedAtIsNull(request.sku().trim())) {
            throw new ConflictException("Ja existe produto ativo com este SKU");
        }
        if (textoInformado(request.codigoBarras())
                && produtoRepository.existsByCodigoBarrasAndAtivoTrueAndDeletedAtIsNull(request.codigoBarras().trim())) {
            throw new ConflictException("Ja existe produto ativo com este codigo de barras");
        }
    }

    private void validarNaoNegativo(BigDecimal valor, String mensagem) {
        if (valor != null && valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(mensagem);
        }
    }

    /**
     * Mantem validacao flexivel para GTIN: aceita EAN-8, UPC/GTIN-12, EAN-13 e GTIN-14.
     */
    private void validarCodigoBarras(String codigoBarras) {
        if (!textoInformado(codigoBarras)) {
            return;
        }
        String codigoNormalizado = codigoBarras.trim();
        int tamanho = codigoNormalizado.length();
        if (!codigoNormalizado.matches("\\d+") || (tamanho != 8 && tamanho != 12 && tamanho != 13 && tamanho != 14)) {
            throw new BusinessException("Codigo de barras deve conter 8, 12, 13 ou 14 digitos numericos");
        }
    }

    private boolean textoInformado(String valor) {
        return valor != null && !valor.isBlank();
    }

    private boolean mudouTexto(String atual, String novo) {
        String atualNormalizado = atual == null || atual.isBlank() ? null : atual.trim();
        String novoNormalizado = novo == null || novo.isBlank() ? null : novo.trim();
        if (atualNormalizado == null) {
            return novoNormalizado != null;
        }
        return !atualNormalizado.equals(novoNormalizado);
    }
}
