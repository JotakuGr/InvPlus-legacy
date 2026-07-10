package br.com.invplus.modules.financeiro.mapper;

import br.com.invplus.modules.financeiro.dto.response.ConciliacaoBancariaResponse;
import br.com.invplus.modules.financeiro.entity.ConciliacaoBancaria;
import br.com.invplus.modules.financeiro.entity.ExtratoBancarioLancamento;
import org.springframework.stereotype.Component;

/** Converte conciliacoes sem realizar consultas ao banco. */
@Component
public class ConciliacaoBancariaMapper {

    public ConciliacaoBancariaResponse toResponse(ConciliacaoBancaria entity) {
        ExtratoBancarioLancamento lancamento = entity.getExtratoBancarioLancamento();
        return new ConciliacaoBancariaResponse(
                entity.getId(),
                lancamento.getId(),
                lancamento.getDescricao(),
                entity.getMovimentoFinanceiro().getId(),
                entity.getMovimentoFinanceiro().getDescricao(),
                entity.getContaBancaria().getId(),
                lancamento.getExtratoBancario().descricaoContaBancaria(),
                entity.getTipoConciliacao(),
                entity.getStatus(),
                entity.getValorExtrato(),
                entity.getValorMovimento(),
                entity.getDiferenca(),
                entity.getConciliadoEm(),
                entity.getConciliadoPor(),
                entity.getCanceladoEm(),
                entity.getCanceladoPor(),
                entity.getMotivoCancelamento(),
                entity.getObservacao(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }
}
