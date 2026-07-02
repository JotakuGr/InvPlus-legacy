package br.com.invplus.shared.domain.valueobject;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Objeto de valor para email normalizado e validado.
 */
public record Email(String valor) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Email {
        valor = normalizar(valor);
        if (valor != null && !EMAIL_PATTERN.matcher(valor).matches()) {
            throw new IllegalArgumentException("Email invalido");
        }
    }

    private static String normalizar(String valor) {
        return valor == null ? null : valor.trim().toLowerCase(Locale.ROOT);
    }
}
