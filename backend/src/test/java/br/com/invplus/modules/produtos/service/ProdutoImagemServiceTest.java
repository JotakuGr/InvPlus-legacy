package br.com.invplus.modules.produtos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.invplus.modules.cadastros.entity.Arquivo;
import br.com.invplus.modules.cadastros.enums.StorageArquivo;
import br.com.invplus.modules.cadastros.repository.ArquivoRepository;
import br.com.invplus.modules.produtos.dto.request.ProdutoImagemRequest;
import br.com.invplus.modules.produtos.dto.response.ProdutoImagemResponse;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.entity.ProdutoImagem;
import br.com.invplus.modules.produtos.mapper.ProdutoImagemMapper;
import br.com.invplus.modules.produtos.repository.ProdutoImagemRepository;
import br.com.invplus.modules.produtos.repository.ProdutoRepository;
import br.com.invplus.modules.produtos.validator.ProdutoImagemValidator;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

/**
 * Testes unitarios das regras de ProdutoImagem.
 */
@ExtendWith(MockitoExtension.class)
class ProdutoImagemServiceTest {

    @Mock
    private ProdutoImagemRepository repository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private ArquivoRepository arquivoRepository;

    private ProdutoImagemService service;

    @BeforeEach
    void setUp() {
        ProdutoImagemMapper mapper = new ProdutoImagemMapper();
        ProdutoImagemValidator validator = new ProdutoImagemValidator(repository);
        service = new ProdutoImagemService(repository, mapper, validator, produtoRepository, arquivoRepository);
    }

    /**
     * Cria imagem principal para produto e arquivo ativos.
     */
    @Test
    void deveCriarImagemComSucesso() {
        Produto produto = produtoAtivo();
        Arquivo arquivo = arquivoAtivo();
        ProdutoImagemRequest request = request(produto.getId(), arquivo.getId(), true, 0);
        when(repository.existsByProdutoIdAndArquivoIdAndAtivoTrueAndDeletedAtIsNull(produto.getId(), arquivo.getId()))
                .thenReturn(false);
        when(repository.findByProdutoIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(produto.getId()))
                .thenReturn(Optional.empty());
        when(produtoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(produto.getId())).thenReturn(Optional.of(produto));
        when(arquivoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(arquivo.getId())).thenReturn(Optional.of(arquivo));
        when(repository.save(any(ProdutoImagem.class))).thenAnswer(invocation -> {
            ProdutoImagem imagem = invocation.getArgument(0);
            imagem.setId(UUID.randomUUID());
            return imagem;
        });

        ProdutoImagemResponse response = service.criar(request);

        assertThat(response.produtoId()).isEqualTo(produto.getId());
        assertThat(response.arquivoId()).isEqualTo(arquivo.getId());
        assertThat(response.principal()).isTrue();
        assertThat(response.ordem()).isZero();
        assertThat(response.arquivoUrl()).contains("/uploads/");
    }

    /**
     * Produto inexistente ou inativo deve retornar 404.
     */
    @Test
    void deveBloquearProdutoInexistente() {
        Arquivo arquivo = arquivoAtivo();
        UUID produtoId = UUID.randomUUID();
        ProdutoImagemRequest request = request(produtoId, arquivo.getId(), false, 0);
        when(repository.existsByProdutoIdAndArquivoIdAndAtivoTrueAndDeletedAtIsNull(produtoId, arquivo.getId()))
                .thenReturn(false);
        when(produtoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(produtoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Produto nao encontrado");
    }

    /**
     * Arquivo inexistente ou inativo deve retornar 404.
     */
    @Test
    void deveBloquearArquivoInexistente() {
        Produto produto = produtoAtivo();
        UUID arquivoId = UUID.randomUUID();
        ProdutoImagemRequest request = request(produto.getId(), arquivoId, false, 0);
        when(repository.existsByProdutoIdAndArquivoIdAndAtivoTrueAndDeletedAtIsNull(produto.getId(), arquivoId))
                .thenReturn(false);
        when(produtoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(produto.getId())).thenReturn(Optional.of(produto));
        when(arquivoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(arquivoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Arquivo nao encontrado");
    }

    /**
     * Produto nao pode apontar duas vezes para o mesmo arquivo ativo.
     */
    @Test
    void deveBloquearProdutoArquivoDuplicado() {
        Produto produto = produtoAtivo();
        Arquivo arquivo = arquivoAtivo();
        ProdutoImagemRequest request = request(produto.getId(), arquivo.getId(), false, 0);
        when(repository.existsByProdutoIdAndArquivoIdAndAtivoTrueAndDeletedAtIsNull(produto.getId(), arquivo.getId()))
                .thenReturn(true);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Produto ja possui vinculo ativo com este arquivo");
    }

    /**
     * Ordem negativa deve ser recusada.
     */
    @Test
    void deveBloquearOrdemNegativa() {
        ProdutoImagemRequest request = request(UUID.randomUUID(), UUID.randomUUID(), false, -1);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Ordem nao pode ser negativa");
    }

    /**
     * Produto deve ter no maximo uma imagem principal ativa.
     */
    @Test
    void deveBloquearSegundaImagemPrincipal() {
        ProdutoImagem existente = imagemAtiva();
        ProdutoImagemRequest request = request(existente.getProduto().getId(), UUID.randomUUID(), true, 1);
        when(repository.existsByProdutoIdAndArquivoIdAndAtivoTrueAndDeletedAtIsNull(
                request.produtoId(),
                request.arquivoId()
        )).thenReturn(false);
        when(repository.findByProdutoIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(request.produtoId()))
                .thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Produto ja possui imagem principal ativa");
    }

    /**
     * Filtros devem delegar para Specification paginada.
     */
    @Test
    @SuppressWarnings("unchecked")
    void deveListarComFiltros() {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        service.listarComFiltros(UUID.randomUUID(), UUID.randomUUID(), true, true, PageRequest.of(0, 20));

        verify(repository).findAll(any(Specification.class), any(Pageable.class));
    }

    /**
     * Atualizacao ignora o proprio registro ao manter imagem principal.
     */
    @Test
    void deveAtualizarMantendoPrincipal() {
        ProdutoImagem imagem = imagemAtiva();
        ProdutoImagemRequest request = request(imagem.getProduto().getId(), imagem.getArquivo().getId(), true, 2);
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(imagem.getId())).thenReturn(Optional.of(imagem));
        when(repository.findByProdutoIdAndArquivoIdAndAtivoTrueAndDeletedAtIsNull(request.produtoId(), request.arquivoId()))
                .thenReturn(Optional.of(imagem));
        when(repository.findByProdutoIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(request.produtoId()))
                .thenReturn(Optional.of(imagem));
        when(produtoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(request.produtoId())).thenReturn(Optional.of(imagem.getProduto()));
        when(arquivoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(request.arquivoId())).thenReturn(Optional.of(imagem.getArquivo()));

        ProdutoImagemResponse response = service.atualizar(imagem.getId(), request);

        assertThat(response.principal()).isTrue();
        assertThat(response.ordem()).isEqualTo(2);
    }

    /**
     * Exclusao logica mantem historico e oculta em consultas padrao.
     */
    @Test
    void deveExcluirLogicamente() {
        ProdutoImagem imagem = imagemAtiva();
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(imagem.getId())).thenReturn(Optional.of(imagem));

        service.excluir(imagem.getId());

        assertThat(imagem.getAtivo()).isFalse();
        assertThat(imagem.getDeletedAt()).isNotNull();
    }

    private ProdutoImagemRequest request(UUID produtoId, UUID arquivoId, Boolean principal, Integer ordem) {
        return new ProdutoImagemRequest(produtoId, arquivoId, principal, ordem, "Imagem do produto");
    }

    private ProdutoImagem imagemAtiva() {
        ProdutoImagem imagem = new ProdutoImagem();
        imagem.setId(UUID.randomUUID());
        imagem.setProduto(produtoAtivo());
        imagem.setArquivo(arquivoAtivo());
        imagem.atualizar(true, 0, "Principal");
        return imagem;
    }

    private Produto produtoAtivo() {
        Produto produto = new Produto();
        produto.setId(UUID.randomUUID());
        produto.setCodigo("PROD-IMG");
        produto.setNome("Produto com Imagem");
        return produto;
    }

    private Arquivo arquivoAtivo() {
        Arquivo arquivo = new Arquivo();
        arquivo.setId(UUID.randomUUID());
        arquivo.atualizar(
                "produto.png",
                "2026/07/04/produto.png",
                "image/png",
                "png",
                10L,
                "a".repeat(64),
                "http://localhost:8080/uploads/2026/07/04/produto.png",
                StorageArquivo.LOCAL,
                "PRODUTO",
                UUID.randomUUID()
        );
        return arquivo;
    }
}
