package br.com.invplus.modules.produtos.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.produtos.dto.request.ProdutoImagemRequest;
import br.com.invplus.modules.produtos.dto.response.ProdutoImagemResponse;
import br.com.invplus.modules.produtos.service.ProdutoImagemService;
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
 * Endpoints REST para imagens vinculadas a produtos.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/produtos-imagens")
@RequiredArgsConstructor
public class ProdutoImagemController extends BaseCrudController {

    private final ProdutoImagemService produtoImagemService;

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUTO_IMAGEM:VISUALIZAR') or hasAuthority('PRODUTO_IMAGEM:*') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<Page<ProdutoImagemResponse>> listar(
            @RequestParam(required = false) UUID produtoId,
            @RequestParam(required = false) UUID arquivoId,
            @RequestParam(required = false) Boolean principal,
            @RequestParam(required = false) Boolean ativo,
            Pageable pageable
    ) {
        return ResponseEntity.ok(produtoImagemService.listarComFiltros(produtoId, arquivoId, principal, ativo, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO_IMAGEM:VISUALIZAR') or hasAuthority('PRODUTO_IMAGEM:*') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<ProdutoImagemResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(produtoImagemService.buscar(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUTO_IMAGEM:CRIAR') or hasAuthority('PRODUTO_IMAGEM:*') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<ProdutoImagemResponse> criar(@Valid @RequestBody ProdutoImagemRequest request) {
        ProdutoImagemResponse response = produtoImagemService.criar(request);
        return created(AppConstants.API_BASE_PATH + "/produtos-imagens", response.id(), response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO_IMAGEM:EDITAR') or hasAuthority('PRODUTO_IMAGEM:*') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<ProdutoImagemResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ProdutoImagemRequest request
    ) {
        return ResponseEntity.ok(produtoImagemService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO_IMAGEM:EXCLUIR') or hasAuthority('PRODUTO_IMAGEM:*') or hasAuthority('PRODUTO:*')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        produtoImagemService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
