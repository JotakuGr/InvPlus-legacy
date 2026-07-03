package br.com.invplus.modules.pessoas.mapper;

import br.com.invplus.modules.pessoas.dto.request.FuncionarioRequest;
import br.com.invplus.modules.pessoas.dto.response.FuncionarioResponse;
import br.com.invplus.modules.pessoas.entity.Funcionario;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.security.entity.Usuario;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Converte Funcionario entre entidade JPA e DTOs sem consultar Pessoa ou Usuario.
 */
@Component
public class FuncionarioMapper implements CrudMapper<Funcionario, FuncionarioRequest, FuncionarioResponse> {

    /**
     * Cria entidade vazia para uso pelo fluxo especializado do service.
     */
    @Override
    public Funcionario novaEntidade() {
        return new Funcionario();
    }

    /**
     * Cria funcionario sem relacionamentos; o service associa Pessoa e Usuario.
     */
    public Funcionario toEntity(FuncionarioRequest request) {
        Funcionario funcionario = novaEntidade();
        updateEntity(funcionario, request);
        return funcionario;
    }

    /**
     * Monta resposta com dados trabalhistas e resumo da Pessoa/Usuario.
     */
    @Override
    public FuncionarioResponse toResponse(Funcionario funcionario) {
        Pessoa pessoa = funcionario.getPessoa();
        Usuario usuario = funcionario.getUsuario();
        return new FuncionarioResponse(
                funcionario.getId(),
                pessoa.getId(),
                pessoa.getNome(),
                pessoa.getCpfCnpj(),
                pessoa.getEmail(),
                pessoa.getTelefone(),
                usuario == null ? null : usuario.getId(),
                usuario == null ? null : usuario.getEmail(),
                funcionario.getCargo(),
                funcionario.getSalario(),
                funcionario.getDataAdmissao(),
                funcionario.getDataDemissao(),
                funcionario.getObservacao(),
                funcionario.getAtivo(),
                funcionario.getCriadoEm(),
                funcionario.getAtualizadoEm()
        );
    }

    /**
     * Aplica somente dados trabalhistas, mantendo relacionamentos sob responsabilidade do service.
     */
    @Override
    public void aplicar(Funcionario funcionario, FuncionarioRequest request) {
        updateEntity(funcionario, request);
    }

    /**
     * Atualiza campos trabalhistas informados no request.
     */
    public void updateEntity(Funcionario funcionario, FuncionarioRequest request) {
        funcionario.atualizar(
                request.cargo(),
                request.salario(),
                request.dataAdmissao(),
                request.dataDemissao(),
                request.observacao()
        );
    }
}
