package br.com.invplus.modules.pessoas.service;

import br.com.invplus.common.enums.PapelPessoa;
import br.com.invplus.modules.pessoas.dto.request.FuncionarioRequest;
import br.com.invplus.modules.pessoas.dto.response.FuncionarioResponse;
import br.com.invplus.modules.pessoas.entity.Funcionario;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.pessoas.mapper.FuncionarioMapper;
import br.com.invplus.modules.pessoas.repository.FuncionarioRepository;
import br.com.invplus.modules.pessoas.repository.PessoaRepository;
import br.com.invplus.modules.pessoas.validator.FuncionarioValidator;
import br.com.invplus.security.entity.Usuario;
import br.com.invplus.security.repository.UsuarioRepository;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import br.com.invplus.shared.domain.service.BaseCrudService;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio para o papel Funcionario.
 */
@Service
public class FuncionarioService extends BaseCrudService<Funcionario, FuncionarioRequest, FuncionarioResponse> {

    private final FuncionarioRepository funcionarioRepository;
    private final FuncionarioMapper funcionarioMapper;
    private final FuncionarioValidator funcionarioValidator;
    private final PessoaRepository pessoaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PessoaRoleService pessoaRoleService;

    public FuncionarioService(
            FuncionarioRepository funcionarioRepository,
            FuncionarioMapper funcionarioMapper,
            FuncionarioValidator funcionarioValidator,
            PessoaRepository pessoaRepository,
            UsuarioRepository usuarioRepository,
            PessoaRoleService pessoaRoleService
    ) {
        super(funcionarioRepository, funcionarioMapper, "Funcionario");
        this.funcionarioRepository = funcionarioRepository;
        this.funcionarioMapper = funcionarioMapper;
        this.funcionarioValidator = funcionarioValidator;
        this.pessoaRepository = pessoaRepository;
        this.usuarioRepository = usuarioRepository;
        this.pessoaRoleService = pessoaRoleService;
    }

    /**
     * Lista funcionarios ativos com dados basicos da pessoa e usuario vinculado.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<FuncionarioResponse> listar(Pageable pageable) {
        return super.listar(pageable);
    }

    /**
     * Busca funcionario ativo pelo identificador.
     */
    @Override
    @Transactional(readOnly = true)
    public FuncionarioResponse buscar(UUID id) {
        return super.buscar(id);
    }

    /**
     * Cria funcionario associando Pessoa ativa e Usuario ativo opcional.
     */
    @Override
    @Transactional
    public FuncionarioResponse criar(FuncionarioRequest request) {
        funcionarioValidator.validarCriacao(request);
        Pessoa pessoa = buscarPessoaAtiva(request.pessoaId());
        Usuario usuario = buscarUsuarioAtivoOpcional(request.usuarioId());
        Funcionario funcionario = funcionarioMapper.toEntity(request);
        funcionario.setPessoa(pessoa);
        funcionario.setUsuario(usuario);
        Funcionario salvo = funcionarioRepository.save(funcionario);
        pessoaRoleService.registrar(pessoa, PapelPessoa.FUNCIONARIO);
        return funcionarioMapper.toResponse(salvo);
    }

    /**
     * Atualiza funcionario e permite trocar Pessoa ou Usuario quando os novos vinculos estiverem livres.
     */
    @Override
    @Transactional
    public FuncionarioResponse atualizar(UUID id, FuncionarioRequest request) {
        funcionarioValidator.validarAtualizacao(id, request);
        Funcionario funcionario = buscarEntidade(id);
        UUID pessoaAnteriorId = funcionario.getPessoa().getId();
        Pessoa pessoa = buscarPessoaAtiva(request.pessoaId());
        funcionario.setPessoa(pessoa);
        funcionario.setUsuario(buscarUsuarioAtivoOpcional(request.usuarioId()));
        funcionarioMapper.updateEntity(funcionario, request);
        sincronizarPessoaRole(pessoaAnteriorId, pessoa, PapelPessoa.FUNCIONARIO);
        return funcionarioMapper.toResponse(funcionario);
    }

    /**
     * Exclui logicamente o funcionario sem remover Pessoa nem Usuario.
     */
    @Override
    @Transactional
    public void excluir(UUID id) {
        Funcionario funcionario = buscarEntidade(id);
        UUID pessoaId = funcionario.getPessoa().getId();
        funcionario.excluirLogicamente();
        pessoaRoleService.remover(pessoaId, PapelPessoa.FUNCIONARIO);
    }

    private Pessoa buscarPessoaAtiva(UUID pessoaId) {
        return pessoaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(pessoaId)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa nao encontrada"));
    }

    private Usuario buscarUsuarioAtivoOpcional(UUID usuarioId) {
        if (usuarioId == null) {
            return null;
        }
        return usuarioRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
    }

    private void sincronizarPessoaRole(UUID pessoaAnteriorId, Pessoa pessoaAtual, PapelPessoa papel) {
        if (!pessoaAnteriorId.equals(pessoaAtual.getId())) {
            pessoaRoleService.remover(pessoaAnteriorId, papel);
            pessoaRoleService.registrar(pessoaAtual, papel);
        }
    }
}
