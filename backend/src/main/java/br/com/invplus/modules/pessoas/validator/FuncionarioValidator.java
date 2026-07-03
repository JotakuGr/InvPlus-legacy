package br.com.invplus.modules.pessoas.validator;

import br.com.invplus.modules.pessoas.dto.request.FuncionarioRequest;
import br.com.invplus.modules.pessoas.entity.Funcionario;
import br.com.invplus.modules.pessoas.repository.FuncionarioRepository;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ConflictException;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Valida regras funcionais de Funcionario antes da persistencia.
 */
@Component
@RequiredArgsConstructor
public class FuncionarioValidator {

    private final FuncionarioRepository funcionarioRepository;

    /**
     * Valida criacao garantindo Pessoa e Usuario unicos em funcionarios ativos.
     */
    public void validarCriacao(FuncionarioRequest request) {
        validarCampos(request);
        validarPessoaSemFuncionarioAtivo(null, request.pessoaId());
        validarUsuarioSemFuncionarioAtivo(null, request.usuarioId());
    }

    /**
     * Valida atualizacao permitindo manter os proprios vinculos atuais.
     */
    public void validarAtualizacao(UUID id, FuncionarioRequest request) {
        validarCampos(request);
        validarPessoaSemFuncionarioAtivo(id, request.pessoaId());
        validarUsuarioSemFuncionarioAtivo(id, request.usuarioId());
    }

    private void validarCampos(FuncionarioRequest request) {
        if (request.pessoaId() == null) {
            throw new BusinessException("Pessoa do funcionario e obrigatoria");
        }
        if (request.cargo() == null || request.cargo().isBlank()) {
            throw new BusinessException("Cargo do funcionario e obrigatorio");
        }
        if (request.salario() != null && request.salario().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Salario nao pode ser negativo");
        }
        if (request.dataAdmissao() == null) {
            throw new BusinessException("Data de admissao e obrigatoria");
        }
        if (request.dataDemissao() != null && request.dataAdmissao() != null
                && request.dataDemissao().isBefore(request.dataAdmissao())) {
            throw new BusinessException("Data de demissao nao pode ser anterior a admissao");
        }
    }

    private void validarPessoaSemFuncionarioAtivo(UUID idAtual, UUID pessoaId) {
        funcionarioRepository.findByPessoaIdAndAtivoTrueAndDeletedAtIsNull(pessoaId)
                .map(Funcionario::getId)
                .filter(idEncontrado -> !idEncontrado.equals(idAtual))
                .ifPresent(id -> {
                    throw new ConflictException("Pessoa ja possui funcionario ativo");
                });
    }

    private void validarUsuarioSemFuncionarioAtivo(UUID idAtual, UUID usuarioId) {
        if (usuarioId == null) {
            return;
        }
        funcionarioRepository.findByUsuarioIdAndAtivoTrueAndDeletedAtIsNull(usuarioId)
                .map(Funcionario::getId)
                .filter(idEncontrado -> !idEncontrado.equals(idAtual))
                .ifPresent(id -> {
                    throw new ConflictException("Usuario ja vinculado a funcionario ativo");
                });
    }
}
