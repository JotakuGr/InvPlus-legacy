package br.com.invplus.modules.cadastros.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.cadastros.dto.request.FilialRequest;
import br.com.invplus.modules.cadastros.dto.response.FilialResponse;
import br.com.invplus.modules.cadastros.service.FilialService;
import jakarta.validation.Valid;
import java.net.URI;
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
 * Endpoints REST para filiais.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/cadastros/filiais")
@RequiredArgsConstructor
public class FilialController {

    private final FilialService filialService;

    /**
     * Lista filiais ativas da empresa autenticada.
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('CADASTROS:*', 'CADASTROS:VISUALIZAR')")
    public ResponseEntity<Page<FilialResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(filialService.listar(pageable));
    }

    /**
     * Busca filial ativa pelo id.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CADASTROS:*', 'CADASTROS:VISUALIZAR')")
    public ResponseEntity<FilialResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(filialService.buscar(id));
    }

    /**
     * Cria filial e retorna a URL do recurso.
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('CADASTROS:*', 'CADASTROS:CRIAR')")
    public ResponseEntity<FilialResponse> criar(@Valid @RequestBody FilialRequest request) {
        FilialResponse response = filialService.criar(request);
        return ResponseEntity.created(URI.create(AppConstants.API_BASE_PATH + "/cadastros/filiais/" + response.id()))
                .body(response);
    }

    /**
     * Atualiza filial existente.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CADASTROS:*', 'CADASTROS:EDITAR')")
    public ResponseEntity<FilialResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody FilialRequest request) {
        return ResponseEntity.ok(filialService.atualizar(id, request));
    }

    /**
     * Exclui logicamente a filial.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CADASTROS:*', 'CADASTROS:EXCLUIR')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        filialService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
