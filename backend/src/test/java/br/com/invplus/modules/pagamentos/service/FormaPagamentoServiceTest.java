package br.com.invplus.modules.pagamentos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.invplus.modules.pagamentos.dto.request.FormaPagamentoRequest;
import br.com.invplus.modules.pagamentos.dto.response.FormaPagamentoResponse;
import br.com.invplus.modules.pagamentos.entity.FormaPagamento;
import br.com.invplus.modules.pagamentos.enums.TipoFormaPagamento;
import br.com.invplus.modules.pagamentos.mapper.FormaPagamentoMapper;
import br.com.invplus.modules.pagamentos.repository.FormaPagamentoRepository;
import br.com.invplus.modules.pagamentos.validator.FormaPagamentoValidator;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

/**
 * Testes unitarios das regras de forma de pagamento.
 */
@ExtendWith(MockitoExtension.class)
class FormaPagamentoServiceTest {

    @Mock
    private FormaPagamentoRepository repository;

    private FormaPagamentoService service;

    @BeforeEach
    void setUp() {
        FormaPagamentoMapper mapper = new FormaPagamentoMapper();
        service = new FormaPagamentoService(repository, mapper, new FormaPagamentoValidator(repository));
    }

    @Test
    void deveCriarFormaPagamentoComSucesso() {
        when(repository.findByCodigoIgnoreCaseAndAtivoTrueAndDeletedAtIsNull("PIX")).thenReturn(Optional.empty());
        when(repository.save(any(FormaPagamento.class))).thenAnswer(invocation -> {
            FormaPagamento entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        FormaPagamentoResponse response = service.criar(new FormaPagamentoRequest(
                "PIX",
                "Pix",
                TipoFormaPagamento.PIX,
                false,
                false
        ));

        assertThat(response.id()).isNotNull();
        assertThat(response.codigo()).isEqualTo("PIX");
        assertThat(response.tipo()).isEqualTo(TipoFormaPagamento.PIX);
    }

    @Test
    void deveBloquearCodigoDuplicado() {
        FormaPagamento existente = formaPagamento("PIX", TipoFormaPagamento.PIX);
        when(repository.findByCodigoIgnoreCaseAndAtivoTrueAndDeletedAtIsNull("PIX")).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.criar(new FormaPagamentoRequest("PIX", "Pix", TipoFormaPagamento.PIX, false, false)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Ja existe forma de pagamento");
    }

    @Test
    void deveBloquearTrocoParaTipoDiferenteDeDinheiro() {
        when(repository.findByCodigoIgnoreCaseAndAtivoTrueAndDeletedAtIsNull("PIX")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(new FormaPagamentoRequest("PIX", "Pix", TipoFormaPagamento.PIX, true, false)))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Troco");
    }

    @Test
    void deveBloquearParcelamentoParaTipoNaoParcelavel() {
        when(repository.findByCodigoIgnoreCaseAndAtivoTrueAndDeletedAtIsNull("PIX")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(new FormaPagamentoRequest("PIX", "Pix", TipoFormaPagamento.PIX, false, true)))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Parcelamento");
    }

    @Test
    void deveBuscarListarAtualizarEExcluirLogicamente() {
        FormaPagamento entity = formaPagamento("DIN", TipoFormaPagamento.DINHEIRO);
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(entity.getId())).thenReturn(Optional.of(entity));
        when(repository.findAll(anySpecification(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(java.util.List.of(entity)));
        when(repository.findByCodigoIgnoreCaseAndAtivoTrueAndDeletedAtIsNull("DIN")).thenReturn(Optional.of(entity));

        assertThat(service.buscar(entity.getId()).codigo()).isEqualTo("DIN");
        assertThat(service.listarComFiltros("DIN", null, TipoFormaPagamento.DINHEIRO, true, PageRequest.of(0, 10))
                .getTotalElements()).isEqualTo(1);

        FormaPagamentoResponse atualizado = service.atualizar(entity.getId(), new FormaPagamentoRequest(
                "DIN",
                "Dinheiro",
                TipoFormaPagamento.DINHEIRO,
                true,
                false
        ));

        assertThat(atualizado.permiteTroco()).isTrue();
        service.excluir(entity.getId());
        assertThat(entity.getAtivo()).isFalse();
        assertThat(entity.getDeletedAt()).isNotNull();
    }

    private FormaPagamento formaPagamento(String codigo, TipoFormaPagamento tipo) {
        FormaPagamento entity = new FormaPagamento();
        entity.setId(UUID.randomUUID());
        entity.atualizar(codigo, codigo, tipo, false, false);
        return entity;
    }

    private Specification<FormaPagamento> anySpecification() {
        return any();
    }
}
