package br.com.invplus.modules.produtos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.invplus.modules.cadastros.entity.Categoria;
import br.com.invplus.modules.cadastros.entity.UnidadeMedida;
import br.com.invplus.modules.produtos.dto.request.ProdutoFiscalRequest;
import br.com.invplus.modules.produtos.dto.response.ProdutoFiscalResponse;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.entity.ProdutoFiscal;
import br.com.invplus.modules.produtos.enums.NaturezaProduto;
import br.com.invplus.modules.produtos.enums.OrigemMercadoria;
import br.com.invplus.modules.produtos.enums.StatusProduto;
import br.com.invplus.modules.produtos.enums.TipoProduto;
import br.com.invplus.modules.produtos.mapper.ProdutoFiscalMapper;
import br.com.invplus.modules.produtos.repository.ProdutoFiscalRepository;
import br.com.invplus.modules.produtos.repository.ProdutoRepository;
import br.com.invplus.modules.produtos.validator.ProdutoFiscalValidator;
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
 * Testes unitarios das regras de ProdutoFiscal.
 */
@ExtendWith(MockitoExtension.class)
class ProdutoFiscalServiceTest {

    @Mock
    private ProdutoFiscalRepository repository;

    @Mock
    private ProdutoRepository produtoRepository;

    private ProdutoFiscalService service;

    @BeforeEach
    void setUp() {
        ProdutoFiscalMapper mapper = new ProdutoFiscalMapper();
        ProdutoFiscalValidator validator = new ProdutoFiscalValidator(repository);
        service = new ProdutoFiscalService(repository, mapper, validator, produtoRepository);
    }

    /**
     * Cria dados fiscais para produto ativo sem duplicidade.
     */
    @Test
    void deveCriarProdutoFiscalComSucesso() {
        Produto produto = produtoAtivo();
        ProdutoFiscalRequest request = request(produto.getId(), "12345678", "1234567");
        when(repository.findByProdutoIdAndAtivoTrueAndDeletedAtIsNull(produto.getId())).thenReturn(Optional.empty());
        when(produtoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(produto.getId())).thenReturn(Optional.of(produto));
        when(repository.save(any(ProdutoFiscal.class))).thenAnswer(invocation -> {
            ProdutoFiscal fiscal = invocation.getArgument(0);
            fiscal.setId(UUID.randomUUID());
            return fiscal;
        });

        ProdutoFiscalResponse response = service.criar(request);

        assertThat(response.produtoId()).isEqualTo(produto.getId());
        assertThat(response.produtoCodigo()).isEqualTo("PROD-001");
        assertThat(response.ncm()).isEqualTo("12345678");
        assertThat(response.origemMercadoria()).isEqualTo(OrigemMercadoria.NACIONAL);
    }

    /**
     * Produto inexistente ou inativo deve impedir criacao fiscal.
     */
    @Test
    void deveBloquearProdutoInexistente() {
        UUID produtoId = UUID.randomUUID();
        ProdutoFiscalRequest request = request(produtoId, "12345678", null);
        when(repository.findByProdutoIdAndAtivoTrueAndDeletedAtIsNull(produtoId)).thenReturn(Optional.empty());
        when(produtoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(produtoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Produto nao encontrado");
    }

    /**
     * Produto nao pode ter duas fichas fiscais ativas.
     */
    @Test
    void deveBloquearProdutoFiscalDuplicado() {
        ProdutoFiscal fiscal = fiscalAtivo();
        ProdutoFiscalRequest request = request(fiscal.getProduto().getId(), "12345678", null);
        when(repository.findByProdutoIdAndAtivoTrueAndDeletedAtIsNull(fiscal.getProduto().getId())).thenReturn(Optional.of(fiscal));

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Produto ja possui dados fiscais ativos");
    }

    /**
     * NCM deve conter oito digitos.
     */
    @Test
    void deveBloquearNcmInvalido() {
        ProdutoFiscalRequest request = request(UUID.randomUUID(), "123", null);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("NCM deve conter 8 digitos");
    }

    /**
     * CEST informado deve conter sete digitos.
     */
    @Test
    void deveBloquearCestInvalido() {
        ProdutoFiscalRequest request = request(UUID.randomUUID(), "12345678", "123");

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CEST deve conter 7 digitos");
    }

    /**
     * Aliquotas negativas sao rejeitadas antes de persistir.
     */
    @Test
    void deveBloquearAliquotaNegativa() {
        ProdutoFiscalRequest request = new ProdutoFiscalRequest(
                UUID.randomUUID(), "12345678", null, OrigemMercadoria.NACIONAL,
                "5102", "00", null, "01", "01",
                BigDecimal.valueOf(-1), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null
        );

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Aliquota ICMS nao pode ser negativa");
    }

    /**
     * Busca por id retorna response com produto associado.
     */
    @Test
    void deveBuscarPorId() {
        ProdutoFiscal fiscal = fiscalAtivo();
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(fiscal.getId())).thenReturn(Optional.of(fiscal));

        ProdutoFiscalResponse response = service.buscar(fiscal.getId());

        assertThat(response.id()).isEqualTo(fiscal.getId());
        assertThat(response.produtoNome()).isEqualTo("Produto Teste");
    }

    /**
     * Filtros devem delegar para Specification paginada.
     */
    @Test
    @SuppressWarnings("unchecked")
    void deveListarComFiltros() {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        service.listarComFiltros(UUID.randomUUID(), "12345678", "1234567",
                OrigemMercadoria.NACIONAL, true, PageRequest.of(0, 20));

        verify(repository).findAll(any(Specification.class), any(Pageable.class));
    }

    /**
     * Atualiza dados fiscais mantendo a regra de unicidade por produto.
     */
    @Test
    void deveAtualizarProdutoFiscal() {
        ProdutoFiscal fiscal = fiscalAtivo();
        ProdutoFiscalRequest request = request(fiscal.getProduto().getId(), "87654321", null);
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(fiscal.getId())).thenReturn(Optional.of(fiscal));
        when(repository.findByProdutoIdAndAtivoTrueAndDeletedAtIsNull(fiscal.getProduto().getId())).thenReturn(Optional.of(fiscal));
        when(produtoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(fiscal.getProduto().getId())).thenReturn(Optional.of(fiscal.getProduto()));

        ProdutoFiscalResponse response = service.atualizar(fiscal.getId(), request);

        assertThat(response.ncm()).isEqualTo("87654321");
    }

    /**
     * Exclusao logica mantem historico e oculta em consultas padrao.
     */
    @Test
    void deveExcluirLogicamente() {
        ProdutoFiscal fiscal = fiscalAtivo();
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(fiscal.getId())).thenReturn(Optional.of(fiscal));

        service.excluir(fiscal.getId());

        assertThat(fiscal.getAtivo()).isFalse();
        assertThat(fiscal.getDeletedAt()).isNotNull();
    }

    private ProdutoFiscalRequest request(UUID produtoId, String ncm, String cest) {
        return new ProdutoFiscalRequest(
                produtoId, ncm, cest, OrigemMercadoria.NACIONAL,
                "5102", "00", "102", "01", "01",
                BigDecimal.valueOf(18), BigDecimal.ZERO, BigDecimal.valueOf(1.65),
                BigDecimal.valueOf(7.60), "Fiscal"
        );
    }

    private ProdutoFiscal fiscalAtivo() {
        ProdutoFiscal fiscal = new ProdutoFiscal();
        fiscal.setId(UUID.randomUUID());
        fiscal.setProduto(produtoAtivo());
        fiscal.atualizar("12345678", "1234567", OrigemMercadoria.NACIONAL,
                "5102", "00", "102", "01", "01",
                BigDecimal.valueOf(18), BigDecimal.ZERO, BigDecimal.valueOf(1.65),
                BigDecimal.valueOf(7.60), "Fiscal");
        return fiscal;
    }

    private Produto produtoAtivo() {
        Categoria categoria = new Categoria();
        categoria.setId(UUID.randomUUID());
        categoria.atualizar(null, "Categoria Produto", null, 0);
        UnidadeMedida unidade = new UnidadeMedida();
        unidade.setId(UUID.randomUUID());
        unidade.atualizar("UN", "Unidade", (short) 0);
        Produto produto = new Produto();
        produto.setId(UUID.randomUUID());
        produto.setCategoria(categoria);
        produto.setUnidadeMedida(unidade);
        produto.atualizar("PROD-001", null, null, null, "Produto Teste", null,
                TipoProduto.PRODUTO, NaturezaProduto.AMBOS, StatusProduto.ATIVO,
                BigDecimal.TEN, BigDecimal.valueOf(20), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                false, false, false, true, true, true);
        return produto;
    }
}
