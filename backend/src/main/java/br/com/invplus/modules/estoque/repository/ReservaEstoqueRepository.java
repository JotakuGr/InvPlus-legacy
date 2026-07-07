package br.com.invplus.modules.estoque.repository;

import br.com.invplus.modules.estoque.entity.ReservaEstoque;
import br.com.invplus.modules.estoque.enums.MovimentacaoOrigem;
import br.com.invplus.modules.estoque.enums.StatusReservaEstoque;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.List;
import java.util.UUID;

/**
 * Acesso as reservas de estoque ativas e historicas.
 */
public interface ReservaEstoqueRepository extends BaseRepository<ReservaEstoque> {

    List<ReservaEstoque> findByEstoqueIdAndStatusAndAtivoTrueAndDeletedAtIsNull(
            UUID estoqueId,
            StatusReservaEstoque status
    );

    boolean existsByOrigemAndOrigemIdAndStatusAndAtivoTrueAndDeletedAtIsNull(
            MovimentacaoOrigem origem,
            UUID origemId,
            StatusReservaEstoque status
    );
}
