package br.com.invplus.modules.cadastros.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.cadastros.dto.request.CategoriaRequest;
import br.com.invplus.modules.cadastros.dto.response.CategoriaResponse;
import br.com.invplus.modules.cadastros.service.CategoriaService;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints REST para categorias do modulo Cadastros.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/categorias")
@RequiredArgsConstructor
public class CategoriaController extends BaseCrudController {

    private final CategoriaService categoriaService;

    /**
     * Retorna todas as categorias cadastradas.
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('CADASTROS:*', 'CATEGORIA:*', 'CATEGORIA:VISUALIZAR')")
    public ResponseEntity<Page<CategoriaResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(categoriaService.listar(pageable));
    }

    /**
     * Retorna uma categoria pelo identificador.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CADASTROS:*', 'CATEGORIA:*', 'CATEGORIA:VISUALIZAR')")
    public ResponseEntity<CategoriaResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(categoriaService.buscar(id));
    }

    /**
     * Cria uma categoria e devolve a URL do novo recurso.
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('CADASTROS:*', 'CATEGORIA:*', 'CATEGORIA:CRIAR')")
    public ResponseEntity<CategoriaResponse> criar(@Valid @RequestBody CategoriaRequest request) {
        CategoriaResponse response = categoriaService.criar(request);
        return created(AppConstants.API_BASE_PATH + "/categorias", response.id(), response);
    }

    /**
     * Atualiza uma categoria existente.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CADASTROS:*', 'CATEGORIA:*', 'CATEGORIA:EDITAR')")
    public ResponseEntity<CategoriaResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.ok(categoriaService.atualizar(id, request));
    }

    /**
     * Exclui logicamente uma categoria.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CADASTROS:*', 'CATEGORIA:*', 'CATEGORIA:EXCLUIR')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        categoriaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
