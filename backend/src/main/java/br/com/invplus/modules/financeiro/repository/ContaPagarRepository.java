package br.com.invplus.modules.financeiro.repository;

import br.com.invplus.modules.financeiro.entity.ContaPagar;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Persistencia das contas a pagar com consultas restritas a registros ativos.
 */
public interface ContaPagarRepository extends BaseRepository<ContaPagar> {

    Optional<ContaPagar> findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(UUID id, UUID empresaId);

    Optional<ContaPagar> findByEmpresaIdAndNumeroIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(
            UUID empresaId, String numero);

    @Query("""
            select coalesce(sum(c.valorAberto), 0)
            from ContaPagar c
            where c.status in (
                br.com.invplus.modules.financeiro.enums.StatusContaPagar.ABERTA,
                br.com.invplus.modules.financeiro.enums.StatusContaPagar.PARCIALMENTE_PAGA,
                br.com.invplus.modules.financeiro.enums.StatusContaPagar.VENCIDA
            )
              and c.ativo = true
              and c.deletedAt is null
            """)
    BigDecimal somarValorAberto();

    long countByStatusAndAtivoTrueAndDeletedAtIsNull(
            br.com.invplus.modules.financeiro.enums.StatusContaPagar status);

    @Query("""
            select coalesce(sum(c.valorAberto), 0)
            from ContaPagar c
            where c.status in (
                br.com.invplus.modules.financeiro.enums.StatusContaPagar.ABERTA,
                br.com.invplus.modules.financeiro.enums.StatusContaPagar.PARCIALMENTE_PAGA,
                br.com.invplus.modules.financeiro.enums.StatusContaPagar.VENCIDA
            )
              and c.centroCusto.id = :centroCustoId
              and c.ativo = true
              and c.deletedAt is null
            """)
    BigDecimal somarValorAbertoPorCentroCusto(@Param("centroCustoId") UUID centroCustoId);

    long countByStatusAndCentroCustoIdAndAtivoTrueAndDeletedAtIsNull(
            br.com.invplus.modules.financeiro.enums.StatusContaPagar status, UUID centroCustoId);

    @Query("""
            select coalesce(sum(c.valorAberto), 0)
            from ContaPagar c
            where c.status in (
                br.com.invplus.modules.financeiro.enums.StatusContaPagar.ABERTA,
                br.com.invplus.modules.financeiro.enums.StatusContaPagar.PARCIALMENTE_PAGA,
                br.com.invplus.modules.financeiro.enums.StatusContaPagar.VENCIDA
            )
              and c.planoConta.id = :planoContaId
              and c.ativo = true
              and c.deletedAt is null
            """)
    BigDecimal somarValorAbertoPorPlanoConta(@Param("planoContaId") UUID planoContaId);

    long countByStatusAndPlanoContaIdAndAtivoTrueAndDeletedAtIsNull(
            br.com.invplus.modules.financeiro.enums.StatusContaPagar status, UUID planoContaId);

    @Query("""
            select coalesce(sum(c.valorAberto), 0)
            from ContaPagar c
            where c.status in (
                br.com.invplus.modules.financeiro.enums.StatusContaPagar.ABERTA,
                br.com.invplus.modules.financeiro.enums.StatusContaPagar.PARCIALMENTE_PAGA,
                br.com.invplus.modules.financeiro.enums.StatusContaPagar.VENCIDA
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
            br.com.invplus.modules.financeiro.enums.StatusContaPagar status, UUID centroCustoId, UUID planoContaId);

    @Query("""
            select coalesce(sum(c.valorAberto), 0) from ContaPagar c
            where c.empresa.id = :empresaId
              and c.status in (br.com.invplus.modules.financeiro.enums.StatusContaPagar.ABERTA,
                br.com.invplus.modules.financeiro.enums.StatusContaPagar.PARCIALMENTE_PAGA,
                br.com.invplus.modules.financeiro.enums.StatusContaPagar.VENCIDA)
              and c.ativo = true and c.deletedAt is null
              and (:centroCustoId is null or c.centroCusto.id = :centroCustoId)
              and (:planoContaId is null or c.planoConta.id = :planoContaId)
            """)
    BigDecimal somarValorAbertoPorEmpresa(@Param("empresaId") UUID empresaId,
            @Param("centroCustoId") UUID centroCustoId, @Param("planoContaId") UUID planoContaId);

    @Query("""
            select count(c) from ContaPagar c where c.empresa.id = :empresaId and c.status = :status
              and c.ativo = true and c.deletedAt is null
              and (:centroCustoId is null or c.centroCusto.id = :centroCustoId)
              and (:planoContaId is null or c.planoConta.id = :planoContaId)
            """)
    long contarPorEmpresaStatus(@Param("empresaId") UUID empresaId,
            @Param("status") br.com.invplus.modules.financeiro.enums.StatusContaPagar status,
            @Param("centroCustoId") UUID centroCustoId, @Param("planoContaId") UUID planoContaId);
}
