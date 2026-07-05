package br.com.invplus.modules.produtos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.invplus.common.enums.TipoPessoa;
import br.com.invplus.modules.cadastros.entity.Categoria;
import br.com.invplus.modules.cadastros.entity.UnidadeMedida;
import br.com.invplus.modules.pessoas.entity.Fornecedor;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.produtos.dto.request.ProdutoFornecedorPrecoRequest;
import br.com.invplus.modules.produtos.dto.response.ProdutoFornecedorPrecoResponse;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.entity.ProdutoFornecedor;
import br.com.invplus.modules.produtos.entity.ProdutoFornecedorPreco;
import br.com.invplus.modules.produtos.enums.FornecedorProdutoStatus;
import br.com.invplus.modules.produtos.enums.NaturezaProduto;
import br.com.invplus.modules.produtos.enums.StatusProduto;
import br.com.invplus.modules.produtos.enums.TipoProduto;
import br.com.invplus.modules.produtos.mapper.ProdutoFornecedorPrecoMapper;
import br.com.invplus.modules.produtos.repository.ProdutoFornecedorPrecoRepository;
import br.com.invplus.modules.produtos.repository.ProdutoFornecedorRepository;
import br.com.invplus.modules.produtos.validator.ProdutoFornecedorPrecoValidator;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ConflictException;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

/**
 * Testes unitarios do historico de custo de ProdutoFornecedor.
 */
@ExtendWith(MockitoExtension.class)
class ProdutoFornecedorPrecoServiceTest {

    @Mock
    private ProdutoFornecedorPrecoRepository repository;

    @Mock
    private ProdutoFornecedorRepository produtoFornecedorRepository;

    private ProdutoFornecedorPrecoService service;

    @BeforeEach
    void setUp() {
        ProdutoFornecedorPrecoMapper mapper = new ProdutoFornecedorPrecoMapper();
        ProdutoFornecedorPrecoValidator validator = new ProdutoFornecedorPrecoValidator(repository);
        service = new ProdutoFornecedorPrecoService(repository, mapper, validator, produtoFornecedorRepository);
    }

    /**
     * Registra preco historico para vinculo ativo.
     */
    @Test
    void deveCriarPrecoComSucesso() {
        ProdutoFornecedor vinculo = vinculoAtivo();
        ProdutoFornecedorPrecoRequest request = request(vinculo.getId(), LocalDate.of(2026, 1, 1));
        when(repository.findByProdutoFornecedorIdAndVigenciaInicioAndAtivoTrueAndDeletedAtIsNull(
                request.produtoFornecedorId(), request.vigenciaInicio())).thenReturn(Optional.empty());
        when(produtoFornecedorRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(vinculo.getId())).thenReturn(Optional.of(vinculo));
        when(repository.save(any(ProdutoFornecedorPreco.class))).thenAnswer(invocation -> {
            ProdutoFornecedorPreco preco = invocation.getArgument(0);
            preco.setId(UUID.randomUUID());
            return preco;
        });

        ProdutoFornecedorPrecoResponse response = service.criar(request);

        assertThat(response.produtoFornecedorId()).isEqualTo(vinculo.getId());
        assertThat(response.preco()).isEqualByComparingTo("11.50");
        assertThat(response.produtoNome()).isEqualTo("Produto Teste");
    }

    /**
     * Nao permite duplicar vigencia inicial ativa no mesmo vinculo.
     */
    @Test
    void deveBloquearVigenciaInicialDuplicada() {
        ProdutoFornecedorPreco preco = precoAtivo();
        ProdutoFornecedorPrecoRequest request = request(preco.getProdutoFornecedor().getId(), preco.getVigenciaInicio());
        when(repository.findByProdutoFornecedorIdAndVigenciaInicioAndAtivoTrueAndDeletedAtIsNull(
                request.produtoFornecedorId(), request.vigenciaInicio())).thenReturn(Optional.of(preco));

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Ja existe preco ativo para este vinculo nesta vigencia inicial");
    }

    /**
     * Vigencia final anterior a inicial deve retornar erro de negocio.
     */
    @Test
    void deveBloquearVigenciaFinalAnterior() {
        ProdutoFornecedorPrecoRequest request = new ProdutoFornecedorPrecoRequest(
                UUID.randomUUID(), BigDecimal.TEN, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 1)
        );

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Vigencia final nao pode ser anterior a inicial");
    }

    /**
     * Filtros de historico devem usar Specification.
     */
    @Test
    @SuppressWarnings("unchecked")
    void deveListarComFiltros() {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        service.listarComFiltros(UUID.randomUUID(), LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), true, PageRequest.of(0, 20));

        verify(repository).findAll(any(Specification.class), any(Pageable.class));
    }

    /**
     * Confirma exclusao logica do preco historico.
     */
    @Test
    void deveExcluirLogicamente() {
        ProdutoFornecedorPreco preco = precoAtivo();
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(preco.getId())).thenReturn(Optional.of(preco));

        service.excluir(preco.getId());

        assertThat(preco.getAtivo()).isFalse();
        assertThat(preco.getDeletedAt()).isNotNull();
    }

    private ProdutoFornecedorPrecoRequest request(UUID vinculoId, LocalDate inicio) {
        return new ProdutoFornecedorPrecoRequest(vinculoId, BigDecimal.valueOf(11.50), inicio, null);
    }

    private ProdutoFornecedorPreco precoAtivo() {
        ProdutoFornecedorPreco preco = new ProdutoFornecedorPreco();
        preco.setId(UUID.randomUUID());
        preco.setProdutoFornecedor(vinculoAtivo());
        preco.atualizar(BigDecimal.valueOf(11.50), LocalDate.of(2026, 1, 1), null);
        return preco;
    }

    private ProdutoFornecedor vinculoAtivo() {
        ProdutoFornecedor vinculo = new ProdutoFornecedor();
        vinculo.setId(UUID.randomUUID());
        vinculo.setProduto(produtoAtivo());
        vinculo.setFornecedor(fornecedorAtivo());
        vinculo.atualizar("FORN-001", 3, true, 1, FornecedorProdutoStatus.PREFERENCIAL, "Principal");
        return vinculo;
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

    private Fornecedor fornecedorAtivo() {
        Pessoa pessoa = new Pessoa();
        pessoa.setId(UUID.randomUUID());
        pessoa.atualizar(TipoPessoa.JURIDICA, "Fornecedor Teste", "Fornecedor", "13123456000199",
                null, "7133334444", null, "fornecedor@teste.com", null);
        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setId(UUID.randomUUID());
        fornecedor.setPessoa(pessoa);
        fornecedor.atualizar(UUID.randomUUID(), 7, "Fornecedor ativo");
        return fornecedor;
    }
}
