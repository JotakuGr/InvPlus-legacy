package br.com.invplus.modules.financeiro.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.AuditorAware;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.financeiro.dto.request.ConciliacaoBancariaCancelamentoRequest;
import br.com.invplus.modules.financeiro.dto.request.ConciliacaoBancariaRequest;
import br.com.invplus.modules.financeiro.entity.ConciliacaoBancaria;
import br.com.invplus.modules.financeiro.entity.ContaBancaria;
import br.com.invplus.modules.financeiro.entity.ExtratoBancario;
import br.com.invplus.modules.financeiro.entity.ExtratoBancarioLancamento;
import br.com.invplus.modules.financeiro.entity.MovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.CategoriaLancamentoBancario;
import br.com.invplus.modules.financeiro.enums.OrigemExtratoBancario;
import br.com.invplus.modules.financeiro.enums.OrigemMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.StatusConciliacaoBancaria;
import br.com.invplus.modules.financeiro.enums.TipoConciliacaoBancaria;
import br.com.invplus.modules.financeiro.enums.TipoContaBancaria;
import br.com.invplus.modules.financeiro.enums.TipoLancamentoBancario;
import br.com.invplus.modules.financeiro.enums.TipoMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.mapper.ConciliacaoBancariaMapper;
import br.com.invplus.modules.financeiro.repository.ConciliacaoBancariaRepository;
import br.com.invplus.modules.financeiro.validator.ConciliacaoBancariaValidator;
import br.com.invplus.modules.pagamentos.entity.FormaPagamento;
import br.com.invplus.modules.pagamentos.enums.TipoFormaPagamento;
import br.com.invplus.security.context.EmpresaContext;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;

/** Testes unitarios das transicoes e compatibilidades da conciliacao bancaria. */
@ExtendWith(MockitoExtension.class)
class ConciliacaoBancariaServiceTest {

    @Mock private ConciliacaoBancariaRepository repository;
    @Mock private ExtratoBancarioLancamentoService lancamentoService;
    @Mock private ExtratoBancarioService extratoService;
    @Mock private MovimentoFinanceiroService movimentoService;
    @Mock private AuditorAware<UUID> auditorAware;
    @Mock private EmpresaContext empresaContext;

    private ConciliacaoBancariaService service;
    private ExtratoBancarioLancamento lancamento;
    private MovimentoFinanceiro movimento;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        service = new ConciliacaoBancariaService(repository, lancamentoService, extratoService,
                movimentoService, new ConciliacaoBancariaMapper(),
                new ConciliacaoBancariaValidator(repository), auditorAware, empresaContext);
        lancamento = lancamento(TipoLancamentoBancario.CREDITO, new BigDecimal("100.00"));
        movimento = movimento(TipoMovimentoFinanceiro.ENTRADA, new BigDecimal("100.00"));
        when(empresaContext.getEmpresaId()).thenReturn(
                lancamento.getExtratoBancario().getContaBancaria().getEmpresa().getId());
    }

    @Test
    void deveConciliarCreditoComEntradaEDebitoComSaida() {
        var credito = conciliarValido();
        assertThat(credito.status()).isEqualTo(StatusConciliacaoBancaria.CONCILIADA);
        assertThat(credito.diferenca()).isZero();

        lancamento = lancamento(TipoLancamentoBancario.DEBITO, new BigDecimal("50.00"));
        movimento = movimento(TipoMovimentoFinanceiro.SAIDA, new BigDecimal("50.00"));
        var debito = conciliarValido();
        assertThat(debito.valorExtrato()).isEqualByComparingTo("50.00");
    }

    @Test
    void deveBloquearLancamentoOuMovimentoInexistente() {
        UUID empresaId = empresaContext.getEmpresaId();
        when(lancamentoService.buscarEntidade(lancamento.getId(), empresaId))
                .thenThrow(new ResourceNotFoundException("Lancamento nao encontrado"));
        assertThatThrownBy(this::criar).isInstanceOf(ResourceNotFoundException.class);

        doReturn(lancamento).when(lancamentoService).buscarEntidade(lancamento.getId(), empresaId);
        when(movimentoService.buscarEntidade(movimento.getId(), empresaId))
                .thenThrow(new ResourceNotFoundException("Movimento nao encontrado"));
        assertThatThrownBy(this::criar).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveBloquearLancamentoOuMovimentoJaConciliado() {
        prepararBuscas();
        lancamento.marcarComoConciliado();
        assertThatThrownBy(this::criar).isInstanceOf(ConflictException.class);

        lancamento.desmarcarComoConciliado();
        when(repository.existsByMovimentoFinanceiroIdAndStatus(
                movimento.getId(), StatusConciliacaoBancaria.CONCILIADA)).thenReturn(true);
        assertThatThrownBy(this::criar).isInstanceOf(ConflictException.class);
    }

    @Test
    void deveBloquearTiposIncompativeis() {
        prepararBuscas();
        movimento.setTipoMovimento(TipoMovimentoFinanceiro.SAIDA);
        assertThatThrownBy(this::criar).isInstanceOf(InvalidOperationException.class);

        lancamento.setTipo(TipoLancamentoBancario.DEBITO);
        movimento.setTipoMovimento(TipoMovimentoFinanceiro.ENTRADA);
        assertThatThrownBy(this::criar).isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void deveBloquearValoresDiferentes() {
        prepararBuscas();
        movimento.setValor(new BigDecimal("99.99"));
        assertThatThrownBy(this::criar)
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("igual");
    }

    @Test
    void deveCancelarConciliacaoERestaurarLancamento() {
        ConciliacaoBancaria entity = entidadeConciliada();
        when(repository.findByIdAndStatusAndContaBancariaEmpresaId(entity.getId(),
                StatusConciliacaoBancaria.CONCILIADA, empresaContext.getEmpresaId()))
                .thenReturn(Optional.of(entity));
        when(auditorAware.getCurrentAuditor()).thenReturn(Optional.of(UUID.randomUUID()));
        when(repository.saveAndFlush(entity)).thenReturn(entity);

        var response = service.cancelar(entity.getId(),
                new ConciliacaoBancariaCancelamentoRequest("Vinculo bancario incorreto"));

        assertThat(response.status()).isEqualTo(StatusConciliacaoBancaria.CANCELADA);
        assertThat(response.motivoCancelamento()).isEqualTo("Vinculo bancario incorreto");
        verify(lancamentoService).desmarcarComoConciliado(lancamento.getId());
        verify(extratoService).recalcularStatusConciliacao(lancamento.getExtratoBancario().getId());
    }

    @Test
    void deveBloquearCancelamentoDuplicado() {
        when(repository.findByIdAndStatusAndContaBancariaEmpresaId(any(), any(), any()))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.cancelar(UUID.randomUUID(),
                new ConciliacaoBancariaCancelamentoRequest("Cancelamento repetido")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private br.com.invplus.modules.financeiro.dto.response.ConciliacaoBancariaResponse conciliarValido() {
        prepararBuscas();
        when(auditorAware.getCurrentAuditor()).thenReturn(Optional.of(UUID.randomUUID()));
        when(repository.saveAndFlush(any(ConciliacaoBancaria.class))).thenAnswer(invocation -> {
            ConciliacaoBancaria entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });
        return criar();
    }

    private br.com.invplus.modules.financeiro.dto.response.ConciliacaoBancariaResponse criar() {
        return service.criar(new ConciliacaoBancariaRequest(lancamento.getId(), movimento.getId(),
                TipoConciliacaoBancaria.MANUAL, "Teste"));
    }

    private void prepararBuscas() {
        UUID empresaId = empresaContext.getEmpresaId();
        when(lancamentoService.buscarEntidade(lancamento.getId(), empresaId)).thenReturn(lancamento);
        when(movimentoService.buscarEntidade(movimento.getId(), empresaId)).thenReturn(movimento);
    }

    private ConciliacaoBancaria entidadeConciliada() {
        ConciliacaoBancaria entity = new ConciliacaoBancaria();
        entity.setId(UUID.randomUUID());
        entity.conciliar(lancamento, movimento, TipoConciliacaoBancaria.MANUAL, null, null);
        return entity;
    }

    private ExtratoBancarioLancamento lancamento(TipoLancamentoBancario tipo, BigDecimal valor) {
        Empresa empresa = new Empresa("Empresa", "Empresa", "12345678000199", null, null, null);
        empresa.setId(UUID.randomUUID());
        ContaBancaria conta = new ContaBancaria();
        conta.setId(UUID.randomUUID());
        conta.atualizar(empresa, null, "001", "Banco", "1234", null, "9876", null,
                TipoContaBancaria.CORRENTE, "Empresa", null, null, BigDecimal.ZERO, LocalDate.now(), true);
        ExtratoBancario extrato = new ExtratoBancario();
        extrato.setId(UUID.randomUUID());
        extrato.importar(conta, LocalDate.now(), LocalDate.now(), BigDecimal.ZERO, valor,
                OrigemExtratoBancario.OFX, null, null, null);
        ExtratoBancarioLancamento entity = new ExtratoBancarioLancamento();
        entity.setId(UUID.randomUUID());
        entity.atualizar(extrato, LocalDate.now(), "Lancamento", null, tipo, valor, null,
                UUID.randomUUID().toString(), CategoriaLancamentoBancario.PIX, null);
        return entity;
    }

    private MovimentoFinanceiro movimento(TipoMovimentoFinanceiro tipo, BigDecimal valor) {
        FormaPagamento forma = new FormaPagamento();
        forma.setId(UUID.randomUUID());
        forma.atualizar("PIX", "Pix", TipoFormaPagamento.PIX, false, false);
        MovimentoFinanceiro entity = new MovimentoFinanceiro();
        entity.setId(UUID.randomUUID());
        entity.registrar(lancamento.getExtratoBancario().getContaBancaria().getEmpresa(), null, tipo,
                OrigemMovimentoFinanceiro.AJUSTE_MANUAL, null, forma, null, null,
                valor, LocalDate.now(), "Movimento", null);
        return entity;
    }
}
