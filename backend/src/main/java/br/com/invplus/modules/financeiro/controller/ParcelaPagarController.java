package br.com.invplus.modules.financeiro.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.financeiro.dto.request.ParcelaPagarBaixaRequest;
import br.com.invplus.modules.financeiro.dto.request.ParcelaPagarRequest;
import br.com.invplus.modules.financeiro.dto.response.ParcelaPagarResponse;
import br.com.invplus.modules.financeiro.enums.StatusParcelaPagar;
import br.com.invplus.modules.financeiro.service.ParcelaPagarService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints REST das parcelas a pagar.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/parcelas-pagar")
@RequiredArgsConstructor
public class ParcelaPagarController extends BaseCrudController {

    private final ParcelaPagarService service;

    @PostMapping
    @PreAuthorize("hasAuthority('PARCELA_PAGAR:CRIAR') or hasAuthority('PARCELA_PAGAR:*')")
    public ResponseEntity<ParcelaPagarResponse> criar(@Valid @RequestBody ParcelaPagarRequest request) {
        ParcelaPagarResponse response = service.criar(request);
        return created(AppConstants.API_BASE_PATH + "/parcelas-pagar", response.id(), response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PARCELA_PAGAR:VISUALIZAR') or hasAuthority('PARCELA_PAGAR:*')")
    public ResponseEntity<Page<ParcelaPagarResponse>> listar(
            @RequestParam(required = false) UUID contaPagarId,
            @RequestParam(required = false) StatusParcelaPagar status,
            @RequestParam(required = false) LocalDate dataVencimentoDe,
            @RequestParam(required = false) LocalDate dataVencimentoAte,
            @RequestParam(required = false) LocalDate dataPagamentoDe,
            @RequestParam(required = false) LocalDate dataPagamentoAte,
            @RequestParam(required = false) Boolean ativo,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.listarComFiltros(contaPagarId, status, dataVencimentoDe,
                dataVencimentoAte, dataPagamentoDe, dataPagamentoAte, ativo, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PARCELA_PAGAR:VISUALIZAR') or hasAuthority('PARCELA_PAGAR:*')")
    public ResponseEntity<ParcelaPagarResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PARCELA_PAGAR:EDITAR') or hasAuthority('PARCELA_PAGAR:*')")
    public ResponseEntity<ParcelaPagarResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ParcelaPagarRequest request
    ) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @PostMapping("/{id}/baixar")
    @PreAuthorize("hasAuthority('PARCELA_PAGAR:BAIXAR') or hasAuthority('PARCELA_PAGAR:*')")
    public ResponseEntity<ParcelaPagarResponse> baixar(
            @PathVariable UUID id,
            @Valid @RequestBody ParcelaPagarBaixaRequest request
    ) {
        return ResponseEntity.ok(service.baixar(id, request));
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('PARCELA_PAGAR:CANCELAR') or hasAuthority('PARCELA_PAGAR:*')")
    public ResponseEntity<ParcelaPagarResponse> cancelar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.cancelar(id));
    }
}
