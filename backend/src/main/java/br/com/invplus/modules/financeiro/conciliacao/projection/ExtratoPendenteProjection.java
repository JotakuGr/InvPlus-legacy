package br.com.invplus.modules.financeiro.conciliacao.projection;

import br.com.invplus.modules.financeiro.enums.StatusExtratoBancario;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface ExtratoPendenteProjection {
    UUID getExtratoBancarioId();
    UUID getContaBancariaId();
    String getBancoNome();
    String getAgencia();
    String getNumeroConta();
    LocalDate getDataInicio();
    LocalDate getDataFim();
    StatusExtratoBancario getStatus();
    long getQuantidadeLancamentos();
    long getQuantidadeConciliados();
    BigDecimal getValorCreditosPendentes();
    BigDecimal getValorDebitosPendentes();
    OffsetDateTime getImportadoEm();
}
