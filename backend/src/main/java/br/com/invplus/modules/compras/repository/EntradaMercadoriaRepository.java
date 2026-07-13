package br.com.invplus.modules.compras.repository;

import br.com.invplus.modules.compras.entity.EntradaMercadoria;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistencia das entradas de mercadoria.
 */
public interface EntradaMercadoriaRepository extends BaseRepository<EntradaMercadoria> {

    Optional<EntradaMercadoria> findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(UUID id, UUID empresaId);

    boolean existsByEmpresaIdAndNumeroIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(UUID empresaId, String numero);

    Optional<EntradaMercadoria> findByEmpresaIdAndNumeroIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(UUID empresaId, String numero);

    List<EntradaMercadoria> findByPedidoCompraIdAndAtivoTrueAndDeletedAtIsNull(UUID pedidoCompraId);
}
