package br.com.invplus.shared.domain.exception;

/**
 * Indica uma operacao invalida para a regra de negocio atual.
 */
public class InvalidOperationException extends RuntimeException {

    public InvalidOperationException(String message) {
        super(message);
    }
}
