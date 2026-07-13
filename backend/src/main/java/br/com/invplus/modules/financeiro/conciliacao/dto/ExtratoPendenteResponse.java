package br.com.invplus.modules.financeiro.conciliacao.dto;

import br.com.invplus.modules.financeiro.enums.StatusExtratoBancario;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ExtratoPendenteResponse(
        UUID extratoBancarioId, UUID contaBancariaId, String contaBancariaDescricao,
        LocalDate dataInicio, LocalDate dataFim, StatusExtratoBancario status,
        int quantidadeLancamentos, int quantidadeConciliados, int quantidadePendentes,
        BigDecimal percentualConciliado, BigDecimal valorCreditosPendentes,
        BigDecimal valorDebitosPendentes, OffsetDateTime importadoEm
) {}
