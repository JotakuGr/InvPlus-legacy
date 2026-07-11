package br.com.invplus.modules.financeiro.conciliacao.dto;

import br.com.invplus.modules.financeiro.enums.TipoLancamentoBancario;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ResumoSugestoesConciliacaoResponse(
        UUID lancamentoBancarioId,
        String descricaoLancamento,
        LocalDate dataLancamento,
        TipoLancamentoBancario tipoLancamento,
        BigDecimal valorLancamento,
        Integer quantidadeCandidatos,
        List<SugestaoConciliacaoResponse> sugestoes
) {
}
