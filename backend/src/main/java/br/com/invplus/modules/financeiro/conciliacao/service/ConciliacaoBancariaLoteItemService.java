package br.com.invplus.modules.financeiro.conciliacao.service;

import br.com.invplus.modules.financeiro.conciliacao.dto.*;
import br.com.invplus.modules.financeiro.dto.request.ConciliacaoBancariaRequest;
import br.com.invplus.modules.financeiro.enums.*;
import br.com.invplus.modules.financeiro.repository.ExtratoBancarioLancamentoRepository;
import br.com.invplus.modules.financeiro.service.ConciliacaoBancariaService;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Processa um item por transacao independente para permitir sucesso parcial. */
@Service
@RequiredArgsConstructor
public class ConciliacaoBancariaLoteItemService {
    private final ConciliacaoBancariaService conciliacaoService;
    private final ExtratoBancarioLancamentoRepository lancamentoRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ConciliacaoBancariaLoteItemResponse processar(UUID empresaId,
            ConciliacaoBancariaLoteItemRequest item) {
        lancamentoRepository
                .findByIdAndExtratoBancarioContaBancariaEmpresaIdAndAtivoTrueAndDeletedAtIsNull(
                        item.lancamentoBancarioId(), empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Lancamento bancario nao encontrado"));
        var response = conciliacaoService.criar(new ConciliacaoBancariaRequest(
                item.lancamentoBancarioId(), item.movimentoFinanceiroId(),
                TipoConciliacaoBancaria.MANUAL, item.observacao()));
        return new ConciliacaoBancariaLoteItemResponse(item.lancamentoBancarioId(),
                item.movimentoFinanceiroId(), response.id(), StatusResultadoConciliacaoLote.CONCILIADO,
                null, "Conciliado com sucesso");
    }
}
