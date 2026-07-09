package br.com.invplus.modules.financeiro.mapper;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.financeiro.dto.request.CentroCustoRequest;
import br.com.invplus.modules.financeiro.dto.response.CentroCustoResponse;
import br.com.invplus.modules.financeiro.entity.CentroCusto;
import org.springframework.stereotype.Component;

/**
 * Mapeia CentroCusto sem executar consultas de persistencia.
 */
@Component
public class CentroCustoMapper {

    public void aplicar(CentroCusto entity, CentroCustoRequest request,
            Empresa empresa, CentroCusto centroCustoPai) {
        entity.atualizar(empresa, centroCustoPai, request.codigo(), request.nome(),
                request.descricao(), request.tipo());
    }

    public CentroCustoResponse toResponse(CentroCusto entity) {
        CentroCusto pai = entity.getCentroCustoPai();
        return new CentroCustoResponse(
                entity.getId(),
                entity.getEmpresa().getId(),
                entity.getEmpresa().getNomeFantasia(),
                pai == null ? null : pai.getId(),
                pai == null ? null : pai.getNome(),
                entity.getCodigo(),
                entity.getNome(),
                entity.getDescricao(),
                entity.getTipo(),
                entity.caminhoCompleto(),
                entity.getAtivo(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }
}
