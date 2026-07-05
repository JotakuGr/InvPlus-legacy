package br.com.invplus.modules.produtos.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.produtos.dto.request.ProdutoRequest;
import br.com.invplus.modules.produtos.dto.response.ProdutoCompletoResponse;
import br.com.invplus.modules.produtos.dto.response.ProdutoResponse;
import br.com.invplus.modules.produtos.enums.NaturezaProduto;
import br.com.invplus.modules.produtos.enums.StatusProduto;
import br.com.invplus.modules.produtos.enums.TipoProduto;
import br.com.invplus.modules.produtos.service.ProdutoAggregateService;
import br.com.invplus.modules.produtos.service.ProdutoService;
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
 * Endpoints REST do catalogo de Produtos.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/produtos")
@RequiredArgsConstructor
public class ProdutoController extends BaseCrudController {

    private final ProdutoService produtoService;
    private final ProdutoAggregateService produtoAggregateService;

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUTO:VISUALIZAR') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<Page<ProdutoResponse>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) String codigoBarras,
            @RequestParam(required = false) UUID categoriaId,
            @RequestParam(required = false) UUID marcaId,
            @RequestParam(required = false) TipoProduto tipoProduto,
            @RequestParam(required = false) NaturezaProduto naturezaProduto,
            @RequestParam(required = false) StatusProduto statusProduto,
            @RequestParam(required = false) Boolean controlaEstoque,
            @RequestParam(required = false) Boolean permiteCompra,
            @RequestParam(required = false) Boolean permiteVenda,
            @RequestParam(required = false) Boolean ativo,
            Pageable pageable
    ) {
        return ResponseEntity.ok(produtoService.listarComFiltros(
                nome,
                codigo,
                sku,
                codigoBarras,
                categoriaId,
                marcaId,
                tipoProduto,
                naturezaProduto,
                statusProduto,
                controlaEstoque,
                permiteCompra,
                permiteVenda,
                ativo,
                pageable
        ));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('PRODUTO:VISUALIZAR') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<Page<ProdutoResponse>> buscarRapido(@RequestParam String termo, Pageable pageable) {
        return ResponseEntity.ok(produtoService.buscarRapido(termo, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO:VISUALIZAR') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<ProdutoResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(produtoService.buscar(id));
    }

    @GetMapping("/{id}/completo")
    @PreAuthorize("hasAuthority('PRODUTO:VISUALIZAR') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<ProdutoCompletoResponse> buscarCompleto(@PathVariable UUID id) {
        return ResponseEntity.ok(produtoAggregateService.buscar(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUTO:CRIAR') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<ProdutoResponse> criar(@Valid @RequestBody ProdutoRequest request) {
        ProdutoResponse response = produtoService.criar(request);
        return created(AppConstants.API_BASE_PATH + "/produtos", response.id(), response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO:EDITAR') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<ProdutoResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody ProdutoRequest request) {
        return ResponseEntity.ok(produtoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO:EXCLUIR') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        produtoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
