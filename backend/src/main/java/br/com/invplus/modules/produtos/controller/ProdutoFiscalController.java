package br.com.invplus.modules.produtos.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.produtos.dto.request.ProdutoFiscalRequest;
import br.com.invplus.modules.produtos.dto.response.ProdutoFiscalResponse;
import br.com.invplus.modules.produtos.enums.OrigemMercadoria;
import br.com.invplus.modules.produtos.service.ProdutoFiscalService;
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
 * Endpoints REST dos dados fiscais de Produto.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/produtos-fiscais")
@RequiredArgsConstructor
public class ProdutoFiscalController extends BaseCrudController {

    private final ProdutoFiscalService service;

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUTO_FISCAL:VISUALIZAR') or hasAuthority('PRODUTO_FISCAL:*') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<Page<ProdutoFiscalResponse>> listar(
            @RequestParam(required = false) UUID produtoId,
            @RequestParam(required = false) String ncm,
            @RequestParam(required = false) String cest,
            @RequestParam(required = false) OrigemMercadoria origemMercadoria,
            @RequestParam(required = false) Boolean ativo,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.listarComFiltros(produtoId, ncm, cest, origemMercadoria, ativo, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO_FISCAL:VISUALIZAR') or hasAuthority('PRODUTO_FISCAL:*') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<ProdutoFiscalResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUTO_FISCAL:CRIAR') or hasAuthority('PRODUTO_FISCAL:*') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<ProdutoFiscalResponse> criar(@Valid @RequestBody ProdutoFiscalRequest request) {
        ProdutoFiscalResponse response = service.criar(request);
        return created(AppConstants.API_BASE_PATH + "/produtos-fiscais", response.id(), response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO_FISCAL:EDITAR') or hasAuthority('PRODUTO_FISCAL:*') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<ProdutoFiscalResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ProdutoFiscalRequest request
    ) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO_FISCAL:EXCLUIR') or hasAuthority('PRODUTO_FISCAL:*') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
