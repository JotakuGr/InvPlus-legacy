package br.com.invplus.modules.compras.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.compras.dto.request.PedidoCompraItemRequest;
import br.com.invplus.modules.compras.dto.response.PedidoCompraItemResponse;
import br.com.invplus.modules.compras.service.PedidoCompraItemService;
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
 * Endpoints REST dos itens de pedido de compra.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/pedidos-compra-itens")
@RequiredArgsConstructor
public class PedidoCompraItemController extends BaseCrudController {

    private final PedidoCompraItemService service;

    @GetMapping
    @PreAuthorize("hasAuthority('PEDIDO_COMPRA_ITEM:VISUALIZAR') or hasAuthority('PEDIDO_COMPRA_ITEM:*') or hasAuthority('PEDIDO_COMPRA:*') or hasAuthority('COMPRAS:*')")
    public ResponseEntity<Page<PedidoCompraItemResponse>> listar(
            @RequestParam(required = false) UUID pedidoCompraId,
            @RequestParam(required = false) UUID produtoId,
            @RequestParam(required = false) Boolean ativo,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.listarComFiltros(pedidoCompraId, produtoId, ativo, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PEDIDO_COMPRA_ITEM:VISUALIZAR') or hasAuthority('PEDIDO_COMPRA_ITEM:*') or hasAuthority('PEDIDO_COMPRA:*') or hasAuthority('COMPRAS:*')")
    public ResponseEntity<PedidoCompraItemResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PEDIDO_COMPRA_ITEM:CRIAR') or hasAuthority('PEDIDO_COMPRA_ITEM:*') or hasAuthority('PEDIDO_COMPRA:*') or hasAuthority('COMPRAS:*')")
    public ResponseEntity<PedidoCompraItemResponse> criar(@Valid @RequestBody PedidoCompraItemRequest request) {
        PedidoCompraItemResponse response = service.criar(request);
        return created(AppConstants.API_BASE_PATH + "/pedidos-compra-itens", response.id(), response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PEDIDO_COMPRA_ITEM:EDITAR') or hasAuthority('PEDIDO_COMPRA_ITEM:*') or hasAuthority('PEDIDO_COMPRA:*') or hasAuthority('COMPRAS:*')")
    public ResponseEntity<PedidoCompraItemResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody PedidoCompraItemRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PEDIDO_COMPRA_ITEM:EXCLUIR') or hasAuthority('PEDIDO_COMPRA_ITEM:*') or hasAuthority('PEDIDO_COMPRA:*') or hasAuthority('COMPRAS:*')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
