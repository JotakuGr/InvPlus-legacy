package br.com.invplus.modules.produtos.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.produtos.dto.request.ProdutoUnidadeRequest;
import br.com.invplus.modules.produtos.dto.response.ProdutoUnidadeResponse;
import br.com.invplus.modules.produtos.service.ProdutoUnidadeService;
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
 * Endpoints REST de ProdutoUnidade.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/produtos-unidades")
@RequiredArgsConstructor
public class ProdutoUnidadeController extends BaseCrudController {

    private final ProdutoUnidadeService service;

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUTO_UNIDADE:VISUALIZAR') or hasAuthority('PRODUTO_UNIDADE:*') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<Page<ProdutoUnidadeResponse>> listar(
            @RequestParam(required = false) UUID produtoId,
            @RequestParam(required = false) UUID unidadeMedidaId,
            @RequestParam(required = false) Boolean unidadePadrao,
            @RequestParam(required = false) Boolean permiteCompra,
            @RequestParam(required = false) Boolean permiteVenda,
            @RequestParam(required = false) Boolean permiteEstoque,
            @RequestParam(required = false) Boolean permiteFracionamento,
            @RequestParam(required = false) Boolean ativo,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.listarComFiltros(produtoId, unidadeMedidaId, unidadePadrao,
                permiteCompra, permiteVenda, permiteEstoque, permiteFracionamento, ativo, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO_UNIDADE:VISUALIZAR') or hasAuthority('PRODUTO_UNIDADE:*') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<ProdutoUnidadeResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUTO_UNIDADE:CRIAR') or hasAuthority('PRODUTO_UNIDADE:*') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<ProdutoUnidadeResponse> criar(@Valid @RequestBody ProdutoUnidadeRequest request) {
        ProdutoUnidadeResponse response = service.criar(request);
        return created(AppConstants.API_BASE_PATH + "/produtos-unidades", response.id(), response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO_UNIDADE:EDITAR') or hasAuthority('PRODUTO_UNIDADE:*') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<ProdutoUnidadeResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ProdutoUnidadeRequest request
    ) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO_UNIDADE:EXCLUIR') or hasAuthority('PRODUTO_UNIDADE:*') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
