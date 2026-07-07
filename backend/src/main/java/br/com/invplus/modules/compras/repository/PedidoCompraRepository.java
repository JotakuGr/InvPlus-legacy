package br.com.invplus.modules.compras.repository;

import br.com.invplus.modules.compras.entity.PedidoCompra;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistencia de pedidos de compra com consultas por numero ativo.
 */
public interface PedidoCompraRepository extends BaseRepository<PedidoCompra> {

    boolean existsByEmpresaIdAndNumeroIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(UUID empresaId, String numero);

    Optional<PedidoCompra> findByEmpresaIdAndNumeroIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(UUID empresaId, String numero);
}
