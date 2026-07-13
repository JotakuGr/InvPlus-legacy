package br.com.invplus.modules.vendas.validator;

import br.com.invplus.modules.vendas.dto.request.PedidoVendaRequest;
import br.com.invplus.modules.vendas.entity.PedidoVenda;
import br.com.invplus.modules.vendas.repository.PedidoVendaRepository;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ConflictException;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Regras de consistencia do cabecalho do pedido de venda.
 */
@Component
public class PedidoVendaValidator {

    private final PedidoVendaRepository repository;

    public PedidoVendaValidator(PedidoVendaRepository repository) {
        this.repository = repository;
    }

    public void validarCriacao(PedidoVendaRequest request, UUID empresaId) {
        validarValores(request);
        validarNumeroUnico(empresaId, request.numero(), null);
    }

    public void validarAtualizacao(PedidoVenda entity, PedidoVendaRequest request, UUID empresaId) {
        entity.validarEditavel();
        validarValores(request);
        validarNumeroUnico(empresaId, request.numero(), entity.getId());
    }

    private void validarValores(PedidoVendaRequest request) {
        if (menorQueZero(request.desconto())) {
            throw new BusinessException("Desconto nao pode ser negativo");
        }
        if (menorQueZero(request.acrescimo())) {
            throw new BusinessException("Acrescimo nao pode ser negativo");
        }
    }

    private void validarNumeroUnico(UUID empresaId, String numero, UUID idAtual) {
        repository.findByEmpresaIdAndNumeroIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(empresaId, numero)
                .filter(existente -> idAtual == null || !existente.getId().equals(idAtual))
                .ifPresent(existente -> {
                    throw new ConflictException("Ja existe pedido de venda ativo com este numero para a empresa");
                });
    }

    private boolean menorQueZero(BigDecimal valor) {
        return valor != null && valor.compareTo(BigDecimal.ZERO) < 0;
    }
}
