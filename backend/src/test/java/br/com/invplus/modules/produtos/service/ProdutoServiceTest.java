package br.com.invplus.modules.produtos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.invplus.modules.cadastros.entity.Categoria;
import br.com.invplus.modules.cadastros.entity.Marca;
import br.com.invplus.modules.cadastros.entity.UnidadeMedida;
import br.com.invplus.modules.cadastros.repository.CategoriaRepository;
import br.com.invplus.modules.cadastros.repository.MarcaRepository;
import br.com.invplus.modules.cadastros.repository.UnidadeMedidaRepository;
import br.com.invplus.modules.produtos.dto.request.ProdutoRequest;
import br.com.invplus.modules.produtos.dto.response.ProdutoResponse;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.enums.NaturezaProduto;
import br.com.invplus.modules.produtos.enums.StatusProduto;
import br.com.invplus.modules.produtos.enums.TipoProduto;
import br.com.invplus.modules.produtos.mapper.ProdutoMapper;
import br.com.invplus.modules.produtos.repository.ProdutoRepository;
import br.com.invplus.modules.produtos.validator.ProdutoValidator;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

/**
 * Testes unitarios das regras de negocio de Produto.
 */
@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private MarcaRepository marcaRepository;

    @Mock
    private UnidadeMedidaRepository unidadeMedidaRepository;

    private ProdutoService produtoService;

    @BeforeEach
    void setUp() {
        ProdutoMapper mapper = new ProdutoMapper();
        ProdutoValidator validator = new ProdutoValidator(produtoRepository);
        produtoService = new ProdutoService(
                produtoRepository,
                mapper,
                validator,
                categoriaRepository,
                marcaRepository,
                unidadeMedidaRepository
        );
    }

    /**
     * Garante criacao do produto com categoria, marca e unidade ativas.
     */
    @Test
    void deveCriarProdutoComSucesso() {
        Categoria categoria = categoriaAtiva();
        Marca marca = marcaAtiva();
        UnidadeMedida unidade = unidadeAtiva();
        when(produtoRepository.existsByCodigoAndAtivoTrueAndDeletedAtIsNull("PROD-001")).thenReturn(false);
        when(produtoRepository.existsBySkuAndAtivoTrueAndDeletedAtIsNull("SKU-001")).thenReturn(false);
        when(produtoRepository.existsByCodigoBarrasAndAtivoTrueAndDeletedAtIsNull("789000000001")).thenReturn(false);
        when(categoriaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(categoria.getId())).thenReturn(Optional.of(categoria));
        when(marcaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(marca.getId())).thenReturn(Optional.of(marca));
        when(unidadeMedidaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(unidade.getId())).thenReturn(Optional.of(unidade));
        when(produtoRepository.save(any(Produto.class))).thenAnswer(invocation -> {
            Produto produto = invocation.getArgument(0);
            produto.setId(UUID.randomUUID());
            return produto;
        });

        ProdutoResponse response = produtoService.criar(request(categoria.getId(), marca.getId(), unidade.getId()));

        assertThat(response.codigo()).isEqualTo("PROD-001");
        assertThat(response.categoriaId()).isEqualTo(categoria.getId());
        assertThat(response.marcaId()).isEqualTo(marca.getId());
        assertThat(response.unidadeMedidaSigla()).isEqualTo("UN");
        assertThat(response.precoVenda()).isEqualByComparingTo("20.00");
    }

    /**
     * Categoria inexistente ou inativa deve bloquear criacao.
     */
    @Test
    void deveBloquearCategoriaInexistenteOuInativa() {
        UUID categoriaId = UUID.randomUUID();
        UUID unidadeId = UUID.randomUUID();
        when(categoriaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(categoriaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.criar(request(categoriaId, null, unidadeId)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Categoria nao encontrada");
    }

    /**
     * Unidade inexistente ou inativa deve bloquear criacao.
     */
    @Test
    void deveBloquearUnidadeInexistenteOuInativa() {
        Categoria categoria = categoriaAtiva();
        UUID unidadeId = UUID.randomUUID();
        when(categoriaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(categoria.getId())).thenReturn(Optional.of(categoria));
        when(unidadeMedidaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(unidadeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.criar(request(categoria.getId(), null, unidadeId)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Unidade de medida nao encontrada");
    }

    /**
     * Marca inexistente ou inativa deve bloquear criacao quando informada.
     */
    @Test
    void deveBloquearMarcaInexistenteOuInativa() {
        Categoria categoria = categoriaAtiva();
        UUID marcaId = UUID.randomUUID();
        when(categoriaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(categoria.getId())).thenReturn(Optional.of(categoria));
        when(marcaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(marcaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.criar(request(categoria.getId(), marcaId, UUID.randomUUID())))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Marca nao encontrada");
    }

    /**
     * Codigo duplicado deve retornar conflito.
     */
    @Test
    void deveBloquearCodigoDuplicado() {
        when(produtoRepository.existsByCodigoAndAtivoTrueAndDeletedAtIsNull("PROD-001")).thenReturn(true);

        assertThatThrownBy(() -> produtoService.criar(request(UUID.randomUUID(), null, UUID.randomUUID())))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Ja existe produto ativo com este codigo");
    }

    /**
     * SKU duplicado deve retornar conflito.
     */
    @Test
    void deveBloquearSkuDuplicado() {
        when(produtoRepository.existsByCodigoAndAtivoTrueAndDeletedAtIsNull("PROD-001")).thenReturn(false);
        when(produtoRepository.existsBySkuAndAtivoTrueAndDeletedAtIsNull("SKU-001")).thenReturn(true);

        assertThatThrownBy(() -> produtoService.criar(request(UUID.randomUUID(), null, UUID.randomUUID())))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Ja existe produto ativo com este SKU");
    }

    /**
     * Codigo de barras duplicado deve retornar conflito.
     */
    @Test
    void deveBloquearCodigoBarrasDuplicado() {
        when(produtoRepository.existsByCodigoAndAtivoTrueAndDeletedAtIsNull("PROD-001")).thenReturn(false);
        when(produtoRepository.existsBySkuAndAtivoTrueAndDeletedAtIsNull("SKU-001")).thenReturn(false);
        when(produtoRepository.existsByCodigoBarrasAndAtivoTrueAndDeletedAtIsNull("789000000001")).thenReturn(true);

        assertThatThrownBy(() -> produtoService.criar(request(UUID.randomUUID(), null, UUID.randomUUID())))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Ja existe produto ativo com este codigo de barras");
    }

    /**
     * Preco negativo deve retornar erro de negocio.
     */
    @Test
    void deveBloquearPrecoNegativo() {
        ProdutoRequest request = new ProdutoRequest(
                UUID.randomUUID(), null, UUID.randomUUID(), "PROD-NEG", "SKU-NEG", null, null,
                "Produto negativo", null, TipoProduto.PRODUTO, NaturezaProduto.AMBOS, StatusProduto.ATIVO,
                BigDecimal.valueOf(-1), BigDecimal.TEN,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                false, false, false, true, true, true
        );

        assertThatThrownBy(() -> produtoService.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Preco de custo nao pode ser negativo");
    }

    /**
     * Codigo de barras fora do padrao flexivel de GTIN deve retornar erro de negocio.
     */
    @Test
    void deveBloquearCodigoBarrasInvalido() {
        ProdutoRequest request = new ProdutoRequest(
                UUID.randomUUID(), null, UUID.randomUUID(), "PROD-BAR", "SKU-BAR", null, "ABC123",
                "Produto barras invalido", null, TipoProduto.PRODUTO, NaturezaProduto.AMBOS, StatusProduto.ATIVO,
                BigDecimal.TEN, BigDecimal.TEN,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                false, false, false, true, true, true
        );

        assertThatThrownBy(() -> produtoService.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Codigo de barras deve conter 8, 12, 13 ou 14 digitos numericos");
    }

    /**
     * Busca produto ativo por id.
     */
    @Test
    void deveBuscarPorId() {
        Produto produto = produtoAtivo();
        when(produtoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(produto.getId())).thenReturn(Optional.of(produto));

        ProdutoResponse response = produtoService.buscar(produto.getId());

        assertThat(response.id()).isEqualTo(produto.getId());
        assertThat(response.nome()).isEqualTo("Produto Teste");
    }

    /**
     * Mantem listagem paginada por padrao.
     */
    @Test
    void deveListarComPaginacao() {
        when(produtoRepository.findByAtivoTrueAndDeletedAtIsNull(PageRequest.of(0, 20)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        produtoService.listar(PageRequest.of(0, 20));

        verify(produtoRepository).findByAtivoTrueAndDeletedAtIsNull(PageRequest.of(0, 20));
    }

    /**
     * Filtros avancados devem usar Specification para combinar criterios opcionais.
     */
    @Test
    @SuppressWarnings("unchecked")
    void deveListarComFiltrosAvancados() {
        when(produtoRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        produtoService.listarComFiltros(
                "Produto",
                "PROD",
                "SKU",
                "789",
                UUID.randomUUID(),
                UUID.randomUUID(),
                TipoProduto.PRODUTO,
                NaturezaProduto.AMBOS,
                StatusProduto.ATIVO,
                true,
                true,
                true,
                true,
                PageRequest.of(0, 20)
        );

        verify(produtoRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    /**
     * Busca rapida deve pesquisar por termo mantendo somente produtos ativos.
     */
    @Test
    @SuppressWarnings("unchecked")
    void deveBuscarRapidoPorTermo() {
        when(produtoRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        produtoService.buscarRapido("produto", PageRequest.of(0, 10));

        verify(produtoRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    /**
     * Atualiza dados comerciais e relacionamentos do produto.
     */
    @Test
    void deveAtualizarProduto() {
        Produto produto = produtoAtivo();
        Categoria categoria = produto.getCategoria();
        UnidadeMedida unidade = produto.getUnidadeMedida();
        when(produtoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(produto.getId())).thenReturn(Optional.of(produto));
        when(categoriaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(categoria.getId())).thenReturn(Optional.of(categoria));
        when(unidadeMedidaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(unidade.getId())).thenReturn(Optional.of(unidade));

        ProdutoResponse response = produtoService.atualizar(produto.getId(), request(categoria.getId(), null, unidade.getId()));

        assertThat(response.codigo()).isEqualTo("PROD-001");
        assertThat(response.marcaId()).isNull();
    }

    /**
     * Confirma exclusao logica do produto.
     */
    @Test
    void deveExcluirLogicamente() {
        Produto produto = produtoAtivo();
        when(produtoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(produto.getId())).thenReturn(Optional.of(produto));

        produtoService.excluir(produto.getId());

        assertThat(produto.getAtivo()).isFalse();
        assertThat(produto.getDeletedAt()).isNotNull();
    }

    private ProdutoRequest request(UUID categoriaId, UUID marcaId, UUID unidadeId) {
        return new ProdutoRequest(
                categoriaId,
                marcaId,
                unidadeId,
                "PROD-001",
                "SKU-001",
                "INT-001",
                "789000000001",
                "Produto Teste",
                "Produto para testes",
                TipoProduto.PRODUTO,
                NaturezaProduto.AMBOS,
                StatusProduto.ATIVO,
                BigDecimal.TEN,
                BigDecimal.valueOf(20),
                BigDecimal.ONE,
                BigDecimal.valueOf(1.2),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(5),
                true,
                false,
                true,
                true,
                true,
                true
        );
    }

    private Produto produtoAtivo() {
        Produto produto = new Produto();
        produto.setId(UUID.randomUUID());
        produto.setCategoria(categoriaAtiva());
        produto.setMarca(marcaAtiva());
        produto.setUnidadeMedida(unidadeAtiva());
        produto.atualizar("PROD-001", "SKU-001", "INT-001", "789000000001", "Produto Teste",
                "Produto para testes", TipoProduto.PRODUTO, NaturezaProduto.AMBOS, StatusProduto.ATIVO,
                BigDecimal.TEN, BigDecimal.valueOf(20), BigDecimal.ONE,
                BigDecimal.valueOf(1.2), BigDecimal.valueOf(10), BigDecimal.valueOf(20), BigDecimal.valueOf(30),
                BigDecimal.valueOf(5), true, false, true, true, true, true);
        return produto;
    }

    private Categoria categoriaAtiva() {
        Categoria categoria = new Categoria();
        categoria.setId(UUID.randomUUID());
        categoria.atualizar(null, "Categoria Produto", "Categoria de produto", 0);
        return categoria;
    }

    private Marca marcaAtiva() {
        Marca marca = new Marca();
        marca.setId(UUID.randomUUID());
        marca.atualizar("Marca Produto", "Marca de produto", "https://marca.example.com", "Fabricante");
        return marca;
    }

    private UnidadeMedida unidadeAtiva() {
        UnidadeMedida unidade = new UnidadeMedida();
        unidade.setId(UUID.randomUUID());
        unidade.atualizar("UN", "Unidade", (short) 0);
        return unidade;
    }
}
