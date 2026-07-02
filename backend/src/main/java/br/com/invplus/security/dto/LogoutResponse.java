package br.com.invplus.security.dto;

public record LogoutResponse(
        boolean success,
        String message
) {
}
