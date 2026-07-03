package br.com.invplus.modules.cadastros.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.invplus.modules.cadastros.dto.request.EmpresaRequest;
import br.com.invplus.modules.cadastros.dto.response.EmpresaResponse;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.mapper.EmpresaMapper;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.cadastros.validator.EmpresaValidator;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ConflictException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

/**
 * Testes unitarios das regras de negocio de Empresa.
 */
@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;

    private EmpresaService empresaService;

    @BeforeEach
    void setUp() {
        EmpresaMapper mapper = new EmpresaMapper();
        EmpresaValidator validator = new EmpresaValidator(empresaRepository);
        empresaService = new EmpresaService(empresaRepository, mapper, validator);
    }

    /**
     * Garante criacao com CNPJ normalizado e CRT valido.
     */
    @Test
    void deveCriarEmpresaComSucesso() {
        when(empresaRepository.findByCnpjAndAtivoTrueAndDeletedAtIsNull("11222333000181"))
                .thenReturn(Optional.empty());
        when(empresaRepository.save(any(Empresa.class))).thenAnswer(invocation -> {
            Empresa empresa = invocation.getArgument(0);
            empresa.setId(UUID.randomUUID());
            return empresa;
        });

        EmpresaResponse response = empresaService.criar(request("11.222.333/0001-81", "empresa@teste.com"));

        assertThat(response.razaoSocial()).isEqualTo("Empresa Teste LTDA");
        assertThat(response.cnpj()).isEqualTo("11222333000181");
        assertThat(response.crt()).isEqualTo(1);
        assertThat(response.regimeTributario()).isEqualTo("SIMPLES_NACIONAL");
    }

    /**
     * Bloqueia CNPJ repetido entre empresas ativas.
     */
    @Test
    void deveBloquearCnpjDuplicado() {
        Empresa existente = new Empresa();
        existente.setId(UUID.randomUUID());
        existente.atualizar("Empresa Existente", null, "11222333000181", null, null, null, 1, null, null);

        when(empresaRepository.findByCnpjAndAtivoTrueAndDeletedAtIsNull("11222333000181"))
                .thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> empresaService.criar(request("11222333000181", "empresa@teste.com")))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Ja existe empresa ativa");
    }

    /**
     * Bloqueia CNPJ fora do formato simples de 14 digitos.
     */
    @Test
    void deveBloquearCnpjInvalido() {
        assertThatThrownBy(() -> empresaService.criar(request("123", "empresa@teste.com")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CNPJ invalido");
    }

    /**
     * Bloqueia email invalido quando informado diretamente ao service.
     */
    @Test
    void deveBloquearEmailInvalido() {
        assertThatThrownBy(() -> empresaService.criar(request("11222333000181", "email-invalido")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Email invalido");
    }

    /**
     * Confirma que a exclusao logica marca a entidade como inativa.
     */
    @Test
    void deveExcluirLogicamente() {
        UUID id = UUID.randomUUID();
        Empresa empresa = new Empresa();
        empresa.setId(id);
        empresa.atualizar("Empresa Excluir", null, "11222333000181", null, null, null, 1, null, null);

        when(empresaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)).thenReturn(Optional.of(empresa));

        empresaService.excluir(id);

        assertThat(empresa.getAtivo()).isFalse();
        assertThat(empresa.getDeletedAt()).isNotNull();
    }

    /**
     * Mantem o contrato paginado do service.
     */
    @Test
    void deveListarComPaginacao() {
        when(empresaRepository.findByAtivoTrueAndDeletedAtIsNull(PageRequest.of(0, 20)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        empresaService.listar(PageRequest.of(0, 20));

        verify(empresaRepository).findByAtivoTrueAndDeletedAtIsNull(PageRequest.of(0, 20));
    }

    private EmpresaRequest request(String cnpj, String email) {
        return new EmpresaRequest(
                "Empresa Teste LTDA",
                "Empresa Teste",
                cnpj,
                "ISENTO",
                "12345",
                "SIMPLES_NACIONAL",
                1,
                "71999990000",
                email
        );
    }
}
