package br.com.invplus.modules.financeiro.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.financeiro.dto.request.MovimentoFinanceiroRequest;
import br.com.invplus.modules.financeiro.dto.response.MovimentoFinanceiroResponse;
import br.com.invplus.modules.financeiro.enums.OrigemMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.StatusMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.TipoMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.service.MovimentoFinanceiroService;
import br.com.invplus.shared.api.controller.BaseCrudController;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API paginada para consulta e lancamento manual de movimentos financeiros.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/movimentos-financeiros")
@RequiredArgsConstructor
public class MovimentoFinanceiroController extends BaseCrudController {

    private final MovimentoFinanceiroService service;

    @PostMapping
    @PreAuthorize("hasAuthority('MOVIMENTO_FINANCEIRO:CRIAR') or hasAuthority('MOVIMENTO_FINANCEIRO:*')")
    public ResponseEntity<MovimentoFinanceiroResponse> criar(@Valid @RequestBody MovimentoFinanceiroRequest request) {
        MovimentoFinanceiroResponse response = service.criarManual(request);
        return created(AppConstants.API_BASE_PATH + "/movimentos-financeiros", response.id(), response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MOVIMENTO_FINANCEIRO:VISUALIZAR') or hasAuthority('MOVIMENTO_FINANCEIRO:*')")
    public ResponseEntity<Page<MovimentoFinanceiroResponse>> listar(
            @RequestParam(required = false) UUID filialId,
            @RequestParam(required = false) TipoMovimentoFinanceiro tipoMovimento,
            @RequestParam(required = false) OrigemMovimentoFinanceiro origem,
            @RequestParam(required = false) UUID origemId,
            @RequestParam(required = false) UUID formaPagamentoId,
            @RequestParam(required = false) UUID clienteId,
            @RequestParam(required = false) UUID fornecedorId,
            @RequestParam(required = false) UUID centroCustoId,
            @RequestParam(required = false) UUID planoContaId,
            @RequestParam(required = false) StatusMovimentoFinanceiro status,
            @RequestParam(required = false) LocalDate dataMovimentoDe,
            @RequestParam(required = false) LocalDate dataMovimentoAte,
            @RequestParam(required = false) Boolean ativo,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.listarComFiltros(filialId, tipoMovimento, origem, origemId,
                formaPagamentoId, clienteId, fornecedorId, centroCustoId, planoContaId, status, dataMovimentoDe, dataMovimentoAte,
                ativo, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MOVIMENTO_FINANCEIRO:VISUALIZAR') or hasAuthority('MOVIMENTO_FINANCEIRO:*')")
    public ResponseEntity<MovimentoFinanceiroResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('MOVIMENTO_FINANCEIRO:CANCELAR') or hasAuthority('MOVIMENTO_FINANCEIRO:*')")
    public ResponseEntity<MovimentoFinanceiroResponse> cancelar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.cancelar(id));
    }
}
