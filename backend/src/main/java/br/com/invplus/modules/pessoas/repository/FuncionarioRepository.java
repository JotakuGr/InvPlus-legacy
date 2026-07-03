package br.com.invplus.modules.pessoas.repository;

import br.com.invplus.modules.pessoas.entity.Funcionario;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio do papel Funcionario com consultas por Pessoa e Usuario ativos.
 */
public interface FuncionarioRepository extends BaseRepository<Funcionario> {

    boolean existsByPessoaIdAndAtivoTrueAndDeletedAtIsNull(UUID pessoaId);

    boolean existsByUsuarioIdAndAtivoTrueAndDeletedAtIsNull(UUID usuarioId);

    Optional<Funcionario> findByPessoaIdAndAtivoTrueAndDeletedAtIsNull(UUID pessoaId);

    Optional<Funcionario> findByUsuarioIdAndAtivoTrueAndDeletedAtIsNull(UUID usuarioId);
}
