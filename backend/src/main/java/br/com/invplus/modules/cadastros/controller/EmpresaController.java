package br.com.invplus.modules.cadastros.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.cadastros.dto.request.EmpresaRequest;
import br.com.invplus.modules.cadastros.dto.response.EmpresaResponse;
import br.com.invplus.modules.cadastros.service.EmpresaService;
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
 * Endpoints REST para empresas do modulo Cadastros.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/empresas")
@RequiredArgsConstructor
public class EmpresaController extends BaseCrudController {

    private final EmpresaService empresaService;

    /**
     * Retorna todas as empresas cadastradas.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('EMPRESA:VISUALIZAR') or hasAuthority('EMPRESA:*') or hasAuthority('CADASTROS:*')")
    public ResponseEntity<Page<EmpresaResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(empresaService.listar(pageable));
    }

    /**
     * Retorna uma empresa pelo identificador.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPRESA:VISUALIZAR') or hasAuthority('EMPRESA:*') or hasAuthority('CADASTROS:*')")
    public ResponseEntity<EmpresaResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(empresaService.buscar(id));
    }

    /**
     * Cria uma empresa e devolve a URL do novo recurso.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('EMPRESA:CRIAR') or hasAuthority('EMPRESA:*') or hasAuthority('CADASTROS:*')")
    public ResponseEntity<EmpresaResponse> criar(@Valid @RequestBody EmpresaRequest request) {
        EmpresaResponse response = empresaService.criar(request);
        return created(AppConstants.API_BASE_PATH + "/empresas", response.id(), response);
    }

    /**
     * Atualiza uma empresa existente.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPRESA:EDITAR') or hasAuthority('EMPRESA:*') or hasAuthority('CADASTROS:*')")
    public ResponseEntity<EmpresaResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody EmpresaRequest request) {
        return ResponseEntity.ok(empresaService.atualizar(id, request));
    }

    /**
     * Exclui logicamente uma empresa.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPRESA:EXCLUIR') or hasAuthority('EMPRESA:*') or hasAuthority('CADASTROS:*')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        empresaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
