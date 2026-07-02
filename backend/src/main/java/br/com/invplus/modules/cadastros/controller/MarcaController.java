package br.com.invplus.modules.cadastros.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.cadastros.dto.request.MarcaRequest;
import br.com.invplus.modules.cadastros.dto.response.MarcaResponse;
import br.com.invplus.modules.cadastros.service.MarcaService;
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
 * Endpoints REST para marcas do modulo Cadastros.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/cadastros/marcas")
@RequiredArgsConstructor
public class MarcaController {

    private final MarcaService marcaService;

    /**
     * Retorna todas as marcas cadastradas.
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('CADASTROS:*', 'CADASTROS:VISUALIZAR')")
    public ResponseEntity<Page<MarcaResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(marcaService.listar(pageable));
    }

    /**
     * Retorna uma marca pelo identificador.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CADASTROS:*', 'CADASTROS:VISUALIZAR')")
    public ResponseEntity<MarcaResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(marcaService.buscar(id));
    }

    /**
     * Cria uma marca e devolve a URL do novo recurso.
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('CADASTROS:*', 'CADASTROS:CRIAR')")
    public ResponseEntity<MarcaResponse> criar(@Valid @RequestBody MarcaRequest request) {
        MarcaResponse response = marcaService.criar(request);
        return ResponseEntity.created(URI.create(AppConstants.API_BASE_PATH + "/cadastros/marcas/" + response.id()))
                .body(response);
    }

    /**
     * Atualiza uma marca existente.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CADASTROS:*', 'CADASTROS:EDITAR')")
    public ResponseEntity<MarcaResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody MarcaRequest request) {
        return ResponseEntity.ok(marcaService.atualizar(id, request));
    }

    /**
     * Exclui logicamente uma marca.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CADASTROS:*', 'CADASTROS:EXCLUIR')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        marcaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
