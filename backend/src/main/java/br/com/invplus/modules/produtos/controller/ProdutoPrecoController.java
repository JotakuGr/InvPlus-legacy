package br.com.invplus.modules.produtos.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.produtos.dto.request.ProdutoPrecoRequest;
import br.com.invplus.modules.produtos.dto.response.ProdutoPrecoResponse;
import br.com.invplus.modules.produtos.enums.TipoPrecoProduto;
import br.com.invplus.modules.produtos.service.ProdutoPrecoService;
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
 * Endpoints REST de ProdutoPreco.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/produtos-precos")
@RequiredArgsConstructor
public class ProdutoPrecoController extends BaseCrudController {

    private final ProdutoPrecoService service;

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUTO_PRECO:VISUALIZAR') or hasAuthority('PRODUTO_PRECO:*') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<Page<ProdutoPrecoResponse>> listar(
            @RequestParam(required = false) UUID produtoId,
            @RequestParam(required = false) UUID empresaId,
            @RequestParam(required = false) TipoPrecoProduto tipoPreco,
            @RequestParam(required = false) Boolean principal,
            @RequestParam(required = false) LocalDate vigenciaInicioDe,
            @RequestParam(required = false) LocalDate vigenciaFimAte,
            @RequestParam(required = false) Boolean ativo,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.listarComFiltros(produtoId, empresaId, tipoPreco, principal,
                vigenciaInicioDe, vigenciaFimAte, ativo, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO_PRECO:VISUALIZAR') or hasAuthority('PRODUTO_PRECO:*') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<ProdutoPrecoResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @GetMapping("/vigente")
    @PreAuthorize("hasAuthority('PRODUTO_PRECO:VISUALIZAR') or hasAuthority('PRODUTO_PRECO:*') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<ProdutoPrecoResponse> buscarPrecoVigente(
            @RequestParam UUID produtoId,
            @RequestParam TipoPrecoProduto tipoPreco,
            @RequestParam(required = false) LocalDate data
    ) {
        return ResponseEntity.of(service.buscarPrecoVigente(produtoId, tipoPreco, data));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUTO_PRECO:CRIAR') or hasAuthority('PRODUTO_PRECO:*') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<ProdutoPrecoResponse> criar(@Valid @RequestBody ProdutoPrecoRequest request) {
        ProdutoPrecoResponse response = service.criar(request);
        return created(AppConstants.API_BASE_PATH + "/produtos-precos", response.id(), response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO_PRECO:EDITAR') or hasAuthority('PRODUTO_PRECO:*') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<ProdutoPrecoResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ProdutoPrecoRequest request
    ) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO_PRECO:EXCLUIR') or hasAuthority('PRODUTO_PRECO:*') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
