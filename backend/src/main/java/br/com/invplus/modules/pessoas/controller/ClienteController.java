package br.com.invplus.modules.pessoas.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.pessoas.dto.request.ClienteRequest;
import br.com.invplus.modules.pessoas.dto.response.ClienteResponse;
import br.com.invplus.modules.pessoas.service.ClienteService;
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
 * Endpoints REST para o papel comercial Cliente.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/clientes")
@RequiredArgsConstructor
public class ClienteController extends BaseCrudController {

    private final ClienteService clienteService;

    /**
     * Lista clientes ativos em paginas.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('CLIENTE:VISUALIZAR') or hasAuthority('CLIENTE:*') or hasAuthority('PESSOAS:*')")
    public ResponseEntity<Page<ClienteResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(clienteService.listar(pageable));
    }

    /**
     * Busca um cliente ativo pelo identificador.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENTE:VISUALIZAR') or hasAuthority('CLIENTE:*') or hasAuthority('PESSOAS:*')")
    public ResponseEntity<ClienteResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(clienteService.buscar(id));
    }

    /**
     * Cria cliente vinculado a uma pessoa existente.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('CLIENTE:CRIAR') or hasAuthority('CLIENTE:*') or hasAuthority('PESSOAS:*')")
    public ResponseEntity<ClienteResponse> criar(@Valid @RequestBody ClienteRequest request) {
        ClienteResponse response = clienteService.criar(request);
        return created(AppConstants.API_BASE_PATH + "/clientes", response.id(), response);
    }

    /**
     * Atualiza dados comerciais do cliente.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENTE:EDITAR') or hasAuthority('CLIENTE:*') or hasAuthority('PESSOAS:*')")
    public ResponseEntity<ClienteResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.ok(clienteService.atualizar(id, request));
    }

    /**
     * Exclui logicamente o cliente.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENTE:EXCLUIR') or hasAuthority('CLIENTE:*') or hasAuthority('PESSOAS:*')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        clienteService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
