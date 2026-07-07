package br.com.invplus.modules.estoque.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.invplus.modules.cadastros.entity.Categoria;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.UnidadeMedida;
import br.com.invplus.modules.estoque.dto.request.ReservaEstoqueRequest;
import br.com.invplus.modules.estoque.dto.response.ReservaEstoqueResponse;
import br.com.invplus.modules.estoque.entity.Deposito;
import br.com.invplus.modules.estoque.entity.Estoque;
import br.com.invplus.modules.estoque.entity.LocalizacaoEstoque;
import br.com.invplus.modules.estoque.entity.ReservaEstoque;
import br.com.invplus.modules.estoque.enums.MovimentacaoOrigem;
import br.com.invplus.modules.estoque.enums.StatusReservaEstoque;
import br.com.invplus.modules.estoque.enums.TipoDeposito;
import br.com.invplus.modules.estoque.mapper.ReservaEstoqueMapper;
import br.com.invplus.modules.estoque.repository.EstoqueRepository;
import br.com.invplus.modules.estoque.repository.ReservaEstoqueRepository;
import br.com.invplus.modules.estoque.validator.ReservaEstoqueValidator;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.enums.NaturezaProduto;
import br.com.invplus.modules.produtos.enums.StatusProduto;
import br.com.invplus.modules.produtos.enums.TipoProduto;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import br.com.invplus.shared.domain.service.EstoqueDomainService;
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
 * Testes unitarios do fluxo de estado de ReservaEstoque.
 */
@ExtendWith(MockitoExtension.class)
class ReservaEstoqueServiceTest {

    @Mock
    private ReservaEstoqueRepository repository;
    @Mock
    private EstoqueRepository estoqueRepository;
    @Mock
    private EstoqueDomainService estoqueDomainService;

    private ReservaEstoqueService service;

    @BeforeEach
    void setUp() {
        ReservaEstoqueMapper mapper = new ReservaEstoqueMapper();
        ReservaEstoqueValidator validator = new ReservaEstoqueValidator(repository);
        service = new ReservaEstoqueService(repository, estoqueRepository, mapper, validator, estoqueDomainService);
    }

    @Test
    void deveCriarReservaComSucesso() {
        Estoque estoque = estoque("100.000", "0.000");
        UUID origemId = UUID.randomUUID();
        when(estoqueRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(estoque.getId())).thenReturn(Optional.of(estoque));
        when(repository.existsByOrigemAndOrigemIdAndStatusAndAtivoTrueAndDeletedAtIsNull(
                MovimentacaoOrigem.VENDA, origemId, StatusReservaEstoque.ATIVA)).thenReturn(false);
        when(repository.save(any(ReservaEstoque.class))).thenAnswer(invocation -> {
            ReservaEstoque entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        ReservaEstoqueResponse response = service.criar(request(estoque.getId(), "20.000", origemId));

        assertThat(response.status()).isEqualTo(StatusReservaEstoque.ATIVA);
        assertThat(response.quantidade()).isEqualByComparingTo("20.000");
        assertThat(response.produtoId()).isEqualTo(estoque.getProduto().getId());
        verify(estoqueDomainService).reservar(estoque, bd("20.000"), response.id(), null, "Reserva de estoque");
    }

    @Test
    void deveBloquearReservaAcimaDoDisponivel() {
        Estoque estoque = estoque("10.000", "3.000");
        when(estoqueRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(estoque.getId())).thenReturn(Optional.of(estoque));

        assertThatThrownBy(() -> service.criar(request(estoque.getId(), "8.000", UUID.randomUUID())))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("maior que a quantidade disponivel");
    }

    @Test
    void deveBloquearQuantidadeZero() {
        Estoque estoque = estoque("10.000", "0.000");
        when(estoqueRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(estoque.getId())).thenReturn(Optional.of(estoque));

        assertThatThrownBy(() -> service.criar(request(estoque.getId(), "0.000", UUID.randomUUID())))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("maior que zero");
    }

    @Test
    void deveBloquearEstoqueInexistente() {
        UUID estoqueId = UUID.randomUUID();
        when(estoqueRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(estoqueId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(request(estoqueId, "1.000", UUID.randomUUID())))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Estoque nao encontrado");
    }

    @Test
    void deveBloquearOrigemDuplicadaAtiva() {
        Estoque estoque = estoque("10.000", "0.000");
        UUID origemId = UUID.randomUUID();
        when(estoqueRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(estoque.getId())).thenReturn(Optional.of(estoque));
        when(repository.existsByOrigemAndOrigemIdAndStatusAndAtivoTrueAndDeletedAtIsNull(
                MovimentacaoOrigem.VENDA, origemId, StatusReservaEstoque.ATIVA)).thenReturn(true);

        assertThatThrownBy(() -> service.criar(request(estoque.getId(), "1.000", origemId)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("reserva ativa");
    }

    @Test
    void deveCancelarReservaAtiva() {
        ReservaEstoque reserva = reservaAtiva("5.000");
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(reserva.getId())).thenReturn(Optional.of(reserva));

        ReservaEstoqueResponse response = service.cancelar(reserva.getId());

        assertThat(response.status()).isEqualTo(StatusReservaEstoque.CANCELADA);
        assertThat(response.canceladaEm()).isNotNull();
        verify(estoqueDomainService).cancelarReserva(reserva.getEstoque(), bd("5.000"), reserva.getId(),
                null, "Cancelamento de reserva de estoque");
    }

    @Test
    void deveBaixarReservaAtiva() {
        ReservaEstoque reserva = reservaAtiva("5.000");
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(reserva.getId())).thenReturn(Optional.of(reserva));

        ReservaEstoqueResponse response = service.baixar(reserva.getId());

        assertThat(response.status()).isEqualTo(StatusReservaEstoque.BAIXADA);
        assertThat(response.baixadaEm()).isNotNull();
        verify(estoqueDomainService).baixarReserva(reserva.getEstoque(), bd("5.000"), reserva.getId(),
                null, "Baixa de reserva de estoque");
    }

    @Test
    void deveBloquearBaixaDeReservaCancelada() {
        ReservaEstoque reserva = reservaAtiva("5.000");
        reserva.cancelar();
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(reserva.getId())).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> service.baixar(reserva.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cancelada nao pode ser baixada");
    }

    @Test
    void deveBloquearCancelamentoDeReservaBaixada() {
        ReservaEstoque reserva = reservaAtiva("5.000");
        reserva.baixar();
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(reserva.getId())).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> service.cancelar(reserva.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Somente reserva ativa");
    }

    @Test
    @SuppressWarnings("unchecked")
    void deveListarComFiltros() {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        service.listarComFiltros(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), StatusReservaEstoque.ATIVA, MovimentacaoOrigem.VENDA,
                UUID.randomUUID(), true, PageRequest.of(0, 20));

        verify(repository).findAll(any(Specification.class), any(Pageable.class));
    }

    private ReservaEstoqueRequest request(UUID estoqueId, String quantidade, UUID origemId) {
        return new ReservaEstoqueRequest(estoqueId, bd(quantidade), MovimentacaoOrigem.VENDA,
                origemId, null, "Reserva teste");
    }

    private ReservaEstoque reservaAtiva(String quantidade) {
        ReservaEstoque reserva = new ReservaEstoque();
        reserva.setId(UUID.randomUUID());
        reserva.vincularEstoque(estoque("20.000", quantidade));
        reserva.atualizarDados(bd(quantidade), MovimentacaoOrigem.VENDA, UUID.randomUUID(), null, "Teste");
        reserva.setStatus(StatusReservaEstoque.ATIVA);
        return reserva;
    }

    private Estoque estoque(String quantidade, String reservada) {
        Empresa empresa = new Empresa();
        empresa.setId(UUID.randomUUID());
        empresa.atualizar("Empresa Reserva", "Empresa", "12345678000199", "ISENTO", null, "NORMAL", 3,
                "71999990000", "empresa@teste.com");

        Deposito deposito = new Deposito();
        deposito.setId(UUID.randomUUID());
        deposito.setEmpresa(empresa);
        deposito.atualizar("DEP-01", "Deposito", "Teste", TipoDeposito.CENTRAL,
                false, true, true, true, true, false);

        LocalizacaoEstoque localizacao = new LocalizacaoEstoque();
        localizacao.setId(UUID.randomUUID());
        localizacao.setDeposito(deposito);
        localizacao.atualizar("A-01", "Rua A", "Rua A", "03", "02", "04", "15", false, false, "Teste");

        Estoque estoque = new Estoque();
        estoque.setId(UUID.randomUUID());
        estoque.setProduto(produto());
        estoque.setEmpresa(empresa);
        estoque.setDeposito(deposito);
        estoque.setLocalizacao(localizacao);
        estoque.atualizarSaldos(bd(quantidade), bd(reservada), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, null, null, null);
        return estoque;
    }

    private Produto produto() {
        Categoria categoria = new Categoria();
        categoria.setId(UUID.randomUUID());
        categoria.atualizar(null, "Categoria", "Teste", 0);

        UnidadeMedida unidade = new UnidadeMedida();
        unidade.setId(UUID.randomUUID());
        unidade.atualizar("UN", "Unidade", (short) 0);

        Produto produto = new Produto();
        produto.setId(UUID.randomUUID());
        produto.setCategoria(categoria);
        produto.setUnidadeMedida(unidade);
        produto.atualizar("PROD-01", null, null, null, "Produto", "Teste", TipoProduto.PRODUTO,
                NaturezaProduto.AMBOS, StatusProduto.ATIVO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                false, false, false, true, true, true);
        return produto;
    }

    private BigDecimal bd(String valor) {
        return new BigDecimal(valor);
    }
}
