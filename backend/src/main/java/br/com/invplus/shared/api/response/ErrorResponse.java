package br.com.invplus.shared.api.response;

import java.time.OffsetDateTime;
import java.util.List;

public record ErrorResponse(
        boolean success,
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> errors
) {
    /**
     * Monta uma resposta padronizada para todos os erros da API.
     */
    public static ErrorResponse of(int status, String error, String message, String path, List<String> errors) {
        return new ErrorResponse(false, OffsetDateTime.now(), status, error, message, path, errors);
    }
}
