package br.com.invplus.shared.api.response;

import java.time.OffsetDateTime;
import java.util.List;

public record ErrorResponse(
        boolean success,
        String message,
        List<String> errors,
        OffsetDateTime timestamp
) {
    public static ErrorResponse of(String message, List<String> errors) {
        return new ErrorResponse(false, message, errors, OffsetDateTime.now());
    }
}
