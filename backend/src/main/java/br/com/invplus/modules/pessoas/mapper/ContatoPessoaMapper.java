package br.com.invplus.modules.pessoas.mapper;

import br.com.invplus.modules.pessoas.dto.request.ContatoPessoaRequest;
import br.com.invplus.modules.pessoas.dto.response.ContatoPessoaResponse;
import br.com.invplus.modules.pessoas.entity.ContatoPessoa;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Converte ContatoPessoa entre entidade JPA e DTOs sem buscar Pessoa no banco.
 */
@Component
public class ContatoPessoaMapper implements CrudMapper<ContatoPessoa, ContatoPessoaRequest, ContatoPessoaResponse> {

    @Override
    public ContatoPessoa novaEntidade() {
        return new ContatoPessoa();
    }

    /**
     * Cria contato sem pessoa vinculada; o service associa a Pessoa.
     */
    public ContatoPessoa toEntity(ContatoPessoaRequest request) {
        ContatoPessoa contato = novaEntidade();
        updateEntity(contato, request);
        return contato;
    }

    @Override
    public ContatoPessoaResponse toResponse(ContatoPessoa contato) {
        Pessoa pessoa = contato.getPessoa();
        return new ContatoPessoaResponse(
                contato.getId(),
                pessoa.getId(),
                pessoa.getNome(),
                contato.getNome(),
                contato.getTelefone(),
                contato.getCelular(),
                contato.getEmail(),
                contato.getCargo(),
                contato.getTipo(),
                contato.getPrincipal(),
                contato.getObservacao(),
                contato.getAtivo(),
                contato.getCriadoEm(),
                contato.getAtualizadoEm()
        );
    }

    @Override
    public void aplicar(ContatoPessoa contato, ContatoPessoaRequest request) {
        updateEntity(contato, request);
    }

    /**
     * Atualiza apenas dados do contato.
     */
    public void updateEntity(ContatoPessoa contato, ContatoPessoaRequest request) {
        contato.atualizar(
                request.nome(),
                request.telefone(),
                request.celular(),
                request.email(),
                request.cargo(),
                request.tipo(),
                request.principal(),
                request.observacao()
        );
    }
}
