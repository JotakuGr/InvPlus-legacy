package br.com.invplus.modules.pessoas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.invplus.common.enums.TipoPessoa;
import br.com.invplus.modules.pessoas.dto.request.ClienteRequest;
import br.com.invplus.modules.pessoas.dto.response.ClienteResponse;
import br.com.invplus.modules.pessoas.entity.Cliente;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.pessoas.mapper.ClienteMapper;
import br.com.invplus.modules.pessoas.repository.ClienteRepository;
import br.com.invplus.modules.pessoas.repository.PessoaRepository;
import br.com.invplus.modules.pessoas.validator.ClienteValidator;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

/**
 * Testes unitarios das regras de negocio de Cliente.
 */
@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private PessoaRoleService pessoaRoleService;

    private ClienteService clienteService;

    @BeforeEach
    void setUp() {
        ClienteMapper mapper = new ClienteMapper();
        ClienteValidator validator = new ClienteValidator(clienteRepository);
        clienteService = new ClienteService(clienteRepository, mapper, validator, pessoaRepository, pessoaRoleService);
    }

    /**
     * Garante criacao do cliente com dados comerciais e pessoa vinculada.
     */
    @Test
    void deveCriarClienteComSucesso() {
        Pessoa pessoa = pessoaAtiva();
        when(clienteRepository.findByPessoaIdAndAtivoTrueAndDeletedAtIsNull(pessoa.getId()))
                .thenReturn(Optional.empty());
        when(pessoaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(pessoa.getId()))
                .thenReturn(Optional.of(pessoa));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> {
            Cliente cliente = invocation.getArgument(0);
            cliente.setId(UUID.randomUUID());
            return cliente;
        });

        ClienteResponse response = clienteService.criar(request(pessoa.getId(), BigDecimal.valueOf(1500)));

        assertThat(response.pessoaId()).isEqualTo(pessoa.getId());
        assertThat(response.nome()).isEqualTo("Maria Cliente");
        assertThat(response.cpfCnpj()).isEqualTo("12345678901");
        assertThat(response.limiteCredito()).isEqualByComparingTo("1500");
    }

    /**
     * Bloqueia criacao quando a pessoa nao existe ou nao esta ativa.
     */
    @Test
    void deveBloquearPessoaInexistenteOuInativa() {
        UUID pessoaId = UUID.randomUUID();
        when(clienteRepository.findByPessoaIdAndAtivoTrueAndDeletedAtIsNull(pessoaId))
                .thenReturn(Optional.empty());
        when(pessoaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(pessoaId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.criar(request(pessoaId, BigDecimal.TEN)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Pessoa nao encontrada");
    }

    /**
     * Bloqueia dois clientes ativos para a mesma pessoa.
     */
    @Test
    void deveBloquearClienteDuplicadoParaMesmaPessoa() {
        Pessoa pessoa = pessoaAtiva();
        Cliente existente = new Cliente();
        existente.setId(UUID.randomUUID());
        existente.setPessoa(pessoa);

        when(clienteRepository.findByPessoaIdAndAtivoTrueAndDeletedAtIsNull(pessoa.getId()))
                .thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> clienteService.criar(request(pessoa.getId(), BigDecimal.TEN)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Pessoa ja possui cliente ativo");
    }

    /**
     * Bloqueia limite de credito negativo.
     */
    @Test
    void deveBloquearLimiteCreditoNegativo() {
        assertThatThrownBy(() -> clienteService.criar(request(UUID.randomUUID(), BigDecimal.valueOf(-1))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Limite de credito nao pode ser negativo");
    }

    /**
     * Busca cliente ativo por id.
     */
    @Test
    void deveBuscarPorId() {
        Cliente cliente = clienteAtivo();
        when(clienteRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(cliente.getId()))
                .thenReturn(Optional.of(cliente));

        ClienteResponse response = clienteService.buscar(cliente.getId());

        assertThat(response.id()).isEqualTo(cliente.getId());
        assertThat(response.nome()).isEqualTo("Maria Cliente");
    }

    /**
     * Mantem listagem paginada.
     */
    @Test
    void deveListarComPaginacao() {
        when(clienteRepository.findByAtivoTrueAndDeletedAtIsNull(PageRequest.of(0, 20)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        clienteService.listar(PageRequest.of(0, 20));

        verify(clienteRepository).findByAtivoTrueAndDeletedAtIsNull(PageRequest.of(0, 20));
    }

    /**
     * Atualiza limite e observacao do cliente.
     */
    @Test
    void deveAtualizarCliente() {
        Cliente cliente = clienteAtivo();
        UUID pessoaId = cliente.getPessoa().getId();

        when(clienteRepository.findByPessoaIdAndAtivoTrueAndDeletedAtIsNull(pessoaId))
                .thenReturn(Optional.of(cliente));
        when(clienteRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(cliente.getId()))
                .thenReturn(Optional.of(cliente));
        when(pessoaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(pessoaId))
                .thenReturn(Optional.of(cliente.getPessoa()));

        ClienteResponse response = clienteService.atualizar(
                cliente.getId(),
                request(pessoaId, BigDecimal.valueOf(3000))
        );

        assertThat(response.limiteCredito()).isEqualByComparingTo("3000");
        assertThat(response.observacao()).isEqualTo("Cliente preferencial");
    }

    /**
     * Confirma exclusao logica do papel Cliente.
     */
    @Test
    void deveExcluirLogicamente() {
        Cliente cliente = clienteAtivo();
        when(clienteRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(cliente.getId()))
                .thenReturn(Optional.of(cliente));

        clienteService.excluir(cliente.getId());

        assertThat(cliente.getAtivo()).isFalse();
        assertThat(cliente.getDeletedAt()).isNotNull();
    }

    private ClienteRequest request(UUID pessoaId, BigDecimal limiteCredito) {
        return new ClienteRequest(
                pessoaId,
                limiteCredito,
                UUID.randomUUID(),
                UUID.randomUUID(),
                " Cliente preferencial "
        );
    }

    private Cliente clienteAtivo() {
        Cliente cliente = new Cliente();
        cliente.setId(UUID.randomUUID());
        cliente.setPessoa(pessoaAtiva());
        cliente.atualizar(BigDecimal.valueOf(1000), null, null, "Cliente ativo");
        return cliente;
    }

    private Pessoa pessoaAtiva() {
        Pessoa pessoa = new Pessoa();
        pessoa.setId(UUID.randomUUID());
        pessoa.atualizar(TipoPessoa.FISICA, "Maria Cliente", null, "12345678901",
                null, "7133334444", "71999998888", "maria@teste.com", null);
        return pessoa;
    }
}
