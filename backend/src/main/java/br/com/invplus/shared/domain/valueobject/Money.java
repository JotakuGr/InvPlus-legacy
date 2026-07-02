package br.com.invplus.shared.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

/**
 * Objeto de valor para valores monetarios com moeda explicita.
 */
public record Money(BigDecimal valor, Currency moeda) {

    public Money {
        if (valor == null) {
            throw new IllegalArgumentException("Valor monetario e obrigatorio");
        }
        moeda = moeda == null ? Currency.getInstance("BRL") : moeda;
        valor = valor.setScale(2, RoundingMode.HALF_UP);
    }
}
