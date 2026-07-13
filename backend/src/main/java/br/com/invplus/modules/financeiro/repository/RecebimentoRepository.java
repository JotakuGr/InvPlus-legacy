package br.com.invplus.modules.financeiro.repository;

import br.com.invplus.modules.financeiro.entity.Recebimento;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistencia dos recebimentos financeiros registrados para parcelas.
 */
public interface RecebimentoRepository extends BaseRepository<Recebimento> {

    Optional<Recebimento> findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(UUID id, UUID empresaId);

    List<Recebimento> findByParcelaReceberIdAndAtivoTrueAndDeletedAtIsNull(UUID parcelaId);

    List<Recebimento> findByContaReceberIdAndAtivoTrueAndDeletedAtIsNull(UUID contaId);
}
