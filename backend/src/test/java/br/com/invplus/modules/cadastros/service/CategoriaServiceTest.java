package br.com.invplus.modules.cadastros.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.invplus.modules.cadastros.dto.request.CategoriaRequest;
import br.com.invplus.modules.cadastros.dto.response.CategoriaResponse;
import br.com.invplus.modules.cadastros.entity.Categoria;
import br.com.invplus.modules.cadastros.mapper.CategoriaMapper;
import br.com.invplus.modules.cadastros.repository.CategoriaRepository;
import br.com.invplus.modules.cadastros.validator.CategoriaValidator;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * Testes unitarios das regras de negocio de Categoria.
 */
@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    private CategoriaService categoriaService;

    @BeforeEach
    void setUp() {
        CategoriaMapper mapper = new CategoriaMapper();
        CategoriaValidator validator = new CategoriaValidator(categoriaRepository);
        categoriaService = new CategoriaService(categoriaRepository, mapper, validator);
    }

    /**
     * Garante criacao com nome unico e pai opcional.
     */
    @Test
    void deveCriarCategoriaComSucesso() {
        UUID paiId = UUID.randomUUID();
        Categoria pai = categoria("Informatica", null);
        pai.setId(paiId);

        when(categoriaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(paiId)).thenReturn(Optional.of(pai));
        when(categoriaRepository.findByNomeIgnoreCaseAndAtivoTrueAndDeletedAtIsNull("Perifericos"))
                .thenReturn(Optional.empty());
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> {
            Categoria categoria = invocation.getArgument(0);
            categoria.setId(UUID.randomUUID());
            return categoria;
        });

        CategoriaResponse response = categoriaService.criar(
                new CategoriaRequest(" Perifericos ", "Itens de informatica", paiId, 10)
        );

        assertThat(response.nome()).isEqualTo("Perifericos");
        assertThat(response.categoriaPaiId()).isEqualTo(paiId);
        assertThat(response.categoriaPaiNome()).isEqualTo("Informatica");
        assertThat(response.caminhoCompleto()).isEqualTo("Informatica > Perifericos");
        assertThat(response.ordem()).isEqualTo(10);
    }

    /**
     * Bloqueia nomes repetidos entre registros ativos.
     */
    @Test
    void deveBloquearNomeDuplicado() {
        Categoria existente = categoria("Limpeza", null);
        existente.setId(UUID.randomUUID());

        when(categoriaRepository.findByNomeIgnoreCaseAndAtivoTrueAndDeletedAtIsNull("Limpeza"))
                .thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> categoriaService.criar(new CategoriaRequest("Limpeza", "Duplicada", null, 0)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Ja existe categoria ativa");
    }

    /**
     * Impede que uma categoria seja marcada como pai dela mesma.
     */
    @Test
    void deveBloquearCategoriaComoPaiDelaMesma() {
        UUID id = UUID.randomUUID();
        Categoria categoria = categoria("Alimentos", null);
        categoria.setId(id);

        when(categoriaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.findByNomeIgnoreCaseAndAtivoTrueAndDeletedAtIsNull("Alimentos"))
                .thenReturn(Optional.of(categoria));

        assertThatThrownBy(() -> categoriaService.atualizar(id, new CategoriaRequest("Alimentos", null, id, 0)))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("pai dela mesma");
    }

    /**
     * Impede ciclos como A -> B -> A na hierarquia.
     */
    @Test
    void deveBloquearCicloHierarquico() {
        UUID categoriaId = UUID.randomUUID();
        UUID paiId = UUID.randomUUID();

        Categoria categoria = categoria("Eletronicos", null);
        categoria.setId(categoriaId);
        Categoria pai = categoria("Smartphones", categoria);
        pai.setId(paiId);

        when(categoriaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(categoriaId)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(paiId)).thenReturn(Optional.of(pai));
        when(categoriaRepository.findByNomeIgnoreCaseAndAtivoTrueAndDeletedAtIsNull("Eletronicos"))
                .thenReturn(Optional.of(categoria));

        assertThatThrownBy(() -> categoriaService.atualizar(
                categoriaId,
                new CategoriaRequest("Eletronicos", null, paiId, 0)
        ))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("ciclo hierarquico");
    }

    /**
     * Confirma que a exclusao logica marca a categoria como inativa.
     */
    @Test
    void deveExcluirLogicamente() {
        UUID id = UUID.randomUUID();
        Categoria categoria = categoria("Arquivada", null);
        categoria.setId(id);

        when(categoriaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)).thenReturn(Optional.of(categoria));

        categoriaService.excluir(id);

        assertThat(categoria.getAtivo()).isFalse();
        assertThat(categoria.getDeletedAt()).isNotNull();
    }

    /**
     * Mantem o contrato paginado do endpoint de listagem.
     */
    @Test
    void deveListarComPaginacao() {
        PageRequest pageRequest = PageRequest.of(0, 20, Sort.by("ordem").ascending().and(Sort.by("nome").ascending()));
        when(categoriaRepository.findByAtivoTrueAndDeletedAtIsNull(pageRequest))
                .thenReturn(org.springframework.data.domain.Page.empty());

        categoriaService.listar(PageRequest.of(0, 20));

        verify(categoriaRepository).findByAtivoTrueAndDeletedAtIsNull(pageRequest);
    }

    private Categoria categoria(String nome, Categoria pai) {
        Categoria categoria = new Categoria();
        categoria.atualizar(pai, nome, null, 0);
        return categoria;
    }
}
