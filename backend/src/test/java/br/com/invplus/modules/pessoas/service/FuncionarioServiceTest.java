package br.com.invplus.modules.pessoas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.invplus.common.enums.TipoPessoa;
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
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

/**
 * Testes unitarios das regras de negocio de Funcionario.
 */
@ExtendWith(MockitoExtension.class)
class FuncionarioServiceTest {

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PessoaRoleService pessoaRoleService;

    private FuncionarioService funcionarioService;

    @BeforeEach
    void setUp() {
        FuncionarioMapper mapper = new FuncionarioMapper();
        FuncionarioValidator validator = new FuncionarioValidator(funcionarioRepository);
        funcionarioService = new FuncionarioService(funcionarioRepository, mapper, validator, pessoaRepository, usuarioRepository, pessoaRoleService);
    }

    /**
     * Garante criacao do funcionario sem usuario de acesso.
     */
    @Test
    void deveCriarFuncionarioSemUsuario() {
        Pessoa pessoa = pessoaAtiva();
        prepararPessoaLivre(pessoa);
        when(funcionarioRepository.save(any(Funcionario.class))).thenAnswer(invocation -> {
            Funcionario funcionario = invocation.getArgument(0);
            funcionario.setId(UUID.randomUUID());
            return funcionario;
        });

        FuncionarioResponse response = funcionarioService.criar(request(pessoa.getId(), null));

        assertThat(response.pessoaId()).isEqualTo(pessoa.getId());
        assertThat(response.usuarioId()).isNull();
        assertThat(response.cargo()).isEqualTo("Analista");
    }

    /**
     * Garante criacao do funcionario com usuario ativo vinculado.
     */
    @Test
    void deveCriarFuncionarioComUsuario() {
        Pessoa pessoa = pessoaAtiva();
        Usuario usuario = usuarioAtivo();
        prepararPessoaLivre(pessoa);
        when(funcionarioRepository.findByUsuarioIdAndAtivoTrueAndDeletedAtIsNull(usuario.getId()))
                .thenReturn(Optional.empty());
        when(usuarioRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(usuario.getId()))
                .thenReturn(Optional.of(usuario));
        when(funcionarioRepository.save(any(Funcionario.class))).thenAnswer(invocation -> {
            Funcionario funcionario = invocation.getArgument(0);
            funcionario.setId(UUID.randomUUID());
            return funcionario;
        });

        FuncionarioResponse response = funcionarioService.criar(request(pessoa.getId(), usuario.getId()));

        assertThat(response.usuarioId()).isEqualTo(usuario.getId());
        assertThat(response.usuarioEmail()).isEqualTo("funcionario@teste.com");
    }

    /**
     * Bloqueia criacao quando a pessoa nao existe ou nao esta ativa.
     */
    @Test
    void deveBloquearPessoaInexistenteOuInativa() {
        UUID pessoaId = UUID.randomUUID();
        when(funcionarioRepository.findByPessoaIdAndAtivoTrueAndDeletedAtIsNull(pessoaId))
                .thenReturn(Optional.empty());
        when(pessoaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(pessoaId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> funcionarioService.criar(request(pessoaId, null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Pessoa nao encontrada");
    }

    /**
     * Bloqueia dois funcionarios ativos para a mesma pessoa.
     */
    @Test
    void deveBloquearFuncionarioDuplicadoParaMesmaPessoa() {
        Pessoa pessoa = pessoaAtiva();
        Funcionario existente = funcionarioAtivo();
        existente.setPessoa(pessoa);

        when(funcionarioRepository.findByPessoaIdAndAtivoTrueAndDeletedAtIsNull(pessoa.getId()))
                .thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> funcionarioService.criar(request(pessoa.getId(), null)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Pessoa ja possui funcionario ativo");
    }

    /**
     * Bloqueia usuario ja vinculado a outro funcionario ativo.
     */
    @Test
    void deveBloquearUsuarioVinculadoAOutroFuncionario() {
        Pessoa pessoa = pessoaAtiva();
        Usuario usuario = usuarioAtivo();
        Funcionario existente = funcionarioAtivo();
        existente.setUsuario(usuario);

        when(funcionarioRepository.findByPessoaIdAndAtivoTrueAndDeletedAtIsNull(pessoa.getId()))
                .thenReturn(Optional.empty());
        when(funcionarioRepository.findByUsuarioIdAndAtivoTrueAndDeletedAtIsNull(usuario.getId()))
                .thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> funcionarioService.criar(request(pessoa.getId(), usuario.getId())))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Usuario ja vinculado a funcionario ativo");
    }

    /**
     * Bloqueia salario negativo.
     */
    @Test
    void deveBloquearSalarioNegativo() {
        FuncionarioRequest request = new FuncionarioRequest(
                UUID.randomUUID(), null, "Analista", BigDecimal.valueOf(-1),
                LocalDate.now(), null, null
        );

        assertThatThrownBy(() -> funcionarioService.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Salario nao pode ser negativo");
    }

    /**
     * Bloqueia data de demissao anterior a admissao.
     */
    @Test
    void deveBloquearDataDemissaoAnteriorAdmissao() {
        FuncionarioRequest request = new FuncionarioRequest(
                UUID.randomUUID(), null, "Analista", BigDecimal.TEN,
                LocalDate.of(2026, 7, 3), LocalDate.of(2026, 7, 2), null
        );

        assertThatThrownBy(() -> funcionarioService.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Data de demissao nao pode ser anterior a admissao");
    }

    /**
     * Busca funcionario ativo por id.
     */
    @Test
    void deveBuscarPorId() {
        Funcionario funcionario = funcionarioAtivo();
        when(funcionarioRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(funcionario.getId()))
                .thenReturn(Optional.of(funcionario));

        FuncionarioResponse response = funcionarioService.buscar(funcionario.getId());

        assertThat(response.id()).isEqualTo(funcionario.getId());
        assertThat(response.nome()).isEqualTo("Funcionario Teste");
    }

    /**
     * Mantem listagem paginada.
     */
    @Test
    void deveListarComPaginacao() {
        when(funcionarioRepository.findByAtivoTrueAndDeletedAtIsNull(PageRequest.of(0, 20)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        funcionarioService.listar(PageRequest.of(0, 20));

        verify(funcionarioRepository).findByAtivoTrueAndDeletedAtIsNull(PageRequest.of(0, 20));
    }

    /**
     * Atualiza dados trabalhistas e permite manter os vinculos atuais.
     */
    @Test
    void deveAtualizarFuncionario() {
        Funcionario funcionario = funcionarioAtivo();
        UUID pessoaId = funcionario.getPessoa().getId();

        when(funcionarioRepository.findByPessoaIdAndAtivoTrueAndDeletedAtIsNull(pessoaId))
                .thenReturn(Optional.of(funcionario));
        when(funcionarioRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(funcionario.getId()))
                .thenReturn(Optional.of(funcionario));
        when(pessoaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(pessoaId))
                .thenReturn(Optional.of(funcionario.getPessoa()));

        FuncionarioResponse response = funcionarioService.atualizar(
                funcionario.getId(),
                new FuncionarioRequest(pessoaId, null, "Coordenador", BigDecimal.valueOf(7000),
                        LocalDate.of(2026, 1, 10), null, " Promovido ")
        );

        assertThat(response.cargo()).isEqualTo("Coordenador");
        assertThat(response.salario()).isEqualByComparingTo("7000");
        assertThat(response.observacao()).isEqualTo("Promovido");
    }

    /**
     * Confirma exclusao logica do papel Funcionario.
     */
    @Test
    void deveExcluirLogicamente() {
        Funcionario funcionario = funcionarioAtivo();
        when(funcionarioRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(funcionario.getId()))
                .thenReturn(Optional.of(funcionario));

        funcionarioService.excluir(funcionario.getId());

        assertThat(funcionario.getAtivo()).isFalse();
        assertThat(funcionario.getDeletedAt()).isNotNull();
    }

    private void prepararPessoaLivre(Pessoa pessoa) {
        when(funcionarioRepository.findByPessoaIdAndAtivoTrueAndDeletedAtIsNull(pessoa.getId()))
                .thenReturn(Optional.empty());
        when(pessoaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(pessoa.getId()))
                .thenReturn(Optional.of(pessoa));
    }

    private FuncionarioRequest request(UUID pessoaId, UUID usuarioId) {
        return new FuncionarioRequest(
                pessoaId,
                usuarioId,
                "Analista",
                BigDecimal.valueOf(5000),
                LocalDate.of(2026, 1, 10),
                null,
                " Funcionario ativo "
        );
    }

    private Funcionario funcionarioAtivo() {
        Funcionario funcionario = new Funcionario();
        funcionario.setId(UUID.randomUUID());
        funcionario.setPessoa(pessoaAtiva());
        funcionario.atualizar("Analista", BigDecimal.valueOf(5000), LocalDate.of(2026, 1, 10), null,
                "Funcionario ativo");
        return funcionario;
    }

    private Pessoa pessoaAtiva() {
        Pessoa pessoa = new Pessoa();
        pessoa.setId(UUID.randomUUID());
        pessoa.atualizar(TipoPessoa.FISICA, "Funcionario Teste", null, "12345678901",
                null, "7133334444", "71999998888", "funcionario.pessoa@teste.com", null);
        return pessoa;
    }

    private Usuario usuarioAtivo() {
        Usuario usuario = new Usuario("Funcionario Usuario", "funcionario@teste.com", "hash");
        usuario.setId(UUID.randomUUID());
        return usuario;
    }
}
