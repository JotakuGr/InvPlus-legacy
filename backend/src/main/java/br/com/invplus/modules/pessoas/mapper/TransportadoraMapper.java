package br.com.invplus.modules.pessoas.mapper;

import br.com.invplus.modules.pessoas.dto.request.TransportadoraRequest;
import br.com.invplus.modules.pessoas.dto.response.TransportadoraResponse;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.pessoas.entity.Transportadora;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Converte Transportadora entre entidade JPA e DTOs sem buscar Pessoa no banco.
 */
@Component
public class TransportadoraMapper implements CrudMapper<Transportadora, TransportadoraRequest, TransportadoraResponse> {

    /**
     * Cria entidade vazia para uso pelo fluxo especializado do service.
     */
    @Override
    public Transportadora novaEntidade() {
        return new Transportadora();
    }

    /**
     * Cria transportadora sem pessoa vinculada; o service deve associar a Pessoa.
     */
    public Transportadora toEntity(TransportadoraRequest request) {
        Transportadora transportadora = novaEntidade();
        updateEntity(transportadora, request);
        return transportadora;
    }

    /**
     * Monta resposta com dados logisticos e resumo da Pessoa para facilitar o frontend.
     */
    @Override
    public TransportadoraResponse toResponse(Transportadora transportadora) {
        Pessoa pessoa = transportadora.getPessoa();
        return new TransportadoraResponse(
                transportadora.getId(),
                pessoa.getId(),
                pessoa.getNome(),
                pessoa.getCpfCnpj(),
                pessoa.getEmail(),
                pessoa.getTelefone(),
                transportadora.getAntt(),
                transportadora.getTipoVeiculoPrincipal(),
                transportadora.getCapacidadeCargaKg(),
                transportadora.getObservacao(),
                transportadora.getAtivo(),
                transportadora.getCriadoEm(),
                transportadora.getAtualizadoEm()
        );
    }

    /**
     * Aplica somente dados logisticos, mantendo Pessoa sob responsabilidade do service.
     */
    @Override
    public void aplicar(Transportadora transportadora, TransportadoraRequest request) {
        updateEntity(transportadora, request);
    }

    /**
     * Atualiza os campos logisticos informados no request.
     */
    public void updateEntity(Transportadora transportadora, TransportadoraRequest request) {
        transportadora.atualizar(
                request.antt(),
                request.tipoVeiculoPrincipal(),
                request.capacidadeCargaKg(),
                request.observacao()
        );
    }
}
