package br.com.invplus.modules.pessoas.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Dados retornados para o frontend com informacoes trabalhistas e resumo da Pessoa/Usuario.
 */
public record FuncionarioResponse(
        UUID id,
        UUID pessoaId,
        String nome,
        String cpfCnpj,
        String email,
        String telefone,
        UUID usuarioId,
        String usuarioEmail,
        String cargo,
        BigDecimal salario,
        LocalDate dataAdmissao,
        LocalDate dataDemissao,
        String observacao,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
