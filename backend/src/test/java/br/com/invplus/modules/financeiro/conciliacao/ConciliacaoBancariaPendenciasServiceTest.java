package br.com.invplus.modules.financeiro.conciliacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.financeiro.conciliacao.service.*;
import br.com.invplus.modules.financeiro.entity.*;
import br.com.invplus.modules.financeiro.enums.*;
import br.com.invplus.modules.financeiro.repository.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

@ExtendWith(MockitoExtension.class)
class ConciliacaoBancariaPendenciasServiceTest {
    @Mock ExtratoBancarioRepository extratoRepository;
    @Mock ExtratoBancarioLancamentoRepository lancamentoRepository;
    @Mock MovimentoFinanceiroRepository movimentoRepository;
    @Mock ContaBancariaRepository contaRepository;
    @Mock PontuacaoConciliacaoService pontuacaoService;

    @Test void deveRetornarPendenciaSemSugestao() {
        Empresa empresa = new Empresa(); empresa.setId(UUID.randomUUID());
        ContaBancaria conta = new ContaBancaria(); conta.setId(UUID.randomUUID()); conta.setEmpresa(empresa);
        conta.setBancoNome("Banco"); conta.setAgencia("1"); conta.setNumeroConta("2");
        ExtratoBancario extrato = new ExtratoBancario(); extrato.setId(UUID.randomUUID()); extrato.setContaBancaria(conta);
        ExtratoBancarioLancamento l = new ExtratoBancarioLancamento(); l.setId(UUID.randomUUID());
        l.setExtratoBancario(extrato); l.setDataLancamento(LocalDate.now()); l.setDescricao("Credito");
        l.setTipo(TipoLancamentoBancario.CREDITO); l.setCategoriaBancaria(CategoriaLancamentoBancario.PIX);
        l.setValor(new BigDecimal("10.00"));
        when(lancamentoRepository.buscarPendencias(eq(empresa.getId()), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), any())).thenReturn(new PageImpl<>(List.of(l)));
        when(movimentoRepository.buscarCandidatosConciliacao(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());
        var service = new ConciliacaoBancariaPainelService(extratoRepository, lancamentoRepository,
                movimentoRepository, contaRepository, pontuacaoService, null);
        var pagina = service.buscarPendencias(empresa.getId(), null, null, null, null,
                null, null, null, null, PageRequest.of(0, 20));
        assertThat(pagina.getContent()).singleElement().satisfies(item -> {
            assertThat(item.melhorSugestao()).isNull();
            assertThat(item.quantidadeSugestoes()).isZero();
        });
    }
}
