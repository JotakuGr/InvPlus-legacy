package br.com.invplus.modules.pessoas.service;

import br.com.invplus.modules.pessoas.dto.request.PessoaRequest;
import br.com.invplus.modules.pessoas.dto.response.PessoaResponse;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.pessoas.mapper.PessoaMapper;
import br.com.invplus.modules.pessoas.repository.PessoaRepository;
import br.com.invplus.modules.pessoas.validator.PessoaValidator;
import br.com.invplus.shared.domain.service.BaseCrudService;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio para cadastro base de pessoas.
 */
@Service
public class PessoaService extends BaseCrudService<Pessoa, PessoaRequest, PessoaResponse> {

    private final PessoaValidator pessoaValidator;

    public PessoaService(PessoaRepository pessoaRepository, PessoaMapper pessoaMapper, PessoaValidator pessoaValidator) {
        super(pessoaRepository, pessoaMapper, "Pessoa");
        this.pessoaValidator = pessoaValidator;
    }

    /**
     * Lista pessoas ativas com paginacao obrigatoria.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<PessoaResponse> listar(Pageable pageable) {
        return super.listar(pageable);
    }

    /**
     * Busca uma pessoa ativa pelo identificador.
     */
    @Override
    @Transactional(readOnly = true)
    public PessoaResponse buscar(UUID id) {
        return super.buscar(id);
    }

    /**
     * Cria uma pessoa fisica ou juridica apos validar o documento.
     */
    @Override
    @Transactional
    public PessoaResponse criar(PessoaRequest request) {
        pessoaValidator.validarCriacao(request);
        return super.criar(request);
    }

    /**
     * Atualiza uma pessoa existente mantendo a unicidade do documento.
     */
    @Override
    @Transactional
    public PessoaResponse atualizar(UUID id, PessoaRequest request) {
        pessoaValidator.validarAtualizacao(id, request);
        return super.atualizar(id, request);
    }

    /**
     * Exclui logicamente a pessoa sem remover historico.
     */
    @Override
    @Transactional
    public void excluir(UUID id) {
        super.excluir(id);
    }
}
