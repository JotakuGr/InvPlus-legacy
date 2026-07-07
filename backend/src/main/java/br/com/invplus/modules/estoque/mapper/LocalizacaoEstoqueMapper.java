package br.com.invplus.modules.estoque.mapper;

import br.com.invplus.modules.estoque.dto.request.LocalizacaoEstoqueRequest;
import br.com.invplus.modules.estoque.dto.response.LocalizacaoEstoqueResponse;
import br.com.invplus.modules.estoque.entity.Deposito;
import br.com.invplus.modules.estoque.entity.LocalizacaoEstoque;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Converte LocalizacaoEstoque entre entidade e DTOs.
 */
@Component
public class LocalizacaoEstoqueMapper implements CrudMapper<LocalizacaoEstoque, LocalizacaoEstoqueRequest, LocalizacaoEstoqueResponse> {

    @Override
    public LocalizacaoEstoque novaEntidade() {
        return new LocalizacaoEstoque();
    }

    public LocalizacaoEstoque toEntity(LocalizacaoEstoqueRequest request) {
        LocalizacaoEstoque entity = novaEntidade();
        updateEntity(entity, request);
        return entity;
    }

    @Override
    public LocalizacaoEstoqueResponse toResponse(LocalizacaoEstoque entity) {
        Deposito deposito = entity.getDeposito();
        return new LocalizacaoEstoqueResponse(
                entity.getId(),
                deposito.getId(),
                deposito.getCodigo(),
                deposito.getNome(),
                entity.getCodigo(),
                entity.getDescricao(),
                entity.getRua(),
                entity.getCorredor(),
                entity.getPrateleira(),
                entity.getNivel(),
                entity.getPosicao(),
                entity.getPrincipal(),
                entity.getBloqueada(),
                entity.getAtivo(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }

    @Override
    public void aplicar(LocalizacaoEstoque entity, LocalizacaoEstoqueRequest request) {
        updateEntity(entity, request);
    }

    /**
     * Atualiza campos simples; deposito e resolvido no service.
     */
    public void updateEntity(LocalizacaoEstoque entity, LocalizacaoEstoqueRequest request) {
        entity.atualizar(
                request.codigo(),
                request.descricao(),
                request.rua(),
                request.corredor(),
                request.prateleira(),
                request.nivel(),
                request.posicao(),
                request.principal(),
                request.bloqueada(),
                request.observacao()
        );
    }
}
