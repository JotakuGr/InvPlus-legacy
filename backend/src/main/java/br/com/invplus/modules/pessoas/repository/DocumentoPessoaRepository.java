package br.com.invplus.modules.pessoas.repository;

import br.com.invplus.modules.pessoas.entity.DocumentoPessoa;
import br.com.invplus.modules.pessoas.enums.TipoDocumentoPessoa;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.UUID;

/**
 * Acesso aos documentos vinculados a Pessoa.
 */
public interface DocumentoPessoaRepository extends BaseRepository<DocumentoPessoa> {

    /**
     * Verifica duplicidade ativa do mesmo documento para a mesma Pessoa.
     */
    boolean existsByPessoaIdAndTipoDocumentoAndNumeroAndAtivoTrueAndDeletedAtIsNull(
            UUID pessoaId,
            TipoDocumentoPessoa tipoDocumento,
            String numero
    );
}
