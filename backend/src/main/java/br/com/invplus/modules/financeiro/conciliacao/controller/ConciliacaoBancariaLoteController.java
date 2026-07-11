package br.com.invplus.modules.financeiro.conciliacao.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.financeiro.conciliacao.dto.*;
import br.com.invplus.modules.financeiro.conciliacao.service.ConciliacaoBancariaLoteService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import br.com.invplus.security.context.EmpresaContext;
import br.com.invplus.security.exception.AcessoEmpresaNegadoException;

@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/conciliacoes-bancarias")
@RequiredArgsConstructor
public class ConciliacaoBancariaLoteController {
    private final ConciliacaoBancariaLoteService service;
    private final EmpresaContext empresaContext;

    @PostMapping("/confirmar-lote")
    @PreAuthorize("hasAnyAuthority('CONCILIACAO_BANCARIA:*','CONCILIACAO_BANCARIA:CONCILIAR_LOTE')")
    public ResponseEntity<ConciliacaoBancariaLoteResponse> confirmar(
            @RequestParam(required = false) UUID empresaId,
            @Valid @RequestBody ConciliacaoBancariaLoteRequest request) {
        UUID contexto=empresaContext.getEmpresaId();
        if(empresaId!=null&&!contexto.equals(empresaId))throw new AcessoEmpresaNegadoException();
        return ResponseEntity.ok(service.confirmar(contexto, request));
    }
}
