package br.com.invplus.modules.financeiro.repository;

import br.com.invplus.modules.financeiro.entity.ExtratoBancarioLancamento;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistencia de lancamentos vinculados a extratos bancarios.
 */
public interface ExtratoBancarioLancamentoRepository extends BaseRepository<ExtratoBancarioLancamento> {

    Optional<ExtratoBancarioLancamento> findByExtratoBancarioIdAndIdentificadorBancarioIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(
            UUID extratoBancarioId, String identificadorBancario);
}
