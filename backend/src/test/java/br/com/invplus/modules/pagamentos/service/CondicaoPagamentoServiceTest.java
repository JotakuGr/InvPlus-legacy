package br.com.invplus.modules.pagamentos.service;

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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import br.com.invplus.modules.financeiro.enums.RegraVencimento;
import br.com.invplus.modules.financeiro.service.CalculadoraVencimentoService;
import br.com.invplus.modules.financeiro.service.GeradorParcelasService;
import br.com.invplus.modules.pagamentos.dto.request.CondicaoPagamentoParcelaRequest;
import br.com.invplus.modules.pagamentos.dto.request.CondicaoPagamentoRequest;
import br.com.invplus.modules.pagamentos.dto.request.SimulacaoCondicaoPagamentoRequest;
import br.com.invplus.modules.pagamentos.dto.response.CondicaoPagamentoResponse;
import br.com.invplus.modules.pagamentos.entity.CondicaoPagamento;
import br.com.invplus.modules.pagamentos.mapper.CondicaoPagamentoMapper;
import br.com.invplus.modules.pagamentos.repository.CondicaoPagamentoRepository;
import br.com.invplus.modules.pagamentos.validator.CondicaoPagamentoValidator;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;

/**
 * Testes unitarios das regras de condicao de pagamento.
 */
@ExtendWith(MockitoExtension.class)
class CondicaoPagamentoServiceTest {

    @Mock
    private CondicaoPagamentoRepository repository;

    private CondicaoPagamentoService service;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        CondicaoPagamentoMapper mapper = new CondicaoPagamentoMapper();
        GeradorParcelasService geradorParcelasService = new GeradorParcelasService(new CalculadoraVencimentoService());
        service = new CondicaoPagamentoService(repository, mapper, new CondicaoPagamentoValidator(repository), geradorParcelasService);
    }

    @Test
    void deveCriarCondicaoPagamentoComSucesso() {
        when(repository.findByCodigoIgnoreCaseAndAtivoTrueAndDeletedAtIsNull("30D")).thenReturn(Optional.empty());
        when(repository.save(any(CondicaoPagamento.class))).thenAnswer(invocation -> {
            CondicaoPagamento entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        CondicaoPagamentoResponse response = service.criar(request("30D", 1, 30));

        assertThat(response.id()).isNotNull();
        assertThat(response.codigo()).isEqualTo("30D");
        assertThat(response.parcelas()).hasSize(1);
        assertThat(response.parcelas().getFirst().dias()).isEqualTo(30);
    }

    @Test
    void deveBloquearCodigoDuplicado() {
        CondicaoPagamento existente = condicao("30D");
        when(repository.findByCodigoIgnoreCaseAndAtivoTrueAndDeletedAtIsNull("30D")).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.criar(request("30D", 1, 30)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Ja existe condicao");
    }

    @Test
    void deveBloquearSomaDeParcelasInvalida() {
        when(repository.findByCodigoIgnoreCaseAndAtivoTrueAndDeletedAtIsNull("INV")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(requestComParcelas("INV", List.of(parcela(1, 30, "90.00")))))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("100%");
    }

    @Test
    void deveBloquearParcelaRepetidaEDiasNegativos() {
        when(repository.findByCodigoIgnoreCaseAndAtivoTrueAndDeletedAtIsNull("REP")).thenReturn(Optional.empty());

        CondicaoPagamentoRequest repetida = requestComParcelas("REP", List.of(
                parcela(1, 0, "50.00"),
                parcela(1, 30, "50.00")
        ));

        assertThatThrownBy(() -> service.criar(repetida))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("repetir");

        when(repository.findByCodigoIgnoreCaseAndAtivoTrueAndDeletedAtIsNull("NEG")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.criar(requestComParcelas("NEG", List.of(parcela(1, -1, "100.00")))))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Dias");
    }

    @Test
    void deveBuscarListarAtualizarEExcluirLogicamente() {
        CondicaoPagamento entity = condicao("30D");
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(entity.getId())).thenReturn(Optional.of(entity));
        when(repository.findAll(anySpecification(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(java.util.List.of(entity)));
        when(repository.findByCodigoIgnoreCaseAndAtivoTrueAndDeletedAtIsNull("30D")).thenReturn(Optional.of(entity));

        assertThat(service.buscar(entity.getId()).codigo()).isEqualTo("30D");
        assertThat(service.listarComFiltros("30D", null, true, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);

        CondicaoPagamentoResponse atualizado = service.atualizar(entity.getId(), requestComParcelas("30D", List.of(
                parcela(1, 0, "40.00"),
                parcela(2, 30, "30.00"),
                parcela(3, 60, "30.00")
        )));

        assertThat(atualizado.parcelas()).hasSize(3);
        service.excluir(entity.getId());
        assertThat(entity.getAtivo()).isFalse();
        assertThat(entity.getDeletedAt()).isNotNull();
    }

    @Test
    void deveSimularVencimentosComRegrasDeDiaUtil() {
        CondicaoPagamento entity = condicao("SIM");
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(entity.getId())).thenReturn(Optional.of(entity));

        var parcelas = service.simular(entity.getId(), new SimulacaoCondicaoPagamentoRequest(
                LocalDate.of(2026, 7, 10),
                new BigDecimal("1000.00")
        ));

        assertThat(parcelas).hasSize(1);
        assertThat(parcelas.getFirst().vencimento()).isEqualTo(LocalDate.of(2026, 8, 10));
        assertThat(parcelas.getFirst().valor()).isEqualByComparingTo("1000.00");
    }

    private CondicaoPagamentoRequest request(String codigo, int parcelas, int intervalo) {
        return requestComParcelas(codigo, parcelas(parcelas, intervalo));
    }

    private CondicaoPagamentoRequest requestComParcelas(String codigo, List<CondicaoPagamentoParcelaRequest> parcelas) {
        return new CondicaoPagamentoRequest(
                codigo,
                "Condicao " + codigo,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                RegraVencimento.PROXIMO_DIA_UTIL,
                true,
                parcelas
        );
    }

    private CondicaoPagamento condicao(String codigo) {
        CondicaoPagamento entity = new CondicaoPagamento();
        entity.setId(UUID.randomUUID());
        CondicaoPagamentoMapper mapper = new CondicaoPagamentoMapper();
        mapper.aplicar(entity, request(codigo, 1, 30));
        return entity;
    }

    private List<CondicaoPagamentoParcelaRequest> parcelas(int quantidade, int intervalo) {
        BigDecimal percentual = new BigDecimal("100.00").divide(new BigDecimal(quantidade), 4, java.math.RoundingMode.HALF_UP);
        java.util.ArrayList<CondicaoPagamentoParcelaRequest> lista = new java.util.ArrayList<>();
        for (int i = 1; i <= quantidade; i++) {
            BigDecimal valor = i == quantidade
                    ? new BigDecimal("100.00").subtract(percentual.multiply(new BigDecimal(quantidade - 1)))
                    : percentual;
            lista.add(parcela(i, intervalo * i, valor.toPlainString()));
        }
        return lista;
    }

    private CondicaoPagamentoParcelaRequest parcela(int numero, int dias, String percentual) {
        return new CondicaoPagamentoParcelaRequest(numero, dias, new BigDecimal(percentual));
    }

    private Specification<CondicaoPagamento> anySpecification() {
        return any();
    }
}
