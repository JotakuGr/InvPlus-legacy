package br.com.invplus.modules.produtos.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.produtos.dto.request.ProdutoFornecedorPrecoRequest;
import br.com.invplus.modules.produtos.dto.response.ProdutoFornecedorPrecoResponse;
import br.com.invplus.modules.produtos.service.ProdutoFornecedorPrecoService;
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
 * Endpoints REST do historico de preco de ProdutoFornecedor.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/produtos-fornecedores-precos")
@RequiredArgsConstructor
public class ProdutoFornecedorPrecoController extends BaseCrudController {

    private final ProdutoFornecedorPrecoService service;

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUTO_FORNECEDOR_PRECO:VISUALIZAR') or hasAuthority('PRODUTO_FORNECEDOR_PRECO:*') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<Page<ProdutoFornecedorPrecoResponse>> listar(
            @RequestParam(required = false) UUID produtoFornecedorId,
            @RequestParam(required = false) LocalDate vigenciaInicioDe,
            @RequestParam(required = false) LocalDate vigenciaFimAte,
            @RequestParam(required = false) Boolean ativo,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.listarComFiltros(produtoFornecedorId, vigenciaInicioDe, vigenciaFimAte, ativo, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO_FORNECEDOR_PRECO:VISUALIZAR') or hasAuthority('PRODUTO_FORNECEDOR_PRECO:*') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<ProdutoFornecedorPrecoResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUTO_FORNECEDOR_PRECO:CRIAR') or hasAuthority('PRODUTO_FORNECEDOR_PRECO:*') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<ProdutoFornecedorPrecoResponse> criar(@Valid @RequestBody ProdutoFornecedorPrecoRequest request) {
        ProdutoFornecedorPrecoResponse response = service.criar(request);
        return created(AppConstants.API_BASE_PATH + "/produtos-fornecedores-precos", response.id(), response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO_FORNECEDOR_PRECO:EDITAR') or hasAuthority('PRODUTO_FORNECEDOR_PRECO:*') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<ProdutoFornecedorPrecoResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ProdutoFornecedorPrecoRequest request
    ) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO_FORNECEDOR_PRECO:EXCLUIR') or hasAuthority('PRODUTO_FORNECEDOR_PRECO:*') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
