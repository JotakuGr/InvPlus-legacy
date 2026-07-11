package br.com.invplus.modules.financeiro.conciliacao.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ConciliacaoBancariaLoteRequest(
        @NotEmpty @Size(max = 100) List<@Valid ConciliacaoBancariaLoteItemRequest> itens
) {}
