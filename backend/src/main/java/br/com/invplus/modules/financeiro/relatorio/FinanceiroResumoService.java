package br.com.invplus.modules.financeiro.relatorio;

import br.com.invplus.modules.financeiro.enums.StatusContaPagar;
import br.com.invplus.modules.financeiro.enums.StatusContaReceber;
import br.com.invplus.modules.financeiro.enums.TipoMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.relatorio.dto.DreFinanceiraResponse;
import br.com.invplus.modules.financeiro.relatorio.dto.DreLinhaResponse;
import br.com.invplus.modules.financeiro.relatorio.dto.FinanceiroDashboardResponse;
import br.com.invplus.modules.financeiro.relatorio.dto.FluxoCaixaResumoResponse;
import br.com.invplus.modules.financeiro.repository.ContaPagarRepository;
import br.com.invplus.modules.financeiro.repository.ContaReceberRepository;
import br.com.invplus.modules.financeiro.repository.MovimentoFinanceiroRepository;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.invplus.security.context.EmpresaContext;
import br.com.invplus.security.exception.AcessoEmpresaNegadoException;

/**
 * Consolida indicadores financeiros por meio de consultas agregadas no banco.
 */
@Service
@RequiredArgsConstructor
public class FinanceiroResumoService {

    private final MovimentoFinanceiroRepository movimentoRepository;
    private final ContaReceberRepository contaReceberRepository;
    private final ContaPagarRepository contaPagarRepository;
    private final EmpresaContext empresaContext;

    /**
     * Calcula entradas, saidas e saldo para um intervalo inclusivo.
     */
    @Transactional(readOnly = true)
    public FluxoCaixaResumoResponse fluxoCaixa(LocalDate dataInicio, LocalDate dataFim) {
        return fluxoCaixa(dataInicio, dataFim, null, null);
    }

    /**
     * Calcula o fluxo restringindo opcionalmente a um centro de custo.
     */
    @Transactional(readOnly = true)
    public FluxoCaixaResumoResponse fluxoCaixa(
            LocalDate dataInicio, LocalDate dataFim, UUID centroCustoId) {
        return fluxoCaixa(dataInicio, dataFim, centroCustoId, null);
    }

    /**
     * Calcula o fluxo restringindo opcionalmente por centro de custo e plano de conta.
     */
    @Transactional(readOnly = true)
    public FluxoCaixaResumoResponse fluxoCaixa(
            LocalDate dataInicio, LocalDate dataFim, UUID centroCustoId, UUID planoContaId) {
        validarPeriodo(dataInicio, dataFim);
        BigDecimal entradas = somarMovimentos(TipoMovimentoFinanceiro.ENTRADA,
                dataInicio, dataFim, centroCustoId, planoContaId);
        BigDecimal saidas = somarMovimentos(TipoMovimentoFinanceiro.SAIDA,
                dataInicio, dataFim, centroCustoId, planoContaId);
        return new FluxoCaixaResumoResponse(entradas, saidas, entradas.subtract(saidas), dataInicio, dataFim);
    }

    /**
     * Reune saldos em aberto, fluxo do periodo e quantidade de titulos vencidos.
     */
    @Transactional(readOnly = true)
    public FinanceiroDashboardResponse dashboard(LocalDate dataInicio, LocalDate dataFim) {
        return dashboard(dataInicio, dataFim, null, null);
    }

    /**
     * Monta o dashboard global ou filtrado por classificacao gerencial.
     */
    @Transactional(readOnly = true)
    public FinanceiroDashboardResponse dashboard(
            LocalDate dataInicio, LocalDate dataFim, UUID centroCustoId) {
        return dashboard(dataInicio, dataFim, centroCustoId, null);
    }

    /**
     * Monta o dashboard global ou filtrado por classificacoes gerenciais e contabeis.
     */
    @Transactional(readOnly = true)
    public FinanceiroDashboardResponse dashboard(
            LocalDate dataInicio, LocalDate dataFim, UUID centroCustoId, UUID planoContaId) {
        FluxoCaixaResumoResponse fluxo = fluxoCaixa(dataInicio, dataFim, centroCustoId, planoContaId);
        return new FinanceiroDashboardResponse(
                somarContasReceber(centroCustoId, planoContaId),
                somarContasPagar(centroCustoId, planoContaId),
                fluxo.totalEntradas(),
                fluxo.totalSaidas(),
                fluxo.saldo(),
                contarReceberVencidas(centroCustoId, planoContaId),
                contarPagarVencidas(centroCustoId, planoContaId)
        );
    }

    /**
     * Gera uma DRE financeira simples agrupando receitas e despesas por plano de conta.
     */
    @Transactional(readOnly = true)
    public DreFinanceiraResponse gerarDre(LocalDate dataInicio, LocalDate dataFim,
            UUID filialId, UUID centroCustoId, UUID planoContaId) {
        return gerarDre(dataInicio, dataFim, empresaContext.getEmpresaId(), filialId, centroCustoId, planoContaId);
    }

    @Transactional(readOnly = true)
    public DreFinanceiraResponse gerarDre(LocalDate dataInicio, LocalDate dataFim,
            UUID empresaId, UUID filialId, UUID centroCustoId, UUID planoContaId) {
        validarPeriodo(dataInicio, dataFim);
        empresaId = resolverEmpresa(empresaId);
        List<DreLinhaResponse> receitas = toLinhas(movimentoRepository.somarReceitasPorPlanoConta(
                dataInicio, dataFim, empresaId, filialId, centroCustoId, planoContaId));
        List<DreLinhaResponse> despesas = toLinhas(movimentoRepository.somarDespesasPorPlanoConta(
                dataInicio, dataFim, empresaId, filialId, centroCustoId, planoContaId));
        BigDecimal receitaBruta = somarLinhas(receitas);
        BigDecimal despesasOperacionais = somarLinhas(despesas);
        BigDecimal resultadoOperacional = receitaBruta.subtract(despesasOperacionais);

        return new DreFinanceiraResponse(dataInicio, dataFim, receitaBruta, despesasOperacionais,
                resultadoOperacional, resultadoOperacional, receitas, despesas);
    }

    private BigDecimal somarMovimentos(TipoMovimentoFinanceiro tipo, LocalDate inicio,
            LocalDate fim, UUID centroCustoId, UUID planoContaId) {
        UUID empresaId = empresaAtualOuNull();
        if (empresaId != null) return zeroSeNulo(movimentoRepository.somarPorEmpresaTipoPeriodo(
                empresaId, tipo, inicio, fim, centroCustoId, planoContaId));
        BigDecimal total;
        if (centroCustoId != null && planoContaId != null) {
            total = movimentoRepository.somarPorTipoNoPeriodoECentroCustoEPlanoConta(
                    tipo, inicio, fim, centroCustoId, planoContaId);
        } else if (centroCustoId != null) {
            total = movimentoRepository.somarPorTipoNoPeriodoECentroCusto(tipo, inicio, fim, centroCustoId);
        } else if (planoContaId != null) {
            total = movimentoRepository.somarPorTipoNoPeriodoEPlanoConta(tipo, inicio, fim, planoContaId);
        } else {
            total = movimentoRepository.somarPorTipoNoPeriodo(tipo, inicio, fim);
        }
        return zeroSeNulo(total);
    }

    private BigDecimal somarContasReceber(UUID centroCustoId, UUID planoContaId) {
        UUID empresaId = empresaAtualOuNull();
        if (empresaId != null) return zeroSeNulo(contaReceberRepository.somarValorAbertoPorEmpresa(
                empresaId, centroCustoId, planoContaId));
        if (centroCustoId != null && planoContaId != null) {
            return zeroSeNulo(contaReceberRepository.somarValorAbertoPorCentroCustoEPlanoConta(
                    centroCustoId, planoContaId));
        }
        if (centroCustoId != null) {
            return zeroSeNulo(contaReceberRepository.somarValorAbertoPorCentroCusto(centroCustoId));
        }
        if (planoContaId != null) {
            return zeroSeNulo(contaReceberRepository.somarValorAbertoPorPlanoConta(planoContaId));
        }
        return zeroSeNulo(contaReceberRepository.somarValorAberto());
    }

    private BigDecimal somarContasPagar(UUID centroCustoId, UUID planoContaId) {
        UUID empresaId = empresaAtualOuNull();
        if (empresaId != null) return zeroSeNulo(contaPagarRepository.somarValorAbertoPorEmpresa(
                empresaId, centroCustoId, planoContaId));
        if (centroCustoId != null && planoContaId != null) {
            return zeroSeNulo(contaPagarRepository.somarValorAbertoPorCentroCustoEPlanoConta(
                    centroCustoId, planoContaId));
        }
        if (centroCustoId != null) {
            return zeroSeNulo(contaPagarRepository.somarValorAbertoPorCentroCusto(centroCustoId));
        }
        if (planoContaId != null) {
            return zeroSeNulo(contaPagarRepository.somarValorAbertoPorPlanoConta(planoContaId));
        }
        return zeroSeNulo(contaPagarRepository.somarValorAberto());
    }

    private long contarReceberVencidas(UUID centroCustoId, UUID planoContaId) {
        UUID empresaId = empresaAtualOuNull();
        if (empresaId != null) return contaReceberRepository.contarPorEmpresaStatus(
                empresaId, StatusContaReceber.VENCIDA, centroCustoId, planoContaId);
        if (centroCustoId != null && planoContaId != null) {
            return contaReceberRepository.countByStatusAndCentroCustoIdAndPlanoContaIdAndAtivoTrueAndDeletedAtIsNull(
                    StatusContaReceber.VENCIDA, centroCustoId, planoContaId);
        }
        if (centroCustoId != null) {
            return contaReceberRepository.countByStatusAndCentroCustoIdAndAtivoTrueAndDeletedAtIsNull(
                    StatusContaReceber.VENCIDA, centroCustoId);
        }
        if (planoContaId != null) {
            return contaReceberRepository.countByStatusAndPlanoContaIdAndAtivoTrueAndDeletedAtIsNull(
                    StatusContaReceber.VENCIDA, planoContaId);
        }
        return contaReceberRepository.countByStatusAndAtivoTrueAndDeletedAtIsNull(StatusContaReceber.VENCIDA);
    }

    private long contarPagarVencidas(UUID centroCustoId, UUID planoContaId) {
        UUID empresaId = empresaAtualOuNull();
        if (empresaId != null) return contaPagarRepository.contarPorEmpresaStatus(
                empresaId, StatusContaPagar.VENCIDA, centroCustoId, planoContaId);
        if (centroCustoId != null && planoContaId != null) {
            return contaPagarRepository.countByStatusAndCentroCustoIdAndPlanoContaIdAndAtivoTrueAndDeletedAtIsNull(
                    StatusContaPagar.VENCIDA, centroCustoId, planoContaId);
        }
        if (centroCustoId != null) {
            return contaPagarRepository.countByStatusAndCentroCustoIdAndAtivoTrueAndDeletedAtIsNull(
                    StatusContaPagar.VENCIDA, centroCustoId);
        }
        if (planoContaId != null) {
            return contaPagarRepository.countByStatusAndPlanoContaIdAndAtivoTrueAndDeletedAtIsNull(
                    StatusContaPagar.VENCIDA, planoContaId);
        }
        return contaPagarRepository.countByStatusAndAtivoTrueAndDeletedAtIsNull(StatusContaPagar.VENCIDA);
    }

    private List<DreLinhaResponse> toLinhas(List<Object[]> linhas) {
        return linhas.stream()
                .map(this::toLinha)
                .toList();
    }

    private DreLinhaResponse toLinha(Object[] linha) {
        return new DreLinhaResponse((String) linha[0], (String) linha[1], bigDecimal(linha[2]));
    }

    private BigDecimal somarLinhas(List<DreLinhaResponse> linhas) {
        return linhas.stream()
                .map(DreLinhaResponse::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal bigDecimal(Object valor) {
        if (valor == null) {
            return BigDecimal.ZERO;
        }
        if (valor instanceof BigDecimal decimal) {
            return decimal;
        }
        if (valor instanceof Number numero) {
            return BigDecimal.valueOf(numero.doubleValue());
        }
        return new BigDecimal(valor.toString());
    }

    private void validarPeriodo(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null) {
            throw new InvalidOperationException("Data inicial e data final sao obrigatorias");
        }
        if (fim.isBefore(inicio)) {
            throw new InvalidOperationException("Data final nao pode ser anterior a data inicial");
        }
    }

    private BigDecimal zeroSeNulo(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private UUID resolverEmpresa(UUID recebida) {
        if (empresaContext == null) return recebida;
        UUID contexto = empresaAtualOuNull();
        if (contexto == null) return recebida;
        if (recebida != null && !contexto.equals(recebida)) throw new AcessoEmpresaNegadoException();
        return contexto;
    }

    private UUID empresaAtualOuNull() {
        if (empresaContext == null) return null;
        try { return empresaContext.getEmpresaId(); }
        catch (br.com.invplus.security.exception.ContextoEmpresarialAusenteException
                | org.springframework.security.authentication.AuthenticationCredentialsNotFoundException ex) { return null; }
    }
}
