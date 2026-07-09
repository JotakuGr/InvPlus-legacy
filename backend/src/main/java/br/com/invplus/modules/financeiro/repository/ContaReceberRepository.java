package br.com.invplus.modules.financeiro.repository;

import br.com.invplus.modules.financeiro.entity.ContaReceber;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Persistencia das contas a receber com soft delete aplicado por padrao.
 */
public interface ContaReceberRepository extends BaseRepository<ContaReceber> {

    boolean existsByEmpresaIdAndNumeroIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(UUID empresaId, String numero);

    boolean existsByVendaFaturamentoIdAndAtivoTrueAndDeletedAtIsNull(UUID vendaFaturamentoId);

    Optional<ContaReceber> findByEmpresaIdAndNumeroIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(UUID empresaId, String numero);

    Optional<ContaReceber> findByVendaFaturamentoIdAndAtivoTrueAndDeletedAtIsNull(UUID vendaFaturamentoId);

    @Query("""
            select coalesce(sum(c.valorAberto), 0)
            from ContaReceber c
            where c.status in (
                br.com.invplus.modules.financeiro.enums.StatusContaReceber.ABERTA,
                br.com.invplus.modules.financeiro.enums.StatusContaReceber.PARCIALMENTE_PAGA,
                br.com.invplus.modules.financeiro.enums.StatusContaReceber.VENCIDA
            )
              and c.ativo = true
              and c.deletedAt is null
            """)
    BigDecimal somarValorAberto();

    long countByStatusAndAtivoTrueAndDeletedAtIsNull(
            br.com.invplus.modules.financeiro.enums.StatusContaReceber status);

    @Query("""
            select coalesce(sum(c.valorAberto), 0)
            from ContaReceber c
            where c.status in (
                br.com.invplus.modules.financeiro.enums.StatusContaReceber.ABERTA,
                br.com.invplus.modules.financeiro.enums.StatusContaReceber.PARCIALMENTE_PAGA,
                br.com.invplus.modules.financeiro.enums.StatusContaReceber.VENCIDA
            )
              and c.centroCusto.id = :centroCustoId
              and c.ativo = true
              and c.deletedAt is null
            """)
    BigDecimal somarValorAbertoPorCentroCusto(@Param("centroCustoId") UUID centroCustoId);

    long countByStatusAndCentroCustoIdAndAtivoTrueAndDeletedAtIsNull(
            br.com.invplus.modules.financeiro.enums.StatusContaReceber status, UUID centroCustoId);

    @Query("""
            select coalesce(sum(c.valorAberto), 0)
            from ContaReceber c
            where c.status in (
                br.com.invplus.modules.financeiro.enums.StatusContaReceber.ABERTA,
                br.com.invplus.modules.financeiro.enums.StatusContaReceber.PARCIALMENTE_PAGA,
                br.com.invplus.modules.financeiro.enums.StatusContaReceber.VENCIDA
            )
              and c.planoConta.id = :planoContaId
              and c.ativo = true
              and c.deletedAt is null
            """)
    BigDecimal somarValorAbertoPorPlanoConta(@Param("planoContaId") UUID planoContaId);

    long countByStatusAndPlanoContaIdAndAtivoTrueAndDeletedAtIsNull(
            br.com.invplus.modules.financeiro.enums.StatusContaReceber status, UUID planoContaId);

    @Query("""
            select coalesce(sum(c.valorAberto), 0)
            from ContaReceber c
            where c.status in (
                br.com.invplus.modules.financeiro.enums.StatusContaReceber.ABERTA,
                br.com.invplus.modules.financeiro.enums.StatusContaReceber.PARCIALMENTE_PAGA,
                br.com.invplus.modules.financeiro.enums.StatusContaReceber.VENCIDA
            )
              and c.centroCusto.id = :centroCustoId
              and c.planoConta.id = :planoContaId
              and c.ativo = true
              and c.deletedAt is null
            """)
    BigDecimal somarValorAbertoPorCentroCustoEPlanoConta(
            @Param("centroCustoId") UUID centroCustoId,
            @Param("planoContaId") UUID planoContaId);

    long countByStatusAndCentroCustoIdAndPlanoContaIdAndAtivoTrueAndDeletedAtIsNull(
            br.com.invplus.modules.financeiro.enums.StatusContaReceber status, UUID centroCustoId, UUID planoContaId);
}
