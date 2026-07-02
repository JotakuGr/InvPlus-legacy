package br.com.invplus.modules.cadastros.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.cadastros.dto.request.EstadoRequest;
import br.com.invplus.modules.cadastros.dto.response.EstadoResponse;
import br.com.invplus.modules.cadastros.service.EstadoService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints REST para estados.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/cadastros/estados")
@RequiredArgsConstructor
public class EstadoController {

    private final EstadoService estadoService;

    /**
     * Lista estados ativos, com filtro opcional por pais.
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('CADASTROS:*', 'CADASTROS:VISUALIZAR')")
    public ResponseEntity<Page<EstadoResponse>> listar(@RequestParam(required = false) UUID paisId, Pageable pageable) {
        return ResponseEntity.ok(estadoService.listar(paisId, pageable));
    }

    /**
     * Busca estado ativo pelo id.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CADASTROS:*', 'CADASTROS:VISUALIZAR')")
    public ResponseEntity<EstadoResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(estadoService.buscar(id));
    }

    /**
     * Cria estado e retorna a URL do recurso.
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('CADASTROS:*', 'CADASTROS:CRIAR')")
    public ResponseEntity<EstadoResponse> criar(@Valid @RequestBody EstadoRequest request) {
        EstadoResponse response = estadoService.criar(request);
        return ResponseEntity.created(URI.create(AppConstants.API_BASE_PATH + "/cadastros/estados/" + response.id()))
                .body(response);
    }

    /**
     * Atualiza estado existente.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CADASTROS:*', 'CADASTROS:EDITAR')")
    public ResponseEntity<EstadoResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody EstadoRequest request) {
        return ResponseEntity.ok(estadoService.atualizar(id, request));
    }

    /**
     * Exclui logicamente o estado.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CADASTROS:*', 'CADASTROS:EXCLUIR')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        estadoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
