package br.com.invplus.modules.pessoas.service;

import br.com.invplus.common.enums.PapelPessoa;
import br.com.invplus.modules.pessoas.dto.request.TransportadoraRequest;
import br.com.invplus.modules.pessoas.dto.response.TransportadoraResponse;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.pessoas.entity.Transportadora;
import br.com.invplus.modules.pessoas.mapper.TransportadoraMapper;
import br.com.invplus.modules.pessoas.repository.PessoaRepository;
import br.com.invplus.modules.pessoas.repository.TransportadoraRepository;
import br.com.invplus.modules.pessoas.validator.TransportadoraValidator;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import br.com.invplus.shared.domain.service.BaseCrudService;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio para o papel Transportadora.
 */
@Service
public class TransportadoraService extends BaseCrudService<Transportadora, TransportadoraRequest, TransportadoraResponse> {

    private final TransportadoraRepository transportadoraRepository;
    private final TransportadoraMapper transportadoraMapper;
    private final TransportadoraValidator transportadoraValidator;
    private final PessoaRepository pessoaRepository;
    private final PessoaRoleService pessoaRoleService;

    public TransportadoraService(
            TransportadoraRepository transportadoraRepository,
            TransportadoraMapper transportadoraMapper,
            TransportadoraValidator transportadoraValidator,
            PessoaRepository pessoaRepository,
            PessoaRoleService pessoaRoleService
    ) {
        super(transportadoraRepository, transportadoraMapper, "Transportadora");
        this.transportadoraRepository = transportadoraRepository;
        this.transportadoraMapper = transportadoraMapper;
        this.transportadoraValidator = transportadoraValidator;
        this.pessoaRepository = pessoaRepository;
        this.pessoaRoleService = pessoaRoleService;
    }

    /**
     * Lista transportadoras ativas com dados basicos da pessoa vinculada.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<TransportadoraResponse> listar(Pageable pageable) {
        return super.listar(pageable);
    }

    /**
     * Busca transportadora ativa pelo identificador.
     */
    @Override
    @Transactional(readOnly = true)
    public TransportadoraResponse buscar(UUID id) {
        return super.buscar(id);
    }

    /**
     * Cria transportadora associando uma pessoa ativa existente.
     */
    @Override
    @Transactional
    public TransportadoraResponse criar(TransportadoraRequest request) {
        transportadoraValidator.validarCriacao(request);
        Pessoa pessoa = buscarPessoaAtiva(request.pessoaId());
        Transportadora transportadora = transportadoraMapper.toEntity(request);
        transportadora.setPessoa(pessoa);
        Transportadora salvo = transportadoraRepository.save(transportadora);
        pessoaRoleService.registrar(pessoa, PapelPessoa.TRANSPORTADORA);
        return transportadoraMapper.toResponse(salvo);
    }

    /**
     * Atualiza transportadora e permite alterar a pessoa vinculada quando a nova pessoa ainda nao for transportadora.
     */
    @Override
    @Transactional
    public TransportadoraResponse atualizar(UUID id, TransportadoraRequest request) {
        transportadoraValidator.validarAtualizacao(id, request);
        Transportadora transportadora = buscarEntidade(id);
        UUID pessoaAnteriorId = transportadora.getPessoa().getId();
        Pessoa pessoa = buscarPessoaAtiva(request.pessoaId());
        transportadora.setPessoa(pessoa);
        transportadoraMapper.updateEntity(transportadora, request);
        sincronizarPessoaRole(pessoaAnteriorId, pessoa, PapelPessoa.TRANSPORTADORA);
        return transportadoraMapper.toResponse(transportadora);
    }

    /**
     * Exclui logicamente o papel Transportadora sem remover a Pessoa.
     */
    @Override
    @Transactional
    public void excluir(UUID id) {
        Transportadora transportadora = buscarEntidade(id);
        UUID pessoaId = transportadora.getPessoa().getId();
        transportadora.excluirLogicamente();
        pessoaRoleService.remover(pessoaId, PapelPessoa.TRANSPORTADORA);
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
