package br.com.invplus.modules.pessoas.service;

import br.com.invplus.modules.cadastros.entity.Arquivo;
import br.com.invplus.modules.cadastros.repository.ArquivoRepository;
import br.com.invplus.modules.pessoas.dto.request.DocumentoPessoaRequest;
import br.com.invplus.modules.pessoas.dto.response.DocumentoPessoaResponse;
import br.com.invplus.modules.pessoas.entity.DocumentoPessoa;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.pessoas.mapper.DocumentoPessoaMapper;
import br.com.invplus.modules.pessoas.repository.DocumentoPessoaRepository;
import br.com.invplus.modules.pessoas.repository.PessoaRepository;
import br.com.invplus.modules.pessoas.validator.DocumentoPessoaValidator;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import br.com.invplus.shared.domain.service.BaseCrudService;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio para documentos vinculados a Pessoa.
 */
@Service
public class DocumentoPessoaService extends BaseCrudService<DocumentoPessoa, DocumentoPessoaRequest, DocumentoPessoaResponse> {

    private final DocumentoPessoaRepository documentoPessoaRepository;
    private final DocumentoPessoaMapper documentoPessoaMapper;
    private final DocumentoPessoaValidator documentoPessoaValidator;
    private final PessoaRepository pessoaRepository;
    private final ArquivoRepository arquivoRepository;

    public DocumentoPessoaService(
            DocumentoPessoaRepository documentoPessoaRepository,
            DocumentoPessoaMapper documentoPessoaMapper,
            DocumentoPessoaValidator documentoPessoaValidator,
            PessoaRepository pessoaRepository,
            ArquivoRepository arquivoRepository
    ) {
        super(documentoPessoaRepository, documentoPessoaMapper, "DocumentoPessoa");
        this.documentoPessoaRepository = documentoPessoaRepository;
        this.documentoPessoaMapper = documentoPessoaMapper;
        this.documentoPessoaValidator = documentoPessoaValidator;
        this.pessoaRepository = pessoaRepository;
        this.arquivoRepository = arquivoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentoPessoaResponse> listar(Pageable pageable) {
        return super.listar(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentoPessoaResponse buscar(UUID id) {
        return super.buscar(id);
    }

    /**
     * Cria documento associado a Pessoa ativa e, opcionalmente, a Arquivo ativo.
     */
    @Override
    @Transactional
    public DocumentoPessoaResponse criar(DocumentoPessoaRequest request) {
        documentoPessoaValidator.validarCriacao(request);
        Pessoa pessoa = buscarPessoaAtiva(request.pessoaId());
        Arquivo arquivo = buscarArquivoAtivoSeInformado(request.arquivoId());
        DocumentoPessoa documento = documentoPessoaMapper.toEntity(request);
        documento.setPessoa(pessoa);
        documento.setArquivo(arquivo);
        return documentoPessoaMapper.toResponse(documentoPessoaRepository.save(documento));
    }

    /**
     * Atualiza documento e permite trocar Pessoa ou Arquivo vinculados quando necessario.
     */
    @Override
    @Transactional
    public DocumentoPessoaResponse atualizar(UUID id, DocumentoPessoaRequest request) {
        DocumentoPessoa documento = buscarEntidade(id);
        documentoPessoaValidator.validarAtualizacao(documento, request);
        documento.setPessoa(buscarPessoaAtiva(request.pessoaId()));
        documento.setArquivo(buscarArquivoAtivoSeInformado(request.arquivoId()));
        documentoPessoaMapper.updateEntity(documento, request);
        return documentoPessoaMapper.toResponse(documento);
    }

    @Override
    @Transactional
    public void excluir(UUID id) {
        super.excluir(id);
    }

    private Pessoa buscarPessoaAtiva(UUID pessoaId) {
        return pessoaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(pessoaId)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa nao encontrada"));
    }

    private Arquivo buscarArquivoAtivoSeInformado(UUID arquivoId) {
        if (arquivoId == null) {
            return null;
        }
        return arquivoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(arquivoId)
                .orElseThrow(() -> new ResourceNotFoundException("Arquivo nao encontrado"));
    }
}
