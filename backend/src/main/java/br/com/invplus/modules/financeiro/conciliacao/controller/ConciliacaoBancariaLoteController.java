package br.com.invplus.modules.financeiro.conciliacao.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.financeiro.conciliacao.dto.*;
import br.com.invplus.modules.financeiro.conciliacao.service.ConciliacaoBancariaLoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/conciliacoes-bancarias")
@RequiredArgsConstructor
public class ConciliacaoBancariaLoteController {
    private final ConciliacaoBancariaLoteService service;

    @PostMapping("/confirmar-lote")
    @PreAuthorize("hasAnyAuthority('CONCILIACAO_BANCARIA:*','CONCILIACAO_BANCARIA:CONCILIAR_LOTE')")
    public ResponseEntity<ConciliacaoBancariaLoteResponse> confirmar(
            @Valid @RequestBody ConciliacaoBancariaLoteRequest request) {
        return ResponseEntity.ok(service.confirmar(request));
    }
}
