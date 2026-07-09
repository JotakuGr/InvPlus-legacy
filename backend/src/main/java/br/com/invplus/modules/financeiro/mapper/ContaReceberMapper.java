package br.com.invplus.modules.financeiro.mapper;

import br.com.invplus.modules.financeiro.dto.request.ContaReceberRequest;
import br.com.invplus.modules.financeiro.dto.response.ContaReceberResponse;
import br.com.invplus.modules.financeiro.entity.ContaReceber;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Converte ContaReceber entre entidade JPA e DTOs da API.
 */
@Component
public class ContaReceberMapper implements CrudMapper<ContaReceber, ContaReceberRequest, ContaReceberResponse> {

    @Override
    public ContaReceber novaEntidade() {
        return new ContaReceber();
    }

    @Override
    public void aplicar(ContaReceber entity, ContaReceberRequest request) {
        throw new UnsupportedOperationException("ContaReceberService aplica os relacionamentos antes de persistir");
    }

    @Override
    public ContaReceberResponse toResponse(ContaReceber entity) {
        return new ContaReceberResponse(
                entity.getId(),
                entity.getEmpresa().getId(),
                entity.getEmpresa().getNomeFantasia(),
                entity.getFilial() == null ? null : entity.getFilial().getId(),
                entity.getFilial() == null ? null : entity.getFilial().getNome(),
                entity.getCliente().getId(),
                entity.getCliente().getPessoa().getNome(),
                entity.getVendaFaturamento() == null ? null : entity.getVendaFaturamento().getId(),
                entity.getCentroCusto() == null ? null : entity.getCentroCusto().getId(),
                entity.getCentroCusto() == null ? null : entity.getCentroCusto().getNome(),
                entity.getCentroCusto() == null ? null : entity.getCentroCusto().getCodigo(),
                entity.getPlanoConta() == null ? null : entity.getPlanoConta().getId(),
                entity.getPlanoConta() == null ? null : entity.getPlanoConta().getCodigo(),
                entity.getPlanoConta() == null ? null : entity.getPlanoConta().getNome(),
                entity.getPlanoConta() == null ? null : entity.getPlanoConta().getNatureza(),
                entity.getNumero(),
                entity.getDescricao(),
                entity.getDataEmissao(),
                entity.getValorTotal(),
                entity.getValorPago(),
                entity.getValorAberto(),
                entity.getStatus(),
                entity.getObservacao(),
                entity.getAtivo(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }
}
