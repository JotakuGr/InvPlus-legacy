package br.com.invplus.modules.estoque.repository;

import br.com.invplus.modules.estoque.entity.Estoque;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.Optional;
import java.util.UUID;

/**
 * Acesso aos saldos atuais de estoque.
 */
public interface EstoqueRepository extends BaseRepository<Estoque> {

    boolean existsByProdutoIdAndEmpresaIdAndDepositoIdAndLocalizacaoIdAndAtivoTrueAndDeletedAtIsNull(
            UUID produtoId,
            UUID empresaId,
            UUID depositoId,
            UUID localizacaoId
    );

    Optional<Estoque> findByProdutoIdAndEmpresaIdAndDepositoIdAndLocalizacaoIdAndAtivoTrueAndDeletedAtIsNull(
            UUID produtoId,
            UUID empresaId,
            UUID depositoId,
            UUID localizacaoId
    );
}
