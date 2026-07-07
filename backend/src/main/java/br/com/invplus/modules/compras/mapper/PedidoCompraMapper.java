package br.com.invplus.modules.compras.mapper;

import br.com.invplus.modules.compras.dto.request.PedidoCompraRequest;
import br.com.invplus.modules.compras.dto.response.PedidoCompraResponse;
import br.com.invplus.modules.compras.entity.PedidoCompra;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Converte PedidoCompra entre entidade JPA, request e response.
 */
@Component
public class PedidoCompraMapper implements CrudMapper<PedidoCompra, PedidoCompraRequest, PedidoCompraResponse> {

    @Override
    public PedidoCompra novaEntidade() {
        return new PedidoCompra();
    }

    @Override
    public void aplicar(PedidoCompra entity, PedidoCompraRequest request) {
        entity.atualizar(request.numero(), request.dataEmissao(), request.dataPrevistaEntrega(), request.observacao());
    }

    public PedidoCompra toEntity(PedidoCompraRequest request) {
        PedidoCompra entity = novaEntidade();
        aplicar(entity, request);
        return entity;
    }

    public void updateEntity(PedidoCompra entity, PedidoCompraRequest request) {
        aplicar(entity, request);
    }

    @Override
    public PedidoCompraResponse toResponse(PedidoCompra entity) {
        return new PedidoCompraResponse(
                entity.getId(),
                entity.getEmpresa().getId(),
                entity.getEmpresa().getNomeFantasia(),
                entity.getFilial() == null ? null : entity.getFilial().getId(),
                entity.getFilial() == null ? null : entity.getFilial().getNome(),
                entity.getFornecedor().getId(),
                entity.getFornecedor().getPessoa().getNome(),
                entity.getFornecedor().getPessoa().getCpfCnpj(),
                entity.getNumero(),
                entity.getStatus(),
                entity.getDataEmissao(),
                entity.getDataPrevistaEntrega(),
                entity.getObservacao(),
                entity.getValorTotal(),
                entity.getNivelAprovacao(),
                entity.getAprovadoPor(),
                entity.getAprovadoEm(),
                entity.getAtivo(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }
}
