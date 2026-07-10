package br.com.invplus.modules.financeiro.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.financeiro.dto.request.ConciliacaoBancariaCancelamentoRequest;
import br.com.invplus.modules.financeiro.dto.request.ConciliacaoBancariaRequest;
import br.com.invplus.modules.financeiro.dto.response.ConciliacaoBancariaResponse;
import br.com.invplus.modules.financeiro.enums.StatusConciliacaoBancaria;
import br.com.invplus.modules.financeiro.enums.TipoConciliacaoBancaria;
import br.com.invplus.modules.financeiro.service.ConciliacaoBancariaService;
import br.com.invplus.shared.api.controller.BaseCrudController;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** API imutavel de criacao, consulta e cancelamento de conciliacoes. */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/conciliacoes-bancarias")
@RequiredArgsConstructor
public class ConciliacaoBancariaController extends BaseCrudController {

    private final ConciliacaoBancariaService service;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('CONCILIACAO_BANCARIA:*', 'CONCILIACAO_BANCARIA:CRIAR')")
    public ResponseEntity<ConciliacaoBancariaResponse> criar(
            @Valid @RequestBody ConciliacaoBancariaRequest request) {
        ConciliacaoBancariaResponse response = service.criar(request);
        return created(AppConstants.API_BASE_PATH + "/conciliacoes-bancarias", response.id(), response);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('CONCILIACAO_BANCARIA:*', 'CONCILIACAO_BANCARIA:VISUALIZAR')")
    public ResponseEntity<Page<ConciliacaoBancariaResponse>> listar(
            @RequestParam(required = false) UUID extratoBancarioLancamentoId,
            @RequestParam(required = false) UUID movimentoFinanceiroId,
            @RequestParam(required = false) UUID contaBancariaId,
            @RequestParam(required = false) TipoConciliacaoBancaria tipoConciliacao,
            @RequestParam(required = false) StatusConciliacaoBancaria status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime conciliadoEmDe,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime conciliadoEmAte,
            Pageable pageable) {
        return ResponseEntity.ok(service.listar(extratoBancarioLancamentoId, movimentoFinanceiroId,
                contaBancariaId, tipoConciliacao, status, conciliadoEmDe, conciliadoEmAte, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CONCILIACAO_BANCARIA:*', 'CONCILIACAO_BANCARIA:VISUALIZAR')")
    public ResponseEntity<ConciliacaoBancariaResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyAuthority('CONCILIACAO_BANCARIA:*', 'CONCILIACAO_BANCARIA:CANCELAR')")
    public ResponseEntity<ConciliacaoBancariaResponse> cancelar(@PathVariable UUID id,
            @Valid @RequestBody ConciliacaoBancariaCancelamentoRequest request) {
        return ResponseEntity.ok(service.cancelar(id, request));
    }
}
