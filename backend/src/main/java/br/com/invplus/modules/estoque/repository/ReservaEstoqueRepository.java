package br.com.invplus.modules.estoque.repository;

import br.com.invplus.modules.estoque.entity.ReservaEstoque;
import br.com.invplus.modules.estoque.enums.MovimentacaoOrigem;
import br.com.invplus.modules.estoque.enums.StatusReservaEstoque;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Acesso as reservas de estoque ativas e historicas.
 */
public interface ReservaEstoqueRepository extends BaseRepository<ReservaEstoque> {

    Optional<ReservaEstoque> findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(UUID id, UUID empresaId);

    List<ReservaEstoque> findByEstoqueIdAndStatusAndAtivoTrueAndDeletedAtIsNull(
            UUID estoqueId,
            StatusReservaEstoque status
    );

    boolean existsByOrigemAndOrigemIdAndStatusAndAtivoTrueAndDeletedAtIsNull(
            MovimentacaoOrigem origem,
            UUID origemId,
            StatusReservaEstoque status
    );

    boolean existsByOrigemAndOrigemIdAndStatusAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(
            MovimentacaoOrigem origem,
            UUID origemId,
            StatusReservaEstoque status,
            UUID empresaId
    );

    boolean existsByEstoqueIdAndOrigemAndOrigemIdAndStatusAndAtivoTrueAndDeletedAtIsNull(
            UUID estoqueId,
            MovimentacaoOrigem origem,
            UUID origemId,
            StatusReservaEstoque status
    );

    List<ReservaEstoque> findByOrigemAndOrigemIdAndStatusAndAtivoTrueAndDeletedAtIsNull(
            MovimentacaoOrigem origem,
            UUID origemId,
            StatusReservaEstoque status
    );

    List<ReservaEstoque> findByOrigemAndOrigemIdAndStatusAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(
            MovimentacaoOrigem origem,
            UUID origemId,
            StatusReservaEstoque status,
            UUID empresaId
    );

    List<ReservaEstoque> findByOrigemAndOrigemIdAndAtivoTrueAndDeletedAtIsNull(
            MovimentacaoOrigem origem,
            UUID origemId
    );

    List<ReservaEstoque> findByOrigemAndOrigemIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(
            MovimentacaoOrigem origem,
            UUID origemId,
            UUID empresaId
    );
}
