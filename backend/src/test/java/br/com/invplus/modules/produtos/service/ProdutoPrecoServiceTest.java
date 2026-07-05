package br.com.invplus.modules.produtos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.produtos.dto.request.ProdutoPrecoRequest;
import br.com.invplus.modules.produtos.dto.response.ProdutoPrecoResponse;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.entity.ProdutoPreco;
import br.com.invplus.modules.produtos.enums.TipoPrecoProduto;
import br.com.invplus.modules.produtos.mapper.ProdutoPrecoMapper;
import br.com.invplus.modules.produtos.repository.ProdutoPrecoRepository;
import br.com.invplus.modules.produtos.repository.ProdutoRepository;
import br.com.invplus.modules.produtos.validator.ProdutoPrecoValidator;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
 * Testes unitarios das regras de ProdutoPreco.
 */
@ExtendWith(MockitoExtension.class)
class ProdutoPrecoServiceTest {

    @Mock
    private ProdutoPrecoRepository repository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    private ProdutoPrecoService service;

    @BeforeEach
    void setUp() {
        ProdutoPrecoMapper mapper = new ProdutoPrecoMapper();
        ProdutoPrecoValidator validator = new ProdutoPrecoValidator(repository);
        service = new ProdutoPrecoService(repository, mapper, validator, produtoRepository, empresaRepository);
    }

    /**
     * Cria preco global para produto ativo.
     */
    @Test
    void deveCriarPrecoGlobalComSucesso() {
        Produto produto = produtoAtivo();
        ProdutoPrecoRequest request = request(produto.getId(), null, TipoPrecoProduto.VENDA, "19.9000", true);
        when(repository.findByProdutoIdAndTipoPrecoAndEmpresaIsNullAndVigenciaInicioAndAtivoTrueAndDeletedAtIsNull(
                produto.getId(), TipoPrecoProduto.VENDA, request.vigenciaInicio())).thenReturn(Optional.empty());
        when(repository.findByProdutoIdAndTipoPrecoAndEmpresaIsNullAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(
                produto.getId(), TipoPrecoProduto.VENDA)).thenReturn(Optional.empty());
        when(produtoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(produto.getId())).thenReturn(Optional.of(produto));
        when(repository.save(any(ProdutoPreco.class))).thenAnswer(invocation -> {
            ProdutoPreco preco = invocation.getArgument(0);
            preco.setId(UUID.randomUUID());
            return preco;
        });

        ProdutoPrecoResponse response = service.criar(request);

        assertThat(response.produtoId()).isEqualTo(produto.getId());
        assertThat(response.empresaId()).isNull();
        assertThat(response.tipoPreco()).isEqualTo(TipoPrecoProduto.VENDA);
        assertThat(response.preco()).isEqualByComparingTo("19.9000");
        assertThat(response.principal()).isTrue();
    }

    /**
     * Cria preco especifico para uma empresa ativa.
     */
    @Test
    void deveCriarPrecoPorEmpresaComSucesso() {
        Produto produto = produtoAtivo();
        Empresa empresa = empresaAtiva();
        ProdutoPrecoRequest request = request(produto.getId(), empresa.getId(), TipoPrecoProduto.ATACADO, "15.5000", false);
        when(repository.findByProdutoIdAndTipoPrecoAndEmpresaIdAndVigenciaInicioAndAtivoTrueAndDeletedAtIsNull(
                produto.getId(), TipoPrecoProduto.ATACADO, empresa.getId(), request.vigenciaInicio())).thenReturn(Optional.empty());
        when(produtoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(produto.getId())).thenReturn(Optional.of(produto));
        when(empresaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(empresa.getId())).thenReturn(Optional.of(empresa));
        when(repository.save(any(ProdutoPreco.class))).thenAnswer(invocation -> {
            ProdutoPreco preco = invocation.getArgument(0);
            preco.setId(UUID.randomUUID());
            return preco;
        });

        ProdutoPrecoResponse response = service.criar(request);

        assertThat(response.empresaId()).isEqualTo(empresa.getId());
        assertThat(response.empresaRazaoSocial()).isEqualTo(empresa.getRazaoSocial());
        assertThat(response.tipoPreco()).isEqualTo(TipoPrecoProduto.ATACADO);
    }

    /**
     * Produto inexistente ou inativo deve retornar 404.
     */
    @Test
    void deveBloquearProdutoInexistente() {
        UUID produtoId = UUID.randomUUID();
        ProdutoPrecoRequest request = request(produtoId, null, TipoPrecoProduto.VENDA, "19.9000", false);
        when(repository.findByProdutoIdAndTipoPrecoAndEmpresaIsNullAndVigenciaInicioAndAtivoTrueAndDeletedAtIsNull(
                produtoId, TipoPrecoProduto.VENDA, request.vigenciaInicio())).thenReturn(Optional.empty());
        when(produtoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(produtoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Produto nao encontrado");
    }

    /**
     * Empresa inexistente ou inativa deve retornar 404 quando informada.
     */
    @Test
    void deveBloquearEmpresaInexistente() {
        Produto produto = produtoAtivo();
        UUID empresaId = UUID.randomUUID();
        ProdutoPrecoRequest request = request(produto.getId(), empresaId, TipoPrecoProduto.VENDA, "19.9000", false);
        when(repository.findByProdutoIdAndTipoPrecoAndEmpresaIdAndVigenciaInicioAndAtivoTrueAndDeletedAtIsNull(
                produto.getId(), TipoPrecoProduto.VENDA, empresaId, request.vigenciaInicio())).thenReturn(Optional.empty());
        when(produtoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(produto.getId())).thenReturn(Optional.of(produto));
        when(empresaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(empresaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Empresa nao encontrada");
    }

    /**
     * Produto, tipo, empresa e vigencia inicial nao podem duplicar ativos.
     */
    @Test
    void deveBloquearPrecoDuplicado() {
        ProdutoPreco existente = precoAtivo();
        ProdutoPrecoRequest request = request(existente.getProduto().getId(), null, TipoPrecoProduto.VENDA, "19.9000", false);
        when(repository.findByProdutoIdAndTipoPrecoAndEmpresaIsNullAndVigenciaInicioAndAtivoTrueAndDeletedAtIsNull(
                request.produtoId(), request.tipoPreco(), request.vigenciaInicio())).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Ja existe preco ativo");
    }

    /**
     * Preco negativo deve retornar erro funcional.
     */
    @Test
    void deveBloquearPrecoNegativo() {
        ProdutoPrecoRequest request = request(UUID.randomUUID(), null, TipoPrecoProduto.VENDA, "-1.0000", false);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Preco nao pode ser negativo");
    }

    /**
     * Vigencia final anterior a inicial deve retornar erro funcional.
     */
    @Test
    void deveBloquearVigenciaInvalida() {
        ProdutoPrecoRequest request = new ProdutoPrecoRequest(UUID.randomUUID(), null, TipoPrecoProduto.VENDA,
                new BigDecimal("10.0000"), LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 1), false, "Invalido");

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Vigencia final nao pode ser anterior a inicial");
    }

    /**
     * Produto deve ter apenas um preco principal ativo por tipo e empresa.
     */
    @Test
    void deveBloquearSegundoPrecoPrincipal() {
        ProdutoPreco existente = precoAtivo();
        ProdutoPrecoRequest request = request(existente.getProduto().getId(), null, TipoPrecoProduto.VENDA, "20.0000", true);
        when(repository.findByProdutoIdAndTipoPrecoAndEmpresaIsNullAndVigenciaInicioAndAtivoTrueAndDeletedAtIsNull(
                request.produtoId(), request.tipoPreco(), request.vigenciaInicio())).thenReturn(Optional.empty());
        when(repository.findByProdutoIdAndTipoPrecoAndEmpresaIsNullAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(
                request.produtoId(), request.tipoPreco())).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Produto ja possui preco principal ativo");
    }

    /**
     * Filtros devem delegar para Specification paginada.
     */
    @Test
    @SuppressWarnings("unchecked")
    void deveListarComFiltros() {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        service.listarComFiltros(UUID.randomUUID(), UUID.randomUUID(), TipoPrecoProduto.VENDA,
                true, LocalDate.of(2026, 7, 1), null, true, PageRequest.of(0, 20));

        verify(repository).findAll(any(Specification.class), any(Pageable.class));
    }

    /**
     * Busca preco vigente considerando a data informada.
     */
    @Test
    void deveBuscarPrecoVigente() {
        ProdutoPreco preco = precoAtivo();
        when(repository.buscarPrecosVigentesGlobais(preco.getProduto().getId(), TipoPrecoProduto.VENDA, LocalDate.of(2026, 7, 5)))
                .thenReturn(List.of(preco));

        Optional<ProdutoPrecoResponse> response = service.buscarPrecoVigente(
                preco.getProduto().getId(), TipoPrecoProduto.VENDA, LocalDate.of(2026, 7, 5));

        assertThat(response).isPresent();
        assertThat(response.get().preco()).isEqualByComparingTo("19.9000");
    }

    /**
     * Atualizacao ignora o proprio registro ao manter preco principal.
     */
    @Test
    void deveAtualizarMantendoPrincipal() {
        ProdutoPreco preco = precoAtivo();
        ProdutoPrecoRequest request = request(preco.getProduto().getId(), null, TipoPrecoProduto.VENDA, "21.0000", true);
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(preco.getId())).thenReturn(Optional.of(preco));
        when(repository.findByProdutoIdAndTipoPrecoAndEmpresaIsNullAndVigenciaInicioAndAtivoTrueAndDeletedAtIsNull(
                request.produtoId(), request.tipoPreco(), request.vigenciaInicio())).thenReturn(Optional.of(preco));
        when(repository.findByProdutoIdAndTipoPrecoAndEmpresaIsNullAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(
                request.produtoId(), request.tipoPreco())).thenReturn(Optional.of(preco));
        when(produtoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(request.produtoId())).thenReturn(Optional.of(preco.getProduto()));

        ProdutoPrecoResponse response = service.atualizar(preco.getId(), request);

        assertThat(response.preco()).isEqualByComparingTo("21.0000");
        assertThat(response.principal()).isTrue();
    }

    /**
     * Exclusao logica mantem historico e oculta em consultas padrao.
     */
    @Test
    void deveExcluirLogicamente() {
        ProdutoPreco preco = precoAtivo();
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(preco.getId())).thenReturn(Optional.of(preco));

        service.excluir(preco.getId());

        assertThat(preco.getAtivo()).isFalse();
        assertThat(preco.getDeletedAt()).isNotNull();
    }

    private ProdutoPrecoRequest request(UUID produtoId, UUID empresaId, TipoPrecoProduto tipoPreco, String preco, Boolean principal) {
        return new ProdutoPrecoRequest(
                produtoId,
                empresaId,
                tipoPreco,
                new BigDecimal(preco),
                LocalDate.of(2026, 7, 1),
                null,
                principal,
                "Preco de teste"
        );
    }

    private ProdutoPreco precoAtivo() {
        ProdutoPreco preco = new ProdutoPreco();
        preco.setId(UUID.randomUUID());
        preco.setProduto(produtoAtivo());
        preco.atualizar(TipoPrecoProduto.VENDA, new BigDecimal("19.9000"), LocalDate.of(2026, 7, 1), null, true, "Principal");
        return preco;
    }

    private Produto produtoAtivo() {
        Produto produto = new Produto();
        produto.setId(UUID.randomUUID());
        produto.setCodigo("PROD-PRECO");
        produto.setNome("Produto com Preco");
        return produto;
    }

    private Empresa empresaAtiva() {
        Empresa empresa = new Empresa();
        empresa.setId(UUID.randomUUID());
        empresa.setRazaoSocial("Empresa Preco");
        empresa.setCnpj("12345678000190");
        empresa.setCrt(3);
        return empresa;
    }
}
