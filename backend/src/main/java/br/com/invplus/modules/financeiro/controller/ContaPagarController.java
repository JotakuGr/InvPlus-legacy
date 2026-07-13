package br.com.invplus.modules.financeiro.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.financeiro.dto.request.ContaPagarRequest;
import br.com.invplus.modules.financeiro.dto.request.GerarParcelasPagarRequest;
import br.com.invplus.modules.financeiro.dto.response.ContaPagarResponse;
import br.com.invplus.modules.financeiro.dto.response.ParcelaPagarResponse;
import br.com.invplus.modules.financeiro.enums.StatusContaPagar;
import br.com.invplus.modules.financeiro.service.ContaPagarService;
import br.com.invplus.modules.financeiro.service.ParcelaPagarService;
import br.com.invplus.shared.api.controller.BaseCrudController;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
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
 * Endpoints REST para manutencao e consulta de contas a pagar.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/contas-pagar")
@RequiredArgsConstructor
public class ContaPagarController extends BaseCrudController {

    private final ContaPagarService service;
    private final ParcelaPagarService parcelaPagarService;

    @PostMapping
    @PreAuthorize("hasAuthority('CONTA_PAGAR:CRIAR') or hasAuthority('CONTA_PAGAR:*')")
    public ResponseEntity<ContaPagarResponse> criar(@Valid @RequestBody ContaPagarRequest request) {
        ContaPagarResponse response = service.criar(request);
        return created(AppConstants.API_BASE_PATH + "/contas-pagar", response.id(), response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CONTA_PAGAR:VISUALIZAR') or hasAuthority('CONTA_PAGAR:*')")
    public ResponseEntity<Page<ContaPagarResponse>> listar(
            @RequestParam(required = false) UUID filialId,
            @RequestParam(required = false) UUID fornecedorId,
            @RequestParam(required = false) UUID entradaMercadoriaId,
            @RequestParam(required = false) UUID centroCustoId,
            @RequestParam(required = false) UUID planoContaId,
            @RequestParam(required = false) String numero,
            @RequestParam(required = false) StatusContaPagar status,
            @RequestParam(required = false) LocalDate dataEmissaoDe,
            @RequestParam(required = false) LocalDate dataEmissaoAte,
            @RequestParam(required = false) Boolean ativo,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.listarComFiltros(filialId, fornecedorId, entradaMercadoriaId,
                centroCustoId, planoContaId, numero, status, dataEmissaoDe, dataEmissaoAte, ativo, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTA_PAGAR:VISUALIZAR') or hasAuthority('CONTA_PAGAR:*')")
    public ResponseEntity<ContaPagarResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTA_PAGAR:EDITAR') or hasAuthority('CONTA_PAGAR:*')")
    public ResponseEntity<ContaPagarResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ContaPagarRequest request
    ) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('CONTA_PAGAR:CANCELAR') or hasAuthority('CONTA_PAGAR:*')")
    public ResponseEntity<ContaPagarResponse> cancelar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.cancelar(id));
    }

    @PostMapping("/{id}/gerar-parcelas")
    @PreAuthorize("hasAuthority('PARCELA_PAGAR:CRIAR') or hasAuthority('PARCELA_PAGAR:*') or hasAuthority('CONTA_PAGAR:*')")
    public ResponseEntity<List<ParcelaPagarResponse>> gerarParcelas(
            @PathVariable UUID id,
            @Valid @RequestBody GerarParcelasPagarRequest request
    ) {
        return ResponseEntity.ok(parcelaPagarService.gerarParcelas(id, request));
    }
}
