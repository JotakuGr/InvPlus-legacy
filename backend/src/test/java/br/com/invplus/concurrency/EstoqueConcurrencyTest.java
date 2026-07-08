package br.com.invplus.concurrency;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import br.com.invplus.modules.cadastros.entity.Categoria;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.UnidadeMedida;
import br.com.invplus.modules.cadastros.repository.CategoriaRepository;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.cadastros.repository.UnidadeMedidaRepository;
import br.com.invplus.modules.estoque.entity.Deposito;
import br.com.invplus.modules.estoque.entity.Estoque;
import br.com.invplus.modules.estoque.entity.LocalizacaoEstoque;
import br.com.invplus.modules.estoque.enums.TipoDeposito;
import br.com.invplus.modules.estoque.repository.DepositoRepository;
import br.com.invplus.modules.estoque.repository.EstoqueRepository;
import br.com.invplus.modules.estoque.repository.LocalizacaoEstoqueRepository;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.enums.NaturezaProduto;
import br.com.invplus.modules.produtos.enums.StatusProduto;
import br.com.invplus.modules.produtos.enums.TipoProduto;
import br.com.invplus.modules.produtos.repository.ProdutoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.RollbackException;

/**
 * Testes de concorrencia do saldo para validar o uso de @Version em cenarios operacionais.
 */
@SpringBootTest
@ActiveProfiles("test")
class EstoqueConcurrencyTest {

    @Autowired
    private EntityManagerFactory entityManagerFactory;
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
    void deveAplicarOptimisticLockQuandoDuasTransacoesReservamMesmoSaldo() throws Exception {
        Estoque estoque = criarEstoque("100.000");
        CyclicBarrier barreira = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<Boolean> usuarioA = executor.submit(reservarEmTransacaoManual(estoque.getId(), barreira));
            Future<Boolean> usuarioB = executor.submit(reservarEmTransacaoManual(estoque.getId(), barreira));

            long sucessos = List.of(usuarioA.get(), usuarioB.get()).stream().filter(Boolean::booleanValue).count();
            Estoque atualizado = estoqueRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(estoque.getId()).orElseThrow();

            assertThat(sucessos).isEqualTo(1);
            assertThat(atualizado.getQuantidadeReservada()).isEqualByComparingTo("60.000");
            assertThat(atualizado.getQuantidadeDisponivel()).isEqualByComparingTo("40.000");
        } finally {
            executor.shutdownNow();
        }
    }

    @SuppressWarnings("UseSpecificCatch")
    private Callable<Boolean> reservarEmTransacaoManual(UUID estoqueId, CyclicBarrier barreira) {
        return () -> {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            var tx = entityManager.getTransaction();
            try {
                tx.begin();
                Estoque estoque = entityManager.find(Estoque.class, estoqueId);
                barreira.await(10, TimeUnit.SECONDS);
                estoque.reservar(new BigDecimal("60.000"));
                tx.commit();
                return true;
            } catch (RollbackException ex) {
                return false;
            } catch (Exception ex) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                return false;
            } finally {
                entityManager.close();
            }
        };
    }

    private Estoque criarEstoque(String quantidade) {
        Empresa empresa = new Empresa();
        empresa.atualizar("Empresa Concorrencia " + UUID.randomUUID(), "Empresa", cnpjUnico(), "ISENTO", null,
                "NORMAL", 3, "71999990000", "empresa-concorrencia-" + UUID.randomUUID() + "@teste.com");
        empresa = empresaRepository.saveAndFlush(empresa);

        Deposito deposito = new Deposito();
        deposito.setEmpresa(empresa);
        deposito.atualizar("DEP-" + codigoCurto(), "Deposito Concorrencia", "Teste", TipoDeposito.CENTRAL,
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
        categoria.atualizar(null, "Categoria Concorrencia " + UUID.randomUUID(), "Teste", 0);
        categoria = categoriaRepository.saveAndFlush(categoria);

        UnidadeMedida unidade = new UnidadeMedida();
        unidade.atualizar("C" + codigoCurto().substring(0, 6), "Unidade concorrencia", (short) 0);
        unidade = unidadeRepository.saveAndFlush(unidade);

        Produto produto = new Produto();
        produto.setCategoria(categoria);
        produto.setUnidadeMedida(unidade);
        produto.atualizar("PROD-" + codigoCurto(), null, null, null, "Produto Concorrencia", "Teste",
                TipoProduto.PRODUTO, NaturezaProduto.AMBOS, StatusProduto.ATIVO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, false, false, false, true, true, true);
        return produtoRepository.saveAndFlush(produto);
    }

    private String codigoCurto() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private String cnpjUnico() {
        return "9%013d".formatted(Math.abs(UUID.randomUUID().getMostSignificantBits()) % 10_000_000_000_000L);
    }
}
