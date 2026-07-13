package br.com.invplus.modules.compras.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.compras.dto.request.PedidoCompraRequest;
import br.com.invplus.modules.compras.dto.response.PedidoCompraCompletoResponse;
import br.com.invplus.modules.compras.dto.response.PedidoCompraResponse;
import br.com.invplus.modules.compras.enums.StatusPedidoCompra;
import br.com.invplus.modules.compras.service.CompraAggregateService;
import br.com.invplus.modules.compras.service.PedidoCompraService;
import br.com.invplus.shared.api.controller.BaseCrudController;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints REST do pedido de compra.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/pedidos-compra")
@RequiredArgsConstructor
public class PedidoCompraController extends BaseCrudController {

    private final PedidoCompraService service;
    private final CompraAggregateService aggregateService;

    @GetMapping
    @PreAuthorize("hasAuthority('PEDIDO_COMPRA:VISUALIZAR') or hasAuthority('PEDIDO_COMPRA:*') or hasAuthority('COMPRAS:*')")
    public ResponseEntity<Page<PedidoCompraResponse>> listar(
            @RequestParam(required = false) UUID fornecedorId,
            @RequestParam(required = false) StatusPedidoCompra status,
            @RequestParam(required = false) String numero,
            @RequestParam(required = false) Boolean ativo,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.listarComFiltros(fornecedorId, status, numero, ativo, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PEDIDO_COMPRA:VISUALIZAR') or hasAuthority('PEDIDO_COMPRA:*') or hasAuthority('COMPRAS:*')")
    public ResponseEntity<PedidoCompraResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @GetMapping("/{id}/completo")
    @PreAuthorize("hasAuthority('PEDIDO_COMPRA:VISUALIZAR') or hasAuthority('PEDIDO_COMPRA:*') or hasAuthority('COMPRAS:*')")
    public ResponseEntity<PedidoCompraCompletoResponse> buscarCompleto(@PathVariable UUID id) {
        return ResponseEntity.ok(aggregateService.buscarPedido(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PEDIDO_COMPRA:CRIAR') or hasAuthority('PEDIDO_COMPRA:*') or hasAuthority('COMPRAS:*')")
    public ResponseEntity<PedidoCompraResponse> criar(@Valid @RequestBody PedidoCompraRequest request) {
        PedidoCompraResponse response = service.criar(request);
        return created(AppConstants.API_BASE_PATH + "/pedidos-compra", response.id(), response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PEDIDO_COMPRA:EDITAR') or hasAuthority('PEDIDO_COMPRA:*') or hasAuthority('COMPRAS:*')")
    public ResponseEntity<PedidoCompraResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody PedidoCompraRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @PostMapping("/{id}/aprovar")
    @PreAuthorize("hasAuthority('PEDIDO_COMPRA:APROVAR') or hasAuthority('PEDIDO_COMPRA:*') or hasAuthority('COMPRAS:*')")
    public ResponseEntity<PedidoCompraResponse> aprovar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.aprovar(id));
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('PEDIDO_COMPRA:CANCELAR') or hasAuthority('PEDIDO_COMPRA:*') or hasAuthority('COMPRAS:*')")
    public ResponseEntity<PedidoCompraResponse> cancelar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.cancelar(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PEDIDO_COMPRA:CANCELAR') or hasAuthority('PEDIDO_COMPRA:*') or hasAuthority('COMPRAS:*')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
