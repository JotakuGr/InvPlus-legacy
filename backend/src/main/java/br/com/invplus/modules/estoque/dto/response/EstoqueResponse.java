package br.com.invplus.modules.estoque.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Dados retornados pela API para o saldo atual de estoque.
 */
public record EstoqueResponse(
        UUID id,
        UUID produtoId,
        String produtoCodigo,
        String produtoNome,
        UUID empresaId,
        String empresaNome,
        UUID filialId,
        String filialNome,
        UUID depositoId,
        String depositoCodigo,
        String depositoNome,
        UUID localizacaoId,
        String localizacaoCodigo,
        String localizacaoDescricao,
        BigDecimal quantidade,
        BigDecimal quantidadeReservada,
        BigDecimal quantidadeDisponivel,
        BigDecimal quantidadeAvariada,
        BigDecimal quantidadeTransito,
        BigDecimal estoqueMinimo,
        BigDecimal estoqueMaximo,
        OffsetDateTime ultimoMovimentoEm,
        UUID ultimoMovimentoId,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
