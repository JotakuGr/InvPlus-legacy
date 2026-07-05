package br.com.invplus.modules.produtos.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.produtos.dto.request.ProdutoFornecedorRequest;
import br.com.invplus.modules.produtos.dto.response.ProdutoFornecedorResponse;
import br.com.invplus.modules.produtos.enums.FornecedorProdutoStatus;
import br.com.invplus.modules.produtos.service.ProdutoFornecedorService;
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
 * Endpoints REST para vincular produtos a fornecedores.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/produtos-fornecedores")
@RequiredArgsConstructor
public class ProdutoFornecedorController extends BaseCrudController {

    private final ProdutoFornecedorService produtoFornecedorService;

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUTO_FORNECEDOR:VISUALIZAR') or hasAuthority('PRODUTO_FORNECEDOR:*') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<Page<ProdutoFornecedorResponse>> listar(
            @RequestParam(required = false) UUID produtoId,
            @RequestParam(required = false) UUID fornecedorId,
            @RequestParam(required = false) Boolean principal,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) String codigoFornecedor,
            @RequestParam(required = false) FornecedorProdutoStatus status,
            Pageable pageable
    ) {
        return ResponseEntity.ok(produtoFornecedorService.listarComFiltros(
                produtoId,
                fornecedorId,
                principal,
                ativo,
                codigoFornecedor,
                status,
                pageable
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO_FORNECEDOR:VISUALIZAR') or hasAuthority('PRODUTO_FORNECEDOR:*') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<ProdutoFornecedorResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(produtoFornecedorService.buscar(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUTO_FORNECEDOR:CRIAR') or hasAuthority('PRODUTO_FORNECEDOR:*') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<ProdutoFornecedorResponse> criar(@Valid @RequestBody ProdutoFornecedorRequest request) {
        ProdutoFornecedorResponse response = produtoFornecedorService.criar(request);
        return created(AppConstants.API_BASE_PATH + "/produtos-fornecedores", response.id(), response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO_FORNECEDOR:EDITAR') or hasAuthority('PRODUTO_FORNECEDOR:*') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<ProdutoFornecedorResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ProdutoFornecedorRequest request
    ) {
        return ResponseEntity.ok(produtoFornecedorService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO_FORNECEDOR:EXCLUIR') or hasAuthority('PRODUTO_FORNECEDOR:*') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        produtoFornecedorService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
