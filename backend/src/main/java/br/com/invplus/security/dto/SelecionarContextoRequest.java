package br.com.invplus.security.dto;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
public record SelecionarContextoRequest(@NotNull UUID empresaId, UUID filialId) {}
