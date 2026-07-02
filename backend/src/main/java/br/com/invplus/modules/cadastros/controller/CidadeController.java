package br.com.invplus.modules.cadastros.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.cadastros.dto.request.CidadeRequest;
import br.com.invplus.modules.cadastros.dto.response.CidadeResponse;
import br.com.invplus.modules.cadastros.service.CidadeService;
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
 * Endpoints REST para cidades.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/cadastros/cidades")
@RequiredArgsConstructor
public class CidadeController {

    private final CidadeService cidadeService;

    /**
     * Lista cidades ativas, com filtro opcional por estado.
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('CADASTROS:*', 'CADASTROS:VISUALIZAR')")
    public ResponseEntity<Page<CidadeResponse>> listar(@RequestParam(required = false) UUID estadoId, Pageable pageable) {
        return ResponseEntity.ok(cidadeService.listar(estadoId, pageable));
    }

    /**
     * Busca cidade ativa pelo id.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CADASTROS:*', 'CADASTROS:VISUALIZAR')")
    public ResponseEntity<CidadeResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(cidadeService.buscar(id));
    }

    /**
     * Cria cidade e retorna a URL do recurso.
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('CADASTROS:*', 'CADASTROS:CRIAR')")
    public ResponseEntity<CidadeResponse> criar(@Valid @RequestBody CidadeRequest request) {
        CidadeResponse response = cidadeService.criar(request);
        return ResponseEntity.created(URI.create(AppConstants.API_BASE_PATH + "/cadastros/cidades/" + response.id()))
                .body(response);
    }

    /**
     * Atualiza cidade existente.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CADASTROS:*', 'CADASTROS:EDITAR')")
    public ResponseEntity<CidadeResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody CidadeRequest request) {
        return ResponseEntity.ok(cidadeService.atualizar(id, request));
    }

    /**
     * Exclui logicamente a cidade.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CADASTROS:*', 'CADASTROS:EXCLUIR')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        cidadeService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
