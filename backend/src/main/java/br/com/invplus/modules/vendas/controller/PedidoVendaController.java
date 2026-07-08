package br.com.invplus.modules.vendas.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.vendas.dto.request.PedidoVendaRequest;
import br.com.invplus.modules.vendas.dto.response.PedidoVendaCompletoResponse;
import br.com.invplus.modules.vendas.dto.response.PedidoVendaResponse;
import br.com.invplus.modules.vendas.enums.StatusPedidoVenda;
import br.com.invplus.modules.vendas.service.PedidoVendaService;
import br.com.invplus.modules.vendas.service.VendaAggregateService;
import br.com.invplus.shared.api.controller.BaseCrudController;
import jakarta.validation.Valid;
import java.time.LocalDate;
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
 * Endpoints REST do pedido de venda.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/pedidos-venda")
@RequiredArgsConstructor
public class PedidoVendaController extends BaseCrudController {

    private final PedidoVendaService service;
    private final VendaAggregateService aggregateService;

    @GetMapping
    @PreAuthorize("hasAuthority('PEDIDO_VENDA:VISUALIZAR') or hasAuthority('PEDIDO_VENDA:*')")
    public ResponseEntity<Page<PedidoVendaResponse>> listar(
            @RequestParam(required = false) UUID empresaId,
            @RequestParam(required = false) UUID filialId,
            @RequestParam(required = false) UUID clienteId,
            @RequestParam(required = false) UUID vendedorId,
            @RequestParam(required = false) String numero,
            @RequestParam(required = false) StatusPedidoVenda status,
            @RequestParam(required = false) LocalDate dataEmissaoDe,
            @RequestParam(required = false) LocalDate dataEmissaoAte,
            @RequestParam(required = false) Boolean ativo,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.listarComFiltros(
                empresaId,
                filialId,
                clienteId,
                vendedorId,
                numero,
                status,
                dataEmissaoDe,
                dataEmissaoAte,
                ativo,
                pageable
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PEDIDO_VENDA:VISUALIZAR') or hasAuthority('PEDIDO_VENDA:*')")
    public ResponseEntity<PedidoVendaResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @GetMapping("/{id}/completo")
    @PreAuthorize("hasAuthority('PEDIDO_VENDA:VISUALIZAR') or hasAuthority('PEDIDO_VENDA:*')")
    public ResponseEntity<PedidoVendaCompletoResponse> buscarCompleto(@PathVariable UUID id) {
        return ResponseEntity.ok(aggregateService.buscarPedido(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PEDIDO_VENDA:CRIAR') or hasAuthority('PEDIDO_VENDA:*')")
    public ResponseEntity<PedidoVendaResponse> criar(@Valid @RequestBody PedidoVendaRequest request) {
        PedidoVendaResponse response = service.criar(request);
        return created(AppConstants.API_BASE_PATH + "/pedidos-venda", response.id(), response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PEDIDO_VENDA:EDITAR') or hasAuthority('PEDIDO_VENDA:*')")
    public ResponseEntity<PedidoVendaResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody PedidoVendaRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @PostMapping("/{id}/aprovar")
    @PreAuthorize("hasAuthority('PEDIDO_VENDA:APROVAR') or hasAuthority('PEDIDO_VENDA:*')")
    public ResponseEntity<PedidoVendaResponse> aprovar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.aprovar(id));
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('PEDIDO_VENDA:CANCELAR') or hasAuthority('PEDIDO_VENDA:*')")
    public ResponseEntity<PedidoVendaResponse> cancelar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.cancelar(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PEDIDO_VENDA:CANCELAR') or hasAuthority('PEDIDO_VENDA:*')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
