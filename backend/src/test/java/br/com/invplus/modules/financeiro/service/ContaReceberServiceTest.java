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

import br.com.invplus.common.enums.TipoPessoa;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.cadastros.repository.FilialRepository;
import br.com.invplus.modules.financeiro.dto.request.ContaReceberRequest;
import br.com.invplus.modules.financeiro.dto.response.ContaReceberResponse;
import br.com.invplus.modules.financeiro.entity.ContaReceber;
import br.com.invplus.modules.financeiro.enums.StatusContaReceber;
import br.com.invplus.modules.financeiro.mapper.ContaReceberMapper;
import br.com.invplus.modules.financeiro.repository.CentroCustoRepository;
import br.com.invplus.modules.financeiro.repository.ContaReceberRepository;
import br.com.invplus.modules.financeiro.repository.PlanoContaRepository;
import br.com.invplus.modules.financeiro.validator.ContaReceberValidator;
import br.com.invplus.modules.pessoas.entity.Cliente;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.pessoas.repository.ClienteRepository;
import br.com.invplus.modules.vendas.repository.VendaFaturamentoRepository;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import br.com.invplus.security.context.EmpresaContext;

/**
 * Testes unitarios das regras de ContaReceber.
 */
@ExtendWith(MockitoExtension.class)
class ContaReceberServiceTest {

    @Mock
    private ContaReceberRepository repository;
    @Mock
    private EmpresaRepository empresaRepository;
    @Mock
    private FilialRepository filialRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private VendaFaturamentoRepository vendaFaturamentoRepository;
    @Mock
    private ParcelaReceberService parcelaReceberService;
    @Mock
    private CentroCustoRepository centroCustoRepository;
    @Mock
    private PlanoContaRepository planoContaRepository;
    @Mock
    private EmpresaContext empresaContext;

    private ContaReceberService service;
    private Empresa empresa;
    private Cliente cliente;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        ContaReceberMapper mapper = new ContaReceberMapper();
        service = new ContaReceberService(repository, empresaRepository, filialRepository, clienteRepository,
                vendaFaturamentoRepository, parcelaReceberService, centroCustoRepository, planoContaRepository,
                mapper, new ContaReceberValidator(repository), empresaContext);
        empresa = empresa();
        cliente = cliente();
        when(empresaContext.getEmpresaId()).thenReturn(empresa.getId());
    }

    @Test
    void deveCriarContaReceberComSucesso() {
        when(repository.findByEmpresaIdAndNumeroIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(empresa.getId(), "CR-1"))
                .thenReturn(Optional.empty());
        when(empresaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(empresa.getId())).thenReturn(Optional.of(empresa));
        when(clienteRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(cliente.getId())).thenReturn(Optional.of(cliente));
        when(repository.save(any(ContaReceber.class))).thenAnswer(invocation -> {
            ContaReceber conta = invocation.getArgument(0);
            conta.setId(UUID.randomUUID());
            return conta;
        });

        ContaReceberResponse response = service.criar(request("CR-1", "100.00"));

        assertThat(response.id()).isNotNull();
        assertThat(response.status()).isEqualTo(StatusContaReceber.ABERTA);
        assertThat(response.valorPago()).isEqualByComparingTo("0.00");
        assertThat(response.valorAberto()).isEqualByComparingTo("100.00");
    }

    @Test
    void deveBloquearNumeroDuplicado() {
        ContaReceber existente = conta("CR-1", StatusContaReceber.ABERTA);
        when(repository.findByEmpresaIdAndNumeroIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(empresa.getId(), "CR-1"))
                .thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.criar(request("CR-1", "100.00")))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Ja existe conta a receber");
    }

    @Test
    void deveBloquearValorTotalInvalido() {
        when(repository.findByEmpresaIdAndNumeroIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(empresa.getId(), "CR-0"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(request("CR-0", "0.00")))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Valor total");
    }

    @Test
    void deveCancelarContaAbertaEBloquearAlteracaoDeCancelada() {
        ContaReceber conta = conta("CR-2", StatusContaReceber.ABERTA);
        when(repository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(conta.getId(), empresa.getId()))
                .thenReturn(Optional.of(conta));

        ContaReceberResponse response = service.cancelar(conta.getId());

        assertThat(response.status()).isEqualTo(StatusContaReceber.CANCELADA);
        assertThatThrownBy(() -> service.atualizar(conta.getId(), request("CR-2", "150.00")))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("cancelada");
    }

    private ContaReceberRequest request(String numero, String valor) {
        return new ContaReceberRequest(
                null,
                cliente.getId(),
                null,
                null,
                null,
                numero,
                "Conta " + numero,
                LocalDate.of(2026, 7, 8),
                new BigDecimal(valor),
                "Observacao"
        );
    }

    private ContaReceber conta(String numero, StatusContaReceber status) {
        ContaReceber conta = new ContaReceber();
        conta.setId(UUID.randomUUID());
        conta.abrir(empresa, null, cliente, null, numero, "Conta " + numero, LocalDate.of(2026, 7, 8),
                new BigDecimal("100.00"), null);
        conta.setStatus(status);
        return conta;
    }

    private Empresa empresa() {
        Empresa entity = new Empresa("Empresa Teste", "Empresa Teste", "12345678000199", null, null, null);
        entity.setId(UUID.randomUUID());
        return entity;
    }

    private Cliente cliente() {
        Pessoa pessoa = new Pessoa();
        pessoa.setId(UUID.randomUUID());
        pessoa.atualizar(TipoPessoa.JURIDICA, "Cliente Teste", null, "12345678000188", null, null, null, null, null);
        Cliente entity = new Cliente();
        entity.setId(UUID.randomUUID());
        entity.setPessoa(pessoa);
        return entity;
    }
}
