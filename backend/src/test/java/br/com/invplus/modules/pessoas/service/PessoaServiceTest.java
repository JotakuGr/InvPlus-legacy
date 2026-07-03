package br.com.invplus.modules.pessoas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.invplus.common.enums.TipoPessoa;
import br.com.invplus.modules.pessoas.dto.request.PessoaRequest;
import br.com.invplus.modules.pessoas.dto.response.PessoaResponse;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.pessoas.mapper.PessoaMapper;
import br.com.invplus.modules.pessoas.repository.PessoaRepository;
import br.com.invplus.modules.pessoas.validator.PessoaValidator;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ConflictException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

/**
 * Testes unitarios das regras de negocio de Pessoa.
 */
@ExtendWith(MockitoExtension.class)
class PessoaServiceTest {

    @Mock
    private PessoaRepository pessoaRepository;

    private PessoaService pessoaService;

    @BeforeEach
    void setUp() {
        PessoaMapper mapper = new PessoaMapper();
        PessoaValidator validator = new PessoaValidator(pessoaRepository);
        pessoaService = new PessoaService(pessoaRepository, mapper, validator);
    }

    /**
     * Garante criacao de pessoa fisica com CPF normalizado.
     */
    @Test
    void deveCriarPessoaFisicaComSucesso() {
        when(pessoaRepository.findByCpfCnpjAndAtivoTrueAndDeletedAtIsNull("12345678901"))
                .thenReturn(Optional.empty());
        when(pessoaRepository.save(any(Pessoa.class))).thenAnswer(invocation -> {
            Pessoa pessoa = invocation.getArgument(0);
            pessoa.setId(UUID.randomUUID());
            return pessoa;
        });

        PessoaResponse response = pessoaService.criar(requestFisica("123.456.789-01"));

        assertThat(response.tipoPessoa()).isEqualTo(TipoPessoa.FISICA);
        assertThat(response.nome()).isEqualTo("Maria Silva");
        assertThat(response.cpfCnpj()).isEqualTo("12345678901");
    }

    /**
     * Garante criacao de pessoa juridica com CNPJ normalizado.
     */
    @Test
    void deveCriarPessoaJuridicaComSucesso() {
        when(pessoaRepository.findByCpfCnpjAndAtivoTrueAndDeletedAtIsNull("11222333000181"))
                .thenReturn(Optional.empty());
        when(pessoaRepository.save(any(Pessoa.class))).thenAnswer(invocation -> {
            Pessoa pessoa = invocation.getArgument(0);
            pessoa.setId(UUID.randomUUID());
            return pessoa;
        });

        PessoaResponse response = pessoaService.criar(requestJuridica("11.222.333/0001-81"));

        assertThat(response.tipoPessoa()).isEqualTo(TipoPessoa.JURIDICA);
        assertThat(response.nome()).isEqualTo("Empresa Pessoa LTDA");
        assertThat(response.cpfCnpj()).isEqualTo("11222333000181");
    }

    /**
     * Bloqueia documento duplicado entre pessoas ativas.
     */
    @Test
    void deveBloquearCpfCnpjDuplicado() {
        Pessoa existente = new Pessoa();
        existente.setId(UUID.randomUUID());
        existente.atualizar(TipoPessoa.FISICA, "Pessoa Existente", null, "12345678901",
                null, null, null, null, null);

        when(pessoaRepository.findByCpfCnpjAndAtivoTrueAndDeletedAtIsNull("12345678901"))
                .thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> pessoaService.criar(requestFisica("12345678901")))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Ja existe pessoa ativa");
    }

    /**
     * Bloqueia documento com tamanho de CNPJ para pessoa fisica.
     */
    @Test
    void deveBloquearCpfInvalidoParaPessoaFisica() {
        assertThatThrownBy(() -> pessoaService.criar(requestFisica("11222333000181")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CPF invalido para pessoa fisica");
    }

    /**
     * Bloqueia documento com tamanho de CPF para pessoa juridica.
     */
    @Test
    void deveBloquearCnpjInvalidoParaPessoaJuridica() {
        assertThatThrownBy(() -> pessoaService.criar(requestJuridica("12345678901")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CNPJ invalido para pessoa juridica");
    }

    /**
     * Confirma que a exclusao logica marca a pessoa como inativa.
     */
    @Test
    void deveExcluirLogicamente() {
        UUID id = UUID.randomUUID();
        Pessoa pessoa = new Pessoa();
        pessoa.setId(id);
        pessoa.atualizar(TipoPessoa.FISICA, "Pessoa Excluir", null, "12345678901",
                null, null, null, null, null);

        when(pessoaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)).thenReturn(Optional.of(pessoa));

        pessoaService.excluir(id);

        assertThat(pessoa.getAtivo()).isFalse();
        assertThat(pessoa.getDeletedAt()).isNotNull();
    }

    /**
     * Mantem o contrato paginado do service.
     */
    @Test
    void deveListarComPaginacao() {
        when(pessoaRepository.findByAtivoTrueAndDeletedAtIsNull(PageRequest.of(0, 20)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        pessoaService.listar(PageRequest.of(0, 20));

        verify(pessoaRepository).findByAtivoTrueAndDeletedAtIsNull(PageRequest.of(0, 20));
    }

    private PessoaRequest requestFisica(String cpf) {
        return new PessoaRequest(
                TipoPessoa.FISICA,
                " Maria Silva ",
                null,
                cpf,
                "1234567",
                "7133334444",
                "71999998888",
                "maria@teste.com",
                "Cliente inicial"
        );
    }

    private PessoaRequest requestJuridica(String cnpj) {
        return new PessoaRequest(
                TipoPessoa.JURIDICA,
                " Empresa Pessoa LTDA ",
                "Empresa Pessoa",
                cnpj,
                "ISENTO",
                "7133334444",
                "71999998888",
                "empresa-pessoa@teste.com",
                "Fornecedor inicial"
        );
    }
}
