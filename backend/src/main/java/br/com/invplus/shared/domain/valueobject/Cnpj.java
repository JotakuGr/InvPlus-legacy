package br.com.invplus.shared.domain.valueobject;

/**
 * Objeto de valor para CNPJ armazenado somente com digitos.
 */
public record Cnpj(String valor) {

    public Cnpj {
        valor = valor == null ? null : valor.replaceAll("\\D", "");
        if (valor != null && valor.length() != 14) {
            throw new IllegalArgumentException("CNPJ deve conter 14 digitos");
        }
    }
}
