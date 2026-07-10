package br.com.invplus.modules.financeiro.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.financeiro.dto.request.ExtratoBancarioRequest;
import br.com.invplus.modules.financeiro.entity.ContaBancaria;
import br.com.invplus.modules.financeiro.entity.ExtratoBancario;
import br.com.invplus.modules.financeiro.enums.OrigemExtratoBancario;
import br.com.invplus.modules.financeiro.enums.StatusExtratoBancario;
import br.com.invplus.modules.financeiro.enums.TipoContaBancaria;
import br.com.invplus.modules.financeiro.mapper.ExtratoBancarioMapper;
import br.com.invplus.modules.financeiro.repository.ExtratoBancarioRepository;
import br.com.invplus.modules.financeiro.repository.ExtratoBancarioLancamentoRepository;
import br.com.invplus.modules.financeiro.validator.ExtratoBancarioValidator;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Testes unitarios das regras de importacao e cancelamento de extratos bancarios.
 */
@ExtendWith(MockitoExtension.class)
class ExtratoBancarioServiceTest {

    private static final String HASH_VALIDO =
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

    @Mock
    private ExtratoBancarioRepository repository;
    @Mock
    private ExtratoBancarioLancamentoRepository lancamentoRepository;
    @Mock
    private ContaBancariaService contaBancariaService;

    private ExtratoBancarioService service;
    private ContaBancaria contaBancaria;

    @BeforeEach
    void setUp() {
        service = new ExtratoBancarioService(repository, lancamentoRepository, contaBancariaService,
                new ExtratoBancarioMapper(), new ExtratoBancarioValidator(repository));
        contaBancaria = contaBancaria();
    }

    @Test
    void deveImportarExtratoComStatusInicialEDataAutomatica() {
        when(contaBancariaService.buscarEntidade(contaBancaria.getId())).thenReturn(contaBancaria);
        when(repository.save(any(ExtratoBancario.class))).thenAnswer(invocation -> {
            ExtratoBancario entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        var response = service.importar(request(HASH_VALIDO));

        assertThat(response.status()).isEqualTo(StatusExtratoBancario.IMPORTADO);
        assertThat(response.importadoEm()).isNotNull();
        assertThat(response.arquivoHash()).isEqualTo(HASH_VALIDO);
        assertThat(response.contaBancariaDescricao()).contains("Banco Teste");
    }

    @Test
    void deveBloquearContaInexistenteOuInativa() {
        when(contaBancariaService.buscarEntidade(contaBancaria.getId()))
                .thenThrow(new ResourceNotFoundException("Conta bancaria nao encontrada"));

        assertThatThrownBy(() -> service.importar(request(HASH_VALIDO)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveBloquearPeriodoInvalido() {
        when(contaBancariaService.buscarEntidade(contaBancaria.getId())).thenReturn(contaBancaria);

        ExtratoBancarioRequest request = new ExtratoBancarioRequest(contaBancaria.getId(),
                LocalDate.of(2026, 7, 31), LocalDate.of(2026, 7, 1),
                BigDecimal.ZERO, BigDecimal.TEN, OrigemExtratoBancario.OFX,
                "extrato.ofx", HASH_VALIDO, null);

        assertThatThrownBy(() -> service.importar(request))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void deveBloquearHashInvalidoEDuplicado() {
        when(contaBancariaService.buscarEntidade(contaBancaria.getId())).thenReturn(contaBancaria);

        assertThatThrownBy(() -> service.importar(request("hash-invalido")))
                .isInstanceOf(InvalidOperationException.class);

        when(repository.findByArquivoHashIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(HASH_VALIDO))
                .thenReturn(Optional.of(new ExtratoBancario()));

        assertThatThrownBy(() -> service.importar(request(HASH_VALIDO)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void deveCancelarExtratoEImpedeCancelamentoDuplicado() {
        ExtratoBancario entity = extrato();
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(entity.getId())).thenReturn(Optional.of(entity));

        var response = service.cancelar(entity.getId());

        assertThat(response.status()).isEqualTo(StatusExtratoBancario.CANCELADO);
        assertThat(entity.getAtivo()).isTrue();
        assertThat(entity.getDeletedAt()).isNull();
        assertThatThrownBy(() -> service.cancelar(entity.getId()))
                .isInstanceOf(InvalidOperationException.class);
    }

    private ExtratoBancarioRequest request(String hash) {
        return new ExtratoBancarioRequest(contaBancaria.getId(), LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31), BigDecimal.ZERO, new BigDecimal("2500.00"),
                OrigemExtratoBancario.OFX, "extrato.ofx", hash, "Importacao teste");
    }

    private ExtratoBancario extrato() {
        ExtratoBancario entity = new ExtratoBancario();
        entity.setId(UUID.randomUUID());
        entity.importar(contaBancaria, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31),
                BigDecimal.ZERO, BigDecimal.TEN, OrigemExtratoBancario.OFX,
                "extrato.ofx", HASH_VALIDO, null);
        return entity;
    }

    private ContaBancaria contaBancaria() {
        Empresa empresa = new Empresa("Empresa Extrato", "Empresa Extrato",
                "12345678000199", null, null, null);
        empresa.setId(UUID.randomUUID());
        ContaBancaria entity = new ContaBancaria();
        entity.setId(UUID.randomUUID());
        entity.atualizar(empresa, null, "001", "Banco Teste", "1234", "5",
                "98765", "0", TipoContaBancaria.CORRENTE, "Empresa Extrato",
                null, null, BigDecimal.ZERO, LocalDate.of(2026, 7, 9), true);
        return entity;
    }
}
