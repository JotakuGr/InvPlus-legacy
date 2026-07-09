package br.com.invplus.modules.financeiro.mapper;

import br.com.invplus.modules.financeiro.dto.request.ContaPagarRequest;
import br.com.invplus.modules.financeiro.dto.response.ContaPagarResponse;
import br.com.invplus.modules.financeiro.entity.ContaPagar;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Converte ContaPagar entre entidade JPA e DTOs.
 */
@Component
public class ContaPagarMapper implements CrudMapper<ContaPagar, ContaPagarRequest, ContaPagarResponse> {

    @Override
    public ContaPagar novaEntidade() {
        return new ContaPagar();
    }

    @Override
    public void aplicar(ContaPagar entity, ContaPagarRequest request) {
        throw new UnsupportedOperationException("ContaPagarService aplica os relacionamentos antes de persistir");
    }

    @Override
    public ContaPagarResponse toResponse(ContaPagar entity) {
        return new ContaPagarResponse(
                entity.getId(),
                entity.getEmpresa().getId(),
                entity.getEmpresa().getNomeFantasia(),
                entity.getFilial() == null ? null : entity.getFilial().getId(),
                entity.getFilial() == null ? null : entity.getFilial().getNome(),
                entity.getFornecedor().getId(),
                entity.getFornecedor().getPessoa().getNome(),
                entity.getEntradaMercadoria() == null ? null : entity.getEntradaMercadoria().getId(),
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
