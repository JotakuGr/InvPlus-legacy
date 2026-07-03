package br.com.invplus.modules.pessoas.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Dados retornados para o frontend com informacoes comerciais e resumo da Pessoa.
 */
public record FornecedorResponse(
        UUID id,
        UUID pessoaId,
        String nome,
        String cpfCnpj,
        String email,
        String telefone,
        UUID condicaoPagamentoId,
        Integer prazoMedioEntrega,
        String observacao,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
