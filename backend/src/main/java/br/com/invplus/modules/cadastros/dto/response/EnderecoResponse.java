package br.com.invplus.modules.cadastros.dto.response;

import br.com.invplus.modules.cadastros.entity.Endereco;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Resposta de endereco com o vinculo opcional da empresa.
 */
public record EnderecoResponse(
        UUID id,
        UUID empresaId,
        UUID cidadeId,
        String tipo,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf,
        String cep,
        Boolean principal,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
    /**
     * Converte a entidade JPA para o contrato publico da API.
     */
    public static EnderecoResponse from(Endereco endereco) {
        return new EnderecoResponse(
                endereco.getId(),
                endereco.getEmpresa() == null ? null : endereco.getEmpresa().getId(),
                endereco.getCidadeCadastro() == null ? null : endereco.getCidadeCadastro().getId(),
                endereco.getTipo(),
                endereco.getLogradouro(),
                endereco.getNumero(),
                endereco.getComplemento(),
                endereco.getBairro(),
                endereco.getCidade(),
                endereco.getUf(),
                endereco.getCep(),
                endereco.getPrincipal(),
                endereco.getAtivo(),
                endereco.getCriadoEm(),
                endereco.getAtualizadoEm()
        );
    }
}
