package br.com.invplus.modules.produtos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.invplus.modules.cadastros.entity.UnidadeMedida;
import br.com.invplus.modules.cadastros.repository.UnidadeMedidaRepository;
import br.com.invplus.modules.produtos.dto.request.ProdutoUnidadeRequest;
import br.com.invplus.modules.produtos.dto.response.ProdutoUnidadeResponse;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.entity.ProdutoUnidade;
import br.com.invplus.modules.produtos.mapper.ProdutoUnidadeMapper;
import br.com.invplus.modules.produtos.repository.ProdutoRepository;
import br.com.invplus.modules.produtos.repository.ProdutoUnidadeRepository;
import br.com.invplus.modules.produtos.validator.ProdutoUnidadeValidator;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

/**
 * Testes unitarios das regras de ProdutoUnidade.
 */
@ExtendWith(MockitoExtension.class)
class ProdutoUnidadeServiceTest {

    @Mock
    private ProdutoUnidadeRepository repository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private UnidadeMedidaRepository unidadeMedidaRepository;

    private ProdutoUnidadeService service;

    @BeforeEach
    void setUp() {
        ProdutoUnidadeMapper mapper = new ProdutoUnidadeMapper();
        ProdutoUnidadeValidator validator = new ProdutoUnidadeValidator(repository);
        service = new ProdutoUnidadeService(repository, mapper, validator, produtoRepository, unidadeMedidaRepository);
    }

    /**
     * Cria unidade padrao para produto ativo.
     */
    @Test
    void deveCriarUnidadeComSucesso() {
        Produto produto = produtoAtivo();
        UnidadeMedida unidade = unidadeAtiva();
        ProdutoUnidadeRequest request = request(produto.getId(), unidade.getId(), "1.000000", true);
        when(repository.findByProdutoIdAndUnidadeMedidaIdAndAtivoTrueAndDeletedAtIsNull(produto.getId(), unidade.getId()))
                .thenReturn(Optional.empty());
        when(repository.findByProdutoIdAndUnidadePadraoTrueAndAtivoTrueAndDeletedAtIsNull(produto.getId()))
                .thenReturn(Optional.empty());
        when(produtoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(produto.getId())).thenReturn(Optional.of(produto));
        when(unidadeMedidaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(unidade.getId())).thenReturn(Optional.of(unidade));
        when(repository.save(any(ProdutoUnidade.class))).thenAnswer(invocation -> {
            ProdutoUnidade entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        ProdutoUnidadeResponse response = service.criar(request);

        assertThat(response.produtoId()).isEqualTo(produto.getId());
        assertThat(response.unidadeMedidaId()).isEqualTo(unidade.getId());
        assertThat(response.fatorConversao()).isEqualByComparingTo("1.000000");
        assertThat(response.unidadePadrao()).isTrue();
    }

    /**
     * Produto inexistente ou inativo deve retornar 404.
     */
    @Test
    void deveBloquearProdutoInexistente() {
        UUID produtoId = UUID.randomUUID();
        UUID unidadeId = UUID.randomUUID();
        ProdutoUnidadeRequest request = request(produtoId, unidadeId, "1.000000", false);
        when(repository.findByProdutoIdAndUnidadeMedidaIdAndAtivoTrueAndDeletedAtIsNull(produtoId, unidadeId))
                .thenReturn(Optional.empty());
        when(produtoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(produtoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Produto nao encontrado");
    }

    /**
     * Unidade inexistente ou inativa deve retornar 404.
     */
    @Test
    void deveBloquearUnidadeInexistente() {
        Produto produto = produtoAtivo();
        UUID unidadeId = UUID.randomUUID();
        ProdutoUnidadeRequest request = request(produto.getId(), unidadeId, "1.000000", false);
        when(repository.findByProdutoIdAndUnidadeMedidaIdAndAtivoTrueAndDeletedAtIsNull(produto.getId(), unidadeId))
                .thenReturn(Optional.empty());
        when(produtoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(produto.getId())).thenReturn(Optional.of(produto));
        when(unidadeMedidaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(unidadeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Unidade de medida nao encontrada");
    }

    /**
     * Produto e unidade nao podem duplicar ativos.
     */
    @Test
    void deveBloquearDuplicidade() {
        ProdutoUnidade existente = produtoUnidadeAtiva();
        ProdutoUnidadeRequest request = request(existente.getProduto().getId(), existente.getUnidadeMedida().getId(), "1.000000", false);
        when(repository.findByProdutoIdAndUnidadeMedidaIdAndAtivoTrueAndDeletedAtIsNull(request.produtoId(), request.unidadeMedidaId()))
                .thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Ja existe unidade ativa");
    }

    /**
     * Fator deve ser maior que zero.
     */
    @Test
    void deveBloquearFatorZeroOuNegativo() {
        ProdutoUnidadeRequest zero = request(UUID.randomUUID(), UUID.randomUUID(), "0.000000", false);
        ProdutoUnidadeRequest negativo = request(UUID.randomUUID(), UUID.randomUUID(), "-1.000000", false);

        assertThatThrownBy(() -> service.criar(zero))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Fator de conversao deve ser maior que zero");
        assertThatThrownBy(() -> service.criar(negativo))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Fator de conversao deve ser maior que zero");
    }

    /**
     * Unidade padrao deve ter fator igual a 1.
     */
    @Test
    void deveBloquearUnidadePadraoComFatorDiferenteDeUm() {
        ProdutoUnidadeRequest request = request(UUID.randomUUID(), UUID.randomUUID(), "12.000000", true);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Unidade padrao deve ter fator de conversao igual a 1");
    }

    /**
     * Produto deve ter apenas uma unidade padrao ativa.
     */
    @Test
    void deveBloquearSegundaUnidadePadrao() {
        ProdutoUnidade existente = produtoUnidadeAtiva();
        ProdutoUnidadeRequest request = request(existente.getProduto().getId(), UUID.randomUUID(), "1.000000", true);
        when(repository.findByProdutoIdAndUnidadeMedidaIdAndAtivoTrueAndDeletedAtIsNull(request.produtoId(), request.unidadeMedidaId()))
                .thenReturn(Optional.empty());
        when(repository.findByProdutoIdAndUnidadePadraoTrueAndAtivoTrueAndDeletedAtIsNull(request.produtoId()))
                .thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Produto ja possui unidade padrao ativa");
    }

    /**
     * Filtros devem delegar para Specification paginada.
     */
    @Test
    @SuppressWarnings("unchecked")
    void deveListarComFiltros() {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        service.listarComFiltros(UUID.randomUUID(), UUID.randomUUID(), true,
                true, true, true, true, true, PageRequest.of(0, 20));

        verify(repository).findAll(any(Specification.class), any(Pageable.class));
    }

    /**
     * Atualizacao ignora o proprio registro ao manter unidade padrao.
     */
    @Test
    void deveAtualizarMantendoUnidadePadrao() {
        ProdutoUnidade entity = produtoUnidadeAtiva();
        ProdutoUnidadeRequest request = request(entity.getProduto().getId(), entity.getUnidadeMedida().getId(), "1.000000", true);
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(entity.getId())).thenReturn(Optional.of(entity));
        when(repository.findByProdutoIdAndUnidadeMedidaIdAndAtivoTrueAndDeletedAtIsNull(request.produtoId(), request.unidadeMedidaId()))
                .thenReturn(Optional.of(entity));
        when(repository.findByProdutoIdAndUnidadePadraoTrueAndAtivoTrueAndDeletedAtIsNull(request.produtoId()))
                .thenReturn(Optional.of(entity));
        when(produtoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(request.produtoId())).thenReturn(Optional.of(entity.getProduto()));
        when(unidadeMedidaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(request.unidadeMedidaId())).thenReturn(Optional.of(entity.getUnidadeMedida()));

        ProdutoUnidadeResponse response = service.atualizar(entity.getId(), request);

        assertThat(response.unidadePadrao()).isTrue();
        assertThat(response.fatorConversao()).isEqualByComparingTo("1.000000");
    }

    /**
     * Exclusao logica mantem historico e oculta em consultas padrao.
     */
    @Test
    void deveExcluirLogicamente() {
        ProdutoUnidade entity = produtoUnidadeAtiva();
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(entity.getId())).thenReturn(Optional.of(entity));

        service.excluir(entity.getId());

        assertThat(entity.getAtivo()).isFalse();
        assertThat(entity.getDeletedAt()).isNotNull();
    }

    private ProdutoUnidadeRequest request(UUID produtoId, UUID unidadeId, String fator, Boolean padrao) {
        return new ProdutoUnidadeRequest(produtoId, unidadeId, new BigDecimal(fator),
                padrao, true, true, true, true, "Unidade de teste");
    }

    private ProdutoUnidade produtoUnidadeAtiva() {
        ProdutoUnidade entity = new ProdutoUnidade();
        entity.setId(UUID.randomUUID());
        entity.setProduto(produtoAtivo());
        entity.setUnidadeMedida(unidadeAtiva());
        entity.atualizar(BigDecimal.ONE, true, true, true, true, true, "Padrao");
        return entity;
    }

    private Produto produtoAtivo() {
        Produto produto = new Produto();
        produto.setId(UUID.randomUUID());
        produto.setCodigo("PROD-UNID");
        produto.setNome("Produto Unidade");
        return produto;
    }

    private UnidadeMedida unidadeAtiva() {
        UnidadeMedida unidade = new UnidadeMedida();
        unidade.setId(UUID.randomUUID());
        unidade.atualizar("UN", "Unidade", (short) 0);
        return unidade;
    }
}
