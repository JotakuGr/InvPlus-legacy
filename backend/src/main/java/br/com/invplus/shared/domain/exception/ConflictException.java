package br.com.invplus.shared.domain.exception;

/**
 * Indica conflito com o estado atual dos dados, como duplicidade ativa.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
