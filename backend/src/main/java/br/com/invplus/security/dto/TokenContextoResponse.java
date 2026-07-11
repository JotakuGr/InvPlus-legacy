package br.com.invplus.security.dto;
public record TokenContextoResponse(String accessToken, String refreshToken, ContextoAtualResponse contexto) {}
