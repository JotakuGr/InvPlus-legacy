package br.com.invplus.modules.financeiro.conciliacao.service;

import br.com.invplus.modules.financeiro.conciliacao.dto.*;
import br.com.invplus.modules.financeiro.enums.StatusResultadoConciliacaoLote;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Valida a estrutura e agrega resultados de transacoes independentes. */
@Service
@RequiredArgsConstructor
public class ConciliacaoBancariaLoteService {
    private final br.com.invplus.security.context.EmpresaContext empresaContext;
    private final ConciliacaoBancariaLoteItemService itemService;

    public ConciliacaoBancariaLoteResponse confirmar(ConciliacaoBancariaLoteRequest request) {
        UUID empresaId = empresaContext.getEmpresaId();
        validarEstrutura(request);
        List<ConciliacaoBancariaLoteItemResponse> resultados = new ArrayList<>();
        for (var item : request.itens()) {
            try {
                resultados.add(itemService.processar(empresaId, item));
            } catch (RuntimeException ex) {
                resultados.add(rejeitado(item, ex));
            }
        }
        int conciliados = (int) resultados.stream()
                .filter(r -> r.status() == StatusResultadoConciliacaoLote.CONCILIADO).count();
        return new ConciliacaoBancariaLoteResponse(resultados.size(), conciliados,
                resultados.size() - conciliados, List.copyOf(resultados));
    }
    private void validarEstrutura(ConciliacaoBancariaLoteRequest request) {
        if (request == null || request.itens() == null || request.itens().isEmpty())
            throw new InvalidOperationException("Lote deve possuir ao menos um item");
        if (request.itens().size() > 100)
            throw new InvalidOperationException("Lote deve possuir no maximo 100 itens");
        Set<UUID> lancamentos = new HashSet<>();
        Set<UUID> movimentos = new HashSet<>();
        for (var item : request.itens()) {
            if (item == null || item.lancamentoBancarioId() == null || item.movimentoFinanceiroId() == null)
                throw new InvalidOperationException("Itens do lote devem informar lancamento e movimento");
            if (!lancamentos.add(item.lancamentoBancarioId()))
                throw new InvalidOperationException("Lancamento bancario repetido no lote");
            if (!movimentos.add(item.movimentoFinanceiroId()))
                throw new InvalidOperationException("Movimento financeiro repetido no lote");
        }
    }

    private ConciliacaoBancariaLoteItemResponse rejeitado(
            ConciliacaoBancariaLoteItemRequest item, RuntimeException ex) {
        return new ConciliacaoBancariaLoteItemResponse(item.lancamentoBancarioId(),
                item.movimentoFinanceiroId(), null, StatusResultadoConciliacaoLote.REJEITADO,
                codigo(ex), ex.getMessage());
    }

    private String codigo(RuntimeException ex) {
        String mensagem = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase(Locale.ROOT);
        if (mensagem.contains("lancamento") && mensagem.contains("conciliado")) return "LANCAMENTO_JA_CONCILIADO";
        if (mensagem.contains("movimento") && mensagem.contains("conciliado")) return "MOVIMENTO_JA_CONCILIADO";
        if (mensagem.contains("valor")) return "VALOR_DIVERGENTE";
        if (mensagem.contains("tipo")) return "TIPO_INCOMPATIVEL";
        if (mensagem.contains("conta bancaria")) return "CONTA_DIVERGENTE";
        if (mensagem.contains("extrato") && mensagem.contains("cancelado")) return "EXTRATO_CANCELADO";
        if (mensagem.contains("movimento") && mensagem.contains("ativo")) return "MOVIMENTO_CANCELADO";
        if (ex instanceof br.com.invplus.shared.domain.exception.ResourceNotFoundException)
            return "RECURSO_NAO_ENCONTRADO";
        if (ex instanceof br.com.invplus.shared.domain.exception.ConflictException
                || ex instanceof org.springframework.dao.DataIntegrityViolationException
                || ex instanceof org.springframework.orm.ObjectOptimisticLockingFailureException)
            return "CONFLITO_CONCORRENTE";
        return "REGRA_NEGOCIO";
    }
}
