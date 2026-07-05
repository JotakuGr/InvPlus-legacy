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

import br.com.invplus.modules.cadastros.dto.request.MarcaRequest;
import br.com.invplus.modules.cadastros.dto.response.MarcaResponse;
import br.com.invplus.modules.cadastros.entity.Marca;
import br.com.invplus.modules.cadastros.mapper.MarcaMapper;
import br.com.invplus.modules.cadastros.repository.MarcaRepository;
import br.com.invplus.modules.cadastros.validator.MarcaValidator;
import br.com.invplus.shared.domain.exception.ConflictException;

/**
 * Testes unitarios das regras de negocio de Marca.
 */
@ExtendWith(MockitoExtension.class)
class MarcaServiceTest {

    @Mock
    private MarcaRepository marcaRepository;

    private MarcaService marcaService;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        MarcaMapper mapper = new MarcaMapper();
        MarcaValidator validator = new MarcaValidator(marcaRepository);
        marcaService = new MarcaService(marcaRepository, mapper, validator);
    }

    /**
     * Garante criacao com campos opcionais e nome normalizado.
     */
    @Test
    void deveCriarMarcaComSucesso() {
        when(marcaRepository.findByNomeIgnoreCaseAndAtivoTrueAndDeletedAtIsNull("Samsung"))
                .thenReturn(Optional.empty());
        when(marcaRepository.save(any(Marca.class))).thenAnswer(invocation -> {
            Marca marca = invocation.getArgument(0);
            marca.setId(UUID.randomUUID());
            return marca;
        });

        MarcaResponse response = marcaService.criar(
                new MarcaRequest(" Samsung ", "Eletronicos", "https://www.samsung.com", "Samsung")
        );

        assertThat(response.nome()).isEqualTo("Samsung");
        assertThat(response.descricao()).isEqualTo("Eletronicos");
        assertThat(response.site()).isEqualTo("https://www.samsung.com");
        assertThat(response.fabricante()).isEqualTo("Samsung");
    }

    /**
     * Bloqueia duplicidade de nome entre marcas ativas.
     */
    @Test
    void deveBloquearNomeDuplicado() {
        Marca existente = new Marca();
        existente.setId(UUID.randomUUID());
        existente.atualizar("Dell", "Computadores", null, null);

        when(marcaRepository.findByNomeIgnoreCaseAndAtivoTrueAndDeletedAtIsNull("Dell"))
                .thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> marcaService.criar(new MarcaRequest("Dell", "Duplicada", null, null)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Ja existe marca ativa");
    }

    /**
     * Confirma que a exclusao logica marca a entidade como inativa.
     */
    @Test
    void deveExcluirLogicamente() {
        UUID id = UUID.randomUUID();
        Marca marca = new Marca();
        marca.setId(id);
        marca.atualizar("Intel", "Processadores", null, "Intel");

        when(marcaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)).thenReturn(Optional.of(marca));

        marcaService.excluir(id);

        assertThat(marca.getAtivo()).isFalse();
        assertThat(marca.getDeletedAt()).isNotNull();
    }

    /**
     * Mantem o contrato paginado do service.
     */
    @Test
    void deveListarComPaginacao() {
        when(marcaRepository.findByAtivoTrueAndDeletedAtIsNull(PageRequest.of(0, 20)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        marcaService.listar(PageRequest.of(0, 20));

        verify(marcaRepository).findByAtivoTrueAndDeletedAtIsNull(PageRequest.of(0, 20));
    }
}
