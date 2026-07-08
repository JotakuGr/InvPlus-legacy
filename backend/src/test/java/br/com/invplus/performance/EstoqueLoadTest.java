package br.com.invplus.performance;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.invplus.modules.cadastros.entity.Categoria;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.UnidadeMedida;
import br.com.invplus.modules.cadastros.repository.CategoriaRepository;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.cadastros.repository.UnidadeMedidaRepository;
import br.com.invplus.modules.estoque.dto.request.ReservaEstoqueRequest;
import br.com.invplus.modules.estoque.entity.Deposito;
import br.com.invplus.modules.estoque.entity.Estoque;
import br.com.invplus.modules.estoque.entity.LocalizacaoEstoque;
import br.com.invplus.modules.estoque.enums.MovimentacaoOrigem;
import br.com.invplus.modules.estoque.enums.TipoDeposito;
import br.com.invplus.modules.estoque.repository.DepositoRepository;
import br.com.invplus.modules.estoque.repository.EstoqueRepository;
import br.com.invplus.modules.estoque.repository.LocalizacaoEstoqueRepository;
import br.com.invplus.modules.estoque.service.ReservaEstoqueService;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.enums.NaturezaProduto;
import br.com.invplus.modules.produtos.enums.StatusProduto;
import br.com.invplus.modules.produtos.enums.TipoProduto;
import br.com.invplus.modules.produtos.repository.ProdutoRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Teste simples de carga local para validar reservas simultaneas sem perder consistencia de saldo.
 */
@SpringBootTest
@ActiveProfiles("test")
class EstoqueLoadTest {

    @Autowired
    private ReservaEstoqueService reservaService;
    @Autowired
    private EstoqueRepository estoqueRepository;
    @Autowired
    private ProdutoRepository produtoRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private UnidadeMedidaRepository unidadeRepository;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private DepositoRepository depositoRepository;
    @Autowired
    private LocalizacaoEstoqueRepository localizacaoRepository;

    @Test
    void deveManterSaldoConsistenteComReservasSimultaneas() throws Exception {
        Estoque estoque = criarEstoque("100.000");
        ExecutorService executor = Executors.newFixedThreadPool(50);
        CountDownLatch inicio = new CountDownLatch(1);
        AtomicInteger sucessos = new AtomicInteger();
        List<Future<?>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < 100; i++) {
                futures.add(executor.submit(() -> {
                    inicio.await(10, TimeUnit.SECONDS);
                    try {
                        reservaService.criar(new ReservaEstoqueRequest(
                                estoque.getId(),
                                BigDecimal.ONE,
                                MovimentacaoOrigem.VENDA,
                                UUID.randomUUID(),
                                null,
                                "Carga concorrente"
                        ));
                        sucessos.incrementAndGet();
                    } catch (RuntimeException ignored) {
                        // Em concorrencia real, algumas transacoes podem perder pelo versionamento otimista.
                    }
                    return null;
                }));
            }

            inicio.countDown();
            for (Future<?> future : futures) {
                future.get();
            }

            Estoque atualizado = estoqueRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(estoque.getId()).orElseThrow();
            assertThat(sucessos.get()).isBetween(1, 100);
            assertThat(atualizado.getQuantidadeReservada()).isEqualByComparingTo(BigDecimal.valueOf(sucessos.get()));
            assertThat(atualizado.getQuantidadeDisponivel())
                    .isEqualByComparingTo(BigDecimal.valueOf(100L - sucessos.get()));
        } finally {
            executor.shutdownNow();
        }
    }

    private Estoque criarEstoque(String quantidade) {
        Empresa empresa = new Empresa();
        empresa.atualizar("Empresa Performance " + UUID.randomUUID(), "Empresa", cnpjUnico(), "ISENTO", null,
                "NORMAL", 3, "71999990000", "empresa-performance-" + UUID.randomUUID() + "@teste.com");
        empresa = empresaRepository.saveAndFlush(empresa);

        Deposito deposito = new Deposito();
        deposito.setEmpresa(empresa);
        deposito.atualizar("DEP-" + codigoCurto(), "Deposito Performance", "Teste", TipoDeposito.CENTRAL,
                false, true, true, true, true, false);
        deposito = depositoRepository.saveAndFlush(deposito);

        LocalizacaoEstoque localizacao = new LocalizacaoEstoque();
        localizacao.setDeposito(deposito);
        localizacao.atualizar("LOC-" + codigoCurto(), "Rua A", "Rua A", "03", "02", "04", "15",
                false, false, "Teste");
        localizacao = localizacaoRepository.saveAndFlush(localizacao);

        Estoque estoque = new Estoque();
        estoque.setProduto(produto());
        estoque.setEmpresa(empresa);
        estoque.setDeposito(deposito);
        estoque.setLocalizacao(localizacao);
        estoque.atualizarSaldos(new BigDecimal(quantidade), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, null, null, null);
        return estoqueRepository.saveAndFlush(estoque);
    }

    private Produto produto() {
        Categoria categoria = new Categoria();
        categoria.atualizar(null, "Categoria Performance " + UUID.randomUUID(), "Teste", 0);
        categoria = categoriaRepository.saveAndFlush(categoria);

        UnidadeMedida unidade = new UnidadeMedida();
        unidade.atualizar("P" + codigoCurto().substring(0, 6), "Unidade performance", (short) 0);
        unidade = unidadeRepository.saveAndFlush(unidade);

        Produto produto = new Produto();
        produto.setCategoria(categoria);
        produto.setUnidadeMedida(unidade);
        produto.atualizar("PROD-" + codigoCurto(), null, null, null, "Produto Performance", "Teste",
                TipoProduto.PRODUTO, NaturezaProduto.AMBOS, StatusProduto.ATIVO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, false, false, false, true, true, true);
        return produtoRepository.saveAndFlush(produto);
    }

    private String codigoCurto() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private String cnpjUnico() {
        return "8%013d".formatted(Math.abs(UUID.randomUUID().getMostSignificantBits()) % 10_000_000_000_000L);
    }
}
