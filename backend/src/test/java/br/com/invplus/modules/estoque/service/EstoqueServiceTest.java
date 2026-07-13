package br.com.invplus.modules.estoque.service;

import java.math.BigDecimal;
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
import static org.mockito.Mockito.lenient;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import br.com.invplus.modules.cadastros.entity.Categoria;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.UnidadeMedida;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.cadastros.repository.FilialRepository;
import br.com.invplus.modules.estoque.dto.request.EstoqueRequest;
import br.com.invplus.modules.estoque.dto.response.EstoqueResponse;
import br.com.invplus.modules.estoque.entity.Deposito;
import br.com.invplus.modules.estoque.entity.Estoque;
import br.com.invplus.modules.estoque.entity.LocalizacaoEstoque;
import br.com.invplus.modules.estoque.enums.TipoDeposito;
import br.com.invplus.modules.estoque.mapper.EstoqueMapper;
import br.com.invplus.modules.estoque.repository.DepositoRepository;
import br.com.invplus.modules.estoque.repository.EstoqueRepository;
import br.com.invplus.modules.estoque.repository.LocalizacaoEstoqueRepository;
import br.com.invplus.modules.estoque.validator.EstoqueValidator;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.enums.NaturezaProduto;
import br.com.invplus.modules.produtos.enums.StatusProduto;
import br.com.invplus.modules.produtos.enums.TipoProduto;
import br.com.invplus.modules.produtos.repository.ProdutoRepository;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import br.com.invplus.shared.domain.service.EstoqueDomainService;
import br.com.invplus.security.context.EmpresaContext;
import br.com.invplus.security.service.UsuarioEmpresaAccessValidator;

/**
 * Testes unitarios das regras de saldo de Estoque.
 */
@ExtendWith(MockitoExtension.class)
class EstoqueServiceTest {

    @Mock
    private EstoqueRepository repository;
    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private EmpresaRepository empresaRepository;
    @Mock
    private FilialRepository filialRepository;
    @Mock
    private DepositoRepository depositoRepository;
    @Mock
    private LocalizacaoEstoqueRepository localizacaoRepository;
    @Mock
    private EstoqueDomainService estoqueDomainService;
    @Mock
    private EmpresaContext empresaContext;
    @Mock
    private UsuarioEmpresaAccessValidator accessValidator;

    private EstoqueService service;
    private UUID empresaId;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        EstoqueMapper mapper = new EstoqueMapper();
        EstoqueValidator validator = new EstoqueValidator(repository);
        empresaId = UUID.randomUUID();
        lenient().when(empresaContext.getEmpresaId()).thenReturn(empresaId);
        service = new EstoqueService(repository, mapper, validator, produtoRepository, empresaRepository,
                filialRepository, depositoRepository, localizacaoRepository, estoqueDomainService,
                empresaContext, accessValidator);
    }

    @Test
    void deveCriarEstoqueComQuantidadeDisponivelCalculada() {
        Contexto contexto = contexto();
        EstoqueRequest request = request(contexto, bd("10.000"), bd("2.000"), bd("3.000"), bd("20.000"));
        prepararBuscaContexto(contexto);
        when(repository.findByProdutoIdAndEmpresaIdAndDepositoIdAndLocalizacaoIdAndAtivoTrueAndDeletedAtIsNull(
                contexto.produto.getId(), contexto.empresa.getId(), contexto.deposito.getId(), contexto.localizacao.getId()))
                .thenReturn(Optional.empty());
        when(repository.save(any(Estoque.class))).thenAnswer(invocation -> {
            Estoque entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        EstoqueResponse response = service.criar(request);

        assertThat(response.quantidade()).isEqualByComparingTo("10.000");
        assertThat(response.quantidadeReservada()).isEqualByComparingTo("2.000");
        assertThat(response.quantidadeDisponivel()).isEqualByComparingTo("8.000");
    }

    @Test
    void deveBloquearProdutoInexistente() {
        Contexto contexto = contexto();
        EstoqueRequest request = request(contexto, bd("1.000"), BigDecimal.ZERO, BigDecimal.ZERO, bd("2.000"));
        when(produtoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(contexto.produto.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Produto nao encontrado");
    }

    @Test
    void deveBloquearLocalizacaoDeOutroDeposito() {
        Contexto contexto = contexto();
        Deposito outroDeposito = deposito(contexto.empresa);
        contexto.localizacao.setDeposito(outroDeposito);
        EstoqueRequest request = request(contexto, bd("1.000"), BigDecimal.ZERO, BigDecimal.ZERO, bd("2.000"));
        prepararBuscaContexto(contexto);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Localizacao deve pertencer ao deposito informado");
    }

    @Test
    void deveBloquearDuplicidadeDeSaldo() {
        Contexto contexto = contexto();
        Estoque existente = estoque(contexto);
        EstoqueRequest request = request(contexto, bd("1.000"), BigDecimal.ZERO, BigDecimal.ZERO, bd("2.000"));
        prepararBuscaContexto(contexto);
        when(repository.findByProdutoIdAndEmpresaIdAndDepositoIdAndLocalizacaoIdAndAtivoTrueAndDeletedAtIsNull(
                contexto.produto.getId(), contexto.empresa.getId(), contexto.deposito.getId(), contexto.localizacao.getId()))
                .thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Ja existe saldo ativo");
    }

    @Test
    void deveValidarEstoqueMaximoMaiorQueMinimo() {
        Contexto contexto = contexto();
        EstoqueRequest request = request(contexto, bd("1.000"), BigDecimal.ZERO, bd("5.000"), bd("2.000"));
        prepararBuscaContexto(contexto);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Estoque maximo deve ser maior ou igual ao estoque minimo");
    }

    @Test
    @SuppressWarnings("unchecked")
    void deveListarComFiltros() {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        service.listarComFiltros(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                true, true, false, true, false, PageRequest.of(0, 20));

        verify(repository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void deveConsultarSaldoPorChaveOperacional() {
        Contexto contexto = contexto();
        Estoque estoque = estoque(contexto);
        when(repository.findByProdutoIdAndEmpresaIdAndDepositoIdAndLocalizacaoIdAndAtivoTrueAndDeletedAtIsNull(
                contexto.produto.getId(), contexto.empresa.getId(), contexto.deposito.getId(), contexto.localizacao.getId()))
                .thenReturn(Optional.of(estoque));

        EstoqueResponse response = service.buscarSaldo(
                contexto.produto.getId(), contexto.deposito.getId(), contexto.localizacao.getId());

        assertThat(response.quantidadeDisponivel()).isEqualByComparingTo("8.000");
    }

    private void prepararBuscaContexto(Contexto contexto) {
        when(produtoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(contexto.produto.getId())).thenReturn(Optional.of(contexto.produto));
        when(empresaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(contexto.empresa.getId())).thenReturn(Optional.of(contexto.empresa));
        when(depositoRepository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(
                contexto.deposito.getId(), contexto.empresa.getId())).thenReturn(Optional.of(contexto.deposito));
        when(localizacaoRepository.findByIdAndDepositoEmpresaIdAndAtivoTrueAndDeletedAtIsNull(
                contexto.localizacao.getId(), contexto.empresa.getId())).thenReturn(Optional.of(contexto.localizacao));
    }

    private EstoqueRequest request(Contexto contexto, BigDecimal quantidade, BigDecimal reservada, BigDecimal minimo, BigDecimal maximo) {
        return new EstoqueRequest(contexto.produto.getId(), null, contexto.deposito.getId(),
                contexto.localizacao.getId(), quantidade, reservada, BigDecimal.ZERO, BigDecimal.ZERO, minimo, maximo, null, null);
    }

    private Estoque estoque(Contexto contexto) {
        Estoque estoque = new Estoque();
        estoque.setId(UUID.randomUUID());
        estoque.setProduto(contexto.produto);
        estoque.setEmpresa(contexto.empresa);
        estoque.setDeposito(contexto.deposito);
        estoque.setLocalizacao(contexto.localizacao);
        estoque.atualizarSaldos(bd("10.000"), bd("2.000"), BigDecimal.ZERO, BigDecimal.ZERO, bd("3.000"), bd("20.000"), null, null);
        return estoque;
    }

    private Contexto contexto() {
        Empresa empresa = new Empresa();
        empresa.setId(empresaId);
        empresa.atualizar("Empresa Estoque", "Empresa", "12345678000199", "ISENTO", null, "NORMAL", 3, "71999990000", "empresa@teste.com");

        Deposito deposito = deposito(empresa);
        LocalizacaoEstoque localizacao = new LocalizacaoEstoque();
        localizacao.setId(UUID.randomUUID());
        localizacao.setDeposito(deposito);
        localizacao.atualizar("A-01", "Rua A", "Rua A", "03", "02", "04", "15", true, false, "Teste");

        Produto produto = produto();
        return new Contexto(produto, empresa, deposito, localizacao);
    }

    private Deposito deposito(Empresa empresa) {
        Deposito deposito = new Deposito();
        deposito.setId(UUID.randomUUID());
        deposito.setEmpresa(empresa);
        deposito.atualizar("DEP-01", "Deposito Central", "Teste", TipoDeposito.CENTRAL, true, true, true, true, true, false);
        return deposito;
    }

    private Produto produto() {
        Categoria categoria = new Categoria();
        categoria.setId(UUID.randomUUID());
        categoria.atualizar(null, "Categoria", "Teste", 0);
        UnidadeMedida unidade = new UnidadeMedida();
        unidade.setId(UUID.randomUUID());
        unidade.atualizar("UN", "Unidade", (short) 0);
        Produto produto = new Produto();
        produto.setId(UUID.randomUUID());
        produto.setCategoria(categoria);
        produto.setUnidadeMedida(unidade);
        produto.atualizar("PROD-01", null, null, null, "Produto", "Teste", TipoProduto.PRODUTO,
                NaturezaProduto.AMBOS, StatusProduto.ATIVO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                false, false, false, true, true, true);
        return produto;
    }

    private BigDecimal bd(String valor) {
        return new BigDecimal(valor);
    }

    private record Contexto(Produto produto, Empresa empresa, Deposito deposito, LocalizacaoEstoque localizacao) {
    }
}
