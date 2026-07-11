package br.com.invplus.modules.financeiro.conciliacao.dto;

import br.com.invplus.modules.financeiro.enums.CategoriaLancamentoBancario;
import br.com.invplus.modules.financeiro.enums.TipoLancamentoBancario;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record LancamentoPendenteResponse(
        UUID lancamentoBancarioId, UUID extratoBancarioId, UUID contaBancariaId,
        String contaBancariaDescricao, LocalDate dataLancamento, String descricao,
        String documento, TipoLancamentoBancario tipo, CategoriaLancamentoBancario categoria,
        BigDecimal valor, BigDecimal saldoAposLancamento, MelhorSugestaoResponse melhorSugestao,
        int quantidadeSugestoes
) {}
