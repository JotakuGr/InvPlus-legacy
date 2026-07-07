package br.com.invplus.modules.estoque.validator;

import br.com.invplus.modules.estoque.dto.request.MovimentacaoEstoqueRequest;
import br.com.invplus.shared.domain.exception.BusinessException;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/**
 * Valida o registro imutavel de movimentacoes de estoque.
 */
@Component
public class MovimentacaoEstoqueValidator {

    public void validarRegistro(MovimentacaoEstoqueRequest request) {
        if (request.estoqueId() == null) {
            throw new BusinessException("Estoque e obrigatorio");
        }
        if (request.tipoMovimentacao() == null) {
            throw new BusinessException("Tipo de movimentacao e obrigatorio");
        }
        if (request.quantidade() == null || request.quantidade().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Quantidade deve ser maior que zero");
        }
        if (request.saldoAnterior() == null) {
            throw new BusinessException("Saldo anterior e obrigatorio");
        }
        if (request.saldoPosterior() == null) {
            throw new BusinessException("Saldo posterior e obrigatorio");
        }
    }
}
