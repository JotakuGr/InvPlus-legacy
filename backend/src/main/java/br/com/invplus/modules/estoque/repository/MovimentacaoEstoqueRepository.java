package br.com.invplus.modules.estoque.repository;

import br.com.invplus.modules.estoque.entity.MovimentacaoEstoque;
import br.com.invplus.modules.estoque.enums.MovimentacaoOrigem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Acesso ao historico imutavel de movimentacoes de estoque.
 */
public interface MovimentacaoEstoqueRepository
        extends JpaRepository<MovimentacaoEstoque, UUID>, JpaSpecificationExecutor<MovimentacaoEstoque> {

    Optional<MovimentacaoEstoque> findTopByEstoqueIdOrderByDataMovimentacaoDesc(UUID estoqueId);

    List<MovimentacaoEstoque> findByOrigemAndOrigemIdOrderByDataMovimentacaoDesc(MovimentacaoOrigem origem, UUID origemId);
}
