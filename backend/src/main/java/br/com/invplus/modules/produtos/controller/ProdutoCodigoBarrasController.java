package br.com.invplus.modules.produtos.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.produtos.dto.request.ProdutoCodigoBarrasRequest;
import br.com.invplus.modules.produtos.dto.response.ProdutoCodigoBarrasResponse;
import br.com.invplus.modules.produtos.enums.TipoCodigoBarras;
import br.com.invplus.modules.produtos.service.ProdutoCodigoBarrasService;
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
 * Endpoints REST para codigos de barras e codigos alternativos de produtos.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/produtos-codigos-barras")
@RequiredArgsConstructor
public class ProdutoCodigoBarrasController extends BaseCrudController {

    private final ProdutoCodigoBarrasService produtoCodigoBarrasService;

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUTO_CODIGO_BARRAS:VISUALIZAR') or hasAuthority('PRODUTO_CODIGO_BARRAS:*') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<Page<ProdutoCodigoBarrasResponse>> listar(
            @RequestParam(required = false) UUID produtoId,
            @RequestParam(required = false) TipoCodigoBarras tipoCodigo,
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) Boolean principal,
            @RequestParam(required = false) Boolean ativo,
            Pageable pageable
    ) {
        return ResponseEntity.ok(produtoCodigoBarrasService.listarComFiltros(
                produtoId,
                tipoCodigo,
                codigo,
                principal,
                ativo,
                pageable
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO_CODIGO_BARRAS:VISUALIZAR') or hasAuthority('PRODUTO_CODIGO_BARRAS:*') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<ProdutoCodigoBarrasResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(produtoCodigoBarrasService.buscar(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUTO_CODIGO_BARRAS:CRIAR') or hasAuthority('PRODUTO_CODIGO_BARRAS:*') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<ProdutoCodigoBarrasResponse> criar(@Valid @RequestBody ProdutoCodigoBarrasRequest request) {
        ProdutoCodigoBarrasResponse response = produtoCodigoBarrasService.criar(request);
        return created(AppConstants.API_BASE_PATH + "/produtos-codigos-barras", response.id(), response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO_CODIGO_BARRAS:EDITAR') or hasAuthority('PRODUTO_CODIGO_BARRAS:*') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<ProdutoCodigoBarrasResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ProdutoCodigoBarrasRequest request
    ) {
        return ResponseEntity.ok(produtoCodigoBarrasService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO_CODIGO_BARRAS:EXCLUIR') or hasAuthority('PRODUTO_CODIGO_BARRAS:*') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        produtoCodigoBarrasService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
