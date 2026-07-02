package br.com.invplus.modules.cadastros.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.cadastros.dto.request.PaisRequest;
import br.com.invplus.modules.cadastros.dto.response.PaisResponse;
import br.com.invplus.modules.cadastros.service.PaisService;
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
 * Endpoints REST para paises.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/cadastros/paises")
@RequiredArgsConstructor
public class PaisController {

    private final PaisService paisService;

    /**
     * Lista paises ativos.
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('CADASTROS:*', 'CADASTROS:VISUALIZAR')")
    public ResponseEntity<Page<PaisResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(paisService.listar(pageable));
    }

    /**
     * Busca pais ativo pelo id.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CADASTROS:*', 'CADASTROS:VISUALIZAR')")
    public ResponseEntity<PaisResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(paisService.buscar(id));
    }

    /**
     * Cria pais e retorna a URL do recurso.
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('CADASTROS:*', 'CADASTROS:CRIAR')")
    public ResponseEntity<PaisResponse> criar(@Valid @RequestBody PaisRequest request) {
        PaisResponse response = paisService.criar(request);
        return ResponseEntity.created(URI.create(AppConstants.API_BASE_PATH + "/cadastros/paises/" + response.id()))
                .body(response);
    }

    /**
     * Atualiza pais existente.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CADASTROS:*', 'CADASTROS:EDITAR')")
    public ResponseEntity<PaisResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody PaisRequest request) {
        return ResponseEntity.ok(paisService.atualizar(id, request));
    }

    /**
     * Exclui logicamente o pais.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CADASTROS:*', 'CADASTROS:EXCLUIR')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        paisService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
