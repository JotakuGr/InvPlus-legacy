package br.com.invplus.modules.financeiro.importacao.dto;

import java.util.UUID;

/** Metadados opcionais recebidos junto ao multipart OFX. */
public record OfxImportacaoRequest(UUID contaBancariaId, String observacao) {
}
