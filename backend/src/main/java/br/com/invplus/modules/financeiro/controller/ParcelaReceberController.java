package br.com.invplus.modules.financeiro.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.financeiro.dto.request.BaixaParcelaReceberRequest;
import br.com.invplus.modules.financeiro.dto.request.ParcelaReceberRequest;
import br.com.invplus.modules.financeiro.dto.response.ParcelaReceberResponse;
import br.com.invplus.modules.financeiro.enums.StatusParcelaReceber;
import br.com.invplus.modules.financeiro.service.ParcelaReceberService;
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
 * Endpoints REST para parcelas a receber.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/parcelas-receber")
@RequiredArgsConstructor
public class ParcelaReceberController extends BaseCrudController {

    private final ParcelaReceberService service;

    @PostMapping
    @PreAuthorize("hasAuthority('PARCELA_RECEBER:CRIAR') or hasAuthority('PARCELA_RECEBER:*')")
    public ResponseEntity<ParcelaReceberResponse> criar(@Valid @RequestBody ParcelaReceberRequest request) {
        ParcelaReceberResponse response = service.criar(request);
        return created(AppConstants.API_BASE_PATH + "/parcelas-receber", response.id(), response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PARCELA_RECEBER:VISUALIZAR') or hasAuthority('PARCELA_RECEBER:*')")
    public ResponseEntity<Page<ParcelaReceberResponse>> listar(
            @RequestParam(required = false) UUID contaReceberId,
            @RequestParam(required = false) StatusParcelaReceber status,
            @RequestParam(required = false) LocalDate dataVencimentoDe,
            @RequestParam(required = false) LocalDate dataVencimentoAte,
            @RequestParam(required = false) LocalDate dataPagamentoDe,
            @RequestParam(required = false) LocalDate dataPagamentoAte,
            @RequestParam(required = false) Boolean ativo,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.listarComFiltros(contaReceberId, status, dataVencimentoDe,
                dataVencimentoAte, dataPagamentoDe, dataPagamentoAte, ativo, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PARCELA_RECEBER:VISUALIZAR') or hasAuthority('PARCELA_RECEBER:*')")
    public ResponseEntity<ParcelaReceberResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PARCELA_RECEBER:EDITAR') or hasAuthority('PARCELA_RECEBER:*')")
    public ResponseEntity<ParcelaReceberResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody ParcelaReceberRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @PostMapping("/{id}/baixar")
    @PreAuthorize("hasAuthority('PARCELA_RECEBER:BAIXAR') or hasAuthority('PARCELA_RECEBER:*')")
    public ResponseEntity<ParcelaReceberResponse> baixar(@PathVariable UUID id, @Valid @RequestBody BaixaParcelaReceberRequest request) {
        return ResponseEntity.ok(service.baixar(id, request));
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('PARCELA_RECEBER:CANCELAR') or hasAuthority('PARCELA_RECEBER:*')")
    public ResponseEntity<ParcelaReceberResponse> cancelar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.cancelar(id));
    }
}
