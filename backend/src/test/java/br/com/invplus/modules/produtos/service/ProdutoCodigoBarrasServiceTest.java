package br.com.invplus.modules.produtos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.invplus.modules.produtos.dto.request.ProdutoCodigoBarrasRequest;
import br.com.invplus.modules.produtos.dto.response.ProdutoCodigoBarrasResponse;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.entity.ProdutoCodigoBarras;
import br.com.invplus.modules.produtos.enums.TipoCodigoBarras;
import br.com.invplus.modules.produtos.mapper.ProdutoCodigoBarrasMapper;
import br.com.invplus.modules.produtos.repository.ProdutoCodigoBarrasRepository;
import br.com.invplus.modules.produtos.repository.ProdutoRepository;
import br.com.invplus.modules.produtos.validator.ProdutoCodigoBarrasValidator;
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
 * Testes unitarios das regras de ProdutoCodigoBarras.
 */
@ExtendWith(MockitoExtension.class)
class ProdutoCodigoBarrasServiceTest {

    @Mock
    private ProdutoCodigoBarrasRepository repository;

    @Mock
    private ProdutoRepository produtoRepository;

    private ProdutoCodigoBarrasService service;

    @BeforeEach
    void setUp() {
        ProdutoCodigoBarrasMapper mapper = new ProdutoCodigoBarrasMapper();
        ProdutoCodigoBarrasValidator validator = new ProdutoCodigoBarrasValidator(repository);
        service = new ProdutoCodigoBarrasService(repository, mapper, validator, produtoRepository);
    }

    /**
     * Cria codigo principal para produto ativo.
     */
    @Test
    void deveCriarCodigoComSucesso() {
        Produto produto = produtoAtivo();
        ProdutoCodigoBarrasRequest request = request(produto.getId(), "7891234567895", true);
        when(repository.existsByCodigoIgnoreCaseAndAtivoTrueAndDeletedAtIsNull("7891234567895")).thenReturn(false);
        when(repository.findByProdutoIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(produto.getId()))
                .thenReturn(Optional.empty());
        when(produtoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(produto.getId())).thenReturn(Optional.of(produto));
        when(repository.save(any(ProdutoCodigoBarras.class))).thenAnswer(invocation -> {
            ProdutoCodigoBarras codigo = invocation.getArgument(0);
            codigo.setId(UUID.randomUUID());
            return codigo;
        });

        ProdutoCodigoBarrasResponse response = service.criar(request);

        assertThat(response.produtoId()).isEqualTo(produto.getId());
        assertThat(response.codigo()).isEqualTo("7891234567895");
        assertThat(response.tipoCodigo()).isEqualTo(TipoCodigoBarras.EAN_UNIDADE);
        assertThat(response.principal()).isTrue();
    }

    /**
     * Produto inexistente ou inativo deve retornar 404.
     */
    @Test
    void deveBloquearProdutoInexistente() {
        UUID produtoId = UUID.randomUUID();
        ProdutoCodigoBarrasRequest request = request(produtoId, "7891234567895", false);
        when(repository.existsByCodigoIgnoreCaseAndAtivoTrueAndDeletedAtIsNull("7891234567895")).thenReturn(false);
        when(produtoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(produtoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Produto nao encontrado");
    }

    /**
     * Codigo ativo deve ser unico.
     */
    @Test
    void deveBloquearCodigoDuplicado() {
        Produto produto = produtoAtivo();
        ProdutoCodigoBarrasRequest request = request(produto.getId(), "7891234567895", false);
        when(repository.existsByCodigoIgnoreCaseAndAtivoTrueAndDeletedAtIsNull("7891234567895")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Ja existe codigo de barras ativo com este codigo");
    }

    /**
     * Codigo em branco deve retornar erro funcional.
     */
    @Test
    void deveBloquearCodigoEmBranco() {
        ProdutoCodigoBarrasRequest request = request(UUID.randomUUID(), " ", false);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Codigo de barras e obrigatorio");
    }

    /**
     * Produto deve ter apenas um codigo principal ativo.
     */
    @Test
    void deveBloquearSegundoCodigoPrincipal() {
        ProdutoCodigoBarras existente = codigoAtivo();
        ProdutoCodigoBarrasRequest request = request(existente.getProduto().getId(), "7891234567802", true);
        when(repository.existsByCodigoIgnoreCaseAndAtivoTrueAndDeletedAtIsNull("7891234567802")).thenReturn(false);
        when(repository.findByProdutoIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(request.produtoId()))
                .thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Produto ja possui codigo de barras principal ativo");
    }

    /**
     * Filtros devem delegar para Specification paginada.
     */
    @Test
    @SuppressWarnings("unchecked")
    void deveListarComFiltros() {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        service.listarComFiltros(UUID.randomUUID(), TipoCodigoBarras.EAN_CAIXA, "789",
                true, true, PageRequest.of(0, 20));

        verify(repository).findAll(any(Specification.class), any(Pageable.class));
    }

    /**
     * Atualizacao ignora o proprio registro ao manter codigo principal.
     */
    @Test
    void deveAtualizarMantendoPrincipal() {
        ProdutoCodigoBarras codigo = codigoAtivo();
        ProdutoCodigoBarrasRequest request = request(codigo.getProduto().getId(), codigo.getCodigo(), true);
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(codigo.getId())).thenReturn(Optional.of(codigo));
        when(repository.findByCodigoIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(codigo.getCodigo()))
                .thenReturn(Optional.of(codigo));
        when(repository.findByProdutoIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(request.produtoId()))
                .thenReturn(Optional.of(codigo));
        when(produtoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(request.produtoId())).thenReturn(Optional.of(codigo.getProduto()));

        ProdutoCodigoBarrasResponse response = service.atualizar(codigo.getId(), request);

        assertThat(response.principal()).isTrue();
        assertThat(response.codigo()).isEqualTo(codigo.getCodigo());
    }

    /**
     * Exclusao logica mantem historico e oculta em consultas padrao.
     */
    @Test
    void deveExcluirLogicamente() {
        ProdutoCodigoBarras codigo = codigoAtivo();
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(codigo.getId())).thenReturn(Optional.of(codigo));

        service.excluir(codigo.getId());

        assertThat(codigo.getAtivo()).isFalse();
        assertThat(codigo.getDeletedAt()).isNotNull();
    }

    private ProdutoCodigoBarrasRequest request(UUID produtoId, String codigo, Boolean principal) {
        return new ProdutoCodigoBarrasRequest(
                produtoId,
                TipoCodigoBarras.EAN_UNIDADE,
                codigo,
                principal,
                "Codigo alternativo"
        );
    }

    private ProdutoCodigoBarras codigoAtivo() {
        ProdutoCodigoBarras codigo = new ProdutoCodigoBarras();
        codigo.setId(UUID.randomUUID());
        codigo.setProduto(produtoAtivo());
        codigo.atualizar(TipoCodigoBarras.EAN_UNIDADE, "7891234567895", true, "Principal");
        return codigo;
    }

    private Produto produtoAtivo() {
        Produto produto = new Produto();
        produto.setId(UUID.randomUUID());
        produto.setCodigo("PROD-COD");
        produto.setNome("Produto com Codigo");
        return produto;
    }
}
