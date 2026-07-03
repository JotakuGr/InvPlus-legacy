package br.com.invplus.modules.pessoas.mapper;

import br.com.invplus.modules.pessoas.dto.request.PessoaRequest;
import br.com.invplus.modules.pessoas.dto.response.PessoaResponse;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Converte Pessoa entre entidade JPA e contratos REST.
 */
@Component
public class PessoaMapper implements CrudMapper<Pessoa, PessoaRequest, PessoaResponse> {

    /**
     * Cria entidade vazia para o fluxo base de CRUD.
     */
    @Override
    public Pessoa novaEntidade() {
        return new Pessoa();
    }

    /**
     * Cria uma entidade a partir do request quando necessario fora do CRUD base.
     */
    public Pessoa toEntity(PessoaRequest request) {
        Pessoa pessoa = novaEntidade();
        updateEntity(pessoa, request);
        return pessoa;
    }

    /**
     * Aplica o request na entidade gerenciada.
     */
    @Override
    public void aplicar(Pessoa pessoa, PessoaRequest request) {
        updateEntity(pessoa, request);
    }

    /**
     * Atualiza campos do dominio delegando normalizacao para a entidade.
     */
    public void updateEntity(Pessoa pessoa, PessoaRequest request) {
        pessoa.atualizar(
                request.tipoPessoa(),
                request.nome(),
                request.nomeFantasia(),
                request.cpfCnpj(),
                request.rgIe(),
                request.telefone(),
                request.celular(),
                request.email(),
                request.observacao()
        );
    }

    /**
     * Converte a entidade persistida para o contrato publico.
     */
    @Override
    public PessoaResponse toResponse(Pessoa pessoa) {
        return new PessoaResponse(
                pessoa.getId(),
                pessoa.getTipoPessoa(),
                pessoa.getNome(),
                pessoa.getNomeFantasia(),
                pessoa.getCpfCnpj(),
                pessoa.getRgIe(),
                pessoa.getTelefone(),
                pessoa.getCelular(),
                pessoa.getEmail(),
                pessoa.getObservacao(),
                pessoa.getAtivo(),
                pessoa.getCriadoEm(),
                pessoa.getAtualizadoEm()
        );
    }
}
