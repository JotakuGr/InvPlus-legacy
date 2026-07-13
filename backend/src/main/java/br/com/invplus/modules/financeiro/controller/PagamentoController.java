package br.com.invplus.modules.financeiro.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.financeiro.dto.request.PagamentoRequest;
import br.com.invplus.modules.financeiro.dto.response.PagamentoResponse;
import br.com.invplus.modules.financeiro.enums.StatusPagamentoFinanceiro;
import br.com.invplus.modules.financeiro.service.PagamentoService;
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
 * Endpoints REST para registro e consulta de pagamentos financeiros.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/pagamentos")
@RequiredArgsConstructor
public class PagamentoController extends BaseCrudController {

    private final PagamentoService service;

    @PostMapping
    @PreAuthorize("hasAuthority('PAGAMENTO:CRIAR') or hasAuthority('PAGAMENTO:*')")
    public ResponseEntity<PagamentoResponse> criar(@Valid @RequestBody PagamentoRequest request) {
        PagamentoResponse response = service.criar(request);
        return created(AppConstants.API_BASE_PATH + "/pagamentos", response.id(), response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PAGAMENTO:VISUALIZAR') or hasAuthority('PAGAMENTO:*')")
    public ResponseEntity<Page<PagamentoResponse>> listar(
            @RequestParam(required = false) UUID parcelaPagarId,
            @RequestParam(required = false) UUID contaPagarId,
            @RequestParam(required = false) UUID formaPagamentoId,
            @RequestParam(required = false) UUID filialId,
            @RequestParam(required = false) UUID fornecedorId,
            @RequestParam(required = false) StatusPagamentoFinanceiro status,
            @RequestParam(required = false) LocalDate dataPagamentoDe,
            @RequestParam(required = false) LocalDate dataPagamentoAte,
            @RequestParam(required = false) Boolean ativo,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.listarComFiltros(parcelaPagarId, contaPagarId, formaPagamentoId,
                null, filialId, fornecedorId, status, dataPagamentoDe, dataPagamentoAte, ativo, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PAGAMENTO:VISUALIZAR') or hasAuthority('PAGAMENTO:*')")
    public ResponseEntity<PagamentoResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('PAGAMENTO:CANCELAR') or hasAuthority('PAGAMENTO:*')")
    public ResponseEntity<PagamentoResponse> cancelar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.cancelar(id));
    }
}
