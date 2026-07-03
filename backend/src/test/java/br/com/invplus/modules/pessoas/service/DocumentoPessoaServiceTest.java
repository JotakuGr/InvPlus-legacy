package br.com.invplus.modules.pessoas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.invplus.common.enums.TipoPessoa;
import br.com.invplus.modules.cadastros.entity.Arquivo;
import br.com.invplus.modules.cadastros.enums.StorageArquivo;
import br.com.invplus.modules.cadastros.repository.ArquivoRepository;
import br.com.invplus.modules.pessoas.dto.request.DocumentoPessoaRequest;
import br.com.invplus.modules.pessoas.dto.response.DocumentoPessoaResponse;
import br.com.invplus.modules.pessoas.entity.DocumentoPessoa;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.pessoas.enums.TipoDocumentoPessoa;
import br.com.invplus.modules.pessoas.mapper.DocumentoPessoaMapper;
import br.com.invplus.modules.pessoas.repository.DocumentoPessoaRepository;
import br.com.invplus.modules.pessoas.repository.PessoaRepository;
import br.com.invplus.modules.pessoas.validator.DocumentoPessoaValidator;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

/**
 * Testes unitarios das regras de negocio de DocumentoPessoa.
 */
@ExtendWith(MockitoExtension.class)
class DocumentoPessoaServiceTest {

    @Mock
    private DocumentoPessoaRepository documentoPessoaRepository;

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private ArquivoRepository arquivoRepository;

    private DocumentoPessoaService documentoPessoaService;

    @BeforeEach
    void setUp() {
        DocumentoPessoaMapper mapper = new DocumentoPessoaMapper();
        DocumentoPessoaValidator validator = new DocumentoPessoaValidator(documentoPessoaRepository);
        documentoPessoaService = new DocumentoPessoaService(
                documentoPessoaRepository,
                mapper,
                validator,
                pessoaRepository,
                arquivoRepository
        );
    }

    /**
     * Garante criacao de documento sem arquivo anexado.
     */
    @Test
    void deveCriarDocumentoSemArquivoComSucesso() {
        Pessoa pessoa = pessoaAtiva();
        when(documentoPessoaRepository.existsByPessoaIdAndTipoDocumentoAndNumeroAndAtivoTrueAndDeletedAtIsNull(
                pessoa.getId(), TipoDocumentoPessoa.RG, "12345")).thenReturn(false);
        when(pessoaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(pessoa.getId()))
                .thenReturn(Optional.of(pessoa));
        when(documentoPessoaRepository.save(any(DocumentoPessoa.class))).thenAnswer(invocation -> {
            DocumentoPessoa documento = invocation.getArgument(0);
            documento.setId(UUID.randomUUID());
            return documento;
        });

        DocumentoPessoaResponse response = documentoPessoaService.criar(request(pessoa.getId(), null, "12345"));

        assertThat(response.pessoaId()).isEqualTo(pessoa.getId());
        assertThat(response.nomePessoa()).isEqualTo("Pessoa Documento");
        assertThat(response.arquivoId()).isNull();
        assertThat(response.tipoDocumento()).isEqualTo(TipoDocumentoPessoa.RG);
        assertThat(response.numero()).isEqualTo("12345");
    }

    /**
     * Garante criacao de documento com anexo digital ativo.
     */
    @Test
    void deveCriarDocumentoComArquivoComSucesso() {
        Pessoa pessoa = pessoaAtiva();
        Arquivo arquivo = arquivoAtivo();
        when(documentoPessoaRepository.existsByPessoaIdAndTipoDocumentoAndNumeroAndAtivoTrueAndDeletedAtIsNull(
                pessoa.getId(), TipoDocumentoPessoa.RG, "12345")).thenReturn(false);
        when(pessoaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(pessoa.getId()))
                .thenReturn(Optional.of(pessoa));
        when(arquivoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(arquivo.getId()))
                .thenReturn(Optional.of(arquivo));
        when(documentoPessoaRepository.save(any(DocumentoPessoa.class))).thenAnswer(invocation -> {
            DocumentoPessoa documento = invocation.getArgument(0);
            documento.setId(UUID.randomUUID());
            return documento;
        });

        DocumentoPessoaResponse response = documentoPessoaService.criar(request(pessoa.getId(), arquivo.getId(), "12345"));

        assertThat(response.arquivoId()).isEqualTo(arquivo.getId());
        assertThat(response.nomeArquivo()).isEqualTo("documento.pdf");
    }

    /**
     * Pessoa inexistente ou inativa deve bloquear criacao.
     */
    @Test
    void deveBloquearPessoaInexistenteOuInativa() {
        UUID pessoaId = UUID.randomUUID();
        when(documentoPessoaRepository.existsByPessoaIdAndTipoDocumentoAndNumeroAndAtivoTrueAndDeletedAtIsNull(
                pessoaId, TipoDocumentoPessoa.RG, "12345")).thenReturn(false);
        when(pessoaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(pessoaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentoPessoaService.criar(request(pessoaId, null, "12345")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Pessoa nao encontrada");
    }

    /**
     * Arquivo inexistente ou inativo deve bloquear criacao quando informado.
     */
    @Test
    void deveBloquearArquivoInexistenteOuInativo() {
        Pessoa pessoa = pessoaAtiva();
        UUID arquivoId = UUID.randomUUID();
        when(documentoPessoaRepository.existsByPessoaIdAndTipoDocumentoAndNumeroAndAtivoTrueAndDeletedAtIsNull(
                pessoa.getId(), TipoDocumentoPessoa.RG, "12345")).thenReturn(false);
        when(pessoaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(pessoa.getId())).thenReturn(Optional.of(pessoa));
        when(arquivoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(arquivoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentoPessoaService.criar(request(pessoa.getId(), arquivoId, "12345")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Arquivo nao encontrado");
    }

    /**
     * Bloqueia documento duplicado para a mesma Pessoa.
     */
    @Test
    void deveBloquearDocumentoDuplicado() {
        Pessoa pessoa = pessoaAtiva();
        when(documentoPessoaRepository.existsByPessoaIdAndTipoDocumentoAndNumeroAndAtivoTrueAndDeletedAtIsNull(
                pessoa.getId(), TipoDocumentoPessoa.RG, "12345")).thenReturn(true);

        assertThatThrownBy(() -> documentoPessoaService.criar(request(pessoa.getId(), null, "12345")))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Pessoa ja possui documento ativo");
    }

    /**
     * Data de validade anterior a emissao e regra invalida.
     */
    @Test
    void deveBloquearValidadeAnteriorAEmissao() {
        DocumentoPessoaRequest request = new DocumentoPessoaRequest(
                UUID.randomUUID(),
                null,
                TipoDocumentoPessoa.CNH,
                "CNH123",
                "DETRAN",
                LocalDate.of(2026, 7, 3),
                LocalDate.of(2026, 7, 2),
                "Documento vencido"
        );

        assertThatThrownBy(() -> documentoPessoaService.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Data de validade nao pode ser anterior");
    }

    /**
     * Busca documento ativo por id.
     */
    @Test
    void deveBuscarPorId() {
        DocumentoPessoa documento = documentoAtivo();
        when(documentoPessoaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(documento.getId()))
                .thenReturn(Optional.of(documento));

        DocumentoPessoaResponse response = documentoPessoaService.buscar(documento.getId());

        assertThat(response.id()).isEqualTo(documento.getId());
        assertThat(response.numero()).isEqualTo("12345");
    }

    /**
     * Mantem listagem paginada por padrao.
     */
    @Test
    void deveListarComPaginacao() {
        when(documentoPessoaRepository.findByAtivoTrueAndDeletedAtIsNull(PageRequest.of(0, 20)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        documentoPessoaService.listar(PageRequest.of(0, 20));

        verify(documentoPessoaRepository).findByAtivoTrueAndDeletedAtIsNull(PageRequest.of(0, 20));
    }

    /**
     * Atualiza dados documentais e permite trocar anexo.
     */
    @Test
    void deveAtualizarDocumento() {
        DocumentoPessoa documento = documentoAtivo();
        Pessoa pessoa = documento.getPessoa();
        Arquivo arquivo = arquivoAtivo();
        when(documentoPessoaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(documento.getId()))
                .thenReturn(Optional.of(documento));
        when(pessoaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(pessoa.getId())).thenReturn(Optional.of(pessoa));
        when(arquivoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(arquivo.getId())).thenReturn(Optional.of(arquivo));

        DocumentoPessoaResponse response = documentoPessoaService.atualizar(
                documento.getId(),
                request(pessoa.getId(), arquivo.getId(), "67890")
        );

        assertThat(response.numero()).isEqualTo("67890");
        assertThat(response.arquivoId()).isEqualTo(arquivo.getId());
    }

    /**
     * Confirma exclusao logica do documento.
     */
    @Test
    void deveExcluirLogicamente() {
        DocumentoPessoa documento = documentoAtivo();
        when(documentoPessoaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(documento.getId()))
                .thenReturn(Optional.of(documento));

        documentoPessoaService.excluir(documento.getId());

        assertThat(documento.getAtivo()).isFalse();
        assertThat(documento.getDeletedAt()).isNotNull();
    }

    private DocumentoPessoaRequest request(UUID pessoaId, UUID arquivoId, String numero) {
        return new DocumentoPessoaRequest(
                pessoaId,
                arquivoId,
                TipoDocumentoPessoa.RG,
                numero,
                "SSP",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2027, 1, 1),
                "Documento cadastral"
        );
    }

    private DocumentoPessoa documentoAtivo() {
        DocumentoPessoa documento = new DocumentoPessoa();
        documento.setId(UUID.randomUUID());
        documento.setPessoa(pessoaAtiva());
        documento.atualizar(TipoDocumentoPessoa.RG, "12345", "SSP",
                LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1), "Documento ativo");
        return documento;
    }

    private Pessoa pessoaAtiva() {
        Pessoa pessoa = new Pessoa();
        pessoa.setId(UUID.randomUUID());
        pessoa.atualizar(TipoPessoa.JURIDICA, "Pessoa Documento", "Pessoa Documento LTDA", "12345678000190",
                null, "7133334444", "71999998888", "pessoa-documento@teste.com", null);
        return pessoa;
    }

    private Arquivo arquivoAtivo() {
        Arquivo arquivo = new Arquivo();
        arquivo.setId(UUID.randomUUID());
        arquivo.atualizar("documento.pdf", "documento-armazenado.pdf", "application/pdf", "pdf", 1024L,
                "a".repeat(64), "/files/documento.pdf", StorageArquivo.LOCAL, "PESSOA", UUID.randomUUID());
        return arquivo;
    }
}
