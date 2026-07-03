package br.com.invplus.modules.pessoas.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.pessoas.dto.request.FornecedorRequest;
import br.com.invplus.modules.pessoas.dto.response.FornecedorResponse;
import br.com.invplus.modules.pessoas.service.FornecedorService;
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
 * Endpoints REST para gerenciar o papel Fornecedor de uma Pessoa.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/fornecedores")
@RequiredArgsConstructor
public class FornecedorController extends BaseCrudController {

    private final FornecedorService fornecedorService;

    @GetMapping
    @PreAuthorize("hasAuthority('FORNECEDOR:VISUALIZAR') or hasAuthority('FORNECEDOR:*') or hasAuthority('PESSOAS:*')")
    public ResponseEntity<Page<FornecedorResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(fornecedorService.listar(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('FORNECEDOR:VISUALIZAR') or hasAuthority('FORNECEDOR:*') or hasAuthority('PESSOAS:*')")
    public ResponseEntity<FornecedorResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(fornecedorService.buscar(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('FORNECEDOR:CRIAR') or hasAuthority('FORNECEDOR:*') or hasAuthority('PESSOAS:*')")
    public ResponseEntity<FornecedorResponse> criar(@Valid @RequestBody FornecedorRequest request) {
        FornecedorResponse response = fornecedorService.criar(request);
        return created(AppConstants.API_BASE_PATH + "/fornecedores", response.id(), response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('FORNECEDOR:EDITAR') or hasAuthority('FORNECEDOR:*') or hasAuthority('PESSOAS:*')")
    public ResponseEntity<FornecedorResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody FornecedorRequest request) {
        return ResponseEntity.ok(fornecedorService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('FORNECEDOR:EXCLUIR') or hasAuthority('FORNECEDOR:*') or hasAuthority('PESSOAS:*')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        fornecedorService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
