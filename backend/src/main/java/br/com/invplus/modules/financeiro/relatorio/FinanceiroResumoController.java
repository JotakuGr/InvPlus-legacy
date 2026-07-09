package br.com.invplus.modules.financeiro.relatorio;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.financeiro.relatorio.dto.DreFinanceiraResponse;
import br.com.invplus.modules.financeiro.relatorio.dto.FinanceiroDashboardResponse;
import br.com.invplus.modules.financeiro.relatorio.dto.FluxoCaixaResumoResponse;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de leitura para fluxo de caixa e dashboard financeiro.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/financeiro/relatorios")
@RequiredArgsConstructor
public class FinanceiroResumoController {

    private final FinanceiroResumoService service;

    @GetMapping("/fluxo-caixa")
    @PreAuthorize("hasAnyAuthority('RELATORIO_FINANCEIRO:*', 'RELATORIO_FINANCEIRO:VISUALIZAR')")
    public ResponseEntity<FluxoCaixaResumoResponse> fluxoCaixa(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) UUID centroCustoId,
            @RequestParam(required = false) UUID planoContaId) {
        return ResponseEntity.ok(service.fluxoCaixa(dataInicio, dataFim, centroCustoId, planoContaId));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority('RELATORIO_FINANCEIRO:*', 'RELATORIO_FINANCEIRO:VISUALIZAR')")
    public ResponseEntity<FinanceiroDashboardResponse> dashboard(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) UUID centroCustoId,
            @RequestParam(required = false) UUID planoContaId) {
        return ResponseEntity.ok(service.dashboard(dataInicio, dataFim, centroCustoId, planoContaId));
    }

    @GetMapping("/dre")
    @PreAuthorize("hasAnyAuthority('RELATORIO_FINANCEIRO:*', 'RELATORIO_FINANCEIRO:VISUALIZAR')")
    public ResponseEntity<DreFinanceiraResponse> dre(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) UUID empresaId,
            @RequestParam(required = false) UUID filialId,
            @RequestParam(required = false) UUID centroCustoId,
            @RequestParam(required = false) UUID planoContaId) {
        return ResponseEntity.ok(service.gerarDre(dataInicio, dataFim,
                empresaId, filialId, centroCustoId, planoContaId));
    }
}
