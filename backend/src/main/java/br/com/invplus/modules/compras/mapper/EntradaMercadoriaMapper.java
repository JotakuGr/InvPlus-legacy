package br.com.invplus.modules.compras.mapper;

import br.com.invplus.modules.compras.dto.request.EntradaMercadoriaRequest;
import br.com.invplus.modules.compras.dto.response.EntradaMercadoriaResponse;
import br.com.invplus.modules.compras.entity.EntradaMercadoria;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Mapper de entradas de mercadoria.
 */
@Component
public class EntradaMercadoriaMapper implements CrudMapper<EntradaMercadoria, EntradaMercadoriaRequest, EntradaMercadoriaResponse> {

    @Override
    public EntradaMercadoria novaEntidade() {
        return new EntradaMercadoria();
    }

    @Override
    public void aplicar(EntradaMercadoria entity, EntradaMercadoriaRequest request) {
        entity.atualizar(request.numero(), request.dataEntrada(), request.observacao());
    }

    public EntradaMercadoria toEntity(EntradaMercadoriaRequest request) {
        EntradaMercadoria entity = novaEntidade();
        aplicar(entity, request);
        return entity;
    }

    public void updateEntity(EntradaMercadoria entity, EntradaMercadoriaRequest request) {
        aplicar(entity, request);
    }

    @Override
    public EntradaMercadoriaResponse toResponse(EntradaMercadoria entity) {
        return new EntradaMercadoriaResponse(
                entity.getId(),
                entity.getEmpresa().getId(),
                entity.getEmpresa().getNomeFantasia(),
                entity.getFilial() == null ? null : entity.getFilial().getId(),
                entity.getFilial() == null ? null : entity.getFilial().getNome(),
                entity.getFornecedor().getId(),
                entity.getFornecedor().getPessoa().getNome(),
                entity.getPedidoCompra() == null ? null : entity.getPedidoCompra().getId(),
                entity.getNumero(),
                entity.getStatus(),
                entity.getDataEntrada(),
                entity.getObservacao(),
                entity.getValorTotal(),
                entity.getAtivo(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }
}
