package br.com.invplus.modules.vendas.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.vendas.dto.request.VendaFaturamentoRequest;
import br.com.invplus.modules.vendas.dto.response.VendaFaturamentoResponse;
import br.com.invplus.modules.vendas.enums.StatusVendaFaturamento;
import br.com.invplus.modules.vendas.service.VendaFaturamentoService;
import br.com.invplus.shared.api.controller.BaseCrudController;
import jakarta.validation.Valid;
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
 * Endpoints REST para faturar pedidos de venda aprovados.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/vendas-faturamentos")
@RequiredArgsConstructor
public class VendaFaturamentoController extends BaseCrudController {

    private final VendaFaturamentoService service;

    @PostMapping("/faturar")
    @PreAuthorize("hasAuthority('VENDA_FATURAMENTO:FATURAR') or hasAuthority('VENDA_FATURAMENTO:*')")
    public ResponseEntity<VendaFaturamentoResponse> faturar(@Valid @RequestBody VendaFaturamentoRequest request) {
        VendaFaturamentoResponse response = service.faturar(request);
        return created(AppConstants.API_BASE_PATH + "/vendas-faturamentos", response.id(), response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VENDA_FATURAMENTO:VISUALIZAR') or hasAuthority('VENDA_FATURAMENTO:*')")
    public ResponseEntity<Page<VendaFaturamentoResponse>> listar(
            @RequestParam(required = false) UUID pedidoVendaId,
            @RequestParam(required = false) UUID filialId,
            @RequestParam(required = false) UUID clienteId,
            @RequestParam(required = false) String numero,
            @RequestParam(required = false) StatusVendaFaturamento status,
            @RequestParam(required = false) Boolean ativo,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.listarComFiltros(
                pedidoVendaId,
                filialId,
                clienteId,
                numero,
                status,
                ativo,
                pageable
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VENDA_FATURAMENTO:VISUALIZAR') or hasAuthority('VENDA_FATURAMENTO:*')")
    public ResponseEntity<VendaFaturamentoResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('VENDA_FATURAMENTO:CANCELAR') or hasAuthority('VENDA_FATURAMENTO:*')")
    public ResponseEntity<VendaFaturamentoResponse> cancelar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.cancelar(id));
    }
}
