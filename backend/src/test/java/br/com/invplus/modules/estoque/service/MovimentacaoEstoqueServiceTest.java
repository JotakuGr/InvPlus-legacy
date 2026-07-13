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
import br.com.invplus.modules.estoque.dto.request.MovimentacaoEstoqueRequest;
import br.com.invplus.modules.estoque.entity.Deposito;
import br.com.invplus.modules.estoque.entity.Estoque;
import br.com.invplus.modules.estoque.entity.LocalizacaoEstoque;
import br.com.invplus.modules.estoque.entity.MovimentacaoEstoque;
import br.com.invplus.modules.estoque.enums.TipoDeposito;
import br.com.invplus.modules.estoque.enums.MovimentacaoOrigem;
import br.com.invplus.modules.estoque.enums.TipoMovimentacaoEstoque;
import br.com.invplus.modules.estoque.mapper.MovimentacaoEstoqueMapper;
import br.com.invplus.modules.estoque.repository.EstoqueRepository;
import br.com.invplus.modules.estoque.repository.MovimentacaoEstoqueRepository;
import br.com.invplus.modules.estoque.validator.MovimentacaoEstoqueValidator;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.enums.NaturezaProduto;
import br.com.invplus.modules.produtos.enums.StatusProduto;
import br.com.invplus.modules.produtos.enums.TipoProduto;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import br.com.invplus.security.context.EmpresaContext;

/**
 * Testes unitarios do historico imutavel de movimentacoes de estoque.
 */
@SuppressWarnings("unused")
@ExtendWith(MockitoExtension.class)
class MovimentacaoEstoqueServiceTest {

    @Mock
    private MovimentacaoEstoqueRepository repository;
    @Mock
    private EstoqueRepository estoqueRepository;
    @Mock
    private EmpresaContext empresaContext;

    private MovimentacaoEstoqueService service;
    private UUID empresaId;

    @BeforeEach
    void setUp() {
        empresaId = UUID.randomUUID();
        lenient().when(empresaContext.getEmpresaId()).thenReturn(empresaId);
        service = new MovimentacaoEstoqueService(repository, estoqueRepository,
                new MovimentacaoEstoqueMapper(), new MovimentacaoEstoqueValidator(), empresaContext);
    }

    @Test
    void deveRegistrarEntrada() {
        Estoque estoque = estoque();
        MovimentacaoEstoqueRequest request = request(estoque, TipoMovimentacaoEstoque.ENTRADA, "10.000");
        when(estoqueRepository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(estoque.getId(), empresaId))
                .thenReturn(Optional.of(estoque));
        when(repository.save(any(MovimentacaoEstoque.class))).thenAnswer(invocation -> {
            MovimentacaoEstoque entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        MovimentacaoEstoque response = service.registrarInterna(request);

        assertThat(response.getTipoMovimentacao()).isEqualTo(TipoMovimentacaoEstoque.ENTRADA);
        assertThat(response.getQuantidade()).isEqualByComparingTo("10.000");
        assertThat(response.getEstoque().getId()).isEqualTo(estoque.getId());
    }

    @Test
    void deveRegistrarSaida() {
        Estoque estoque = estoque();
        when(repository.save(any(MovimentacaoEstoque.class))).thenAnswer(invocation -> {
            MovimentacaoEstoque entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        MovimentacaoEstoque response = service.registrarInterna(estoque, TipoMovimentacaoEstoque.SAIDA,
                bd("2.000"), bd("10.000"), bd("8.000"), MovimentacaoOrigem.API, UUID.randomUUID(), "DOC-1", "Saida");

        assertThat(response.getTipoMovimentacao()).isEqualTo(TipoMovimentacaoEstoque.SAIDA);
        assertThat(response.getSaldoPosterior()).isEqualByComparingTo("8.000");
    }

    @Test
    void deveBloquearQuantidadeZeroOuNegativa() {
        Estoque estoque = estoque();
        MovimentacaoEstoqueRequest request = request(estoque, TipoMovimentacaoEstoque.ENTRADA, "0.000");

        assertThatThrownBy(() -> service.registrarInterna(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Quantidade deve ser maior que zero");
    }

    @Test
    @SuppressWarnings("unchecked")
    void deveListarComFiltros() {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        service.listarComFiltros(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), TipoMovimentacaoEstoque.ENTRADA, MovimentacaoOrigem.API, null, null,
                PageRequest.of(0, 20));

        verify(repository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void naoDevePermitirAlteracaoOuExclusaoFuncional() {
        assertThatThrownBy(() -> service.bloquearAlteracao())
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("nao pode ser alterada");

        assertThatThrownBy(() -> service.bloquearExclusao())
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("nao pode ser excluida");
    }

    private MovimentacaoEstoqueRequest request(Estoque estoque, TipoMovimentacaoEstoque tipo, String quantidade) {
        return new MovimentacaoEstoqueRequest(estoque.getId(), tipo, bd(quantidade), bd("0.000"),
                bd(quantidade), MovimentacaoOrigem.API, UUID.randomUUID(), "DOC-1", "Teste", null);
    }

    private Estoque estoque() {
        Empresa empresa = new Empresa();
        empresa.setId(empresaId);
        empresa.atualizar("Empresa Mov", "Empresa", "12345678000199", "ISENTO", null, "NORMAL", 3, "71999990000", "empresa@teste.com");
        Deposito deposito = new Deposito();
        deposito.setId(UUID.randomUUID());
        deposito.setEmpresa(empresa);
        deposito.atualizar("DEP-01", "Deposito", "Teste", TipoDeposito.CENTRAL, false, true, true, true, true, false);
        LocalizacaoEstoque localizacao = new LocalizacaoEstoque();
        localizacao.setId(UUID.randomUUID());
        localizacao.setDeposito(deposito);
        localizacao.atualizar("A-01", "Rua A", "Rua A", "03", "02", "04", "15", false, false, "Teste");

        Estoque estoque = new Estoque();
        estoque.setId(UUID.randomUUID());
        estoque.setProduto(produto());
        estoque.setEmpresa(empresa);
        estoque.setDeposito(deposito);
        estoque.setLocalizacao(localizacao);
        estoque.atualizarSaldos(bd("10.000"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null, null, null);
        return estoque;
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
}
