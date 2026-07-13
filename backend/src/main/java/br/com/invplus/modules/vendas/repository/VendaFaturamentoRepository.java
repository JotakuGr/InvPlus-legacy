package br.com.invplus.modules.vendas.repository;

import br.com.invplus.modules.vendas.entity.VendaFaturamento;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistencia dos faturamentos de venda gerados a partir de pedidos aprovados.
 */
public interface VendaFaturamentoRepository extends BaseRepository<VendaFaturamento> {

    Optional<VendaFaturamento> findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(UUID id, UUID empresaId);

    boolean existsByPedidoVendaIdAndAtivoTrueAndDeletedAtIsNull(UUID pedidoVendaId);

    boolean existsByEmpresaIdAndNumeroIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(UUID empresaId, String numero);

    Optional<VendaFaturamento> findByPedidoVendaIdAndAtivoTrueAndDeletedAtIsNull(UUID pedidoVendaId);

    Optional<VendaFaturamento> findByPedidoVendaIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(
            UUID pedidoVendaId,
            UUID empresaId
    );
}
