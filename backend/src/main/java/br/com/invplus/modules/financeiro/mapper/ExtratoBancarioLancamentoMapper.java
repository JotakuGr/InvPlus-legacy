package br.com.invplus.modules.financeiro.mapper;

import br.com.invplus.modules.financeiro.dto.request.ExtratoBancarioLancamentoRequest;
import br.com.invplus.modules.financeiro.dto.response.ExtratoBancarioLancamentoResponse;
import br.com.invplus.modules.financeiro.entity.ExtratoBancario;
import br.com.invplus.modules.financeiro.entity.ExtratoBancarioLancamento;
import org.springframework.stereotype.Component;

/**
 * Converte lancamentos de extrato sem acessar repositories.
 */
@Component
public class ExtratoBancarioLancamentoMapper {

    /**
     * Aplica os dados do request em uma entidade carregada pelo service.
     */
    public void aplicar(ExtratoBancarioLancamento entity, ExtratoBancarioLancamentoRequest request,
            ExtratoBancario extratoBancario) {
        entity.atualizar(extratoBancario, request.dataLancamento(), request.descricao(), request.documento(),
                request.tipo(), request.valor(), request.saldoAposLancamento(), request.identificadorBancario(),
                request.categoriaBancaria(), request.observacao());
    }

    /**
     * Monta a resposta com dados resumidos do extrato e da conta bancaria.
     */
    public ExtratoBancarioLancamentoResponse toResponse(ExtratoBancarioLancamento entity) {
        ExtratoBancario extrato = entity.getExtratoBancario();
        return new ExtratoBancarioLancamentoResponse(
                entity.getId(),
                extrato.getId(),
                extrato.getContaBancaria().getId(),
                extrato.descricaoContaBancaria(),
                entity.getDataLancamento(),
                entity.getDescricao(),
                entity.getDocumento(),
                entity.getTipo(),
                entity.getValor(),
                entity.getSaldoAposLancamento(),
                entity.getIdentificadorBancario(),
                entity.getCategoriaBancaria(),
                entity.getConciliado(),
                entity.getObservacao(),
                entity.getAtivo(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }
}
