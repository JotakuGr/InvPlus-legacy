package br.com.invplus.modules.estoque.dto.response;

import br.com.invplus.modules.estoque.enums.TipoDeposito;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Dados de saida do cadastro de Deposito.
 */
public record DepositoResponse(
        UUID id,
        UUID empresaId,
        String empresaNome,
        UUID filialId,
        String filialNome,
        String codigo,
        String nome,
        String descricao,
        TipoDeposito tipoDeposito,
        Boolean principal,
        Boolean permiteVenda,
        Boolean permiteCompra,
        Boolean permiteTransferencia,
        Boolean controlaEstoque,
        Boolean bloqueado,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
