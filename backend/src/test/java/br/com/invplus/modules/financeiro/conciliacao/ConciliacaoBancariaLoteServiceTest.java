package br.com.invplus.modules.financeiro.conciliacao;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

import br.com.invplus.modules.financeiro.conciliacao.dto.*;
import br.com.invplus.modules.financeiro.conciliacao.service.*;
import br.com.invplus.modules.financeiro.enums.StatusResultadoConciliacaoLote;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConciliacaoBancariaLoteServiceTest {
    @Mock ConciliacaoBancariaLoteItemService itemService;

    @Test void devePreservarSucessoQuandoOutroItemFalhar() {
        UUID empresa = UUID.randomUUID(); var a = item(); var b = item();
        when(itemService.processar(empresa, a)).thenReturn(sucesso(a));
        when(itemService.processar(empresa, b)).thenThrow(new ConflictException("Movimento ja conciliado"));
        var resposta = new ConciliacaoBancariaLoteService(itemService)
                .confirmar(empresa, new ConciliacaoBancariaLoteRequest(List.of(a, b)));
        assertThat(resposta.quantidadeConciliada()).isEqualTo(1);
        assertThat(resposta.quantidadeRejeitada()).isEqualTo(1);
        assertThat(resposta.resultados().get(1).codigoErro()).isEqualTo("MOVIMENTO_JA_CONCILIADO");
    }

    @Test void deveRejeitarDuplicidadesEstruturais() {
        var a = item();
        var movimentoRepetido = new ConciliacaoBancariaLoteItemRequest(
                UUID.randomUUID(), a.movimentoFinanceiroId(), null);
        assertThatThrownBy(() -> new ConciliacaoBancariaLoteService(itemService).confirmar(
                UUID.randomUUID(), new ConciliacaoBancariaLoteRequest(List.of(a, movimentoRepetido))))
                .isInstanceOf(InvalidOperationException.class).hasMessageContaining("repetido");
    }

    private ConciliacaoBancariaLoteItemRequest item() {
        return new ConciliacaoBancariaLoteItemRequest(UUID.randomUUID(), UUID.randomUUID(), null);
    }
    private ConciliacaoBancariaLoteItemResponse sucesso(ConciliacaoBancariaLoteItemRequest i) {
        return new ConciliacaoBancariaLoteItemResponse(i.lancamentoBancarioId(), i.movimentoFinanceiroId(),
                UUID.randomUUID(), StatusResultadoConciliacaoLote.CONCILIADO, null, "OK");
    }
}
