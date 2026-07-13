package br.com.invplus.modules.financeiro.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.invplus.common.enums.TipoPessoa;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.financeiro.dto.request.GerarParcelasPagarRequest;
import br.com.invplus.modules.financeiro.dto.request.ParcelaPagarBaixaRequest;
import br.com.invplus.modules.financeiro.dto.request.ParcelaPagarRequest;
import br.com.invplus.modules.financeiro.dto.response.ParcelaPagarResponse;
import br.com.invplus.modules.financeiro.entity.ContaPagar;
import br.com.invplus.modules.financeiro.entity.ParcelaPagar;
import br.com.invplus.modules.financeiro.enums.RegraVencimento;
import br.com.invplus.modules.financeiro.enums.StatusContaPagar;
import br.com.invplus.modules.financeiro.enums.StatusParcelaPagar;
import br.com.invplus.modules.financeiro.mapper.ParcelaPagarMapper;
import br.com.invplus.modules.financeiro.repository.ContaPagarRepository;
import br.com.invplus.modules.financeiro.repository.ParcelaPagarRepository;
import br.com.invplus.modules.financeiro.validator.ParcelaPagarValidator;
import br.com.invplus.modules.pagamentos.entity.CondicaoPagamento;
import br.com.invplus.modules.pagamentos.entity.CondicaoPagamentoParcela;
import br.com.invplus.modules.pagamentos.repository.CondicaoPagamentoRepository;
import br.com.invplus.modules.pessoas.entity.Fornecedor;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;

/**
 * Testes unitarios das regras de ParcelaPagar.
 */
@ExtendWith(MockitoExtension.class)
class ParcelaPagarServiceTest {

    @Mock
    private ParcelaPagarRepository repository;
    @Mock
    private ContaPagarRepository contaPagarRepository;
    @Mock
    private CondicaoPagamentoRepository condicaoPagamentoRepository;

    private ParcelaPagarService service;
    private ContaPagar conta;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        service = new ParcelaPagarService(repository, contaPagarRepository, condicaoPagamentoRepository,
                new GeradorParcelasService(new CalculadoraVencimentoService()), new ParcelaPagarMapper(),
                new ParcelaPagarValidator(repository));
        conta = conta();
    }

    @Test
    void deveCriarParcelaComSucesso() {
        when(contaPagarRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(conta.getId())).thenReturn(Optional.of(conta));
        when(repository.findByContaPagarIdAndNumeroParcelaAndAtivoTrueAndDeletedAtIsNull(conta.getId(), 1))
                .thenReturn(Optional.empty());
        when(repository.save(any(ParcelaPagar.class))).thenAnswer(invocation -> {
            ParcelaPagar parcela = invocation.getArgument(0);
            parcela.setId(UUID.randomUUID());
            return parcela;
        });

        ParcelaPagarResponse response = service.criar(request(1, "300.00"));

        assertThat(response.status()).isEqualTo(StatusParcelaPagar.ABERTA);
        assertThat(response.valorPago()).isEqualByComparingTo("0.00");
        assertThat(response.valorAberto()).isEqualByComparingTo("300.00");
    }

    @Test
    void deveBloquearNumeroDuplicado() {
        ParcelaPagar existente = parcela(1, "300.00");
        when(contaPagarRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(conta.getId())).thenReturn(Optional.of(conta));
        when(repository.findByContaPagarIdAndNumeroParcelaAndAtivoTrueAndDeletedAtIsNull(conta.getId(), 1))
                .thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.criar(request(1, "300.00")))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Ja existe parcela");
    }

    @Test
    void deveBloquearContaCancelada() {
        conta.setStatus(StatusContaPagar.CANCELADA);
        when(contaPagarRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(conta.getId())).thenReturn(Optional.of(conta));

        assertThatThrownBy(() -> service.criar(request(1, "300.00")))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("cancelada");
    }

    @Test
    void deveGerarParcelasPelaCondicaoPagamento() {
        CondicaoPagamento condicao = condicaoTresParcelas();
        when(contaPagarRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(conta.getId())).thenReturn(Optional.of(conta));
        when(repository.existsByContaPagarIdAndAtivoTrueAndDeletedAtIsNull(conta.getId())).thenReturn(false);
        when(condicaoPagamentoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(condicao.getId())).thenReturn(Optional.of(condicao));
        when(repository.saveAll(any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            List<ParcelaPagar> parcelas = invocation.getArgument(0);
            parcelas.forEach(parcela -> parcela.setId(UUID.randomUUID()));
            return parcelas;
        });

        List<ParcelaPagarResponse> response = service.gerarParcelas(conta.getId(),
                new GerarParcelasPagarRequest(condicao.getId(), LocalDate.of(2026, 7, 10)));

        assertThat(response).hasSize(3);
        assertThat(response).extracting(ParcelaPagarResponse::valor)
                .containsExactly(new BigDecimal("300.00"), new BigDecimal("300.00"), new BigDecimal("300.00"));
    }

    @Test
    void deveBaixarParcelaTotalEAtualizarConta() {
        ParcelaPagar parcela = parcela(1, "300.00");
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(parcela.getId())).thenReturn(Optional.of(parcela));
        when(repository.findByContaPagarIdAndAtivoTrueAndDeletedAtIsNull(conta.getId())).thenReturn(List.of(parcela));

        ParcelaPagarResponse response = service.baixar(parcela.getId(),
                new ParcelaPagarBaixaRequest(new BigDecimal("300.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, LocalDate.of(2026, 7, 10), "PIX"));

        assertThat(response.status()).isEqualTo(StatusParcelaPagar.PAGA);
        assertThat(conta.getValorPago()).isEqualByComparingTo("300.00");
        assertThat(conta.getStatus()).isEqualTo(StatusContaPagar.PARCIALMENTE_PAGA);
    }

    @Test
    void deveBaixarParcelaParcial() {
        ParcelaPagar parcela = parcela(1, "300.00");
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(parcela.getId())).thenReturn(Optional.of(parcela));
        when(repository.findByContaPagarIdAndAtivoTrueAndDeletedAtIsNull(conta.getId())).thenReturn(List.of(parcela));

        ParcelaPagarResponse response = service.baixar(parcela.getId(),
                new ParcelaPagarBaixaRequest(new BigDecimal("100.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, LocalDate.of(2026, 7, 10), "Parcial"));

        assertThat(response.status()).isEqualTo(StatusParcelaPagar.PARCIALMENTE_PAGA);
        assertThat(response.valorAberto()).isEqualByComparingTo("200.00");
    }

    @Test
    void deveBloquearBaixaAcimaDoSaldo() {
        ParcelaPagar parcela = parcela(1, "300.00");
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(parcela.getId())).thenReturn(Optional.of(parcela));

        assertThatThrownBy(() -> service.baixar(parcela.getId(),
                new ParcelaPagarBaixaRequest(new BigDecimal("301.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, LocalDate.of(2026, 7, 10), null)))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("ultrapassar");
    }

    private ParcelaPagarRequest request(int numero, String valor) {
        return new ParcelaPagarRequest(conta.getId(), numero, LocalDate.of(2026, 7, 10), new BigDecimal(valor), "Teste");
    }

    private ParcelaPagar parcela(int numero, String valor) {
        ParcelaPagar parcela = new ParcelaPagar();
        parcela.setId(UUID.randomUUID());
        parcela.abrir(conta, numero, LocalDate.of(2026, 7, 10), new BigDecimal(valor), null);
        return parcela;
    }

    private ContaPagar conta() {
        Empresa empresa = new Empresa("Empresa Teste", "Empresa Teste", "12345678000199", null, null, null);
        empresa.setId(UUID.randomUUID());
        Pessoa pessoa = new Pessoa();
        pessoa.setId(UUID.randomUUID());
        pessoa.atualizar(TipoPessoa.JURIDICA, "Fornecedor Teste", null, "12345678000188", null, null, null, null, null);
        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setId(UUID.randomUUID());
        fornecedor.setPessoa(pessoa);
        ContaPagar entity = new ContaPagar();
        entity.setId(UUID.randomUUID());
        entity.abrir(empresa, null, fornecedor, null, "CP-1", "Conta CP-1", LocalDate.of(2026, 7, 8),
                new BigDecimal("900.00"), null);
        return entity;
    }

    private CondicaoPagamento condicaoTresParcelas() {
        CondicaoPagamento condicao = new CondicaoPagamento();
        condicao.setId(UUID.randomUUID());
        condicao.atualizar("3X", "Tres parcelas", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                RegraVencimento.MANTER_DATA, true);
        condicao.substituirParcelas(List.of(parcelaCondicao(condicao, 1, 0), parcelaCondicao(condicao, 2, 30),
                parcelaCondicao(condicao, 3, 60)));
        return condicao;
    }

    private CondicaoPagamentoParcela parcelaCondicao(CondicaoPagamento condicao, int numero, int dias) {
        CondicaoPagamentoParcela parcela = new CondicaoPagamentoParcela();
        parcela.atualizar(condicao, numero, dias, new BigDecimal("33.3333"));
        if (numero == 3) {
            parcela.setPercentual(new BigDecimal("33.3334"));
        }
        return parcela;
    }
}
