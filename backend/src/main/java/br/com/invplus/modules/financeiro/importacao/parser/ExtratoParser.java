package br.com.invplus.modules.financeiro.importacao.parser;

import br.com.invplus.modules.financeiro.importacao.parser.dto.ExtratoImportado;

/** Contrato comum para parsers de arquivos bancarios. */
public interface ExtratoParser {

    boolean suporta(String nomeArquivo, String contentType);

    ExtratoImportado processar(byte[] conteudo);
}
