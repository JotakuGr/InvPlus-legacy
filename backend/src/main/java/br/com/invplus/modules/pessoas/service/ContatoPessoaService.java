package br.com.invplus.modules.pessoas.service;

import br.com.invplus.modules.pessoas.dto.request.ContatoPessoaRequest;
import br.com.invplus.modules.pessoas.dto.response.ContatoPessoaResponse;
import br.com.invplus.modules.pessoas.entity.ContatoPessoa;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.pessoas.mapper.ContatoPessoaMapper;
import br.com.invplus.modules.pessoas.repository.ContatoPessoaRepository;
import br.com.invplus.modules.pessoas.repository.PessoaRepository;
import br.com.invplus.modules.pessoas.validator.ContatoPessoaValidator;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import br.com.invplus.shared.domain.service.BaseCrudService;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio para contatos operacionais de Pessoa.
 */
@Service
public class ContatoPessoaService extends BaseCrudService<ContatoPessoa, ContatoPessoaRequest, ContatoPessoaResponse> {

    private final ContatoPessoaRepository contatoPessoaRepository;
    private final ContatoPessoaMapper contatoPessoaMapper;
    private final ContatoPessoaValidator contatoPessoaValidator;
    private final PessoaRepository pessoaRepository;

    public ContatoPessoaService(
            ContatoPessoaRepository contatoPessoaRepository,
            ContatoPessoaMapper contatoPessoaMapper,
            ContatoPessoaValidator contatoPessoaValidator,
            PessoaRepository pessoaRepository
    ) {
        super(contatoPessoaRepository, contatoPessoaMapper, "ContatoPessoa");
        this.contatoPessoaRepository = contatoPessoaRepository;
        this.contatoPessoaMapper = contatoPessoaMapper;
        this.contatoPessoaValidator = contatoPessoaValidator;
        this.pessoaRepository = pessoaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContatoPessoaResponse> listar(Pageable pageable) {
        return super.listar(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public ContatoPessoaResponse buscar(UUID id) {
        return super.buscar(id);
    }

    /**
     * Cria contato associado a uma Pessoa ativa.
     */
    @Override
    @Transactional
    public ContatoPessoaResponse criar(ContatoPessoaRequest request) {
        contatoPessoaValidator.validar(request);
        Pessoa pessoa = buscarPessoaAtiva(request.pessoaId());
        ContatoPessoa contato = contatoPessoaMapper.toEntity(request);
        contato.setPessoa(pessoa);
        return contatoPessoaMapper.toResponse(contatoPessoaRepository.save(contato));
    }

    /**
     * Atualiza contato e permite trocar a Pessoa vinculada quando necessario.
     */
    @Override
    @Transactional
    public ContatoPessoaResponse atualizar(UUID id, ContatoPessoaRequest request) {
        contatoPessoaValidator.validar(request);
        ContatoPessoa contato = buscarEntidade(id);
        contato.setPessoa(buscarPessoaAtiva(request.pessoaId()));
        contatoPessoaMapper.updateEntity(contato, request);
        return contatoPessoaMapper.toResponse(contato);
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
}
