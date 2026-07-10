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
import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.cadastros.repository.FilialRepository;
import br.com.invplus.modules.compras.repository.EntradaMercadoriaRepository;
import br.com.invplus.modules.financeiro.dto.request.ContaPagarRequest;
import br.com.invplus.modules.financeiro.dto.response.ContaPagarResponse;
import br.com.invplus.modules.financeiro.entity.ContaPagar;
import br.com.invplus.modules.financeiro.enums.StatusContaPagar;
import br.com.invplus.modules.financeiro.mapper.ContaPagarMapper;
import br.com.invplus.modules.financeiro.repository.CentroCustoRepository;
import br.com.invplus.modules.financeiro.repository.ContaPagarRepository;
import br.com.invplus.modules.financeiro.repository.PlanoContaRepository;
import br.com.invplus.modules.financeiro.validator.ContaPagarValidator;
import br.com.invplus.modules.pessoas.entity.Fornecedor;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.pessoas.repository.FornecedorRepository;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;

/**
 * Testes unitarios das regras centrais de ContaPagar.
 */
@ExtendWith(MockitoExtension.class)
class ContaPagarServiceTest {

    @Mock
    private ContaPagarRepository repository;
    @Mock
    private EmpresaRepository empresaRepository;
    @Mock
    private FilialRepository filialRepository;
    @Mock
    private FornecedorRepository fornecedorRepository;
    @Mock
    private EntradaMercadoriaRepository entradaMercadoriaRepository;
    @Mock
    private CentroCustoRepository centroCustoRepository;
    @Mock
    private PlanoContaRepository planoContaRepository;

    private ContaPagarService service;
    private Empresa empresa;
    private Fornecedor fornecedor;

    @BeforeEach
    void setUp() {
        service = new ContaPagarService(repository, empresaRepository, filialRepository, fornecedorRepository,
                entradaMercadoriaRepository, centroCustoRepository, planoContaRepository,
                new ContaPagarMapper(), new ContaPagarValidator(repository));
        empresa = empresa();
        fornecedor = fornecedor();
    }

    @Test
    void deveCriarContaPagarComSucesso() {
        prepararRelacionamentos();
        when(repository.findByEmpresaIdAndNumeroIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(empresa.getId(), "CP-1"))
                .thenReturn(Optional.empty());
        when(repository.save(any(ContaPagar.class))).thenAnswer(invocation -> {
            ContaPagar conta = invocation.getArgument(0);
            conta.setId(UUID.randomUUID());
            return conta;
        });

        ContaPagarResponse response = service.criar(request("CP-1", "100.00"));

        assertThat(response.status()).isEqualTo(StatusContaPagar.ABERTA);
        assertThat(response.valorPago()).isEqualByComparingTo("0.00");
        assertThat(response.valorAberto()).isEqualByComparingTo("100.00");
    }

    @Test
    void deveBloquearEmpresaInexistente() {
        when(repository.findByEmpresaIdAndNumeroIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(empresa.getId(), "CP-2"))
                .thenReturn(Optional.empty());
        when(empresaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(empresa.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(request("CP-2", "100.00")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Empresa");
    }

    @Test
    void deveBloquearFornecedorInexistente() {
        when(repository.findByEmpresaIdAndNumeroIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(empresa.getId(), "CP-3"))
                .thenReturn(Optional.empty());
        when(empresaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(empresa.getId())).thenReturn(Optional.of(empresa));
        when(fornecedorRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(fornecedor.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(request("CP-3", "100.00")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Fornecedor");
    }

    @Test
    void deveBloquearFilialDeOutraEmpresa() {
        Empresa outraEmpresa = empresa();
        Filial filial = new Filial();
        filial.setId(UUID.randomUUID());
        filial.setEmpresa(outraEmpresa);
        ContaPagarRequest request = new ContaPagarRequest(empresa.getId(), filial.getId(), fornecedor.getId(), null,
                "CP-FILIAL", "Conta filial", LocalDate.of(2026, 7, 8), new BigDecimal("100.00"), null);
        when(repository.findByEmpresaIdAndNumeroIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(
                empresa.getId(), "CP-FILIAL")).thenReturn(Optional.empty());
        when(empresaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(empresa.getId())).thenReturn(Optional.of(empresa));
        when(filialRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(filial.getId())).thenReturn(Optional.of(filial));

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("nao pertence a empresa");
    }

    @Test
    void deveBloquearNumeroDuplicado() {
        ContaPagar existente = conta("CP-4", StatusContaPagar.ABERTA);
        when(repository.findByEmpresaIdAndNumeroIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(empresa.getId(), "CP-4"))
                .thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.criar(request("CP-4", "100.00")))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Ja existe conta a pagar");
    }

    @Test
    void deveBloquearValorZeroOuNegativo() {
        when(repository.findByEmpresaIdAndNumeroIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(empresa.getId(), "CP-0"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(request("CP-0", "0.00")))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Valor total");
    }

    @Test
    void deveCancelarContaEBloquearNovaAlteracao() {
        ContaPagar conta = conta("CP-5", StatusContaPagar.ABERTA);
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(conta.getId())).thenReturn(Optional.of(conta));

        assertThat(service.cancelar(conta.getId()).status()).isEqualTo(StatusContaPagar.CANCELADA);
        assertThatThrownBy(() -> service.atualizar(conta.getId(), request("CP-5", "150.00")))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("cancelada");
    }

    @Test
    void deveBloquearAlteracaoDeContaPaga() {
        ContaPagar conta = conta("CP-6", StatusContaPagar.PAGA);
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(conta.getId())).thenReturn(Optional.of(conta));

        assertThatThrownBy(() -> service.atualizar(conta.getId(), request("CP-6", "150.00")))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("paga");
    }

    private void prepararRelacionamentos() {
        when(empresaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(empresa.getId())).thenReturn(Optional.of(empresa));
        when(fornecedorRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(fornecedor.getId()))
                .thenReturn(Optional.of(fornecedor));
    }

    private ContaPagarRequest request(String numero, String valor) {
        return new ContaPagarRequest(empresa.getId(), null, fornecedor.getId(), null, numero,
                "Conta " + numero, LocalDate.of(2026, 7, 8), new BigDecimal(valor), "Observacao");
    }

    private ContaPagar conta(String numero, StatusContaPagar status) {
        ContaPagar conta = new ContaPagar();
        conta.setId(UUID.randomUUID());
        conta.abrir(empresa, null, fornecedor, null, numero, "Conta " + numero,
                LocalDate.of(2026, 7, 8), new BigDecimal("100.00"), null);
        conta.setStatus(status);
        return conta;
    }

    private Empresa empresa() {
        Empresa entity = new Empresa("Empresa Teste", "Empresa Teste", "12345678000199", null, null, null);
        entity.setId(UUID.randomUUID());
        return entity;
    }

    private Fornecedor fornecedor() {
        Pessoa pessoa = new Pessoa();
        pessoa.setId(UUID.randomUUID());
        pessoa.atualizar(TipoPessoa.JURIDICA, "Fornecedor Teste", null, "12345678000188",
                null, null, null, null, null);
        Fornecedor entity = new Fornecedor();
        entity.setId(UUID.randomUUID());
        entity.setPessoa(pessoa);
        return entity;
    }
}
