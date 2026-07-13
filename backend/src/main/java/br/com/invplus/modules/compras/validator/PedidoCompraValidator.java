package br.com.invplus.modules.compras.validator;

import br.com.invplus.modules.compras.dto.request.PedidoCompraRequest;
import br.com.invplus.modules.compras.entity.PedidoCompra;
import br.com.invplus.modules.compras.repository.PedidoCompraRepository;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Regras de consistencia do pedido de compra.
 */
@Component
public class PedidoCompraValidator {

    private final PedidoCompraRepository repository;

    public PedidoCompraValidator(PedidoCompraRepository repository) {
        this.repository = repository;
    }

    public void validarCriacao(PedidoCompraRequest request, UUID empresaId) {
        validarNumeroUnico(empresaId, request.numero(), null);
    }

    public void validarAtualizacao(PedidoCompra entity, PedidoCompraRequest request, UUID empresaId) {
        entity.validarEditavel();
        validarNumeroUnico(empresaId, request.numero(), entity.getId());
    }

    public void validarPodeReceber(PedidoCompra pedido) {
        if (pedido == null) {
            return;
        }
        if (pedido.getStatus().name().equals("CANCELADO")) {
            throw new InvalidOperationException("Pedido cancelado nao pode receber mercadoria");
        }
    }

    private void validarNumeroUnico(UUID empresaId, String numero, UUID idAtual) {
        repository.findByEmpresaIdAndNumeroIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(empresaId, numero)
                .filter(existente -> idAtual == null || !existente.getId().equals(idAtual))
                .ifPresent(existente -> {
                    throw new ConflictException("Ja existe pedido de compra ativo com este numero para a empresa");
                });
    }
}
