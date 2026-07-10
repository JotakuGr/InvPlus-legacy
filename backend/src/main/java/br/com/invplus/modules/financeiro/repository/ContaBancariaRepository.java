package br.com.invplus.modules.financeiro.repository;

import br.com.invplus.modules.financeiro.entity.ContaBancaria;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.Optional;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Persistencia de contas bancarias ativas e nao excluidas.
 */
public interface ContaBancariaRepository extends BaseRepository<ContaBancaria> {

    Optional<ContaBancaria> findByEmpresaIdAndBancoCodigoIgnoreCaseAndAgenciaIgnoreCaseAndNumeroContaIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(
            UUID empresaId, String bancoCodigo, String agencia, String numeroConta);

    Optional<ContaBancaria> findByEmpresaIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(UUID empresaId);

    @Query(value = """
            SELECT * FROM conta_bancaria
            WHERE ativo = TRUE AND deleted_at IS NULL
              AND regexp_replace(banco_codigo, '[^0-9]', '', 'g') = :banco
              AND regexp_replace(agencia, '[^0-9]', '', 'g') = :agencia
            """, nativeQuery = true)
    List<ContaBancaria> localizarAtivasPorBancoEAgenciaNormalizados(
            @Param("banco") String banco, @Param("agencia") String agencia);
}
