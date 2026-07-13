package br.com.invplus.modules.financeiro.service;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.financeiro.dto.request.CentroCustoRequest;
import br.com.invplus.modules.financeiro.entity.CentroCusto;
import br.com.invplus.modules.financeiro.enums.TipoCentroCusto;
import br.com.invplus.modules.financeiro.mapper.CentroCustoMapper;
import br.com.invplus.modules.financeiro.repository.CentroCustoRepository;
import br.com.invplus.modules.financeiro.validator.CentroCustoValidator;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;

/**
 * Testes unitarios das regras hierarquicas de CentroCusto.
 */
@ExtendWith(MockitoExtension.class)
class CentroCustoServiceTest {

    @Mock
    private CentroCustoRepository repository;
    @Mock
    private EmpresaRepository empresaRepository;

    private CentroCustoService service;
    private Empresa empresa;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        CentroCustoMapper mapper = new CentroCustoMapper();
        service = new CentroCustoService(repository, empresaRepository, mapper,
                new CentroCustoValidator(repository));
        empresa = empresa("Empresa Centro");
    }

    @Test
    void deveCriarCentroRaiz() {
        prepararEmpresa();
        when(repository.save(any(CentroCusto.class))).thenAnswer(invocation -> {
            CentroCusto entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        var response = service.criar(request(null, "ADM", "Administrativo"));

        assertThat(response.codigo()).isEqualTo("ADM");
        assertThat(response.caminhoCompleto()).isEqualTo("Administrativo");
    }

    @Test
    void deveCriarCentroFilhoComCaminhoCompleto() {
        CentroCusto pai = centro(empresa, null, "ADM", "Administrativo");
        prepararEmpresa();
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(pai.getId())).thenReturn(Optional.of(pai));
        when(repository.save(any(CentroCusto.class))).thenAnswer(invocation -> {
            CentroCusto entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        var response = service.criar(request(pai.getId(), "MKT", "Marketing"));

        assertThat(response.caminhoCompleto()).isEqualTo("Administrativo > Marketing");
    }

    @Test
    void deveBloquearCodigoDuplicadoPorEmpresa() {
        CentroCusto existente = centro(empresa, null, "ADM", "Administrativo");
        prepararEmpresa();
        when(repository.findByEmpresaIdAndCodigoIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(
                empresa.getId(), "ADM")).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.criar(request(null, "ADM", "Outro")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void deveBloquearPaiDeOutraEmpresa() {
        Empresa outra = empresa("Outra Empresa");
        CentroCusto pai = centro(outra, null, "OUT", "Outro");
        prepararEmpresa();
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(pai.getId())).thenReturn(Optional.of(pai));

        assertThatThrownBy(() -> service.criar(request(pai.getId(), "FILHO", "Filho")))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void deveBloquearAutorelacionamentoECicloIndireto() {
        CentroCusto raiz = centro(empresa, null, "RAIZ", "Raiz");
        CentroCusto filho = centro(empresa, raiz, "FILHO", "Filho");
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(raiz.getId())).thenReturn(Optional.of(raiz));
        prepararEmpresa();

        assertThatThrownBy(() -> service.atualizar(raiz.getId(),
                request(raiz.getId(), "RAIZ", "Raiz")))
                .isInstanceOf(InvalidOperationException.class);

        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(filho.getId())).thenReturn(Optional.of(filho));
        assertThatThrownBy(() -> service.atualizar(raiz.getId(),
                request(filho.getId(), "RAIZ", "Raiz")))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void deveExcluirLogicamente() {
        CentroCusto entity = centro(empresa, null, "ADM", "Administrativo");
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(entity.getId())).thenReturn(Optional.of(entity));

        service.excluir(entity.getId());

        assertThat(entity.getAtivo()).isFalse();
        assertThat(entity.getDeletedAt()).isNotNull();
    }

    private void prepararEmpresa() {
        when(empresaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(empresa.getId()))
                .thenReturn(Optional.of(empresa));
    }

    private CentroCustoRequest request(UUID paiId, String codigo, String nome) {
        return new CentroCustoRequest(empresa.getId(), paiId, codigo, nome,
                "Centro de custo de teste", TipoCentroCusto.AMBOS);
    }

    private CentroCusto centro(Empresa empresa, CentroCusto pai, String codigo, String nome) {
        CentroCusto entity = new CentroCusto();
        entity.setId(UUID.randomUUID());
        entity.atualizar(empresa, pai, codigo, nome, null, TipoCentroCusto.AMBOS);
        return entity;
    }

    private Empresa empresa(String nome) {
        Empresa entity = new Empresa(nome, nome, "12345678000199", null, null, null);
        entity.setId(UUID.randomUUID());
        return entity;
    }
}
