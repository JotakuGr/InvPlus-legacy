package br.com.invplus.security.dto;
import java.util.*;
public record EmpresaDisponivelResponse(UUID empresaId, String nomeFantasia, String razaoSocial,
        boolean principal, boolean acessoTodasFiliais, List<FilialDisponivelResponse> filiais) {}
