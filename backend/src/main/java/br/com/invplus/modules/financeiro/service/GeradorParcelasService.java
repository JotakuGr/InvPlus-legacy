package br.com.invplus.modules.financeiro.service;

import br.com.invplus.modules.pagamentos.entity.CondicaoPagamento;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
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
                .sorted(Comparator.comparingInt(parcela -> Objects.requireNonNull(
                        parcela.getNumeroParcela(), "Numero da parcela nao pode ser nulo")))
                .map(parcela -> {
                    Integer numeroParcela = Objects.requireNonNull(
                            parcela.getNumeroParcela(), "Numero da parcela nao pode ser nulo");
                    Integer dias = Objects.requireNonNull(
                            parcela.getDias(), "Dias da parcela nao pode ser nulo");
                    BigDecimal percentual = Objects.requireNonNull(
                            parcela.getPercentual(), "Percentual da parcela nao pode ser nulo");
                    LocalDate vencimentoBase = dataBase.plusDays(dias);
                    LocalDate vencimento = calculadoraVencimentoService.ajustarVencimento(
                            vencimentoBase,
                            condicao.getRegraVencimento(),
                            Boolean.TRUE.equals(condicao.getConsideraFeriados())
                    );
                    BigDecimal valor = valorTotal
                            .multiply(percentual)
                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    return new ParcelaGerada(numeroParcela, vencimento, valor);
                })
                .toList();
    }
}
