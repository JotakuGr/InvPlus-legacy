package br.com.invplus.modules.pessoas.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Dados retornados para o frontend com informacoes logisticas e resumo da Pessoa.
 */
public record TransportadoraResponse(
        UUID id,
        UUID pessoaId,
        String nome,
        String cpfCnpj,
        String email,
        String telefone,
        String antt,
        String tipoVeiculoPrincipal,
        BigDecimal capacidadeCargaKg,
        String observacao,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
