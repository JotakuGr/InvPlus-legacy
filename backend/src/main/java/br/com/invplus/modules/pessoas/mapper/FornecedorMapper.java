package br.com.invplus.modules.pessoas.mapper;

import br.com.invplus.modules.pessoas.dto.request.FornecedorRequest;
import br.com.invplus.modules.pessoas.dto.response.FornecedorResponse;
import br.com.invplus.modules.pessoas.entity.Fornecedor;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Converte Fornecedor entre entidade JPA e DTOs sem buscar Pessoa no banco.
 */
@Component
public class FornecedorMapper implements CrudMapper<Fornecedor, FornecedorRequest, FornecedorResponse> {

    /**
     * Cria entidade vazia para uso pelo fluxo especializado do service.
     */
    @Override
    public Fornecedor novaEntidade() {
        return new Fornecedor();
    }

    /**
     * Cria fornecedor sem pessoa vinculada; o service deve associar a Pessoa.
     */
    public Fornecedor toEntity(FornecedorRequest request) {
        Fornecedor fornecedor = novaEntidade();
        fornecedor.atualizar(request.condicaoPagamentoId(), request.prazoMedioEntrega(), request.observacao());
        return fornecedor;
    }

    /**
     * Monta resposta com dados comerciais e resumo da Pessoa para facilitar o frontend.
     */
    @Override
    public FornecedorResponse toResponse(Fornecedor fornecedor) {
        Pessoa pessoa = fornecedor.getPessoa();
        return new FornecedorResponse(
                fornecedor.getId(),
                pessoa.getId(),
                pessoa.getNome(),
                pessoa.getCpfCnpj(),
                pessoa.getEmail(),
                pessoa.getTelefone(),
                fornecedor.getCondicaoPagamentoId(),
                fornecedor.getPrazoMedioEntrega(),
                fornecedor.getObservacao(),
                fornecedor.getAtivo(),
                fornecedor.getCriadoEm(),
                fornecedor.getAtualizadoEm()
        );
    }

    /**
     * Atualiza somente dados do papel Fornecedor, mantendo Pessoa sob responsabilidade do Service.
     */
    @Override
    public void aplicar(Fornecedor fornecedor, FornecedorRequest request) {
        updateEntity(fornecedor, request);
    }

    /**
     * Atualiza os campos comerciais informados no request.
     */
    public void updateEntity(Fornecedor fornecedor, FornecedorRequest request) {
        fornecedor.atualizar(request.condicaoPagamentoId(), request.prazoMedioEntrega(), request.observacao());
    }
}
