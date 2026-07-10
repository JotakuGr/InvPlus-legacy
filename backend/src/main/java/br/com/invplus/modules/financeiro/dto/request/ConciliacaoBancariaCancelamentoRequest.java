package br.com.invplus.modules.financeiro.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Justificativa obrigatoria para desfazer uma conciliacao. */
public record ConciliacaoBancariaCancelamentoRequest(
        @NotBlank @Size(min = 5, max = 500) String motivoCancelamento
) {
}
