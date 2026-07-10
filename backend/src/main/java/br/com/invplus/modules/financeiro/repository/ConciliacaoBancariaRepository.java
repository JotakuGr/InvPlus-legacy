package br.com.invplus.modules.financeiro.repository;

import br.com.invplus.modules.financeiro.entity.ConciliacaoBancaria;
import br.com.invplus.modules.financeiro.enums.StatusConciliacaoBancaria;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Persistencia do historico de conciliacoes, sem soft delete. */
public interface ConciliacaoBancariaRepository extends JpaRepository<ConciliacaoBancaria, UUID>,
        JpaSpecificationExecutor<ConciliacaoBancaria> {

    boolean existsByExtratoBancarioLancamentoIdAndStatus(UUID lancamentoId, StatusConciliacaoBancaria status);

    boolean existsByMovimentoFinanceiroIdAndStatus(UUID movimentoId, StatusConciliacaoBancaria status);

    Optional<ConciliacaoBancaria> findByIdAndStatus(UUID id, StatusConciliacaoBancaria status);

    long countByExtratoBancarioLancamentoIdAndStatus(UUID lancamentoId, StatusConciliacaoBancaria status);
}
