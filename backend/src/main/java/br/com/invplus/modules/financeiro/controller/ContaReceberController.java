package br.com.invplus.modules.financeiro.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.financeiro.dto.request.ContaReceberRequest;
import br.com.invplus.modules.financeiro.dto.request.GerarParcelasReceberRequest;
import br.com.invplus.modules.financeiro.dto.response.ParcelaReceberResponse;
import br.com.invplus.modules.financeiro.dto.response.ContaReceberResponse;
import br.com.invplus.modules.financeiro.enums.StatusContaReceber;
import br.com.invplus.modules.financeiro.service.ContaReceberService;
import br.com.invplus.modules.financeiro.service.ParcelaReceberService;
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
 * Endpoints REST para contas a receber.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/contas-receber")
@RequiredArgsConstructor
public class ContaReceberController extends BaseCrudController {

    private final ContaReceberService service;
    private final ParcelaReceberService parcelaReceberService;

    @PostMapping
    @PreAuthorize("hasAuthority('CONTA_RECEBER:CRIAR') or hasAuthority('CONTA_RECEBER:*')")
    public ResponseEntity<ContaReceberResponse> criar(@Valid @RequestBody ContaReceberRequest request) {
        ContaReceberResponse response = service.criar(request);
        return created(AppConstants.API_BASE_PATH + "/contas-receber", response.id(), response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CONTA_RECEBER:VISUALIZAR') or hasAuthority('CONTA_RECEBER:*')")
    public ResponseEntity<Page<ContaReceberResponse>> listar(
            @RequestParam(required = false) UUID filialId,
            @RequestParam(required = false) UUID clienteId,
            @RequestParam(required = false) UUID vendaFaturamentoId,
            @RequestParam(required = false) UUID centroCustoId,
            @RequestParam(required = false) UUID planoContaId,
            @RequestParam(required = false) String numero,
            @RequestParam(required = false) StatusContaReceber status,
            @RequestParam(required = false) LocalDate dataEmissaoDe,
            @RequestParam(required = false) LocalDate dataEmissaoAte,
            @RequestParam(required = false) Boolean ativo,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.listarComFiltros(filialId, clienteId, vendaFaturamentoId,
                centroCustoId, planoContaId, numero, status, dataEmissaoDe, dataEmissaoAte, ativo, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTA_RECEBER:VISUALIZAR') or hasAuthority('CONTA_RECEBER:*')")
    public ResponseEntity<ContaReceberResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTA_RECEBER:EDITAR') or hasAuthority('CONTA_RECEBER:*')")
    public ResponseEntity<ContaReceberResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody ContaReceberRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('CONTA_RECEBER:CANCELAR') or hasAuthority('CONTA_RECEBER:*')")
    public ResponseEntity<ContaReceberResponse> cancelar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.cancelar(id));
    }

    @PostMapping("/{id}/gerar-parcelas")
    @PreAuthorize("hasAuthority('PARCELA_RECEBER:CRIAR') or hasAuthority('PARCELA_RECEBER:*') or hasAuthority('CONTA_RECEBER:*')")
    public ResponseEntity<List<ParcelaReceberResponse>> gerarParcelas(
            @PathVariable UUID id,
            @Valid @RequestBody GerarParcelasReceberRequest request
    ) {
        return ResponseEntity.ok(parcelaReceberService.gerarParcelas(id, request));
    }
}
