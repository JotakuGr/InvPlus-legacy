package br.com.invplus.security.dto;
import java.time.OffsetDateTime;
import java.util.UUID;
public record UsuarioEmpresaFilialResponse(UUID id,UUID usuarioEmpresaId,UUID filialId,String filialNome,
        Boolean principal,Boolean ativo,OffsetDateTime criadoEm,OffsetDateTime atualizadoEm) {}
