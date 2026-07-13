package br.com.invplus.modules.estoque.repository;

import br.com.invplus.modules.estoque.entity.LocalizacaoEstoque;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.Optional;
import java.util.UUID;

/**
 * Acesso as localizacoes internas dos depositos.
 */
public interface LocalizacaoEstoqueRepository extends BaseRepository<LocalizacaoEstoque> {

    Optional<LocalizacaoEstoque> findByIdAndDepositoEmpresaIdAndAtivoTrueAndDeletedAtIsNull(
            UUID id, UUID empresaId);

    boolean existsByDepositoIdAndCodigoAndAtivoTrueAndDeletedAtIsNull(UUID depositoId, String codigo);

    boolean existsByDepositoIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(UUID depositoId);

    Optional<LocalizacaoEstoque> findByDepositoIdAndCodigoAndAtivoTrueAndDeletedAtIsNull(UUID depositoId, String codigo);

    Optional<LocalizacaoEstoque> findByDepositoIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(UUID depositoId);
}
