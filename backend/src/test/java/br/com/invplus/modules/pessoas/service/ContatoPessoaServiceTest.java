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
import br.com.invplus.modules.pessoas.dto.request.ContatoPessoaRequest;
import br.com.invplus.modules.pessoas.dto.response.ContatoPessoaResponse;
import br.com.invplus.modules.pessoas.entity.ContatoPessoa;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.pessoas.mapper.ContatoPessoaMapper;
import br.com.invplus.modules.pessoas.repository.ContatoPessoaRepository;
import br.com.invplus.modules.pessoas.repository.PessoaRepository;
import br.com.invplus.modules.pessoas.validator.ContatoPessoaValidator;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;

/**
 * Testes unitarios das regras de negocio de ContatoPessoa.
 */
@ExtendWith(MockitoExtension.class)
class ContatoPessoaServiceTest {

    @Mock
    private ContatoPessoaRepository contatoPessoaRepository;

    @Mock
    private PessoaRepository pessoaRepository;

    private ContatoPessoaService contatoPessoaService;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        ContatoPessoaMapper mapper = new ContatoPessoaMapper();
        ContatoPessoaValidator validator = new ContatoPessoaValidator();
        contatoPessoaService = new ContatoPessoaService(contatoPessoaRepository, mapper, validator, pessoaRepository);
    }

    /**
     * Garante criacao de contato vinculado a uma Pessoa ativa.
     */
    @Test
    void deveCriarContatoComSucesso() {
        Pessoa pessoa = pessoaAtiva();
        when(pessoaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(pessoa.getId()))
                .thenReturn(Optional.of(pessoa));
        when(contatoPessoaRepository.save(any(ContatoPessoa.class))).thenAnswer(invocation -> {
            ContatoPessoa contato = invocation.getArgument(0);
            contato.setId(UUID.randomUUID());
            return contato;
        });

        ContatoPessoaResponse response = contatoPessoaService.criar(request(pessoa.getId(), "Maria Financeiro", "maria@teste.com"));

        assertThat(response.pessoaId()).isEqualTo(pessoa.getId());
        assertThat(response.pessoaNome()).isEqualTo("Empresa Contato");
        assertThat(response.nome()).isEqualTo("Maria Financeiro");
        assertThat(response.email()).isEqualTo("maria@teste.com");
        assertThat(response.principal()).isTrue();
    }

    /**
     * Bloqueia contato quando a Pessoa nao existe ou nao esta ativa.
     */
    @Test
    void deveBloquearPessoaInexistenteOuInativa() {
        UUID pessoaId = UUID.randomUUID();
        when(pessoaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(pessoaId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> contatoPessoaService.criar(request(pessoaId, "Maria", "maria@teste.com")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Pessoa nao encontrada");
    }

    /**
     * Nome em branco nao deve chegar ao banco.
     */
    @Test
    void deveBloquearNomeObrigatorio() {
        assertThatThrownBy(() -> contatoPessoaService.criar(request(UUID.randomUUID(), " ", "maria@teste.com")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Nome do contato e obrigatorio");
    }

    /**
     * Email invalido retorna erro funcional.
     */
    @Test
    void deveBloquearEmailInvalido() {
        assertThatThrownBy(() -> contatoPessoaService.criar(request(UUID.randomUUID(), "Maria", "email-invalido")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Email do contato invalido");
    }

    /**
     * Busca contato ativo por id.
     */
    @Test
    void deveBuscarPorId() {
        ContatoPessoa contato = contatoAtivo();
        when(contatoPessoaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(contato.getId()))
                .thenReturn(Optional.of(contato));

        ContatoPessoaResponse response = contatoPessoaService.buscar(contato.getId());

        assertThat(response.id()).isEqualTo(contato.getId());
        assertThat(response.nome()).isEqualTo("Maria Financeiro");
    }

    /**
     * Mantem listagem paginada por padrao.
     */
    @Test
    void deveListarComPaginacao() {
        when(contatoPessoaRepository.findByAtivoTrueAndDeletedAtIsNull(PageRequest.of(0, 20)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        contatoPessoaService.listar(PageRequest.of(0, 20));

        verify(contatoPessoaRepository).findByAtivoTrueAndDeletedAtIsNull(PageRequest.of(0, 20));
    }

    /**
     * Atualiza dados do contato sem alterar dados cadastrais da Pessoa.
     */
    @Test
    void deveAtualizarContato() {
        ContatoPessoa contato = contatoAtivo();
        UUID pessoaId = contato.getPessoa().getId();

        when(contatoPessoaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(contato.getId()))
                .thenReturn(Optional.of(contato));
        when(pessoaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(pessoaId))
                .thenReturn(Optional.of(contato.getPessoa()));

        ContatoPessoaResponse response = contatoPessoaService.atualizar(
                contato.getId(),
                request(pessoaId, "Joao Compras", "joao@teste.com")
        );

        assertThat(response.nome()).isEqualTo("Joao Compras");
        assertThat(response.email()).isEqualTo("joao@teste.com");
        assertThat(response.tipo()).isEqualTo("FINANCEIRO");
    }

    /**
     * Confirma exclusao logica do contato.
     */
    @Test
    void deveExcluirLogicamente() {
        ContatoPessoa contato = contatoAtivo();
        when(contatoPessoaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(contato.getId()))
                .thenReturn(Optional.of(contato));

        contatoPessoaService.excluir(contato.getId());

        assertThat(contato.getAtivo()).isFalse();
        assertThat(contato.getDeletedAt()).isNotNull();
    }

    private ContatoPessoaRequest request(UUID pessoaId, String nome, String email) {
        return new ContatoPessoaRequest(
                pessoaId,
                nome,
                "7133334444",
                "71999998888",
                email,
                "Analista",
                "financeiro",
                true,
                "Contato operacional"
        );
    }

    private ContatoPessoa contatoAtivo() {
        ContatoPessoa contato = new ContatoPessoa();
        contato.setId(UUID.randomUUID());
        contato.setPessoa(pessoaAtiva());
        contato.atualizar("Maria Financeiro", "7133334444", "71999998888", "maria@teste.com",
                "Analista", "financeiro", true, "Contato operacional");
        return contato;
    }

    private Pessoa pessoaAtiva() {
        Pessoa pessoa = new Pessoa();
        pessoa.setId(UUID.randomUUID());
        pessoa.atualizar(TipoPessoa.JURIDICA, "Empresa Contato", "Empresa Contato LTDA", "12345678000190",
                null, "7133334444", "71999998888", "empresa-contato@teste.com", null);
        return pessoa;
    }
}
