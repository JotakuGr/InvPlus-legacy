package br.com.invplus.modules.pessoas.service;

import br.com.invplus.common.enums.PapelPessoa;
import br.com.invplus.modules.pessoas.dto.request.ClienteRequest;
import br.com.invplus.modules.pessoas.dto.response.ClienteResponse;
import br.com.invplus.modules.pessoas.entity.Cliente;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.pessoas.mapper.ClienteMapper;
import br.com.invplus.modules.pessoas.repository.ClienteRepository;
import br.com.invplus.modules.pessoas.repository.PessoaRepository;
import br.com.invplus.modules.pessoas.validator.ClienteValidator;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import br.com.invplus.shared.domain.service.BaseCrudService;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio para o papel comercial Cliente.
 */
@Service
public class ClienteService extends BaseCrudService<Cliente, ClienteRequest, ClienteResponse> {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;
    private final ClienteValidator clienteValidator;
    private final PessoaRepository pessoaRepository;
    private final PessoaRoleService pessoaRoleService;

    public ClienteService(
            ClienteRepository clienteRepository,
            ClienteMapper clienteMapper,
            ClienteValidator clienteValidator,
            PessoaRepository pessoaRepository,
            PessoaRoleService pessoaRoleService
    ) {
        super(clienteRepository, clienteMapper, "Cliente");
        this.clienteRepository = clienteRepository;
        this.clienteMapper = clienteMapper;
        this.clienteValidator = clienteValidator;
        this.pessoaRepository = pessoaRepository;
        this.pessoaRoleService = pessoaRoleService;
    }

    /**
     * Lista clientes ativos com dados basicos da pessoa vinculada.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ClienteResponse> listar(Pageable pageable) {
        return super.listar(pageable);
    }

    /**
     * Busca cliente ativo pelo identificador.
     */
    @Override
    @Transactional(readOnly = true)
    public ClienteResponse buscar(UUID id) {
        return super.buscar(id);
    }

    /**
     * Cria cliente associando uma pessoa ativa existente.
     */
    @Override
    @Transactional
    public ClienteResponse criar(ClienteRequest request) {
        clienteValidator.validarCriacao(request);
        Pessoa pessoa = buscarPessoaAtiva(request.pessoaId());
        Cliente cliente = clienteMapper.toEntity(request);
        cliente.setPessoa(pessoa);
        Cliente salvo = clienteRepository.save(cliente);
        pessoaRoleService.registrar(pessoa, PapelPessoa.CLIENTE);
        return clienteMapper.toResponse(salvo);
    }

    /**
     * Atualiza cliente e permite alterar a pessoa vinculada quando a nova pessoa ainda nao for cliente.
     */
    @Override
    @Transactional
    public ClienteResponse atualizar(UUID id, ClienteRequest request) {
        clienteValidator.validarAtualizacao(id, request);
        Cliente cliente = buscarEntidade(id);
        UUID pessoaAnteriorId = cliente.getPessoa().getId();
        Pessoa pessoa = buscarPessoaAtiva(request.pessoaId());
        cliente.setPessoa(pessoa);
        clienteMapper.updateEntity(cliente, request);
        sincronizarPessoaRole(pessoaAnteriorId, pessoa, PapelPessoa.CLIENTE);
        return clienteMapper.toResponse(cliente);
    }

    /**
     * Exclui logicamente o papel Cliente sem remover a Pessoa.
     */
    @Override
    @Transactional
    public void excluir(UUID id) {
        Cliente cliente = buscarEntidade(id);
        UUID pessoaId = cliente.getPessoa().getId();
        cliente.excluirLogicamente();
        pessoaRoleService.remover(pessoaId, PapelPessoa.CLIENTE);
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
