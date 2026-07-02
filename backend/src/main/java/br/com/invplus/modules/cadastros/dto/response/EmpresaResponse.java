package br.com.invplus.modules.cadastros.dto.response;

import br.com.invplus.modules.cadastros.entity.Empresa;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Resposta resumida de empresa para a API.
 */
public record EmpresaResponse(
        UUID id,
        String razaoSocial,
        String nomeFantasia,
        String cnpj,
        String inscricaoEstadual,
        String telefone,
        String email,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
    /**
     * Converte a entidade JPA para o contrato publico da API.
     */
    public static EmpresaResponse from(Empresa empresa) {
        return new EmpresaResponse(
                empresa.getId(),
                empresa.getRazaoSocial(),
                empresa.getNomeFantasia(),
                empresa.getCnpj(),
                empresa.getInscricaoEstadual(),
                empresa.getTelefone(),
                empresa.getEmail(),
                empresa.getAtivo(),
                empresa.getCriadoEm(),
                empresa.getAtualizadoEm()
        );
    }
}
