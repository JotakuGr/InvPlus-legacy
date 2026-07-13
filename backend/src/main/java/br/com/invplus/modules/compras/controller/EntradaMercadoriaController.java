package br.com.invplus.modules.compras.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.compras.dto.request.EntradaMercadoriaRequest;
import br.com.invplus.modules.compras.dto.response.EntradaMercadoriaCompletaResponse;
import br.com.invplus.modules.compras.dto.response.EntradaMercadoriaResponse;
import br.com.invplus.modules.compras.enums.StatusEntradaMercadoria;
import br.com.invplus.modules.compras.service.CompraAggregateService;
import br.com.invplus.modules.compras.service.EntradaMercadoriaService;
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
 * Endpoints REST de entrada de mercadoria.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/entradas-mercadoria")
@RequiredArgsConstructor
public class EntradaMercadoriaController extends BaseCrudController {

    private final EntradaMercadoriaService service;
    private final CompraAggregateService aggregateService;

    @GetMapping
    @PreAuthorize("hasAuthority('ENTRADA_MERCADORIA:VISUALIZAR') or hasAuthority('ENTRADA_MERCADORIA:*') or hasAuthority('COMPRAS:*')")
    public ResponseEntity<Page<EntradaMercadoriaResponse>> listar(
            @RequestParam(required = false) UUID fornecedorId,
            @RequestParam(required = false) UUID pedidoCompraId,
            @RequestParam(required = false) StatusEntradaMercadoria status,
            @RequestParam(required = false) Boolean ativo,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.listarComFiltros(fornecedorId, pedidoCompraId, status, ativo, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ENTRADA_MERCADORIA:VISUALIZAR') or hasAuthority('ENTRADA_MERCADORIA:*') or hasAuthority('COMPRAS:*')")
    public ResponseEntity<EntradaMercadoriaResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @GetMapping("/{id}/completo")
    @PreAuthorize("hasAuthority('ENTRADA_MERCADORIA:VISUALIZAR') or hasAuthority('ENTRADA_MERCADORIA:*') or hasAuthority('COMPRAS:*')")
    public ResponseEntity<EntradaMercadoriaCompletaResponse> buscarCompleta(@PathVariable UUID id) {
        return ResponseEntity.ok(aggregateService.buscarEntrada(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ENTRADA_MERCADORIA:CRIAR') or hasAuthority('ENTRADA_MERCADORIA:*') or hasAuthority('COMPRAS:*')")
    public ResponseEntity<EntradaMercadoriaResponse> criar(@Valid @RequestBody EntradaMercadoriaRequest request) {
        EntradaMercadoriaResponse response = service.criar(request);
        return created(AppConstants.API_BASE_PATH + "/entradas-mercadoria", response.id(), response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ENTRADA_MERCADORIA:EDITAR') or hasAuthority('ENTRADA_MERCADORIA:*') or hasAuthority('COMPRAS:*')")
    public ResponseEntity<EntradaMercadoriaResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody EntradaMercadoriaRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @PostMapping("/{id}/conferir")
    @PreAuthorize("hasAuthority('ENTRADA_MERCADORIA:CONFERIR') or hasAuthority('ENTRADA_MERCADORIA:*') or hasAuthority('COMPRAS:*')")
    public ResponseEntity<EntradaMercadoriaResponse> conferir(@PathVariable UUID id) {
        return ResponseEntity.ok(service.conferir(id));
    }

    @PostMapping("/{id}/efetivar")
    @PreAuthorize("hasAuthority('ENTRADA_MERCADORIA:EFETIVAR') or hasAuthority('ENTRADA_MERCADORIA:*') or hasAuthority('COMPRAS:*')")
    public ResponseEntity<EntradaMercadoriaResponse> efetivar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.efetivar(id));
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('ENTRADA_MERCADORIA:CANCELAR') or hasAuthority('ENTRADA_MERCADORIA:*') or hasAuthority('COMPRAS:*')")
    public ResponseEntity<EntradaMercadoriaResponse> cancelar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.cancelar(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ENTRADA_MERCADORIA:CANCELAR') or hasAuthority('ENTRADA_MERCADORIA:*') or hasAuthority('COMPRAS:*')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
