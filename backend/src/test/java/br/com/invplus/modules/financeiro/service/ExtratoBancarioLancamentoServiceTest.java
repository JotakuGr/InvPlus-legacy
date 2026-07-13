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
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.financeiro.dto.request.ExtratoBancarioLancamentoRequest;
import br.com.invplus.modules.financeiro.entity.ContaBancaria;
import br.com.invplus.modules.financeiro.entity.ExtratoBancario;
import br.com.invplus.modules.financeiro.entity.ExtratoBancarioLancamento;
import br.com.invplus.modules.financeiro.enums.CategoriaLancamentoBancario;
import br.com.invplus.modules.financeiro.enums.OrigemExtratoBancario;
import br.com.invplus.modules.financeiro.enums.TipoContaBancaria;
import br.com.invplus.modules.financeiro.enums.TipoLancamentoBancario;
import br.com.invplus.modules.financeiro.mapper.ExtratoBancarioLancamentoMapper;
import br.com.invplus.modules.financeiro.repository.ExtratoBancarioLancamentoRepository;
import br.com.invplus.modules.financeiro.validator.ExtratoBancarioLancamentoValidator;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;

/**
 * Testes unitarios das regras de lancamentos de extrato bancario.
 */
@ExtendWith(MockitoExtension.class)
class ExtratoBancarioLancamentoServiceTest {

    @Mock
    private ExtratoBancarioLancamentoRepository repository;
    @Mock
    private ExtratoBancarioService extratoBancarioService;

    private ExtratoBancarioLancamentoService service;
    private ExtratoBancario extrato;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        service = new ExtratoBancarioLancamentoService(repository, extratoBancarioService,
                new ExtratoBancarioLancamentoMapper(), new ExtratoBancarioLancamentoValidator(repository));
        extrato = extrato();
    }

    @Test
    void deveCriarCreditoEDebitoComDefaults() {
        prepararExtrato();
        when(repository.save(any(ExtratoBancarioLancamento.class))).thenAnswer(invocation -> {
            ExtratoBancarioLancamento entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        var credito = service.criar(request(TipoLancamentoBancario.CREDITO, "ID-1", null));
        var debito = service.criar(request(TipoLancamentoBancario.DEBITO, "ID-2", CategoriaLancamentoBancario.TARIFA));

        assertThat(credito.tipo()).isEqualTo(TipoLancamentoBancario.CREDITO);
        assertThat(credito.categoriaBancaria()).isEqualTo(CategoriaLancamentoBancario.OUTRO);
        assertThat(credito.conciliado()).isFalse();
        assertThat(debito.tipo()).isEqualTo(TipoLancamentoBancario.DEBITO);
        assertThat(debito.categoriaBancaria()).isEqualTo(CategoriaLancamentoBancario.TARIFA);
    }

    @Test
    void deveBloquearExtratoInexistente() {
        when(extratoBancarioService.buscarEntidade(extrato.getId()))
                .thenThrow(new ResourceNotFoundException("Extrato bancario nao encontrado"));

        assertThatThrownBy(() -> service.criar(request(TipoLancamentoBancario.CREDITO, "ID-1", null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveBloquearExtratoCancelado() {
        extrato.cancelar();
        when(extratoBancarioService.buscarEntidade(extrato.getId())).thenReturn(extrato);

        assertThatThrownBy(() -> service.criar(request(TipoLancamentoBancario.CREDITO, "ID-2", null)))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void deveBloquearValorInvalidoEIdentificadorDuplicado() {
        prepararExtrato();

        assertThatThrownBy(() -> service.criar(request(BigDecimal.ZERO, "ID-1")))
                .isInstanceOf(InvalidOperationException.class);

        when(repository.findByExtratoBancarioIdAndIdentificadorBancarioIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(
                extrato.getId(), "ID-1")).thenReturn(Optional.of(lancamento(false)));

        assertThatThrownBy(() -> service.criar(request(BigDecimal.TEN, "ID-1")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void deveAtualizarExcluirEMarcarConciliacaoQuandoNaoConciliado() {
        ExtratoBancarioLancamento entity = lancamento(false);
        prepararExtrato();
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(entity.getId())).thenReturn(Optional.of(entity));

        var atualizado = service.atualizar(entity.getId(), request(TipoLancamentoBancario.DEBITO, "ID-2",
                CategoriaLancamentoBancario.TARIFA));
        var conciliado = service.marcarComoConciliado(entity.getId());

        assertThat(atualizado.tipo()).isEqualTo(TipoLancamentoBancario.DEBITO);
        assertThat(conciliado.conciliado()).isTrue();

        service.desmarcarComoConciliado(entity.getId());
        service.excluir(entity.getId());

        assertThat(entity.getConciliado()).isFalse();
        assertThat(entity.getDeletedAt()).isNotNull();
    }

    @Test
    void deveBloquearEdicaoEExclusaoDeLancamentoConciliado() {
        ExtratoBancarioLancamento entity = lancamento(true);
        prepararExtrato();
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(entity.getId())).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> service.atualizar(entity.getId(), request(TipoLancamentoBancario.CREDITO,
                "ID-1", null))).isInstanceOf(InvalidOperationException.class);
        assertThatThrownBy(() -> service.excluir(entity.getId()))
                .isInstanceOf(InvalidOperationException.class);
    }

    private void prepararExtrato() {
        when(extratoBancarioService.buscarEntidade(extrato.getId())).thenReturn(extrato);
    }

    private ExtratoBancarioLancamentoRequest request(BigDecimal valor, String identificador) {
        return new ExtratoBancarioLancamentoRequest(extrato.getId(), LocalDate.of(2026, 7, 10),
                "Recebimento teste", "DOC-1", TipoLancamentoBancario.CREDITO, valor,
                new BigDecimal("100.00"), identificador, CategoriaLancamentoBancario.PIX, null);
    }

    private ExtratoBancarioLancamentoRequest request(TipoLancamentoBancario tipo, String identificador,
            CategoriaLancamentoBancario categoria) {
        return new ExtratoBancarioLancamentoRequest(extrato.getId(), LocalDate.of(2026, 7, 10),
                "Lancamento teste", "DOC-1", tipo, BigDecimal.TEN, new BigDecimal("100.00"),
                identificador, categoria, "Observacao");
    }

    private ExtratoBancarioLancamento lancamento(boolean conciliado) {
        ExtratoBancarioLancamento entity = new ExtratoBancarioLancamento();
        entity.setId(UUID.randomUUID());
        entity.atualizar(extrato, LocalDate.of(2026, 7, 10), "Lancamento", null,
                TipoLancamentoBancario.CREDITO, BigDecimal.TEN, null, "ID-1",
                CategoriaLancamentoBancario.PIX, null);
        if (conciliado) {
            entity.marcarComoConciliado();
        }
        return entity;
    }

    private ExtratoBancario extrato() {
        Empresa empresa = new Empresa("Empresa Lancamento", "Empresa Lancamento",
                "12345678000199", null, null, null);
        empresa.setId(UUID.randomUUID());
        ContaBancaria conta = new ContaBancaria();
        conta.setId(UUID.randomUUID());
        conta.atualizar(empresa, null, "001", "Banco Teste", "1234", "5",
                "98765", "0", TipoContaBancaria.CORRENTE, "Empresa Lancamento",
                null, null, BigDecimal.ZERO, LocalDate.of(2026, 7, 9), true);
        ExtratoBancario entity = new ExtratoBancario();
        entity.setId(UUID.randomUUID());
        entity.importar(conta, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31),
                BigDecimal.ZERO, BigDecimal.TEN, OrigemExtratoBancario.OFX, null, null, null);
        return entity;
    }
}
