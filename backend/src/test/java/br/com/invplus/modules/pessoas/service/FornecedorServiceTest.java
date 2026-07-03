package br.com.invplus.modules.pessoas.service;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import br.com.invplus.common.enums.TipoPessoa;
import br.com.invplus.modules.pessoas.dto.request.FornecedorRequest;
import br.com.invplus.modules.pessoas.dto.response.FornecedorResponse;
import br.com.invplus.modules.pessoas.entity.Fornecedor;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.pessoas.mapper.FornecedorMapper;
import br.com.invplus.modules.pessoas.repository.FornecedorRepository;
import br.com.invplus.modules.pessoas.repository.PessoaRepository;
import br.com.invplus.modules.pessoas.validator.FornecedorValidator;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;

/**
 * Testes unitarios das regras de negocio de Fornecedor.
 */
@ExtendWith(MockitoExtension.class)
class FornecedorServiceTest {

    @Mock
    private FornecedorRepository fornecedorRepository;

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private PessoaRoleService pessoaRoleService;

    private FornecedorService fornecedorService;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        FornecedorMapper mapper = new FornecedorMapper();
        FornecedorValidator validator = new FornecedorValidator(fornecedorRepository);
        fornecedorService = new FornecedorService(fornecedorRepository, mapper, validator, pessoaRepository, pessoaRoleService);
    }

    /**
     * Garante criacao do fornecedor com dados comerciais e pessoa vinculada.
     */
    @Test
    void deveCriarFornecedorComSucesso() {
        Pessoa pessoa = pessoaAtiva();
        when(fornecedorRepository.findByPessoaIdAndAtivoTrueAndDeletedAtIsNull(pessoa.getId()))
                .thenReturn(Optional.empty());
        when(pessoaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(pessoa.getId()))
                .thenReturn(Optional.of(pessoa));
        when(fornecedorRepository.save(any(Fornecedor.class))).thenAnswer(invocation -> {
            Fornecedor fornecedor = invocation.getArgument(0);
            fornecedor.setId(UUID.randomUUID());
            return fornecedor;
        });

        FornecedorResponse response = fornecedorService.criar(request(pessoa.getId(), 7));

        assertThat(response.pessoaId()).isEqualTo(pessoa.getId());
        assertThat(response.nome()).isEqualTo("Fornecedor Teste");
        assertThat(response.cpfCnpj()).isEqualTo("12345678000190");
        assertThat(response.prazoMedioEntrega()).isEqualTo(7);
    }

    /**
     * Bloqueia criacao quando a pessoa nao existe ou nao esta ativa.
     */
    @Test
    void deveBloquearPessoaInexistenteOuInativa() {
        UUID pessoaId = UUID.randomUUID();
        when(fornecedorRepository.findByPessoaIdAndAtivoTrueAndDeletedAtIsNull(pessoaId))
                .thenReturn(Optional.empty());
        when(pessoaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(pessoaId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> fornecedorService.criar(request(pessoaId, 10)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Pessoa nao encontrada");
    }

    /**
     * Bloqueia dois fornecedores ativos para a mesma pessoa.
     */
    @Test
    void deveBloquearFornecedorDuplicadoParaMesmaPessoa() {
        Pessoa pessoa = pessoaAtiva();
        Fornecedor existente = new Fornecedor();
        existente.setId(UUID.randomUUID());
        existente.setPessoa(pessoa);

        when(fornecedorRepository.findByPessoaIdAndAtivoTrueAndDeletedAtIsNull(pessoa.getId()))
                .thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> fornecedorService.criar(request(pessoa.getId(), 10)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Pessoa ja possui fornecedor ativo");
    }

    /**
     * Bloqueia prazo medio de entrega negativo.
     */
    @Test
    void deveBloquearPrazoMedioNegativo() {
        assertThatThrownBy(() -> fornecedorService.criar(request(UUID.randomUUID(), -1)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Prazo medio de entrega nao pode ser negativo");
    }

    /**
     * Busca fornecedor ativo por id.
     */
    @Test
    void deveBuscarPorId() {
        Fornecedor fornecedor = fornecedorAtivo();
        when(fornecedorRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(fornecedor.getId()))
                .thenReturn(Optional.of(fornecedor));

        FornecedorResponse response = fornecedorService.buscar(fornecedor.getId());

        assertThat(response.id()).isEqualTo(fornecedor.getId());
        assertThat(response.nome()).isEqualTo("Fornecedor Teste");
    }

    /**
     * Mantem listagem paginada.
     */
    @Test
    void deveListarComPaginacao() {
        when(fornecedorRepository.findByAtivoTrueAndDeletedAtIsNull(PageRequest.of(0, 20)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        fornecedorService.listar(PageRequest.of(0, 20));

        verify(fornecedorRepository).findByAtivoTrueAndDeletedAtIsNull(PageRequest.of(0, 20));
    }

    /**
     * Atualiza prazo e observacao do fornecedor.
     */
    @Test
    void deveAtualizarFornecedor() {
        Fornecedor fornecedor = fornecedorAtivo();
        UUID pessoaId = fornecedor.getPessoa().getId();

        when(fornecedorRepository.findByPessoaIdAndAtivoTrueAndDeletedAtIsNull(pessoaId))
                .thenReturn(Optional.of(fornecedor));
        when(fornecedorRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(fornecedor.getId()))
                .thenReturn(Optional.of(fornecedor));
        when(pessoaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(pessoaId))
                .thenReturn(Optional.of(fornecedor.getPessoa()));

        FornecedorResponse response = fornecedorService.atualizar(
                fornecedor.getId(),
                request(pessoaId, 15)
        );

        assertThat(response.prazoMedioEntrega()).isEqualTo(15);
        assertThat(response.observacao()).isEqualTo("Fornecedor recorrente");
    }

    /**
     * Confirma exclusao logica do papel Fornecedor.
     */
    @Test
    void deveExcluirLogicamente() {
        Fornecedor fornecedor = fornecedorAtivo();
        when(fornecedorRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(fornecedor.getId()))
                .thenReturn(Optional.of(fornecedor));

        fornecedorService.excluir(fornecedor.getId());

        assertThat(fornecedor.getAtivo()).isFalse();
        assertThat(fornecedor.getDeletedAt()).isNotNull();
    }

    private FornecedorRequest request(UUID pessoaId, Integer prazoMedioEntrega) {
        return new FornecedorRequest(
                pessoaId,
                UUID.randomUUID(),
                prazoMedioEntrega,
                " Fornecedor recorrente "
        );
    }

    private Fornecedor fornecedorAtivo() {
        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setId(UUID.randomUUID());
        fornecedor.setPessoa(pessoaAtiva());
        fornecedor.atualizar(null, 5, "Fornecedor ativo");
        return fornecedor;
    }

    private Pessoa pessoaAtiva() {
        Pessoa pessoa = new Pessoa();
        pessoa.setId(UUID.randomUUID());
        pessoa.atualizar(TipoPessoa.JURIDICA, "Fornecedor Teste", "Fornecedor LTDA", "12345678000190",
                null, "7133334444", "71999998888", "fornecedor@teste.com", null);
        return pessoa;
    }
}
