package br.com.invplus.modules.financeiro.conciliacao.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.financeiro.conciliacao.dto.*;
import br.com.invplus.modules.financeiro.conciliacao.service.ConciliacaoBancariaPainelService;
import br.com.invplus.modules.financeiro.enums.*;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/conciliacoes-bancarias")
@RequiredArgsConstructor
public class ConciliacaoBancariaPainelController {
    private final ConciliacaoBancariaPainelService service;

    @GetMapping("/painel")
    @PreAuthorize("hasAnyAuthority('CONCILIACAO_BANCARIA:*','CONCILIACAO_BANCARIA:PAINEL')")
    public ResponseEntity<PainelConciliacaoBancariaResponse> painel(
            @RequestParam(required = false) UUID empresaId, @RequestParam(required = false) UUID contaBancariaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        return ResponseEntity.ok(service.buscarPainel(empresaId, contaBancariaId, dataInicio, dataFim));
    }

    @GetMapping("/extratos-pendentes")
    @PreAuthorize("hasAnyAuthority('CONCILIACAO_BANCARIA:*','CONCILIACAO_BANCARIA:PAINEL')")
    public ResponseEntity<Page<ExtratoPendenteResponse>> extratos(
            @RequestParam(required = false) UUID empresaId, @RequestParam(required = false) UUID contaBancariaId,
            @RequestParam(required = false) StatusExtratoBancario status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            Pageable pageable) {
        return ResponseEntity.ok(service.buscarExtratosPendentes(
                empresaId, contaBancariaId, status, dataInicio, dataFim, pageable));
    }

    @GetMapping("/pendencias")
    @PreAuthorize("hasAnyAuthority('CONCILIACAO_BANCARIA:*','CONCILIACAO_BANCARIA:PAINEL')")
    public ResponseEntity<Page<LancamentoPendenteResponse>> pendencias(
            @RequestParam(required = false) UUID empresaId, @RequestParam(required = false) UUID extratoBancarioId,
            @RequestParam(required = false) UUID contaBancariaId,
            @RequestParam(required = false) TipoLancamentoBancario tipo,
            @RequestParam(required = false) CategoriaLancamentoBancario categoria,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) Boolean possuiSugestao,
            @RequestParam(required = false) NivelConfiancaConciliacao nivelConfianca,
            Pageable pageable) {
        return ResponseEntity.ok(service.buscarPendencias(empresaId, extratoBancarioId, contaBancariaId,
                tipo, categoria, dataInicio, dataFim, possuiSugestao, nivelConfianca, pageable));
    }
}
