package br.com.invplus.shared.domain.valueobject;

/**
 * Objeto de valor para CEP armazenado somente com digitos.
 */
public record Cep(String valor) {

    public Cep {
        valor = valor == null ? null : valor.replaceAll("\\D", "");
        if (valor != null && valor.length() != 8) {
            throw new IllegalArgumentException("CEP deve conter 8 digitos");
        }
    }
}
