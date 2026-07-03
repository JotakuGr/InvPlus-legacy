package br.com.invplus.modules.cadastros.mapper;

import br.com.invplus.modules.cadastros.dto.request.EmpresaRequest;
import br.com.invplus.modules.cadastros.dto.response.EmpresaResponse;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Converte Empresa entre entidade JPA e contratos REST.
 */
@Component
public class EmpresaMapper implements CrudMapper<Empresa, EmpresaRequest, EmpresaResponse> {

    /**
     * Cria uma entidade vazia para o fluxo padrao de cadastro.
     */
    @Override
    public Empresa novaEntidade() {
        return new Empresa();
    }

    /**
     * Cria uma entidade a partir do request quando o dominio precisar desse formato explicito.
     */
    public Empresa toEntity(EmpresaRequest request) {
        Empresa empresa = novaEntidade();
        updateEntity(empresa, request);
        return empresa;
    }

    /**
     * Aplica os dados do request na entidade gerenciada.
     */
    @Override
    public void aplicar(Empresa empresa, EmpresaRequest request) {
        updateEntity(empresa, request);
    }

    /**
     * Atualiza a entidade com normalizacao centralizada na propria entidade.
     */
    public void updateEntity(Empresa empresa, EmpresaRequest request) {
        empresa.atualizar(
                request.razaoSocial(),
                request.nomeFantasia(),
                request.cnpj(),
                request.inscricaoEstadual(),
                request.inscricaoMunicipal(),
                request.regimeTributario(),
                request.crt(),
                request.telefone(),
                request.email()
        );
    }

    /**
     * Converte a entidade para o contrato publico da API.
     */
    @Override
    public EmpresaResponse toResponse(Empresa empresa) {
        return new EmpresaResponse(
                empresa.getId(),
                empresa.getRazaoSocial(),
                empresa.getNomeFantasia(),
                empresa.getCnpj(),
                empresa.getRegimeTributario(),
                empresa.getCrt(),
                empresa.getTelefone(),
                empresa.getEmail(),
                empresa.getAtivo(),
                empresa.getCriadoEm(),
                empresa.getAtualizadoEm()
        );
    }
}
