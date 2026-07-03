package br.com.invplus.modules.pessoas.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.pessoas.dto.request.TransportadoraRequest;
import br.com.invplus.modules.pessoas.dto.response.TransportadoraResponse;
import br.com.invplus.modules.pessoas.service.TransportadoraService;
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
 * Endpoints REST para gerenciar o papel Transportadora de uma Pessoa.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/transportadoras")
@RequiredArgsConstructor
public class TransportadoraController extends BaseCrudController {

    private final TransportadoraService transportadoraService;

    @GetMapping
    @PreAuthorize("hasAuthority('TRANSPORTADORA:VISUALIZAR') or hasAuthority('TRANSPORTADORA:*') or hasAuthority('PESSOAS:*')")
    public ResponseEntity<Page<TransportadoraResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(transportadoraService.listar(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TRANSPORTADORA:VISUALIZAR') or hasAuthority('TRANSPORTADORA:*') or hasAuthority('PESSOAS:*')")
    public ResponseEntity<TransportadoraResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(transportadoraService.buscar(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('TRANSPORTADORA:CRIAR') or hasAuthority('TRANSPORTADORA:*') or hasAuthority('PESSOAS:*')")
    public ResponseEntity<TransportadoraResponse> criar(@Valid @RequestBody TransportadoraRequest request) {
        TransportadoraResponse response = transportadoraService.criar(request);
        return created(AppConstants.API_BASE_PATH + "/transportadoras", response.id(), response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('TRANSPORTADORA:EDITAR') or hasAuthority('TRANSPORTADORA:*') or hasAuthority('PESSOAS:*')")
    public ResponseEntity<TransportadoraResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody TransportadoraRequest request) {
        return ResponseEntity.ok(transportadoraService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('TRANSPORTADORA:EXCLUIR') or hasAuthority('TRANSPORTADORA:*') or hasAuthority('PESSOAS:*')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        transportadoraService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
