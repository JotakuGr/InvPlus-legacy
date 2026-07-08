package br.com.invplus.modules.vendas.repository;

import br.com.invplus.modules.vendas.entity.PedidoVenda;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistencia de pedidos de venda com unicidade por empresa e numero ativo.
 */
public interface PedidoVendaRepository extends BaseRepository<PedidoVenda> {

    boolean existsByEmpresaIdAndNumeroIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(UUID empresaId, String numero);

    Optional<PedidoVenda> findByEmpresaIdAndNumeroIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(UUID empresaId, String numero);
}
