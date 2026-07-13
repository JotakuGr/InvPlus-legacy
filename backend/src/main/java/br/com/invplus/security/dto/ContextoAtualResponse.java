package br.com.invplus.security.dto;
import java.util.*;
public record ContextoAtualResponse(UUID usuarioId, UUID empresaId, String empresaNome,
        UUID filialId, String filialNome, List<String> permissoes) {}
