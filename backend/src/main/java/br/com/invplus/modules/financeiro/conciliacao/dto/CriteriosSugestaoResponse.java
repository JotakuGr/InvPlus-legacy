package br.com.invplus.modules.financeiro.conciliacao.dto;

import java.math.BigDecimal;

public record CriteriosSugestaoResponse(
        boolean valorIgual,
        boolean tipoCompativel,
        boolean mesmaContaBancaria,
        long diferencaDias,
        boolean documentoIgual,
        BigDecimal similaridadeDescricao
) {
}
