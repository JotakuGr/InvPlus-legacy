package br.com.invplus.modules.vendas.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.vendas.dto.request.PedidoVendaItemRequest;
import br.com.invplus.modules.vendas.dto.response.PedidoVendaItemResponse;
import br.com.invplus.modules.vendas.service.PedidoVendaItemService;
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
 * Endpoints REST dos itens do pedido de venda.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/pedidos-venda-itens")
@RequiredArgsConstructor
public class PedidoVendaItemController extends BaseCrudController {

    private final PedidoVendaItemService service;

    @GetMapping
    @PreAuthorize("hasAuthority('PEDIDO_VENDA_ITEM:VISUALIZAR') or hasAuthority('PEDIDO_VENDA_ITEM:*') or hasAuthority('PEDIDO_VENDA:*')")
    public ResponseEntity<Page<PedidoVendaItemResponse>> listar(
            @RequestParam(required = false) UUID pedidoVendaId,
            @RequestParam(required = false) UUID produtoId,
            @RequestParam(required = false) Boolean ativo,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.listarComFiltros(pedidoVendaId, produtoId, ativo, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PEDIDO_VENDA_ITEM:VISUALIZAR') or hasAuthority('PEDIDO_VENDA_ITEM:*') or hasAuthority('PEDIDO_VENDA:*')")
    public ResponseEntity<PedidoVendaItemResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PEDIDO_VENDA_ITEM:CRIAR') or hasAuthority('PEDIDO_VENDA_ITEM:*') or hasAuthority('PEDIDO_VENDA:*')")
    public ResponseEntity<PedidoVendaItemResponse> criar(@Valid @RequestBody PedidoVendaItemRequest request) {
        PedidoVendaItemResponse response = service.criar(request);
        return created(AppConstants.API_BASE_PATH + "/pedidos-venda-itens", response.id(), response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PEDIDO_VENDA_ITEM:EDITAR') or hasAuthority('PEDIDO_VENDA_ITEM:*') or hasAuthority('PEDIDO_VENDA:*')")
    public ResponseEntity<PedidoVendaItemResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody PedidoVendaItemRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PEDIDO_VENDA_ITEM:EXCLUIR') or hasAuthority('PEDIDO_VENDA_ITEM:*') or hasAuthority('PEDIDO_VENDA:*')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
