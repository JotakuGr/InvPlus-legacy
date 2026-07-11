package br.com.invplus.security.dto;
import java.time.OffsetDateTime;
import java.util.UUID;
public record UsuarioEmpresaResponse(UUID id,UUID usuarioId,String usuarioEmail,UUID empresaId,
        String empresaNome,Boolean principal,Boolean acessoTodasFiliais,Boolean ativo,
        OffsetDateTime criadoEm,OffsetDateTime atualizadoEm) {}
