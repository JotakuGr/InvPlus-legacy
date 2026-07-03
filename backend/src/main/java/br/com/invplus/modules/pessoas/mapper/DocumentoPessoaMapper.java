package br.com.invplus.modules.pessoas.mapper;

import br.com.invplus.modules.cadastros.entity.Arquivo;
import br.com.invplus.modules.pessoas.dto.request.DocumentoPessoaRequest;
import br.com.invplus.modules.pessoas.dto.response.DocumentoPessoaResponse;
import br.com.invplus.modules.pessoas.entity.DocumentoPessoa;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Converte DocumentoPessoa entre entidade JPA e DTOs sem consultar banco.
 */
@Component
public class DocumentoPessoaMapper implements CrudMapper<DocumentoPessoa, DocumentoPessoaRequest, DocumentoPessoaResponse> {

    @Override
    public DocumentoPessoa novaEntidade() {
        return new DocumentoPessoa();
    }

    /**
     * Cria documento sem Pessoa ou Arquivo vinculados; o service associa os relacionamentos.
     */
    public DocumentoPessoa toEntity(DocumentoPessoaRequest request) {
        DocumentoPessoa documento = novaEntidade();
        updateEntity(documento, request);
        return documento;
    }

    @Override
    public DocumentoPessoaResponse toResponse(DocumentoPessoa documento) {
        Pessoa pessoa = documento.getPessoa();
        Arquivo arquivo = documento.getArquivo();
        return new DocumentoPessoaResponse(
                documento.getId(),
                pessoa.getId(),
                pessoa.getNome(),
                pessoa.getCpfCnpj(),
                arquivo == null ? null : arquivo.getId(),
                arquivo == null ? null : arquivo.getNomeOriginal(),
                documento.getTipoDocumento(),
                documento.getNumero(),
                documento.getOrgaoEmissor(),
                documento.getDataEmissao(),
                documento.getDataValidade(),
                documento.getObservacao(),
                documento.getAtivo(),
                documento.getCriadoEm(),
                documento.getAtualizadoEm()
        );
    }

    @Override
    public void aplicar(DocumentoPessoa documento, DocumentoPessoaRequest request) {
        updateEntity(documento, request);
    }

    /**
     * Atualiza apenas campos do documento.
     */
    public void updateEntity(DocumentoPessoa documento, DocumentoPessoaRequest request) {
        documento.atualizar(
                request.tipoDocumento(),
                request.numero(),
                request.orgaoEmissor(),
                request.dataEmissao(),
                request.dataValidade(),
                request.observacao()
        );
    }
}
