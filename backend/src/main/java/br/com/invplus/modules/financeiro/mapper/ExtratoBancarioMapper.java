package br.com.invplus.modules.financeiro.mapper;

import br.com.invplus.modules.financeiro.dto.request.ExtratoBancarioRequest;
import br.com.invplus.modules.financeiro.dto.response.ExtratoBancarioResponse;
import br.com.invplus.modules.financeiro.entity.ContaBancaria;
import br.com.invplus.modules.financeiro.entity.ExtratoBancario;
import org.springframework.stereotype.Component;

/**
 * Converte extratos bancarios sem acessar repositories.
 */
@Component
public class ExtratoBancarioMapper {

    /**
     * Aplica o request em uma entidade nova de extrato.
     */
    public void aplicarImportacao(ExtratoBancario entity, ExtratoBancarioRequest request,
            ContaBancaria contaBancaria) {
        entity.importar(contaBancaria, request.dataInicio(), request.dataFim(), request.saldoInicial(),
                request.saldoFinal(), request.origem(), request.arquivoNome(), request.arquivoHash(),
                request.observacao());
    }

    /**
     * Monta a resposta com a descricao resumida da conta bancaria.
     */
    public ExtratoBancarioResponse toResponse(ExtratoBancario entity) {
        return new ExtratoBancarioResponse(
                entity.getId(),
                entity.getContaBancaria().getId(),
                entity.descricaoContaBancaria(),
                entity.getDataInicio(),
                entity.getDataFim(),
                entity.getSaldoInicial(),
                entity.getSaldoFinal(),
                entity.getOrigem(),
                entity.getArquivoNome(),
                entity.getArquivoHash(),
                entity.getImportadoEm(),
                entity.getStatus(),
                entity.getObservacao(),
                entity.getAtivo(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }
}
