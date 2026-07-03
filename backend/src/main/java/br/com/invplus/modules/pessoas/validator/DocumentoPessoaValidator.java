package br.com.invplus.modules.pessoas.validator;

import br.com.invplus.modules.pessoas.dto.request.DocumentoPessoaRequest;
import br.com.invplus.modules.pessoas.entity.DocumentoPessoa;
import br.com.invplus.modules.pessoas.repository.DocumentoPessoaRepository;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ConflictException;
import org.springframework.stereotype.Component;

/**
 * Valida regras funcionais de DocumentoPessoa.
 */
@Component
public class DocumentoPessoaValidator {

    private final DocumentoPessoaRepository documentoPessoaRepository;

    public DocumentoPessoaValidator(DocumentoPessoaRepository documentoPessoaRepository) {
        this.documentoPessoaRepository = documentoPessoaRepository;
    }

    /**
     * Valida criacao de documento e duplicidade ativa para a mesma Pessoa.
     */
    public void validarCriacao(DocumentoPessoaRequest request) {
        validarCampos(request);
        if (documentoPessoaRepository.existsByPessoaIdAndTipoDocumentoAndNumeroAndAtivoTrueAndDeletedAtIsNull(
                request.pessoaId(),
                request.tipoDocumento(),
                request.numero().trim()
        )) {
            throw new ConflictException("Pessoa ja possui documento ativo com este tipo e numero");
        }
    }

    /**
     * Valida atualizacao permitindo manter o proprio documento.
     */
    public void validarAtualizacao(DocumentoPessoa documento, DocumentoPessoaRequest request) {
        validarCampos(request);
        boolean mudouChave = !documento.getPessoa().getId().equals(request.pessoaId())
                || documento.getTipoDocumento() != request.tipoDocumento()
                || !documento.getNumero().equals(request.numero().trim());
        if (mudouChave && documentoPessoaRepository.existsByPessoaIdAndTipoDocumentoAndNumeroAndAtivoTrueAndDeletedAtIsNull(
                request.pessoaId(),
                request.tipoDocumento(),
                request.numero().trim()
        )) {
            throw new ConflictException("Pessoa ja possui documento ativo com este tipo e numero");
        }
    }

    private void validarCampos(DocumentoPessoaRequest request) {
        if (request.pessoaId() == null) {
            throw new BusinessException("Pessoa do documento e obrigatoria");
        }
        if (request.tipoDocumento() == null) {
            throw new BusinessException("Tipo do documento e obrigatorio");
        }
        if (request.numero() == null || request.numero().isBlank()) {
            throw new BusinessException("Numero do documento e obrigatorio");
        }
        if (request.dataEmissao() != null && request.dataValidade() != null
                && request.dataValidade().isBefore(request.dataEmissao())) {
            throw new BusinessException("Data de validade nao pode ser anterior a data de emissao");
        }
    }
}
