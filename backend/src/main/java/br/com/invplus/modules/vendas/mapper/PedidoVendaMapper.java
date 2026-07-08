package br.com.invplus.modules.vendas.mapper;

import br.com.invplus.modules.vendas.dto.request.PedidoVendaRequest;
import br.com.invplus.modules.vendas.dto.response.PedidoVendaResponse;
import br.com.invplus.modules.vendas.entity.PedidoVenda;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Converte PedidoVenda entre entidade JPA, request e response.
 */
@Component
public class PedidoVendaMapper implements CrudMapper<PedidoVenda, PedidoVendaRequest, PedidoVendaResponse> {

    @Override
    public PedidoVenda novaEntidade() {
        return new PedidoVenda();
    }

    @Override
    public void aplicar(PedidoVenda entity, PedidoVendaRequest request) {
        entity.atualizar(
                request.numero(),
                request.dataEmissao(),
                request.dataEntregaPrevista(),
                request.desconto(),
                request.acrescimo(),
                request.observacao()
        );
    }

    public PedidoVenda toEntity(PedidoVendaRequest request) {
        PedidoVenda entity = novaEntidade();
        aplicar(entity, request);
        return entity;
    }

    public void updateEntity(PedidoVenda entity, PedidoVendaRequest request) {
        aplicar(entity, request);
    }

    @Override
    public PedidoVendaResponse toResponse(PedidoVenda entity) {
        return new PedidoVendaResponse(
                entity.getId(),
                entity.getEmpresa().getId(),
                entity.getEmpresa().getNomeFantasia(),
                entity.getFilial() == null ? null : entity.getFilial().getId(),
                entity.getFilial() == null ? null : entity.getFilial().getNome(),
                entity.getCliente().getId(),
                entity.getCliente().getPessoa().getNome(),
                entity.getVendedor() == null ? null : entity.getVendedor().getId(),
                entity.getVendedor() == null ? null : entity.getVendedor().getPessoa().getNome(),
                entity.getNumero(),
                entity.getStatus(),
                entity.getDataEmissao(),
                entity.getDataEntregaPrevista(),
                entity.getValorTotal(),
                entity.getDesconto(),
                entity.getAcrescimo(),
                entity.getObservacao(),
                entity.getNivelAprovacao(),
                entity.getAprovadoPor(),
                entity.getAprovadoEm(),
                entity.getCanceladoEm(),
                entity.getAtivo(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }
}
