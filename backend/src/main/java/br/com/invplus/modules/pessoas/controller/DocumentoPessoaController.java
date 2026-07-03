package br.com.invplus.modules.pessoas.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.pessoas.dto.request.DocumentoPessoaRequest;
import br.com.invplus.modules.pessoas.dto.response.DocumentoPessoaResponse;
import br.com.invplus.modules.pessoas.service.DocumentoPessoaService;
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
 * Endpoints REST para documentos vinculados a Pessoa.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/documentos-pessoa")
@RequiredArgsConstructor
public class DocumentoPessoaController extends BaseCrudController {

    private final DocumentoPessoaService documentoPessoaService;

    @GetMapping
    @PreAuthorize("hasAuthority('DOCUMENTO_PESSOA:VISUALIZAR') or hasAuthority('DOCUMENTO_PESSOA:*') or hasAuthority('PESSOAS:*')")
    public ResponseEntity<Page<DocumentoPessoaResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(documentoPessoaService.listar(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENTO_PESSOA:VISUALIZAR') or hasAuthority('DOCUMENTO_PESSOA:*') or hasAuthority('PESSOAS:*')")
    public ResponseEntity<DocumentoPessoaResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(documentoPessoaService.buscar(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('DOCUMENTO_PESSOA:CRIAR') or hasAuthority('DOCUMENTO_PESSOA:*') or hasAuthority('PESSOAS:*')")
    public ResponseEntity<DocumentoPessoaResponse> criar(@Valid @RequestBody DocumentoPessoaRequest request) {
        DocumentoPessoaResponse response = documentoPessoaService.criar(request);
        return created(AppConstants.API_BASE_PATH + "/documentos-pessoa", response.id(), response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENTO_PESSOA:EDITAR') or hasAuthority('DOCUMENTO_PESSOA:*') or hasAuthority('PESSOAS:*')")
    public ResponseEntity<DocumentoPessoaResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody DocumentoPessoaRequest request) {
        return ResponseEntity.ok(documentoPessoaService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENTO_PESSOA:EXCLUIR') or hasAuthority('DOCUMENTO_PESSOA:*') or hasAuthority('PESSOAS:*')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        documentoPessoaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
