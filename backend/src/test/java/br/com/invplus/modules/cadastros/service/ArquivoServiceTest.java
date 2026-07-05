package br.com.invplus.modules.cadastros.service;

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
import org.springframework.mock.web.MockMultipartFile;

import br.com.invplus.modules.cadastros.dto.request.ArquivoRequest;
import br.com.invplus.modules.cadastros.dto.response.ArquivoResponse;
import br.com.invplus.modules.cadastros.entity.Arquivo;
import br.com.invplus.modules.cadastros.enums.StorageArquivo;
import br.com.invplus.modules.cadastros.mapper.ArquivoMapper;
import br.com.invplus.modules.cadastros.repository.ArquivoRepository;
import br.com.invplus.modules.cadastros.validator.ArquivoValidator;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.storage.StorageFacade;
import br.com.invplus.shared.storage.StorageProvider;
import br.com.invplus.shared.storage.StoredFile;

/**
 * Testes unitarios das regras de negocio de Arquivo.
 */
@ExtendWith(MockitoExtension.class)
class ArquivoServiceTest {

    @Mock
    private ArquivoRepository arquivoRepository;

    @Mock
    private StorageFacade storageFacade;

    private ArquivoService arquivoService;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        ArquivoMapper mapper = new ArquivoMapper();
        ArquivoValidator validator = new ArquivoValidator(arquivoRepository);
        arquivoService = new ArquivoService(arquivoRepository, mapper, validator, storageFacade);
    }

    /**
     * Garante criacao com hash e extensao normalizados.
     */
    @Test
    void deveCriarArquivoComSucesso() {
        String hash = hash("a");
        when(arquivoRepository.findByHashSha256AndAtivoTrueAndDeletedAtIsNull(hash))
                .thenReturn(Optional.empty());
        when(arquivoRepository.save(any(Arquivo.class))).thenAnswer(invocation -> {
            Arquivo arquivo = invocation.getArgument(0);
            arquivo.setId(UUID.randomUUID());
            return arquivo;
        });

        ArquivoResponse response = arquivoService.criar(request(hash));

        assertThat(response.nomeOriginal()).isEqualTo("contrato.pdf");
        assertThat(response.extensao()).isEqualTo("pdf");
        assertThat(response.hashSha256()).isEqualTo(hash);
        assertThat(response.storage()).isEqualTo(StorageArquivo.LOCAL);
        assertThat(response.entidade()).isEqualTo("EMPRESA");
    }

    /**
     * Bloqueia hash duplicado entre arquivos ativos.
     */
    @Test
    void deveBloquearHashDuplicado() {
        String hash = hash("b");
        Arquivo existente = new Arquivo();
        existente.setId(UUID.randomUUID());
        existente.atualizar("contrato.pdf", "arquivo.pdf", "application/pdf", "pdf", 10L,
                hash, "/files/arquivo.pdf", StorageArquivo.LOCAL, "EMPRESA", UUID.randomUUID());

        when(arquivoRepository.findByHashSha256AndAtivoTrueAndDeletedAtIsNull(hash))
                .thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> arquivoService.criar(request(hash)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Ja existe arquivo ativo");
    }

    /**
     * Bloqueia hash fora do formato SHA-256 esperado.
     */
    @Test
    void deveBloquearHashInvalido() {
        assertThatThrownBy(() -> arquivoService.criar(request("123")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Hash SHA-256 invalido");
    }

    /**
     * Confirma que a exclusao logica marca a entidade como inativa.
     */
    @Test
    void deveExcluirLogicamente() {
        UUID id = UUID.randomUUID();
        Arquivo arquivo = new Arquivo();
        arquivo.setId(id);
        arquivo.atualizar("contrato.pdf", "arquivo.pdf", "application/pdf", "pdf", 10L,
                hash("c"), "/files/arquivo.pdf", StorageArquivo.LOCAL, "EMPRESA", UUID.randomUUID());

        when(arquivoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)).thenReturn(Optional.of(arquivo));

        arquivoService.excluir(id);

        assertThat(arquivo.getAtivo()).isFalse();
        assertThat(arquivo.getDeletedAt()).isNotNull();
    }

    /**
     * Mantem o contrato paginado do service.
     */
    @Test
    void deveListarComPaginacao() {
        when(arquivoRepository.findByAtivoTrueAndDeletedAtIsNull(PageRequest.of(0, 20)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        arquivoService.listar(PageRequest.of(0, 20));

        verify(arquivoRepository).findByAtivoTrueAndDeletedAtIsNull(PageRequest.of(0, 20));
    }

    /**
     * Garante que upload valido grave binario no storage e salve metadados no banco.
     */
    @Test
    void deveFazerUploadDeImagemValida() throws Exception {
        UUID entidadeId = UUID.randomUUID();
        String hash = hash("d");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "produto.png",
                "image/png",
                new byte[] {1, 2, 3}
        );

        when(storageFacade.upload(file.getBytes(), "produto.png", "image/png"))
                .thenReturn(new StoredFile(
                        "produto.png",
                        "2026/07/04/arquivo.png",
                        "image/png",
                        "png",
                        3L,
                        hash,
                        "http://localhost:8080/uploads/2026/07/04/arquivo.png",
                        StorageProvider.LOCAL
                ));
        when(arquivoRepository.findByHashSha256AndAtivoTrueAndDeletedAtIsNull(hash))
                .thenReturn(Optional.empty());
        when(arquivoRepository.save(any(Arquivo.class))).thenAnswer(invocation -> {
            Arquivo arquivo = invocation.getArgument(0);
            arquivo.setId(UUID.randomUUID());
            return arquivo;
        });

        ArquivoResponse response = arquivoService.upload(file, "produto", entidadeId);

        assertThat(response.nomeOriginal()).isEqualTo("produto.png");
        assertThat(response.nomeArmazenado()).isEqualTo("2026/07/04/arquivo.png");
        assertThat(response.tipoMime()).isEqualTo("image/png");
        assertThat(response.extensao()).isEqualTo("png");
        assertThat(response.tamanhoBytes()).isEqualTo(3L);
        assertThat(response.hashSha256()).isEqualTo(hash);
        assertThat(response.storage()).isEqualTo(StorageArquivo.LOCAL);
        assertThat(response.entidade()).isEqualTo("PRODUTO");
        assertThat(response.entidadeId()).isEqualTo(entidadeId);
    }

    /**
     * Arquivo vazio nao deve chegar ao provider de storage.
     */
    @Test
    void deveBloquearUploadVazio() {
        MockMultipartFile file = new MockMultipartFile("file", "vazio.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> arquivoService.upload(file, "PRODUTO", UUID.randomUUID()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Arquivo enviado nao pode ser vazio");
    }

    /**
     * Tipos fora da lista permitida devem ser recusados.
     */
    @Test
    void deveBloquearTipoNaoPermitidoNoUpload() {
        MockMultipartFile file = new MockMultipartFile("file", "script.js", "application/javascript", "x".getBytes());

        assertThatThrownBy(() -> arquivoService.upload(file, "PRODUTO", UUID.randomUUID()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Tipo de arquivo nao permitido");
    }

    /**
     * Upload acima de 10 MB deve ser bloqueado pela regra de negocio.
     */
    @Test
    void deveBloquearUploadAcimaDoLimite() {
        byte[] grande = new byte[(10 * 1024 * 1024) + 1];
        MockMultipartFile file = new MockMultipartFile("file", "grande.pdf", "application/pdf", grande);

        assertThatThrownBy(() -> arquivoService.upload(file, "PRODUTO", UUID.randomUUID()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Arquivo excede o tamanho maximo de 10 MB");
    }

    private ArquivoRequest request(String hash) {
        return new ArquivoRequest(
                " contrato.pdf ",
                " contrato-armazenado.pdf ",
                "application/pdf",
                ".PDF",
                1024L,
                hash,
                "/files/contrato-armazenado.pdf",
                StorageArquivo.LOCAL,
                "empresa",
                UUID.randomUUID()
        );
    }

    private String hash(String caractere) {
        return caractere.repeat(64);
    }
}
