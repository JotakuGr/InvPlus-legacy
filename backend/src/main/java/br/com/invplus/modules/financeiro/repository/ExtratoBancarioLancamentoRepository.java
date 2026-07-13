package br.com.invplus.modules.financeiro.repository;

import br.com.invplus.modules.financeiro.entity.ExtratoBancarioLancamento;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.Optional;
import java.util.List;
import java.util.UUID;
import java.time.LocalDate;
import br.com.invplus.modules.financeiro.enums.CategoriaLancamentoBancario;
import br.com.invplus.modules.financeiro.enums.TipoLancamentoBancario;
import br.com.invplus.modules.financeiro.conciliacao.projection.IndicadoresLancamentoProjection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Persistencia de lancamentos vinculados a extratos bancarios.
 */
public interface ExtratoBancarioLancamentoRepository extends BaseRepository<ExtratoBancarioLancamento> {

    Optional<ExtratoBancarioLancamento>
            findByIdAndExtratoBancarioContaBancariaEmpresaIdAndAtivoTrueAndDeletedAtIsNull(
                    UUID id, UUID empresaId);

    Optional<ExtratoBancarioLancamento> findByIdAndAtivoTrueAndDeletedAtIsNull(UUID id);

    Optional<ExtratoBancarioLancamento> findByExtratoBancarioIdAndIdentificadorBancarioIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(
            UUID extratoBancarioId, String identificadorBancario);

    long countByExtratoBancarioIdAndAtivoTrueAndDeletedAtIsNull(UUID extratoBancarioId);

    long countByExtratoBancarioIdAndConciliadoTrueAndAtivoTrueAndDeletedAtIsNull(UUID extratoBancarioId);

    List<ExtratoBancarioLancamento> findByExtratoBancarioIdAndAtivoTrueAndDeletedAtIsNull(UUID extratoBancarioId);

    @Query("""
            select count(l) as quantidadeLancamentos,
              sum(case when l.conciliado = false then 1 else 0 end) as quantidadeLancamentosPendentes,
              sum(case when l.conciliado = true then 1 else 0 end) as quantidadeLancamentosConciliados,
              coalesce(sum(case when l.conciliado = false and l.tipo = br.com.invplus.modules.financeiro.enums.TipoLancamentoBancario.CREDITO then l.valor else 0 end), 0) as valorTotalCreditosPendentes,
              coalesce(sum(case when l.conciliado = false and l.tipo = br.com.invplus.modules.financeiro.enums.TipoLancamentoBancario.DEBITO then l.valor else 0 end), 0) as valorTotalDebitosPendentes
            from ExtratoBancarioLancamento l
            where l.ativo = true and l.deletedAt is null and l.extratoBancario.ativo = true
              and l.extratoBancario.deletedAt is null
              and l.extratoBancario.status <> br.com.invplus.modules.financeiro.enums.StatusExtratoBancario.CANCELADO
              and l.extratoBancario.contaBancaria.empresa.id = :empresaId
              and (:contaId is null or l.extratoBancario.contaBancaria.id = :contaId)
              and (:inicio is null or l.dataLancamento >= :inicio)
              and (:fim is null or l.dataLancamento <= :fim)
            """)
    IndicadoresLancamentoProjection indicadoresPainel(@Param("empresaId") UUID empresaId,
            @Param("contaId") UUID contaId, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @EntityGraph(attributePaths = {"extratoBancario", "extratoBancario.contaBancaria", "extratoBancario.contaBancaria.empresa"})
    @Query("""
            select l from ExtratoBancarioLancamento l
            where l.ativo = true and l.deletedAt is null and l.conciliado = false
              and l.extratoBancario.ativo = true and l.extratoBancario.deletedAt is null
              and l.extratoBancario.status <> br.com.invplus.modules.financeiro.enums.StatusExtratoBancario.CANCELADO
              and l.extratoBancario.contaBancaria.empresa.id = :empresaId
              and (:extratoId is null or l.extratoBancario.id = :extratoId)
              and (:contaId is null or l.extratoBancario.contaBancaria.id = :contaId)
              and (:tipo is null or l.tipo = :tipo)
              and (:categoria is null or l.categoriaBancaria = :categoria)
              and (:inicio is null or l.dataLancamento >= :inicio)
              and (:fim is null or l.dataLancamento <= :fim)
            """)
    Page<ExtratoBancarioLancamento> buscarPendencias(@Param("empresaId") UUID empresaId,
            @Param("extratoId") UUID extratoId, @Param("contaId") UUID contaId,
            @Param("tipo") TipoLancamentoBancario tipo,
            @Param("categoria") CategoriaLancamentoBancario categoria,
            @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim, Pageable pageable);
}
