package br.com.invplus.modules.compras.dto.response;

import br.com.invplus.modules.estoque.dto.response.MovimentacaoEstoqueResponse;
import java.util.List;

/**
 * Visao agregada da entrada com itens, movimentacoes e historico de conferencia.
 */
public record EntradaMercadoriaCompletaResponse(
        EntradaMercadoriaResponse entrada,
        List<EntradaMercadoriaItemResponse> itens,
        List<MovimentacaoEstoqueResponse> movimentacoesGeradas,
        List<EntradaMercadoriaHistoricoResponse> historico
) {
}
