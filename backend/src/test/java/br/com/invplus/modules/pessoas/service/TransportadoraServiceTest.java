package br.com.invplus.modules.pessoas.service;

import java.math.BigDecimal;
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
import br.com.invplus.modules.pessoas.dto.request.TransportadoraRequest;
import br.com.invplus.modules.pessoas.dto.response.TransportadoraResponse;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.pessoas.entity.Transportadora;
import br.com.invplus.modules.pessoas.mapper.TransportadoraMapper;
import br.com.invplus.modules.pessoas.repository.PessoaRepository;
import br.com.invplus.modules.pessoas.repository.TransportadoraRepository;
import br.com.invplus.modules.pessoas.validator.TransportadoraValidator;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;

/**
 * Testes unitarios das regras de negocio de Transportadora.
 */
@ExtendWith(MockitoExtension.class)
class TransportadoraServiceTest {

    @Mock
    private TransportadoraRepository transportadoraRepository;

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private PessoaRoleService pessoaRoleService;

    private TransportadoraService transportadoraService;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        TransportadoraMapper mapper = new TransportadoraMapper();
        TransportadoraValidator validator = new TransportadoraValidator(transportadoraRepository);
        transportadoraService = new TransportadoraService(transportadoraRepository, mapper, validator, pessoaRepository, pessoaRoleService);
    }

    /**
     * Garante criacao da transportadora com dados logisticos e pessoa vinculada.
     */
    @Test
    void deveCriarTransportadoraComSucesso() {
        Pessoa pessoa = pessoaAtiva();
        when(transportadoraRepository.findByPessoaIdAndAtivoTrueAndDeletedAtIsNull(pessoa.getId()))
                .thenReturn(Optional.empty());
        when(pessoaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(pessoa.getId()))
                .thenReturn(Optional.of(pessoa));
        when(transportadoraRepository.save(any(Transportadora.class))).thenAnswer(invocation -> {
            Transportadora transportadora = invocation.getArgument(0);
            transportadora.setId(UUID.randomUUID());
            return transportadora;
        });

        TransportadoraResponse response = transportadoraService.criar(request(pessoa.getId(), BigDecimal.valueOf(1200)));

        assertThat(response.pessoaId()).isEqualTo(pessoa.getId());
        assertThat(response.nome()).isEqualTo("Transportadora Teste");
        assertThat(response.cpfCnpj()).isEqualTo("12345678000190");
        assertThat(response.capacidadeCargaKg()).isEqualByComparingTo("1200");
    }

    /**
     * Bloqueia criacao quando a pessoa nao existe ou nao esta ativa.
     */
    @Test
    void deveBloquearPessoaInexistenteOuInativa() {
        UUID pessoaId = UUID.randomUUID();
        when(transportadoraRepository.findByPessoaIdAndAtivoTrueAndDeletedAtIsNull(pessoaId))
                .thenReturn(Optional.empty());
        when(pessoaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(pessoaId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transportadoraService.criar(request(pessoaId, BigDecimal.TEN)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Pessoa nao encontrada");
    }

    /**
     * Bloqueia duas transportadoras ativas para a mesma pessoa.
     */
    @Test
    void deveBloquearTransportadoraDuplicadaParaMesmaPessoa() {
        Pessoa pessoa = pessoaAtiva();
        Transportadora existente = new Transportadora();
        existente.setId(UUID.randomUUID());
        existente.setPessoa(pessoa);

        when(transportadoraRepository.findByPessoaIdAndAtivoTrueAndDeletedAtIsNull(pessoa.getId()))
                .thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> transportadoraService.criar(request(pessoa.getId(), BigDecimal.TEN)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Pessoa ja possui transportadora ativa");
    }

    /**
     * Bloqueia capacidade de carga negativa.
     */
    @Test
    void deveBloquearCapacidadeCargaNegativa() {
        assertThatThrownBy(() -> transportadoraService.criar(request(UUID.randomUUID(), BigDecimal.valueOf(-1))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Capacidade de carga nao pode ser negativa");
    }

    /**
     * Busca transportadora ativa por id.
     */
    @Test
    void deveBuscarPorId() {
        Transportadora transportadora = transportadoraAtiva();
        when(transportadoraRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(transportadora.getId()))
                .thenReturn(Optional.of(transportadora));

        TransportadoraResponse response = transportadoraService.buscar(transportadora.getId());

        assertThat(response.id()).isEqualTo(transportadora.getId());
        assertThat(response.nome()).isEqualTo("Transportadora Teste");
    }

    /**
     * Mantem listagem paginada.
     */
    @Test
    void deveListarComPaginacao() {
        when(transportadoraRepository.findByAtivoTrueAndDeletedAtIsNull(PageRequest.of(0, 20)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        transportadoraService.listar(PageRequest.of(0, 20));

        verify(transportadoraRepository).findByAtivoTrueAndDeletedAtIsNull(PageRequest.of(0, 20));
    }

    /**
     * Atualiza dados logisticos da transportadora.
     */
    @Test
    void deveAtualizarTransportadora() {
        Transportadora transportadora = transportadoraAtiva();
        UUID pessoaId = transportadora.getPessoa().getId();

        when(transportadoraRepository.findByPessoaIdAndAtivoTrueAndDeletedAtIsNull(pessoaId))
                .thenReturn(Optional.of(transportadora));
        when(transportadoraRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(transportadora.getId()))
                .thenReturn(Optional.of(transportadora));
        when(pessoaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(pessoaId))
                .thenReturn(Optional.of(transportadora.getPessoa()));

        TransportadoraResponse response = transportadoraService.atualizar(
                transportadora.getId(),
                request(pessoaId, BigDecimal.valueOf(2500))
        );

        assertThat(response.capacidadeCargaKg()).isEqualByComparingTo("2500");
        assertThat(response.observacao()).isEqualTo("Transportadora recorrente");
    }

    /**
     * Confirma exclusao logica do papel Transportadora.
     */
    @Test
    void deveExcluirLogicamente() {
        Transportadora transportadora = transportadoraAtiva();
        when(transportadoraRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(transportadora.getId()))
                .thenReturn(Optional.of(transportadora));

        transportadoraService.excluir(transportadora.getId());

        assertThat(transportadora.getAtivo()).isFalse();
        assertThat(transportadora.getDeletedAt()).isNotNull();
    }

    private TransportadoraRequest request(UUID pessoaId, BigDecimal capacidadeCargaKg) {
        return new TransportadoraRequest(
                pessoaId,
                "ANTT123",
                "Caminhao Bau",
                capacidadeCargaKg,
                " Transportadora recorrente "
        );
    }

    private Transportadora transportadoraAtiva() {
        Transportadora transportadora = new Transportadora();
        transportadora.setId(UUID.randomUUID());
        transportadora.setPessoa(pessoaAtiva());
        transportadora.atualizar("ANTT123", "Caminhao Bau", BigDecimal.valueOf(1000), "Transportadora ativa");
        return transportadora;
    }

    private Pessoa pessoaAtiva() {
        Pessoa pessoa = new Pessoa();
        pessoa.setId(UUID.randomUUID());
        pessoa.atualizar(TipoPessoa.JURIDICA, "Transportadora Teste", "Transportes LTDA", "12345678000190",
                null, "7133334444", "71999998888", "transportadora@teste.com", null);
        return pessoa;
    }
}
