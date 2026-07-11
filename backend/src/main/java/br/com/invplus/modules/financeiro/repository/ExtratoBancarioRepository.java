package br.com.invplus.modules.financeiro.repository;

import br.com.invplus.modules.financeiro.entity.ExtratoBancario;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDate;
import br.com.invplus.modules.financeiro.enums.StatusExtratoBancario;
import br.com.invplus.modules.financeiro.conciliacao.projection.IndicadoresExtratoProjection;
import br.com.invplus.modules.financeiro.conciliacao.projection.ExtratoPendenteProjection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Persistencia de extratos bancarios importados e ainda ativos.
 */
public interface ExtratoBancarioRepository extends BaseRepository<ExtratoBancario> {

    Optional<ExtratoBancario> findByIdAndContaBancariaEmpresaIdAndAtivoTrueAndDeletedAtIsNull(
            UUID id, UUID empresaId);

    Optional<ExtratoBancario> findByArquivoHashIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(String arquivoHash);

    @Query("""
            select count(e) as quantidadeExtratos,
              sum(case when e.status = br.com.invplus.modules.financeiro.enums.StatusExtratoBancario.IMPORTADO then 1 else 0 end) as quantidadeExtratosImportados,
              sum(case when e.status = br.com.invplus.modules.financeiro.enums.StatusExtratoBancario.PROCESSANDO then 1 else 0 end) as quantidadeExtratosProcessando,
              sum(case when e.status = br.com.invplus.modules.financeiro.enums.StatusExtratoBancario.CONCILIADO then 1 else 0 end) as quantidadeExtratosConciliados
            from ExtratoBancario e
            where e.ativo = true and e.deletedAt is null
              and e.status <> br.com.invplus.modules.financeiro.enums.StatusExtratoBancario.CANCELADO
              and e.contaBancaria.empresa.id = :empresaId
              and (:contaId is null or e.contaBancaria.id = :contaId)
              and (:inicio is null or e.dataFim >= :inicio)
              and (:fim is null or e.dataInicio <= :fim)
            """)
    IndicadoresExtratoProjection indicadoresPainel(@Param("empresaId") UUID empresaId,
            @Param("contaId") UUID contaId, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query(value = """
            select e.id as extratoBancarioId, c.id as contaBancariaId,
              c.bancoNome as bancoNome, c.agencia as agencia, c.numeroConta as numeroConta,
              e.dataInicio as dataInicio, e.dataFim as dataFim, e.status as status,
              count(l.id) as quantidadeLancamentos,
              coalesce(sum(case when l.conciliado = true then 1 else 0 end), 0) as quantidadeConciliados,
              coalesce(sum(case when l.conciliado = false and l.tipo = br.com.invplus.modules.financeiro.enums.TipoLancamentoBancario.CREDITO then l.valor else 0 end), 0) as valorCreditosPendentes,
              coalesce(sum(case when l.conciliado = false and l.tipo = br.com.invplus.modules.financeiro.enums.TipoLancamentoBancario.DEBITO then l.valor else 0 end), 0) as valorDebitosPendentes,
              e.importadoEm as importadoEm
            from ExtratoBancario e join e.contaBancaria c
              left join ExtratoBancarioLancamento l on l.extratoBancario = e and l.ativo = true and l.deletedAt is null
            where e.ativo = true and e.deletedAt is null and c.empresa.id = :empresaId
              and (:contaId is null or c.id = :contaId)
              and (:status is null or e.status = :status)
              and (:inicio is null or e.dataFim >= :inicio)
              and (:fim is null or e.dataInicio <= :fim)
              and (:incluirTodos = true or e.status in (br.com.invplus.modules.financeiro.enums.StatusExtratoBancario.IMPORTADO, br.com.invplus.modules.financeiro.enums.StatusExtratoBancario.PROCESSANDO))
            group by e.id, c.id, c.bancoNome, c.agencia, c.numeroConta, e.dataInicio, e.dataFim, e.status, e.importadoEm
            """,
            countQuery = """
            select count(e) from ExtratoBancario e where e.ativo = true and e.deletedAt is null
              and e.contaBancaria.empresa.id = :empresaId
              and (:contaId is null or e.contaBancaria.id = :contaId)
              and (:status is null or e.status = :status)
              and (:inicio is null or e.dataFim >= :inicio)
              and (:fim is null or e.dataInicio <= :fim)
              and (:incluirTodos = true or e.status in (br.com.invplus.modules.financeiro.enums.StatusExtratoBancario.IMPORTADO, br.com.invplus.modules.financeiro.enums.StatusExtratoBancario.PROCESSANDO))
            """)
    Page<ExtratoPendenteProjection> buscarResumoExtratos(@Param("empresaId") UUID empresaId,
            @Param("contaId") UUID contaId, @Param("status") StatusExtratoBancario status,
            @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim,
            @Param("incluirTodos") boolean incluirTodos, Pageable pageable);
}
