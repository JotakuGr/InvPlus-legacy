package br.com.invplus.security.dto;
import java.util.UUID;
public record FilialDisponivelResponse(UUID filialId, String nome, boolean principal) {}
