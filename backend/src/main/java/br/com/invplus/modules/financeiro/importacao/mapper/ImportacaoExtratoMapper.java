package br.com.invplus.modules.financeiro.importacao.mapper;

import br.com.invplus.modules.financeiro.importacao.dto.ImportacaoExtratoResponse;
import br.com.invplus.modules.financeiro.importacao.dto.ImportacaoExtratoResumoResponse;
import br.com.invplus.modules.financeiro.importacao.entity.ImportacaoExtratoBancario;
import java.util.List;
import org.springframework.stereotype.Component;

/** Converte o processo persistido em respostas sem acessar repositories. */
@Component
public class ImportacaoExtratoMapper {

    public ImportacaoExtratoResponse toResponse(ImportacaoExtratoBancario entity, List<String> alertas) {
        return new ImportacaoExtratoResponse(
                entity.getId(),
                entity.getExtratoBancario() == null ? null : entity.getExtratoBancario().getId(),
                entity.getContaBancaria() == null ? null : entity.getContaBancaria().getId(),
                entity.getArquivoNome(), entity.getArquivoHash(), entity.getStatus(),
                entity.getQuantidadeLancamentosLidos(), entity.getQuantidadeLancamentosImportados(),
                entity.getQuantidadeLancamentosIgnorados(), List.copyOf(alertas));
    }

    public ImportacaoExtratoResumoResponse toResumo(ImportacaoExtratoBancario entity) {
        return new ImportacaoExtratoResumoResponse(
                entity.getId(),
                entity.getContaBancaria() == null ? null : entity.getContaBancaria().getId(),
                entity.getExtratoBancario() == null ? null : entity.getExtratoBancario().getId(),
                entity.getArquivoNome(), entity.getArquivoHash(), entity.getFormato(), entity.getStatus(),
                entity.getQuantidadeLancamentosLidos(), entity.getQuantidadeLancamentosImportados(),
                entity.getQuantidadeLancamentosIgnorados(), entity.getMensagemErro(), entity.getIniciadaEm(),
                entity.getFinalizadaEm(), entity.getCriadoEm(), entity.getAtualizadoEm());
    }
}
