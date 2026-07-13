package br.com.invplus.modules.financeiro.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.financeiro.dto.request.RecebimentoRequest;
import br.com.invplus.modules.financeiro.dto.response.RecebimentoResponse;
import br.com.invplus.modules.financeiro.service.RecebimentoService;
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
 * Endpoints REST para registro e consulta de recebimentos financeiros.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/recebimentos")
@RequiredArgsConstructor
public class RecebimentoController extends BaseCrudController {

    private final RecebimentoService service;

    @PostMapping
    @PreAuthorize("hasAuthority('RECEBIMENTO:CRIAR') or hasAuthority('RECEBIMENTO:*')")
    public ResponseEntity<RecebimentoResponse> criar(@Valid @RequestBody RecebimentoRequest request) {
        RecebimentoResponse response = service.criar(request);
        return created(AppConstants.API_BASE_PATH + "/recebimentos", response.id(), response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('RECEBIMENTO:VISUALIZAR') or hasAuthority('RECEBIMENTO:*')")
    public ResponseEntity<Page<RecebimentoResponse>> listar(
            @RequestParam(required = false) UUID parcelaReceberId,
            @RequestParam(required = false) UUID contaReceberId,
            @RequestParam(required = false) UUID formaPagamentoId,
            @RequestParam(required = false) UUID filialId,
            @RequestParam(required = false) UUID clienteId,
            @RequestParam(required = false) LocalDate dataRecebimentoDe,
            @RequestParam(required = false) LocalDate dataRecebimentoAte,
            @RequestParam(required = false) Boolean ativo,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.listarComFiltros(parcelaReceberId, contaReceberId, formaPagamentoId,
                null, filialId, clienteId, dataRecebimentoDe, dataRecebimentoAte, ativo, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('RECEBIMENTO:VISUALIZAR') or hasAuthority('RECEBIMENTO:*')")
    public ResponseEntity<RecebimentoResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('RECEBIMENTO:CANCELAR') or hasAuthority('RECEBIMENTO:*')")
    public ResponseEntity<RecebimentoResponse> cancelar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.cancelar(id));
    }
}
