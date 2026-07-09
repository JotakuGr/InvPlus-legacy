package br.com.invplus.modules.financeiro.service;

import br.com.invplus.modules.financeiro.enums.RegraVencimento;
import java.time.DayOfWeek;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

/**
 * Ajusta vencimentos conforme regras comerciais de dias uteis.
 */
@Service
public class CalculadoraVencimentoService {

    public LocalDate ajustarVencimento(LocalDate data, RegraVencimento regra, boolean consideraFeriados) {
        if (data == null || regra == null || regra == RegraVencimento.MANTER_DATA) {
            return data;
        }
        if (regra == RegraVencimento.PROXIMO_DIA_UTIL) {
            return proximoDiaUtil(data);
        }
        return diaUtilAnterior(data);
    }

    private LocalDate proximoDiaUtil(LocalDate data) {
        LocalDate ajustada = data;
        while (isFimDeSemana(ajustada)) {
            ajustada = ajustada.plusDays(1);
        }
        return ajustada;
    }

    private LocalDate diaUtilAnterior(LocalDate data) {
        LocalDate ajustada = data;
        while (isFimDeSemana(ajustada)) {
            ajustada = ajustada.minusDays(1);
        }
        return ajustada;
    }

    private boolean isFimDeSemana(LocalDate data) {
        return data.getDayOfWeek() == DayOfWeek.SATURDAY || data.getDayOfWeek() == DayOfWeek.SUNDAY;
    }
}
