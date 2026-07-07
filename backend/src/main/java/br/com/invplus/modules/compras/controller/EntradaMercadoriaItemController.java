package br.com.invplus.modules.compras.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.compras.dto.request.EntradaMercadoriaItemRequest;
import br.com.invplus.modules.compras.dto.response.EntradaMercadoriaItemResponse;
import br.com.invplus.modules.compras.service.EntradaMercadoriaItemService;
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
 * Endpoints REST de itens de entrada de mercadoria.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/entradas-mercadoria-itens")
@RequiredArgsConstructor
public class EntradaMercadoriaItemController extends BaseCrudController {

    private final EntradaMercadoriaItemService service;

    @GetMapping
    @PreAuthorize("hasAuthority('ENTRADA_MERCADORIA_ITEM:VISUALIZAR') or hasAuthority('ENTRADA_MERCADORIA_ITEM:*') or hasAuthority('ENTRADA_MERCADORIA:*') or hasAuthority('COMPRAS:*')")
    public ResponseEntity<Page<EntradaMercadoriaItemResponse>> listar(
            @RequestParam(required = false) UUID entradaMercadoriaId,
            @RequestParam(required = false) UUID produtoId,
            @RequestParam(required = false) Boolean ativo,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.listarComFiltros(entradaMercadoriaId, produtoId, ativo, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ENTRADA_MERCADORIA_ITEM:VISUALIZAR') or hasAuthority('ENTRADA_MERCADORIA_ITEM:*') or hasAuthority('ENTRADA_MERCADORIA:*') or hasAuthority('COMPRAS:*')")
    public ResponseEntity<EntradaMercadoriaItemResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ENTRADA_MERCADORIA_ITEM:CRIAR') or hasAuthority('ENTRADA_MERCADORIA_ITEM:*') or hasAuthority('ENTRADA_MERCADORIA:*') or hasAuthority('COMPRAS:*')")
    public ResponseEntity<EntradaMercadoriaItemResponse> criar(@Valid @RequestBody EntradaMercadoriaItemRequest request) {
        EntradaMercadoriaItemResponse response = service.criar(request);
        return created(AppConstants.API_BASE_PATH + "/entradas-mercadoria-itens", response.id(), response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ENTRADA_MERCADORIA_ITEM:EDITAR') or hasAuthority('ENTRADA_MERCADORIA_ITEM:*') or hasAuthority('ENTRADA_MERCADORIA:*') or hasAuthority('COMPRAS:*')")
    public ResponseEntity<EntradaMercadoriaItemResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody EntradaMercadoriaItemRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ENTRADA_MERCADORIA_ITEM:EXCLUIR') or hasAuthority('ENTRADA_MERCADORIA_ITEM:*') or hasAuthority('ENTRADA_MERCADORIA:*') or hasAuthority('COMPRAS:*')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
