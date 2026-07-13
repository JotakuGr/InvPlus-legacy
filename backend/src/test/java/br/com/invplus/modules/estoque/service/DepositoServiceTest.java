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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.cadastros.repository.FilialRepository;
import br.com.invplus.modules.estoque.dto.request.DepositoRequest;
import br.com.invplus.modules.estoque.dto.response.DepositoResponse;
import br.com.invplus.modules.estoque.entity.Deposito;
import br.com.invplus.modules.estoque.enums.TipoDeposito;
import br.com.invplus.modules.estoque.mapper.DepositoMapper;
import br.com.invplus.modules.estoque.repository.DepositoRepository;
import br.com.invplus.modules.estoque.validator.DepositoValidator;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;

/**
 * Testes unitarios das regras de Deposito.
 */
@ExtendWith(MockitoExtension.class)
class DepositoServiceTest {

    @Mock
    private DepositoRepository repository;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private FilialRepository filialRepository;

    @Mock
    private EmpresaContext empresaContext;

    @Mock
    private UsuarioEmpresaAccessValidator accessValidator;

    private DepositoService service;
    private UUID empresaId;
    private UUID usuarioId;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        DepositoMapper mapper = new DepositoMapper();
        DepositoValidator validator = new DepositoValidator(repository);
        empresaId = UUID.randomUUID();
        usuarioId = UUID.randomUUID();
        lenient().when(empresaContext.getEmpresaId()).thenReturn(empresaId);
        lenient().when(empresaContext.getUsuarioId()).thenReturn(usuarioId);
        service = new DepositoService(repository, mapper, validator, empresaRepository, filialRepository,
                empresaContext, accessValidator);
    }

    /**
     * Cria deposito com empresa e filial ativas.
     */
    @Test
    void deveCriarDepositoComSucesso() {
        Empresa empresa = empresaAtiva();
        empresa.setId(empresaId);
        Filial filial = filialAtiva(empresa);
        DepositoRequest request = request(filial.getId(), "DEP-01", true);
        when(repository.findByEmpresaIdAndCodigoAndAtivoTrueAndDeletedAtIsNull(empresa.getId(), "DEP-01"))
                .thenReturn(Optional.empty());
        when(repository.findByEmpresaIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(empresa.getId()))
                .thenReturn(Optional.empty());
        when(empresaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(empresa.getId())).thenReturn(Optional.of(empresa));
        when(filialRepository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(filial.getId(), empresaId))
                .thenReturn(Optional.of(filial));
        when(repository.save(any(Deposito.class))).thenAnswer(invocation -> {
            Deposito entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        DepositoResponse response = service.criar(request);

        assertThat(response.empresaId()).isEqualTo(empresa.getId());
        assertThat(response.filialId()).isEqualTo(filial.getId());
        assertThat(response.codigo()).isEqualTo("DEP-01");
        assertThat(response.principal()).isTrue();
        assertThat(response.controlaEstoque()).isTrue();
        assertThat(response.bloqueado()).isFalse();
    }

    /**
     * Empresa inexistente ou inativa deve retornar 404.
     */
    @Test
    void deveBloquearEmpresaInexistente() {
        UUID empresaId = UUID.randomUUID();
        when(empresaContext.getEmpresaId()).thenReturn(empresaId);
        DepositoRequest request = request(null, "DEP-01", false);
        when(repository.findByEmpresaIdAndCodigoAndAtivoTrueAndDeletedAtIsNull(empresaId, "DEP-01"))
                .thenReturn(Optional.empty());
        when(empresaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(empresaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Empresa nao encontrada");
    }

    /**
     * Filial inexistente ou inativa deve retornar 404.
     */
    @Test
    void deveBloquearFilialInexistente() {
        Empresa empresa = empresaAtiva();
        empresa.setId(empresaId);
        UUID filialId = UUID.randomUUID();
        DepositoRequest request = request(filialId, "DEP-01", false);
        when(repository.findByEmpresaIdAndCodigoAndAtivoTrueAndDeletedAtIsNull(empresa.getId(), "DEP-01"))
                .thenReturn(Optional.empty());
        when(empresaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(empresa.getId())).thenReturn(Optional.of(empresa));
        when(filialRepository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(filialId, empresaId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Filial nao encontrada");
    }

    /**
     * Codigo deve ser unico por empresa entre depositos ativos.
     */
    @Test
    void deveBloquearCodigoDuplicadoPorEmpresa() {
        Deposito existente = depositoAtivo();
        existente.getEmpresa().setId(empresaId);
        DepositoRequest request = request(null, existente.getCodigo(), false);
        when(repository.findByEmpresaIdAndCodigoAndAtivoTrueAndDeletedAtIsNull(empresaId, request.codigo()))
                .thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Ja existe deposito ativo");
    }

    /**
     * Empresa deve ter apenas um deposito principal ativo.
     */
    @Test
    void deveBloquearSegundoDepositoPrincipal() {
        Deposito existente = depositoAtivo();
        existente.getEmpresa().setId(empresaId);
        DepositoRequest request = request(null, "DEP-02", true);
        when(repository.findByEmpresaIdAndCodigoAndAtivoTrueAndDeletedAtIsNull(empresaId, request.codigo()))
                .thenReturn(Optional.empty());
        when(repository.findByEmpresaIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(empresaId))
                .thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Empresa ja possui deposito principal ativo");
    }

    /**
     * Filial deve pertencer a mesma empresa do deposito.
     */
    @Test
    void deveBloquearFilialDeOutraEmpresa() {
        Empresa empresa = empresaAtiva();
        empresa.setId(empresaId);
        Empresa outraEmpresa = empresaAtiva();
        Filial filial = filialAtiva(outraEmpresa);
        DepositoRequest request = request(filial.getId(), "DEP-01", false);
        when(repository.findByEmpresaIdAndCodigoAndAtivoTrueAndDeletedAtIsNull(empresaId, "DEP-01"))
                .thenReturn(Optional.empty());
        when(empresaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(empresaId)).thenReturn(Optional.of(empresa));
        when(filialRepository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(filial.getId(), empresaId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Filial nao encontrada");
    }

    /**
     * Filtros devem delegar para Specification paginada.
     */
    @Test
    @SuppressWarnings("unchecked")
    void deveListarComFiltros() {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        service.listarComFiltros(UUID.randomUUID(), "DEP", "Central",
                TipoDeposito.CENTRAL, true, true, false, true, PageRequest.of(0, 20));

        verify(repository).findAll(any(Specification.class), any(Pageable.class));
    }

    /**
     * Atualizacao ignora o proprio registro ao manter codigo e principal.
     */
    @Test
    void deveAtualizarMantendoProprioCodigoEPrincipal() {
        Deposito entity = depositoAtivo();
        entity.getEmpresa().setId(empresaId);
        DepositoRequest request = request(null, entity.getCodigo(), true);
        when(repository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(entity.getId(), empresaId))
                .thenReturn(Optional.of(entity));
        when(repository.findByEmpresaIdAndCodigoAndAtivoTrueAndDeletedAtIsNull(empresaId, request.codigo()))
                .thenReturn(Optional.of(entity));
        when(repository.findByEmpresaIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(empresaId))
                .thenReturn(Optional.of(entity));
        when(empresaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(empresaId))
                .thenReturn(Optional.of(entity.getEmpresa()));

        DepositoResponse response = service.atualizar(entity.getId(), request);

        assertThat(response.codigo()).isEqualTo(entity.getCodigo());
        assertThat(response.principal()).isTrue();
    }

    /**
     * Exclusao logica preserva historico.
     */
    @Test
    void deveExcluirLogicamente() {
        Deposito entity = depositoAtivo();
        entity.getEmpresa().setId(empresaId);
        when(repository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(entity.getId(), empresaId))
                .thenReturn(Optional.of(entity));

        service.excluir(entity.getId());

        assertThat(entity.getAtivo()).isFalse();
        assertThat(entity.getDeletedAt()).isNotNull();
    }

    private DepositoRequest request(UUID filialId, String codigo, Boolean principal) {
        return new DepositoRequest(filialId, codigo, "Deposito Central", "Deposito de teste",
                TipoDeposito.CENTRAL, principal, true, true, true, true, false);
    }

    private Deposito depositoAtivo() {
        Deposito deposito = new Deposito();
        deposito.setId(UUID.randomUUID());
        deposito.setEmpresa(empresaAtiva());
        deposito.atualizar("DEP-01", "Deposito Central", "Teste", TipoDeposito.CENTRAL, true, true, true, true, true, false);
        return deposito;
    }

    private Empresa empresaAtiva() {
        Empresa empresa = new Empresa();
        empresa.setId(UUID.randomUUID());
        empresa.atualizar("Empresa Teste", "Empresa Teste", cnpjUnico(), "ISENTO", null, "NORMAL", 3, "71999990000", "empresa@teste.com");
        return empresa;
    }

    private Filial filialAtiva(Empresa empresa) {
        Filial filial = new Filial();
        filial.setId(UUID.randomUUID());
        filial.atualizar(empresa, null, "FIL-01", "Filial Teste", cnpjUnico(), "ISENTO", "71999991111", "filial@teste.com", false);
        return filial;
    }

    private String cnpjUnico() {
        return "12" + "%012d".formatted(Math.abs(System.nanoTime() % 1_000_000_000_000L));
    }
}
