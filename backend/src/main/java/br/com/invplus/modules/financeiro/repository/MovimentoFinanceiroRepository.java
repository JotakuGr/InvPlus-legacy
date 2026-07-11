package br.com.invplus.modules.financeiro.repository;

import br.com.invplus.modules.financeiro.entity.MovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.OrigemMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.StatusMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.TipoMovimentoFinanceiro;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Persistencia e localizacao dos movimentos financeiros por sua origem.
 */
public interface MovimentoFinanceiroRepository extends BaseRepository<MovimentoFinanceiro> {

    Optional<MovimentoFinanceiro> findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(UUID id, UUID empresaId);

    Optional<MovimentoFinanceiro> findByOrigemAndOrigemIdAndStatusAndAtivoTrueAndDeletedAtIsNull(
            OrigemMovimentoFinanceiro origem,
            UUID origemId,
            StatusMovimentoFinanceiro status
    );

    boolean existsByOrigemAndOrigemIdAndStatusAndAtivoTrueAndDeletedAtIsNull(
            OrigemMovimentoFinanceiro origem,
            UUID origemId,
            StatusMovimentoFinanceiro status
    );

    Optional<MovimentoFinanceiro> findByOrigemAndOrigemIdAndAtivoTrueAndDeletedAtIsNull(
            OrigemMovimentoFinanceiro origem,
            UUID origemId
    );

    @Query("""
            select m from MovimentoFinanceiro m
            where m.ativo = true and m.deletedAt is null
              and m.status = br.com.invplus.modules.financeiro.enums.StatusMovimentoFinanceiro.ATIVO
              and m.contaBancaria.id = :contaBancariaId
              and m.empresa.id = :empresaId
              and m.tipoMovimento = :tipo
              and m.valor = :valor
              and m.dataMovimento between :dataInicio and :dataFim
              and not exists (
                    select c.id from ConciliacaoBancaria c
                    where c.movimentoFinanceiro.id = m.id
                      and c.status = br.com.invplus.modules.financeiro.enums.StatusConciliacaoBancaria.CONCILIADA
              )
            """)
    List<MovimentoFinanceiro> buscarCandidatosConciliacao(
            @Param("contaBancariaId") UUID contaBancariaId,
            @Param("empresaId") UUID empresaId,
            @Param("tipo") TipoMovimentoFinanceiro tipo,
            @Param("valor") BigDecimal valor,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim);

    /**
     * Soma somente movimentos ativos e nao excluidos dentro do periodo informado.
     */
    @Query("""
            select coalesce(sum(m.valor), 0)
            from MovimentoFinanceiro m
            where m.tipoMovimento = :tipo
              and m.status = br.com.invplus.modules.financeiro.enums.StatusMovimentoFinanceiro.ATIVO
              and m.ativo = true
              and m.deletedAt is null
              and m.dataMovimento between :inicio and :fim
            """)
    BigDecimal somarPorTipoNoPeriodo(
            @Param("tipo") TipoMovimentoFinanceiro tipo,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );

    @Query("""
            select coalesce(sum(m.valor), 0)
            from MovimentoFinanceiro m
            where m.tipoMovimento = :tipo
              and m.status = br.com.invplus.modules.financeiro.enums.StatusMovimentoFinanceiro.ATIVO
              and m.ativo = true
              and m.deletedAt is null
              and m.centroCusto.id = :centroCustoId
              and m.dataMovimento between :inicio and :fim
            """)
    BigDecimal somarPorTipoNoPeriodoECentroCusto(
            @Param("tipo") TipoMovimentoFinanceiro tipo,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("centroCustoId") UUID centroCustoId
    );

    @Query("""
            select coalesce(sum(m.valor), 0)
            from MovimentoFinanceiro m
            where m.tipoMovimento = :tipo
              and m.status = br.com.invplus.modules.financeiro.enums.StatusMovimentoFinanceiro.ATIVO
              and m.ativo = true
              and m.deletedAt is null
              and m.planoConta.id = :planoContaId
              and m.dataMovimento between :inicio and :fim
            """)
    BigDecimal somarPorTipoNoPeriodoEPlanoConta(
            @Param("tipo") TipoMovimentoFinanceiro tipo,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("planoContaId") UUID planoContaId
    );

    @Query("""
            select coalesce(sum(m.valor), 0)
            from MovimentoFinanceiro m
            where m.tipoMovimento = :tipo
              and m.status = br.com.invplus.modules.financeiro.enums.StatusMovimentoFinanceiro.ATIVO
              and m.ativo = true
              and m.deletedAt is null
              and m.centroCusto.id = :centroCustoId
              and m.planoConta.id = :planoContaId
              and m.dataMovimento between :inicio and :fim
            """)
    BigDecimal somarPorTipoNoPeriodoECentroCustoEPlanoConta(
            @Param("tipo") TipoMovimentoFinanceiro tipo,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("centroCustoId") UUID centroCustoId,
            @Param("planoContaId") UUID planoContaId
    );

    /**
     * Agrupa receitas da DRE por plano de conta, ignorando movimentos sem classificacao contabil.
     */
    @Query("""
            select m.planoConta.codigo, m.planoConta.nome, coalesce(sum(m.valor), 0)
            from MovimentoFinanceiro m
            where m.status = br.com.invplus.modules.financeiro.enums.StatusMovimentoFinanceiro.ATIVO
              and m.tipoMovimento = br.com.invplus.modules.financeiro.enums.TipoMovimentoFinanceiro.ENTRADA
              and m.ativo = true
              and m.deletedAt is null
              and m.planoConta is not null
              and m.dataMovimento between :inicio and :fim
              and (:empresaId is null or m.empresa.id = :empresaId)
              and (:filialId is null or m.filial.id = :filialId)
              and (:centroCustoId is null or m.centroCusto.id = :centroCustoId)
              and (:planoContaId is null or m.planoConta.id = :planoContaId)
            group by m.planoConta.codigo, m.planoConta.nome
            order by m.planoConta.codigo
            """)
    List<Object[]> somarReceitasPorPlanoConta(
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("empresaId") UUID empresaId,
            @Param("filialId") UUID filialId,
            @Param("centroCustoId") UUID centroCustoId,
            @Param("planoContaId") UUID planoContaId
    );

    /**
     * Agrupa despesas da DRE por plano de conta, ignorando movimentos sem classificacao contabil.
     */
    @Query("""
            select m.planoConta.codigo, m.planoConta.nome, coalesce(sum(m.valor), 0)
            from MovimentoFinanceiro m
            where m.status = br.com.invplus.modules.financeiro.enums.StatusMovimentoFinanceiro.ATIVO
              and m.tipoMovimento = br.com.invplus.modules.financeiro.enums.TipoMovimentoFinanceiro.SAIDA
              and m.ativo = true
              and m.deletedAt is null
              and m.planoConta is not null
              and m.dataMovimento between :inicio and :fim
              and (:empresaId is null or m.empresa.id = :empresaId)
              and (:filialId is null or m.filial.id = :filialId)
              and (:centroCustoId is null or m.centroCusto.id = :centroCustoId)
              and (:planoContaId is null or m.planoConta.id = :planoContaId)
            group by m.planoConta.codigo, m.planoConta.nome
            order by m.planoConta.codigo
            """)
    List<Object[]> somarDespesasPorPlanoConta(
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("empresaId") UUID empresaId,
            @Param("filialId") UUID filialId,
            @Param("centroCustoId") UUID centroCustoId,
            @Param("planoContaId") UUID planoContaId
    );
}
