package br.com.invplus.modules.financeiro.service;

import br.com.invplus.modules.pagamentos.entity.CondicaoPagamento;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

/**
 * Gera parcelas financeiras a partir de uma condicao de pagamento cadastrada.
 */
@Service
public class GeradorParcelasService {

    private final CalculadoraVencimentoService calculadoraVencimentoService;

    public GeradorParcelasService(CalculadoraVencimentoService calculadoraVencimentoService) {
        this.calculadoraVencimentoService = calculadoraVencimentoService;
    }

    public List<ParcelaGerada> gerar(LocalDate dataBase, BigDecimal valorTotal, CondicaoPagamento condicao) {
        return condicao.getParcelas().stream()
                .filter(parcela -> Boolean.TRUE.equals(parcela.getAtivo()) && parcela.getDeletedAt() == null)
                .sorted(java.util.Comparator.comparing(parcela -> Objects.requireNonNull(
                        parcela.getNumeroParcela(), "Numero da parcela nao pode ser nulo")))
                .map(parcela -> {
                    LocalDate vencimentoBase = dataBase.plusDays(parcela.getDias());
                    LocalDate vencimento = calculadoraVencimentoService.ajustarVencimento(
                            vencimentoBase,
                            condicao.getRegraVencimento(),
                            Boolean.TRUE.equals(condicao.getConsideraFeriados())
                    );
                    BigDecimal valor = valorTotal
                            .multiply(parcela.getPercentual())
                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    return new ParcelaGerada(parcela.getNumeroParcela(), vencimento, valor);
                })
                .toList();
    }
}
