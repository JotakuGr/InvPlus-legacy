package br.com.invplus.modules.pessoas.service;

import br.com.invplus.common.enums.PapelPessoa;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.pessoas.entity.PessoaRole;
import br.com.invplus.modules.pessoas.repository.PessoaRoleRepository;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mantem o indice auxiliar de papeis exercidos por uma Pessoa.
 */
@Service
@RequiredArgsConstructor
public class PessoaRoleService {

    private final PessoaRoleRepository pessoaRoleRepository;

    /**
     * Registra o papel ativo quando uma entidade de papel e criada ou trocada de Pessoa.
     */
    @Transactional
    public void registrar(Pessoa pessoa, PapelPessoa role) {
        if (!pessoaRoleRepository.existsByPessoaIdAndRoleAndAtivoTrueAndDeletedAtIsNull(pessoa.getId(), role)) {
            PessoaRole pessoaRole = new PessoaRole();
            pessoaRole.atualizar(pessoa, role);
            pessoaRoleRepository.save(pessoaRole);
        }
    }

    /**
     * Remove logicamente o papel quando a entidade de papel e excluida ou transferida.
     */
    @Transactional
    public void remover(UUID pessoaId, PapelPessoa role) {
        pessoaRoleRepository.findByPessoaIdAndRoleAndAtivoTrueAndDeletedAtIsNull(pessoaId, role)
                .ifPresent(PessoaRole::excluirLogicamente);
    }

    /**
     * Lista os papeis ativos de uma Pessoa para consultas transversais.
     */
    @Transactional(readOnly = true)
    public Set<PapelPessoa> listarPapeis(UUID pessoaId) {
        return pessoaRoleRepository.findByPessoaIdAndAtivoTrueAndDeletedAtIsNull(pessoaId)
                .stream()
                .map(PessoaRole::getRole)
                .collect(Collectors.toSet());
    }
}
