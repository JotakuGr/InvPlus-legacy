package br.com.invplus.modules.compras.validator;

import br.com.invplus.modules.compras.entity.PedidoCompra;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/**
 * Regras dos itens de pedido de compra.
 */
@Component
public class PedidoCompraItemValidator {

    public void validar(PedidoCompra pedido, BigDecimal quantidade, BigDecimal valorUnitario) {
        pedido.validarEditavel();
        if (quantidade == null || quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Quantidade deve ser maior que zero");
        }
        if (valorUnitario == null || valorUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidOperationException("Valor unitario nao pode ser negativo");
        }
    }
}
