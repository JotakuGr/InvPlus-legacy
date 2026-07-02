package br.com.invplus.modules.cadastros.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.invplus.modules.cadastros.dto.request.UnidadeMedidaRequest;
import br.com.invplus.modules.cadastros.dto.response.UnidadeMedidaResponse;
import br.com.invplus.modules.cadastros.entity.UnidadeMedida;
import br.com.invplus.modules.cadastros.mapper.UnidadeMedidaMapper;
import br.com.invplus.modules.cadastros.repository.UnidadeMedidaRepository;
import br.com.invplus.modules.cadastros.validator.UnidadeMedidaValidator;
import br.com.invplus.shared.domain.exception.BusinessException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

/**
 * Testes unitarios das regras de unidade de medida.
 */
@ExtendWith(MockitoExtension.class)
class UnidadeMedidaServiceTest {

    @Mock
    private UnidadeMedidaRepository unidadeMedidaRepository;

    private UnidadeMedidaService unidadeMedidaService;

    @BeforeEach
    void setUp() {
        UnidadeMedidaMapper mapper = new UnidadeMedidaMapper();
        UnidadeMedidaValidator validator = new UnidadeMedidaValidator(unidadeMedidaRepository);
        unidadeMedidaService = new UnidadeMedidaService(unidadeMedidaRepository, mapper, validator);
    }

    /**
     * Garante que a criacao normaliza sigla e devolve DTO preenchido.
     */
    @Test
    void deveCriarUnidadeMedidaComSucesso() {
        when(unidadeMedidaRepository.findBySiglaAndAtivoTrueAndDeletedAtIsNull("KG"))
                .thenReturn(Optional.empty());
        when(unidadeMedidaRepository.save(any(UnidadeMedida.class))).thenAnswer(invocation -> {
            UnidadeMedida unidade = invocation.getArgument(0);
            unidade.setId(UUID.randomUUID());
            return unidade;
        });

        UnidadeMedidaResponse response = unidadeMedidaService.criar(
                new UnidadeMedidaRequest("kg", "Quilograma", (short) 3)
        );

        assertThat(response.sigla()).isEqualTo("KG");
        assertThat(response.descricao()).isEqualTo("Quilograma");
        assertThat(response.casasDecimais()).isEqualTo((short) 3);
    }

    /**
     * Bloqueia duplicidade de sigla ativa antes de salvar.
     */
    @Test
    void deveBloquearSiglaDuplicada() {
        UnidadeMedida existente = new UnidadeMedida();
        existente.setId(UUID.randomUUID());
        existente.atualizar("UN", "Unidade", (short) 0);

        when(unidadeMedidaRepository.findBySiglaAndAtivoTrueAndDeletedAtIsNull("UN"))
                .thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> unidadeMedidaService.criar(
                new UnidadeMedidaRequest("un", "Unidade duplicada", (short) 0)
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Ja existe unidade de medida");
    }

    /**
     * Confirma que a exclusao chama o soft delete na entidade encontrada.
     */
    @Test
    void deveExcluirLogicamente() {
        UUID id = UUID.randomUUID();
        UnidadeMedida unidade = new UnidadeMedida();
        unidade.setId(id);
        unidade.atualizar("CX", "Caixa", (short) 0);

        when(unidadeMedidaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)).thenReturn(Optional.of(unidade));

        unidadeMedidaService.excluir(id);

        assertThat(unidade.getAtivo()).isFalse();
        assertThat(unidade.getDeletedAt()).isNotNull();
    }

    /**
     * Mantem contrato paginado no service.
     */
    @Test
    void deveListarComPaginacao() {
        when(unidadeMedidaRepository.findByAtivoTrueAndDeletedAtIsNull(PageRequest.of(0, 20)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        unidadeMedidaService.listar(PageRequest.of(0, 20));

        verify(unidadeMedidaRepository).findByAtivoTrueAndDeletedAtIsNull(PageRequest.of(0, 20));
    }
}
