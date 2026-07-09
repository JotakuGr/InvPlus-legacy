package br.com.invplus.modules.financeiro.repository;

import br.com.invplus.modules.financeiro.entity.ExtratoBancario;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.Optional;

/**
 * Persistencia de extratos bancarios importados e ainda ativos.
 */
public interface ExtratoBancarioRepository extends BaseRepository<ExtratoBancario> {

    Optional<ExtratoBancario> findByArquivoHashIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(String arquivoHash);
}
