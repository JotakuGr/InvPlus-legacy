package br.com.invplus.modules.cadastros.repository;

import br.com.invplus.modules.cadastros.entity.Endereco;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Acesso aos enderecos cadastrados no modulo Cadastros.
 */
public interface EnderecoRepository extends BaseRepository<Endereco> {

    /**
     * Lista apenas enderecos ativos, ocultando registros excluidos logicamente.
     */
    List<Endereco> findByAtivoTrueAndDeletedAtIsNull();

    /**
     * Lista os enderecos ativos vinculados a uma empresa.
     */
    List<Endereco> findByEmpresaIdAndAtivoTrueAndDeletedAtIsNull(UUID empresaId);

    /**
     * Lista os enderecos ativos vinculados a uma empresa com paginacao.
     */
    Page<Endereco> findByEmpresaIdAndAtivoTrueAndDeletedAtIsNull(UUID empresaId, Pageable pageable);

    Optional<Endereco> findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(UUID id, UUID empresaId);
}
