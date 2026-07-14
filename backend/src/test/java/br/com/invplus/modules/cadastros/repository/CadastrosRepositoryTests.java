package br.com.invplus.modules.cadastros.repository;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.invplus.modules.cadastros.entity.Categoria;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.Endereco;
import br.com.invplus.modules.cadastros.entity.Marca;
import br.com.invplus.modules.cadastros.entity.UnidadeMedida;
import br.com.invplus.security.repository.PermissaoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Valida os repositories e o schema inicial do modulo Cadastros.
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class CadastrosRepositoryTests {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private EnderecoRepository enderecoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private MarcaRepository marcaRepository;

    @Autowired
    private UnidadeMedidaRepository unidadeMedidaRepository;

    @Autowired
    private PermissaoRepository permissaoRepository;

    /**
     * Garante que as entidades base persistem e que a permissao do modulo existe.
     */
    @Test
    void devePersistirEntidadesBaseDeCadastros() {
        Empresa empresa = empresaRepository.save(new Empresa(
                "Empresa Teste LTDA",
                "Empresa Teste",
                "11222333000181",
                "ISENTO",
                "71999990000",
                "empresa@teste.com"
        ));

        Endereco endereco = new Endereco();
        endereco.atualizar(empresa, null, "principal", "Rua Central", "100", null,
                "Centro", "Salvador", "ba", "40000000", true);
        enderecoRepository.save(endereco);

        Categoria categoria = new Categoria();
        categoria.atualizar("Insumos", "Categoria para insumos", "produto");
        categoriaRepository.save(categoria);

        Marca marca = new Marca();
        marca.atualizar("Marca Teste", "Marca usada em testes");
        marcaRepository.save(marca);

        UnidadeMedida unidade = new UnidadeMedida();
        unidade.atualizar("kg", "Quilograma", (short) 3);
        unidadeMedidaRepository.save(unidade);

        assertThat(empresaRepository.findByCnpjAndAtivoTrueAndDeletedAtIsNull("11222333000181")).isPresent();
        assertThat(enderecoRepository.findByEmpresaIdAndAtivoTrueAndDeletedAtIsNull(empresa.getId())).hasSize(1);
        assertThat(categoriaRepository.findByNomeAndTipoAndAtivoTrueAndDeletedAtIsNull("Insumos", "PRODUTO")).isPresent();
        assertThat(marcaRepository.findByNomeAndAtivoTrueAndDeletedAtIsNull("Marca Teste")).isPresent();
        assertThat(unidadeMedidaRepository.findBySiglaAndAtivoTrueAndDeletedAtIsNull("KG")).isPresent();
        assertThat(permissaoRepository.findByRecursoAndAcao("CADASTROS", "*")).isPresent();
    }

    /**
     * Garante que as consultas padrao do modulo ocultam registros excluidos logicamente.
     */
    @Test
    void deveOcultarEmpresaExcluidaLogicamenteNasConsultasAtivas() {
        Empresa empresa = empresaRepository.saveAndFlush(new Empresa(
                "Empresa Excluida LTDA",
                "Empresa Excluida",
                "33444555000166",
                "ISENTO",
                "71988880000",
                "excluida@teste.com"
        ));

        empresa.excluirLogicamente();
        empresaRepository.saveAndFlush(empresa);

        assertThat(empresaRepository.findById(empresa.getId())).isPresent();
        assertThat(empresaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(empresa.getId())).isEmpty();
        assertThat(empresaRepository.findByAtivoTrueAndDeletedAtIsNull())
                .extracting(item -> item.getId())
                .doesNotContain(empresa.getId());
    }
}
