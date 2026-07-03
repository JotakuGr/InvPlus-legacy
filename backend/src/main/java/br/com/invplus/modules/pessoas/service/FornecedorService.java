package br.com.invplus.modules.pessoas.service;

import br.com.invplus.common.enums.PapelPessoa;
import br.com.invplus.modules.pessoas.dto.request.FornecedorRequest;
import br.com.invplus.modules.pessoas.dto.response.FornecedorResponse;
import br.com.invplus.modules.pessoas.entity.Fornecedor;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.pessoas.mapper.FornecedorMapper;
import br.com.invplus.modules.pessoas.repository.FornecedorRepository;
import br.com.invplus.modules.pessoas.repository.PessoaRepository;
import br.com.invplus.modules.pessoas.validator.FornecedorValidator;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import br.com.invplus.shared.domain.service.BaseCrudService;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio para o papel comercial Fornecedor.
 */
@Service
public class FornecedorService extends BaseCrudService<Fornecedor, FornecedorRequest, FornecedorResponse> {

    private final FornecedorRepository fornecedorRepository;
    private final FornecedorMapper fornecedorMapper;
    private final FornecedorValidator fornecedorValidator;
    private final PessoaRepository pessoaRepository;
    private final PessoaRoleService pessoaRoleService;

    public FornecedorService(
            FornecedorRepository fornecedorRepository,
            FornecedorMapper fornecedorMapper,
            FornecedorValidator fornecedorValidator,
            PessoaRepository pessoaRepository,
            PessoaRoleService pessoaRoleService
    ) {
        super(fornecedorRepository, fornecedorMapper, "Fornecedor");
        this.fornecedorRepository = fornecedorRepository;
        this.fornecedorMapper = fornecedorMapper;
        this.fornecedorValidator = fornecedorValidator;
        this.pessoaRepository = pessoaRepository;
        this.pessoaRoleService = pessoaRoleService;
    }

    /**
     * Lista fornecedores ativos com dados basicos da pessoa vinculada.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<FornecedorResponse> listar(Pageable pageable) {
        return super.listar(pageable);
    }

    /**
     * Busca fornecedor ativo pelo identificador.
     */
    @Override
    @Transactional(readOnly = true)
    public FornecedorResponse buscar(UUID id) {
        return super.buscar(id);
    }

    /**
     * Cria fornecedor associando uma pessoa ativa existente.
     */
    @Override
    @Transactional
    public FornecedorResponse criar(FornecedorRequest request) {
        fornecedorValidator.validarCriacao(request);
        Pessoa pessoa = buscarPessoaAtiva(request.pessoaId());
        Fornecedor fornecedor = fornecedorMapper.toEntity(request);
        fornecedor.setPessoa(pessoa);
        Fornecedor salvo = fornecedorRepository.save(fornecedor);
        pessoaRoleService.registrar(pessoa, PapelPessoa.FORNECEDOR);
        return fornecedorMapper.toResponse(salvo);
    }

    /**
     * Atualiza fornecedor e permite alterar a pessoa vinculada quando a nova pessoa ainda nao for fornecedor.
     */
    @Override
    @Transactional
    public FornecedorResponse atualizar(UUID id, FornecedorRequest request) {
        fornecedorValidator.validarAtualizacao(id, request);
        Fornecedor fornecedor = buscarEntidade(id);
        UUID pessoaAnteriorId = fornecedor.getPessoa().getId();
        Pessoa pessoa = buscarPessoaAtiva(request.pessoaId());
        fornecedor.setPessoa(pessoa);
        fornecedorMapper.updateEntity(fornecedor, request);
        sincronizarPessoaRole(pessoaAnteriorId, pessoa, PapelPessoa.FORNECEDOR);
        return fornecedorMapper.toResponse(fornecedor);
    }

    /**
     * Exclui logicamente o papel Fornecedor sem remover a Pessoa.
     */
    @Override
    @Transactional
    public void excluir(UUID id) {
        Fornecedor fornecedor = buscarEntidade(id);
        UUID pessoaId = fornecedor.getPessoa().getId();
        fornecedor.excluirLogicamente();
        pessoaRoleService.remover(pessoaId, PapelPessoa.FORNECEDOR);
    }

    private Pessoa buscarPessoaAtiva(UUID pessoaId) {
        return pessoaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(pessoaId)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa nao encontrada"));
    }

    private void sincronizarPessoaRole(UUID pessoaAnteriorId, Pessoa pessoaAtual, PapelPessoa papel) {
        if (!pessoaAnteriorId.equals(pessoaAtual.getId())) {
            pessoaRoleService.remover(pessoaAnteriorId, papel);
            pessoaRoleService.registrar(pessoaAtual, papel);
        }
    }
}
