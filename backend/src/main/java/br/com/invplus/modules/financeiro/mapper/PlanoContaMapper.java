package br.com.invplus.modules.financeiro.mapper;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.financeiro.dto.request.PlanoContaRequest;
import br.com.invplus.modules.financeiro.dto.response.PlanoContaResponse;
import br.com.invplus.modules.financeiro.entity.PlanoConta;
import org.springframework.stereotype.Component;

/**
 * Mapeia PlanoConta sem acessar a camada de persistencia.
 */
@Component
public class PlanoContaMapper {

    public void aplicar(PlanoConta entity, PlanoContaRequest request,
            Empresa empresa, PlanoConta planoContaPai) {
        entity.atualizar(empresa, planoContaPai, request.codigo(), request.nome(),
                request.descricao(), request.tipo(), request.natureza());
    }

    public PlanoContaResponse toResponse(PlanoConta entity) {
        PlanoConta pai = entity.getPlanoContaPai();
        return new PlanoContaResponse(
                entity.getId(),
                entity.getEmpresa().getId(),
                entity.getEmpresa().getNomeFantasia(),
                pai == null ? null : pai.getId(),
                pai == null ? null : pai.getNome(),
                entity.getCodigo(),
                entity.getNome(),
                entity.getDescricao(),
                entity.getTipo(),
                entity.getNatureza(),
                entity.caminhoCompleto(),
                entity.getAtivo(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }
}
