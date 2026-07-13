package br.com.invplus.modules.estoque.service;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import br.com.invplus.modules.estoque.dto.request.LocalizacaoEstoqueRequest;
import br.com.invplus.modules.estoque.dto.response.LocalizacaoEstoqueResponse;
import br.com.invplus.modules.estoque.entity.Deposito;
import br.com.invplus.modules.estoque.entity.LocalizacaoEstoque;
import br.com.invplus.modules.estoque.mapper.LocalizacaoEstoqueMapper;
import br.com.invplus.modules.estoque.repository.DepositoRepository;
import br.com.invplus.modules.estoque.repository.LocalizacaoEstoqueRepository;
import br.com.invplus.modules.estoque.validator.LocalizacaoEstoqueValidator;
import br.com.invplus.security.context.EmpresaContext;
import br.com.invplus.security.service.UsuarioEmpresaAccessValidator;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;

/**
 * Testes unitarios das regras de LocalizacaoEstoque.
 */
@ExtendWith(MockitoExtension.class)
class LocalizacaoEstoqueServiceTest {

    @Mock
    private LocalizacaoEstoqueRepository repository;

    @Mock
    private DepositoRepository depositoRepository;

    @Mock
    private EmpresaContext empresaContext;

    @Mock
    private UsuarioEmpresaAccessValidator accessValidator;

    private LocalizacaoEstoqueService service;
    private UUID empresaId;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        empresaId = UUID.randomUUID();
        lenient().when(empresaContext.getEmpresaId()).thenReturn(empresaId);
        LocalizacaoEstoqueMapper mapper = new LocalizacaoEstoqueMapper();
        LocalizacaoEstoqueValidator validator = new LocalizacaoEstoqueValidator(repository);
        service = new LocalizacaoEstoqueService(
                repository, mapper, validator, depositoRepository, empresaContext, accessValidator);
    }

    @Test
    void deveCriarLocalizacaoComSucesso() {
        Deposito deposito = depositoAtivo();
        LocalizacaoEstoqueRequest request = request(deposito.getId(), "A-01", true);
        when(repository.findByDepositoIdAndCodigoAndAtivoTrueAndDeletedAtIsNull(deposito.getId(), "A-01"))
                .thenReturn(Optional.empty());
        when(repository.findByDepositoIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(deposito.getId()))
                .thenReturn(Optional.empty());
        when(depositoRepository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(deposito.getId(), empresaId))
                .thenReturn(Optional.of(deposito));
        when(repository.save(any(LocalizacaoEstoque.class))).thenAnswer(invocation -> {
            LocalizacaoEstoque entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        LocalizacaoEstoqueResponse response = service.criar(request);

        assertThat(response.depositoId()).isEqualTo(deposito.getId());
        assertThat(response.codigo()).isEqualTo("A-01");
        assertThat(response.principal()).isTrue();
        assertThat(response.bloqueada()).isFalse();
    }

    @Test
    void deveBloquearDepositoInexistente() {
        UUID depositoId = UUID.randomUUID();
        LocalizacaoEstoqueRequest request = request(depositoId, "A-01", false);
        when(depositoRepository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(depositoId, empresaId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Deposito nao encontrado");
    }

    @Test
    void deveBloquearCodigoDuplicadoPorDeposito() {
        LocalizacaoEstoque existente = localizacaoAtiva();
        LocalizacaoEstoqueRequest request = request(existente.getDeposito().getId(), existente.getCodigo(), false);
        when(depositoRepository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(request.depositoId(), empresaId))
                .thenReturn(Optional.of(existente.getDeposito()));
        when(repository.findByDepositoIdAndCodigoAndAtivoTrueAndDeletedAtIsNull(request.depositoId(), request.codigo()))
                .thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Ja existe localizacao ativa");
    }

    @Test
    void deveBloquearSegundaLocalizacaoPrincipal() {
        LocalizacaoEstoque existente = localizacaoAtiva();
        LocalizacaoEstoqueRequest request = request(existente.getDeposito().getId(), "B-01", true);
        when(depositoRepository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(request.depositoId(), empresaId))
                .thenReturn(Optional.of(existente.getDeposito()));
        when(repository.findByDepositoIdAndCodigoAndAtivoTrueAndDeletedAtIsNull(request.depositoId(), request.codigo()))
                .thenReturn(Optional.empty());
        when(repository.findByDepositoIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(request.depositoId()))
                .thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Deposito ja possui localizacao principal ativa");
    }

    @Test
    @SuppressWarnings("unchecked")
    void deveListarComFiltros() {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        service.listarComFiltros(UUID.randomUUID(), "A", "Rua", "R", "C", "P",
                true, false, true, PageRequest.of(0, 20));

        verify(repository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void deveAtualizarMantendoProprioCodigoEPrincipal() {
        LocalizacaoEstoque entity = localizacaoAtiva();
        LocalizacaoEstoqueRequest request = request(entity.getDeposito().getId(), entity.getCodigo(), true);
        when(repository.findByIdAndDepositoEmpresaIdAndAtivoTrueAndDeletedAtIsNull(entity.getId(), empresaId))
                .thenReturn(Optional.of(entity));
        when(repository.findByDepositoIdAndCodigoAndAtivoTrueAndDeletedAtIsNull(request.depositoId(), request.codigo()))
                .thenReturn(Optional.of(entity));
        when(repository.findByDepositoIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(request.depositoId()))
                .thenReturn(Optional.of(entity));
        when(depositoRepository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(request.depositoId(), empresaId))
                .thenReturn(Optional.of(entity.getDeposito()));

        LocalizacaoEstoqueResponse response = service.atualizar(entity.getId(), request);

        assertThat(response.codigo()).isEqualTo("A-01");
        assertThat(response.principal()).isTrue();
    }

    @Test
    void deveExcluirLogicamente() {
        LocalizacaoEstoque entity = localizacaoAtiva();
        when(repository.findByIdAndDepositoEmpresaIdAndAtivoTrueAndDeletedAtIsNull(entity.getId(), empresaId))
                .thenReturn(Optional.of(entity));

        service.excluir(entity.getId());

        assertThat(entity.getAtivo()).isFalse();
        assertThat(entity.getDeletedAt()).isNotNull();
    }

    private LocalizacaoEstoqueRequest request(UUID depositoId, String codigo, Boolean principal) {
        return new LocalizacaoEstoqueRequest(depositoId, codigo, "Rua A prateleira 1",
                "Rua A", "03", "02", "04", "15", principal, false, "Teste");
    }

    private LocalizacaoEstoque localizacaoAtiva() {
        LocalizacaoEstoque entity = new LocalizacaoEstoque();
        entity.setId(UUID.randomUUID());
        entity.setDeposito(depositoAtivo());
        entity.atualizar("A-01", "Rua A", "Rua A", "03", "02", "04", "15", true, false, "Teste");
        return entity;
    }

    private Deposito depositoAtivo() {
        Deposito deposito = new Deposito();
        deposito.setId(UUID.randomUUID());
        deposito.setCodigo("DEP-01");
        deposito.setNome("Deposito Central");
        return deposito;
    }
}
