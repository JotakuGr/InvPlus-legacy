package br.com.invplus.modules.vendas.validator;

import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.entity.ProdutoPreco;
import br.com.invplus.modules.produtos.entity.ProdutoUnidade;
import br.com.invplus.modules.vendas.entity.PedidoVenda;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

/**
 * Regras de consistencia do item de pedido de venda.
 */
@Component
public class PedidoVendaItemValidator {

    public void validar(PedidoVenda pedido, Produto produto, ProdutoPreco preco, ProdutoUnidade unidade,
                        BigDecimal quantidade, BigDecimal desconto, BigDecimal acrescimo) {
        pedido.validarEditavel();
        validarQuantidade(quantidade);
        validarValores(desconto, acrescimo);
        validarPrecoDoProduto(produto, preco);
        validarUnidadeDoProduto(produto, unidade);
        validarPrecoVigente(pedido, preco);
        validarTotalNaoNegativo(quantidade, preco.getPreco(), desconto, acrescimo);
    }

    private void validarQuantidade(BigDecimal quantidade) {
        if (quantidade == null || quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Quantidade deve ser maior que zero");
        }
    }

    private void validarValores(BigDecimal desconto, BigDecimal acrescimo) {
        if (desconto != null && desconto.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Desconto nao pode ser negativo");
        }
        if (acrescimo != null && acrescimo.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Acrescimo nao pode ser negativo");
        }
    }

    private void validarPrecoDoProduto(Produto produto, ProdutoPreco preco) {
        if (!preco.getProduto().getId().equals(produto.getId())) {
            throw new InvalidOperationException("ProdutoPreco nao pertence ao Produto informado");
        }
    }

    private void validarUnidadeDoProduto(Produto produto, ProdutoUnidade unidade) {
        if (!unidade.getProduto().getId().equals(produto.getId())) {
            throw new InvalidOperationException("ProdutoUnidade nao pertence ao Produto informado");
        }
    }

    private void validarPrecoVigente(PedidoVenda pedido, ProdutoPreco preco) {
        LocalDate data = pedido.getDataEmissao();
        if (preco.getVigenciaInicio().isAfter(data)
                || (preco.getVigenciaFim() != null && preco.getVigenciaFim().isBefore(data))) {
            throw new InvalidOperationException("Produto nao possui preco vigente para a data do pedido");
        }
    }

    private void validarTotalNaoNegativo(BigDecimal quantidade, BigDecimal preco, BigDecimal desconto, BigDecimal acrescimo) {
        BigDecimal total = quantidade.multiply(preco)
                .subtract(desconto == null ? BigDecimal.ZERO : desconto)
                .add(acrescimo == null ? BigDecimal.ZERO : acrescimo);
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Valor total do item nao pode ser negativo");
        }
    }
}
