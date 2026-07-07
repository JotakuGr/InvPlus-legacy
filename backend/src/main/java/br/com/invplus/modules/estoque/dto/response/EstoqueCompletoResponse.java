package br.com.invplus.modules.estoque.dto.response;

import java.util.List;

/**
 * Visao agregada do estoque para telas operacionais que precisam do saldo completo em uma chamada.
 */
public record EstoqueCompletoResponse(
        EstoqueResponse estoque,
        MovimentacaoEstoqueResponse ultimaMovimentacao,
        List<ReservaEstoqueResponse> reservasAtivas,
        EstoqueDisponivelResponse disponibilidade
) {
}
