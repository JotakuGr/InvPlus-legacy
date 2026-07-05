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
import br.com.invplus.modules.pessoas.repository.FornecedorRepository;
import br.com.invplus.modules.produtos.dto.request.ProdutoFornecedorRequest;
import br.com.invplus.modules.produtos.dto.response.ProdutoFornecedorResponse;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.entity.ProdutoFornecedor;
import br.com.invplus.modules.produtos.enums.FornecedorProdutoStatus;
import br.com.invplus.modules.produtos.enums.NaturezaProduto;
import br.com.invplus.modules.produtos.enums.StatusProduto;
import br.com.invplus.modules.produtos.enums.TipoProduto;
import br.com.invplus.modules.produtos.mapper.ProdutoFornecedorMapper;
import br.com.invplus.modules.produtos.repository.ProdutoFornecedorRepository;
import br.com.invplus.modules.produtos.repository.ProdutoRepository;
import br.com.invplus.modules.produtos.validator.ProdutoFornecedorValidator;
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
 * Testes unitarios das regras do vinculo ProdutoFornecedor.
 */
@ExtendWith(MockitoExtension.class)
class ProdutoFornecedorServiceTest {

    @Mock
    private ProdutoFornecedorRepository produtoFornecedorRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private FornecedorRepository fornecedorRepository;

    private ProdutoFornecedorService service;

    @BeforeEach
    void setUp() {
        ProdutoFornecedorMapper mapper = new ProdutoFornecedorMapper();
        ProdutoFornecedorValidator validator = new ProdutoFornecedorValidator(produtoFornecedorRepository);
        service = new ProdutoFornecedorService(
                produtoFornecedorRepository,
                mapper,
                validator,
                produtoRepository,
                fornecedorRepository
        );
    }

    /**
     * Cria vinculo com Produto e Fornecedor ativos.
     */
    @Test
    void deveCriarProdutoFornecedorComSucesso() {
        Produto produto = produtoAtivo();
        Fornecedor fornecedor = fornecedorAtivo();
        when(produtoFornecedorRepository.existsByProdutoIdAndFornecedorIdAndAtivoTrueAndDeletedAtIsNull(produto.getId(), fornecedor.getId()))
                .thenReturn(false);
        when(produtoFornecedorRepository.findByProdutoIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(produto.getId()))
                .thenReturn(Optional.empty());
        when(produtoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(produto.getId())).thenReturn(Optional.of(produto));
        when(fornecedorRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(fornecedor.getId())).thenReturn(Optional.of(fornecedor));
        when(produtoFornecedorRepository.save(any(ProdutoFornecedor.class))).thenAnswer(invocation -> {
            ProdutoFornecedor entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        ProdutoFornecedorResponse response = service.criar(request(produto.getId(), fornecedor.getId(), true));

        assertThat(response.produtoId()).isEqualTo(produto.getId());
        assertThat(response.fornecedorId()).isEqualTo(fornecedor.getId());
        assertThat(response.fornecedorNome()).isEqualTo("Fornecedor Teste");
        assertThat(response.principal()).isTrue();
    }

    /**
     * Produto inexistente ou inativo deve bloquear o vinculo.
     */
    @Test
    void deveBloquearProdutoInexistenteOuInativo() {
        UUID produtoId = UUID.randomUUID();
        UUID fornecedorId = UUID.randomUUID();
        when(produtoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(produtoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(request(produtoId, fornecedorId, false)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Produto nao encontrado");
    }

    /**
     * Fornecedor inexistente ou inativo deve bloquear o vinculo.
     */
    @Test
    void deveBloquearFornecedorInexistenteOuInativo() {
        Produto produto = produtoAtivo();
        UUID fornecedorId = UUID.randomUUID();
        when(produtoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(produto.getId())).thenReturn(Optional.of(produto));
        when(fornecedorRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(fornecedorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(request(produto.getId(), fornecedorId, false)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Fornecedor nao encontrado");
    }

    /**
     * O mesmo produto nao pode repetir fornecedor ativo.
     */
    @Test
    void deveBloquearProdutoFornecedorDuplicado() {
        UUID produtoId = UUID.randomUUID();
        UUID fornecedorId = UUID.randomUUID();
        when(produtoFornecedorRepository.existsByProdutoIdAndFornecedorIdAndAtivoTrueAndDeletedAtIsNull(produtoId, fornecedorId))
                .thenReturn(true);

        assertThatThrownBy(() -> service.criar(request(produtoId, fornecedorId, false)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Produto ja possui vinculo ativo com este fornecedor");
    }

    /**
     * Lead time negativo deve retornar erro de negocio.
     */
    @Test
    void deveBloquearLeadTimeNegativo() {
        ProdutoFornecedorRequest request = new ProdutoFornecedorRequest(
                UUID.randomUUID(), UUID.randomUUID(), "COD-F", -1, false, 1,
                FornecedorProdutoStatus.PREFERENCIAL, null
        );

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Lead time nao pode ser negativo");
    }

    /**
     * Prioridade negativa deve retornar erro de negocio.
     */
    @Test
    void deveBloquearPrioridadeNegativa() {
        ProdutoFornecedorRequest request = new ProdutoFornecedorRequest(
                UUID.randomUUID(), UUID.randomUUID(), "COD-F", 3, false, -1,
                FornecedorProdutoStatus.PREFERENCIAL, null
        );

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Prioridade nao pode ser negativa");
    }

    /**
     * Apenas um fornecedor principal pode existir para cada produto.
     */
    @Test
    void deveBloquearSegundoFornecedorPrincipal() {
        ProdutoFornecedor principal = vinculoAtivo();
        UUID fornecedorId = UUID.randomUUID();
        when(produtoFornecedorRepository.findByProdutoIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(principal.getProduto().getId()))
                .thenReturn(Optional.of(principal));

        assertThatThrownBy(() -> service.criar(request(principal.getProduto().getId(), fornecedorId, true)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Produto ja possui fornecedor principal ativo");
    }

    /**
     * Atualizacao pode manter o proprio registro como principal.
     */
    @Test
    void devePermitirAtualizarProprioRegistroMantendoPrincipal() {
        ProdutoFornecedor vinculo = vinculoAtivo();
        Produto produto = vinculo.getProduto();
        Fornecedor fornecedor = vinculo.getFornecedor();
        when(produtoFornecedorRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(vinculo.getId())).thenReturn(Optional.of(vinculo));
        when(produtoFornecedorRepository.findByProdutoIdAndFornecedorIdAndAtivoTrueAndDeletedAtIsNull(produto.getId(), fornecedor.getId()))
                .thenReturn(Optional.of(vinculo));
        when(produtoFornecedorRepository.findByProdutoIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(produto.getId()))
                .thenReturn(Optional.of(vinculo));
        when(produtoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(produto.getId())).thenReturn(Optional.of(produto));
        when(fornecedorRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(fornecedor.getId())).thenReturn(Optional.of(fornecedor));

        ProdutoFornecedorResponse response = service.atualizar(vinculo.getId(), request(produto.getId(), fornecedor.getId(), true));

        assertThat(response.id()).isEqualTo(vinculo.getId());
        assertThat(response.principal()).isTrue();
    }

    /**
     * Busca vinculo ativo por id.
     */
    @Test
    void deveBuscarPorId() {
        ProdutoFornecedor vinculo = vinculoAtivo();
        when(produtoFornecedorRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(vinculo.getId())).thenReturn(Optional.of(vinculo));

        ProdutoFornecedorResponse response = service.buscar(vinculo.getId());

        assertThat(response.id()).isEqualTo(vinculo.getId());
        assertThat(response.produtoNome()).isEqualTo("Produto Teste");
    }

    /**
     * Listagem com filtros deve usar Specification.
     */
    @Test
    @SuppressWarnings("unchecked")
    void deveListarComFiltros() {
        when(produtoFornecedorRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        service.listarComFiltros(UUID.randomUUID(), UUID.randomUUID(), true, true, "COD",
                FornecedorProdutoStatus.PREFERENCIAL, PageRequest.of(0, 20));

        verify(produtoFornecedorRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    /**
     * Confirma exclusao logica do vinculo.
     */
    @Test
    void deveExcluirLogicamente() {
        ProdutoFornecedor vinculo = vinculoAtivo();
        when(produtoFornecedorRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(vinculo.getId())).thenReturn(Optional.of(vinculo));

        service.excluir(vinculo.getId());

        assertThat(vinculo.getAtivo()).isFalse();
        assertThat(vinculo.getDeletedAt()).isNotNull();
    }

    private ProdutoFornecedorRequest request(UUID produtoId, UUID fornecedorId, Boolean principal) {
        return new ProdutoFornecedorRequest(
                produtoId,
                fornecedorId,
                "FORN-001",
                5,
                principal,
                1,
                FornecedorProdutoStatus.PREFERENCIAL,
                "Fornecedor preferencial"
        );
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
