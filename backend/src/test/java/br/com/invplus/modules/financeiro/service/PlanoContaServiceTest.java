package br.com.invplus.modules.financeiro.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.financeiro.dto.request.PlanoContaRequest;
import br.com.invplus.modules.financeiro.entity.PlanoConta;
import br.com.invplus.modules.financeiro.enums.NaturezaPlanoConta;
import br.com.invplus.modules.financeiro.enums.TipoPlanoConta;
import br.com.invplus.modules.financeiro.mapper.PlanoContaMapper;
import br.com.invplus.modules.financeiro.repository.PlanoContaRepository;
import br.com.invplus.modules.financeiro.validator.PlanoContaValidator;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Testes unitarios das regras hierarquicas do plano de contas.
 */
@ExtendWith(MockitoExtension.class)
class PlanoContaServiceTest {

    @Mock
    private PlanoContaRepository repository;
    @Mock
    private EmpresaRepository empresaRepository;

    private PlanoContaService service;
    private Empresa empresa;

    @BeforeEach
    void setUp() {
        PlanoContaMapper mapper = new PlanoContaMapper();
        service = new PlanoContaService(repository, empresaRepository, mapper,
                new PlanoContaValidator(repository));
        empresa = empresa("Empresa Plano");
    }

    @Test
    void deveCriarPlanoRaizSintetico() {
        prepararEmpresa();
        when(repository.save(any(PlanoConta.class))).thenAnswer(invocation -> {
            PlanoConta entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        var response = service.criar(request(null, "3", "Receitas", TipoPlanoConta.SINTETICA));

        assertThat(response.codigo()).isEqualTo("3");
        assertThat(response.caminhoCompleto()).isEqualTo("Receitas");
    }

    @Test
    void deveCriarPlanoFilhoAnalitico() {
        PlanoConta pai = plano(empresa, null, "3", "Receitas", TipoPlanoConta.SINTETICA);
        prepararEmpresa();
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(pai.getId())).thenReturn(Optional.of(pai));
        when(repository.save(any(PlanoConta.class))).thenAnswer(invocation -> {
            PlanoConta entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        var response = service.criar(request(pai.getId(), "3.1", "Vendas", TipoPlanoConta.ANALITICA));

        assertThat(response.caminhoCompleto()).isEqualTo("Receitas > Vendas");
        assertThat(response.tipo()).isEqualTo(TipoPlanoConta.ANALITICA);
    }

    @Test
    void deveBloquearCodigoDuplicadoPorEmpresa() {
        PlanoConta existente = plano(empresa, null, "3", "Receitas", TipoPlanoConta.SINTETICA);
        prepararEmpresa();
        when(repository.findByEmpresaIdAndCodigoIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(
                empresa.getId(), "3")).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.criar(request(null, "3", "Outro", TipoPlanoConta.SINTETICA)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void deveBloquearPaiDeOutraEmpresa() {
        Empresa outra = empresa("Outra Empresa");
        PlanoConta pai = plano(outra, null, "1", "Ativos", TipoPlanoConta.SINTETICA);
        prepararEmpresa();
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(pai.getId())).thenReturn(Optional.of(pai));

        assertThatThrownBy(() -> service.criar(
                request(pai.getId(), "1.1", "Caixa", TipoPlanoConta.ANALITICA)))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void deveBloquearAutorelacionamentoECicloIndireto() {
        PlanoConta raiz = plano(empresa, null, "3", "Receitas", TipoPlanoConta.SINTETICA);
        PlanoConta filho = plano(empresa, raiz, "3.1", "Vendas", TipoPlanoConta.ANALITICA);
        prepararEmpresa();
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(raiz.getId())).thenReturn(Optional.of(raiz));

        assertThatThrownBy(() -> service.atualizar(raiz.getId(),
                request(raiz.getId(), "3", "Receitas", TipoPlanoConta.SINTETICA)))
                .isInstanceOf(InvalidOperationException.class);

        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(filho.getId())).thenReturn(Optional.of(filho));
        assertThatThrownBy(() -> service.atualizar(raiz.getId(),
                request(filho.getId(), "3", "Receitas", TipoPlanoConta.SINTETICA)))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void deveDiferenciarContaAnaliticaDeSinteticaEExcluirLogicamente() {
        PlanoConta sintetica = plano(empresa, null, "3", "Receitas", TipoPlanoConta.SINTETICA);
        PlanoConta analitica = plano(empresa, sintetica, "3.1", "Vendas", TipoPlanoConta.ANALITICA);
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(analitica.getId()))
                .thenReturn(Optional.of(analitica));

        assertThat(sintetica.permiteLancamento()).isFalse();
        assertThat(analitica.permiteLancamento()).isTrue();

        service.excluir(analitica.getId());

        assertThat(analitica.getAtivo()).isFalse();
        assertThat(analitica.getDeletedAt()).isNotNull();
    }

    private void prepararEmpresa() {
        when(empresaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(empresa.getId()))
                .thenReturn(Optional.of(empresa));
    }

    private PlanoContaRequest request(UUID paiId, String codigo, String nome, TipoPlanoConta tipo) {
        return new PlanoContaRequest(empresa.getId(), paiId, codigo, nome,
                "Plano de conta de teste", tipo, NaturezaPlanoConta.RECEITA);
    }

    private PlanoConta plano(Empresa empresa, PlanoConta pai, String codigo,
            String nome, TipoPlanoConta tipo) {
        PlanoConta entity = new PlanoConta();
        entity.setId(UUID.randomUUID());
        entity.atualizar(empresa, pai, codigo, nome, null, tipo, NaturezaPlanoConta.RECEITA);
        return entity;
    }

    private Empresa empresa(String nome) {
        Empresa entity = new Empresa(nome, nome, "12345678000199", null, null, null);
        entity.setId(UUID.randomUUID());
        return entity;
    }
}
