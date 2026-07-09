package br.com.invplus.businessflow;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.invplus.modules.financeiro.enums.OrigemMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.repository.MovimentoFinanceiroRepository;
import br.com.invplus.modules.financeiro.repository.PagamentoRepository;
import br.com.invplus.modules.financeiro.repository.RecebimentoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Confirma a rastreabilidade entre eventos financeiros e seus movimentos automaticos.
 */
@SpringBootTest
@ActiveProfiles("test")
class MovimentoFinanceiroBusinessFlowIntegrationTest {

    @Autowired
    private MovimentoFinanceiroRepository movimentoRepository;
    @Autowired
    private RecebimentoRepository recebimentoRepository;
    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Test
    @Transactional
    void deveManterMovimentosAutomaticosRastreaveisPorOrigem() {
        movimentoRepository.findAll().forEach(movimento -> {
            if (movimento.getOrigem() == OrigemMovimentoFinanceiro.RECEBIMENTO) {
                var recebimento = recebimentoRepository.findById(movimento.getOrigemId()).orElseThrow();
                assertThat(movimento.getCliente().getId()).isEqualTo(recebimento.getCliente().getId());
                assertThat(movimento.getValor()).isEqualByComparingTo(recebimento.getValorLiquido());
            }
            if (movimento.getOrigem() == OrigemMovimentoFinanceiro.PAGAMENTO) {
                var pagamento = pagamentoRepository.findById(movimento.getOrigemId()).orElseThrow();
                assertThat(movimento.getFornecedor().getId()).isEqualTo(pagamento.getFornecedor().getId());
                assertThat(movimento.getValor()).isEqualByComparingTo(pagamento.getValorLiquido());
            }
        });
    }
}
