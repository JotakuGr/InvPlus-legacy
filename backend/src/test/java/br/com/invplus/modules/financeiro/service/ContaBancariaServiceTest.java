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
import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.cadastros.repository.FilialRepository;
import br.com.invplus.modules.financeiro.dto.request.ContaBancariaRequest;
import br.com.invplus.modules.financeiro.entity.ContaBancaria;
import br.com.invplus.modules.financeiro.enums.TipoContaBancaria;
import br.com.invplus.modules.financeiro.mapper.ContaBancariaMapper;
import br.com.invplus.modules.financeiro.repository.ContaBancariaRepository;
import br.com.invplus.modules.financeiro.validator.ContaBancariaValidator;
import br.com.invplus.security.context.EmpresaContext;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;

/**
 * Testes unitarios das regras de cadastro de contas bancarias.
 */
@ExtendWith(MockitoExtension.class)
class ContaBancariaServiceTest {

    @Mock
    private ContaBancariaRepository repository;
    @Mock
    private EmpresaRepository empresaRepository;
    @Mock
    private FilialRepository filialRepository;
    @Mock
    private EmpresaContext empresaContext;

    private ContaBancariaService service;
    private Empresa empresa;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        service = new ContaBancariaService(repository, empresaRepository, filialRepository,
                new ContaBancariaMapper(), new ContaBancariaValidator(repository), empresaContext);
        empresa = empresa("Empresa Banco");
        when(empresaContext.getEmpresaId()).thenReturn(empresa.getId());
    }

    @Test
    void deveCriarContaBancariaComSaldoInicialDefault() {
        prepararEmpresa();
        when(repository.save(any(ContaBancaria.class))).thenAnswer(invocation -> {
            ContaBancaria entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        var response = service.criar(request(null, false, null));

        assertThat(response.saldoInicial()).isZero();
        assertThat(response.saldoAtual()).isZero();
        assertThat(response.principal()).isFalse();
        assertThat(response.bancoCodigo()).isEqualTo("001");
    }

    @Test
    void deveIniciarSaldoAtualIgualAoSaldoInicial() {
        prepararEmpresa();
        when(repository.save(any(ContaBancaria.class))).thenAnswer(invocation -> {
            ContaBancaria entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        var response = service.criar(request(new BigDecimal("1500.25"), true, "12345678901"));

        assertThat(response.saldoInicial()).isEqualByComparingTo("1500.25");
        assertThat(response.saldoAtual()).isEqualByComparingTo("1500.25");
        assertThat(response.documentoTitular()).isEqualTo("12345678901");
    }

    @Test
    void deveBloquearContaDuplicadaPorEmpresaBancoAgenciaENumero() {
        ContaBancaria existente = conta();
        prepararEmpresa();
        when(repository.findByEmpresaIdAndBancoCodigoIgnoreCaseAndAgenciaIgnoreCaseAndNumeroContaIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(
                empresa.getId(), "001", "1234", "98765")).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.criar(request(BigDecimal.ZERO, false, null)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void deveBloquearSegundaContaPrincipalPorEmpresa() {
        ContaBancaria existente = conta();
        prepararEmpresa();
        when(repository.findByEmpresaIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(empresa.getId()))
                .thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.criar(request(BigDecimal.ZERO, true, null)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void deveBloquearFilialDeOutraEmpresaEDocumentoInvalido() {
        Empresa outra = empresa("Outra Empresa");
        Filial filial = filial(outra);
        prepararEmpresa();
        when(filialRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(filial.getId())).thenReturn(Optional.of(filial));

        assertThatThrownBy(() -> service.criar(request(filial.getId(), BigDecimal.ZERO, false, null)))
                .isInstanceOf(InvalidOperationException.class);

        assertThatThrownBy(() -> service.criar(request(BigDecimal.ZERO, false, "12345")))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void deveAtualizarMantendoPropriaContaPrincipalEExcluirLogicamente() {
        ContaBancaria entity = conta();
        prepararEmpresa();
        when(repository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(entity.getId(), empresa.getId()))
                .thenReturn(Optional.of(entity));
        when(repository.findByEmpresaIdAndBancoCodigoIgnoreCaseAndAgenciaIgnoreCaseAndNumeroContaIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(
                empresa.getId(), "001", "1234", "98765")).thenReturn(Optional.of(entity));
        when(repository.findByEmpresaIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(empresa.getId()))
                .thenReturn(Optional.of(entity));

        var response = service.atualizar(entity.getId(), request(BigDecimal.TEN, true, null));
        service.excluir(entity.getId());

        assertThat(response.principal()).isTrue();
        assertThat(entity.getAtivo()).isFalse();
        assertThat(entity.getDeletedAt()).isNotNull();
    }

    private void prepararEmpresa() {
        when(empresaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(empresa.getId()))
                .thenReturn(Optional.of(empresa));
    }

    private ContaBancariaRequest request(BigDecimal saldo, Boolean principal, String documento) {
        return request(null, saldo, principal, documento);
    }

    private ContaBancariaRequest request(UUID filialId, BigDecimal saldo, Boolean principal, String documento) {
        return new ContaBancariaRequest(filialId, "001", "Banco do Brasil",
                "1234", "5", "98765", "0", TipoContaBancaria.CORRENTE,
                "Empresa Banco", documento, "financeiro@empresa.com", saldo,
                LocalDate.of(2026, 7, 9), principal);
    }

    private ContaBancaria conta() {
        ContaBancaria entity = new ContaBancaria();
        entity.setId(UUID.randomUUID());
        entity.atualizar(empresa, null, "001", "Banco do Brasil", "1234", "5",
                "98765", "0", TipoContaBancaria.CORRENTE, "Empresa Banco",
                null, null, BigDecimal.ZERO, LocalDate.of(2026, 7, 9), true);
        return entity;
    }

    private Filial filial(Empresa empresa) {
        Filial filial = new Filial();
        filial.setId(UUID.randomUUID());
        filial.atualizar(empresa, null, "F01", "Filial", null, null, null, null, false);
        return filial;
    }

    private Empresa empresa(String nome) {
        Empresa entity = new Empresa(nome, nome, "12345678000199", null, null, null);
        entity.setId(UUID.randomUUID());
        return entity;
    }
}
