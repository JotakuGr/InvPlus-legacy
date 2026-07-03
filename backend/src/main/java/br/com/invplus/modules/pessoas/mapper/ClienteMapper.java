package br.com.invplus.modules.pessoas.mapper;

import br.com.invplus.modules.pessoas.dto.request.ClienteRequest;
import br.com.invplus.modules.pessoas.dto.response.ClienteResponse;
import br.com.invplus.modules.pessoas.entity.Cliente;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Converte Cliente entre entidade JPA e contratos REST sem buscar Pessoa no banco.
 */
@Component
public class ClienteMapper implements CrudMapper<Cliente, ClienteRequest, ClienteResponse> {

    /**
     * Cria entidade vazia para uso pelo fluxo especializado do service.
     */
    @Override
    public Cliente novaEntidade() {
        return new Cliente();
    }

    /**
     * Cria cliente sem pessoa vinculada; o service deve associar a Pessoa.
     */
    public Cliente toEntity(ClienteRequest request) {
        Cliente cliente = novaEntidade();
        updateEntity(cliente, request);
        return cliente;
    }

    /**
     * Aplica somente dados comerciais, sem resolver relacionamento com Pessoa.
     */
    @Override
    public void aplicar(Cliente cliente, ClienteRequest request) {
        updateEntity(cliente, request);
    }

    /**
     * Atualiza os campos comerciais informados no request.
     */
    public void updateEntity(Cliente cliente, ClienteRequest request) {
        cliente.atualizar(
                request.limiteCredito(),
                request.condicaoPagamentoId(),
                request.tabelaPrecoId(),
                request.observacao()
        );
    }

    /**
     * Converte o cliente para resposta incluindo dados basicos da pessoa.
     */
    @Override
    public ClienteResponse toResponse(Cliente cliente) {
        Pessoa pessoa = cliente.getPessoa();
        return new ClienteResponse(
                cliente.getId(),
                pessoa.getId(),
                pessoa.getNome(),
                pessoa.getCpfCnpj(),
                pessoa.getEmail(),
                pessoa.getTelefone(),
                cliente.getLimiteCredito(),
                cliente.getCondicaoPagamentoId(),
                cliente.getTabelaPrecoId(),
                cliente.getObservacao(),
                cliente.getAtivo(),
                cliente.getCriadoEm(),
                cliente.getAtualizadoEm()
        );
    }
}
