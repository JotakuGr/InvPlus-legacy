package br.com.invplus.security.dto;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
public record UsuarioEmpresaFilialRequest(@NotNull UUID usuarioEmpresaId,@NotNull UUID filialId,Boolean principal) {}
