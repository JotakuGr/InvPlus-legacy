package br.com.invplus.modules.financeiro.importacao.repository;

import br.com.invplus.modules.financeiro.importacao.entity.ImportacaoExtratoBancario;
import br.com.invplus.modules.financeiro.importacao.enums.StatusImportacaoExtrato;
import java.util.UUID;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Persistencia do historico de processamento de extratos. */
public interface ImportacaoExtratoRepository extends JpaRepository<ImportacaoExtratoBancario, UUID>,
        JpaSpecificationExecutor<ImportacaoExtratoBancario> {

    boolean existsByArquivoHashIgnoreCaseAndStatusNot(String hash, StatusImportacaoExtrato status);

    Optional<ImportacaoExtratoBancario> findByArquivoHashIgnoreCase(String hash);

    Optional<ImportacaoExtratoBancario> findByIdAndContaBancariaEmpresaId(UUID id, UUID empresaId);
}
