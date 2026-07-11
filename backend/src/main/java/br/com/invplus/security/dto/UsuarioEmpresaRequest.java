package br.com.invplus.security.dto;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
public record UsuarioEmpresaRequest(@NotNull UUID usuarioId,@NotNull UUID empresaId,
        Boolean principal,Boolean acessoTodasFiliais) {}
