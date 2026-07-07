package br.com.invplus.modules.compras.validator;

import br.com.invplus.modules.compras.entity.EntradaMercadoria;
import br.com.invplus.modules.estoque.entity.Deposito;
import br.com.invplus.modules.estoque.entity.LocalizacaoEstoque;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/**
 * Regras dos itens de entrada de mercadoria.
 */
@Component
public class EntradaMercadoriaItemValidator {

    public void validar(EntradaMercadoria entrada, Deposito deposito, LocalizacaoEstoque localizacao,
                        BigDecimal quantidade, BigDecimal valorUnitario) {
        entrada.validarEditavel();
        if (!localizacao.getDeposito().getId().equals(deposito.getId())) {
            throw new InvalidOperationException("Localizacao deve pertencer ao deposito informado");
        }
        if (quantidade == null || quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Quantidade deve ser maior que zero");
        }
        if (valorUnitario == null || valorUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidOperationException("Valor unitario nao pode ser negativo");
        }
    }
}
