package br.com.invplus.modules.financeiro.repository;

import br.com.invplus.modules.financeiro.entity.ContaBancaria;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistencia de contas bancarias ativas e nao excluidas.
 */
public interface ContaBancariaRepository extends BaseRepository<ContaBancaria> {

    Optional<ContaBancaria> findByEmpresaIdAndBancoCodigoIgnoreCaseAndAgenciaIgnoreCaseAndNumeroContaIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(
            UUID empresaId, String bancoCodigo, String agencia, String numeroConta);

    Optional<ContaBancaria> findByEmpresaIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(UUID empresaId);
}
