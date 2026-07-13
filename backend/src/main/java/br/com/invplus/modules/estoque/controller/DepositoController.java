package br.com.invplus.modules.estoque.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.estoque.dto.request.DepositoRequest;
import br.com.invplus.modules.estoque.dto.response.DepositoResponse;
import br.com.invplus.modules.estoque.enums.TipoDeposito;
import br.com.invplus.modules.estoque.service.DepositoService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints REST para Depositos do modulo Estoque.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/depositos")
@RequiredArgsConstructor
public class DepositoController extends BaseCrudController {

    private final DepositoService service;

    @GetMapping
    @PreAuthorize("hasAuthority('DEPOSITO:VISUALIZAR') or hasAuthority('DEPOSITO:*') or hasAuthority('ESTOQUE:*')")
    public ResponseEntity<Page<DepositoResponse>> listar(
            @RequestParam(required = false) UUID filialId,
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) TipoDeposito tipoDeposito,
            @RequestParam(required = false) Boolean principal,
            @RequestParam(required = false) Boolean controlaEstoque,
            @RequestParam(required = false) Boolean bloqueado,
            @RequestParam(required = false) Boolean ativo,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.listarComFiltros(filialId, codigo, nome, tipoDeposito,
                principal, controlaEstoque, bloqueado, ativo, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DEPOSITO:VISUALIZAR') or hasAuthority('DEPOSITO:*') or hasAuthority('ESTOQUE:*')")
    public ResponseEntity<DepositoResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('DEPOSITO:CRIAR') or hasAuthority('DEPOSITO:*') or hasAuthority('ESTOQUE:*')")
    public ResponseEntity<DepositoResponse> criar(@Valid @RequestBody DepositoRequest request) {
        DepositoResponse response = service.criar(request);
        return created(AppConstants.API_BASE_PATH + "/depositos", response.id(), response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DEPOSITO:EDITAR') or hasAuthority('DEPOSITO:*') or hasAuthority('ESTOQUE:*')")
    public ResponseEntity<DepositoResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody DepositoRequest request
    ) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DEPOSITO:EXCLUIR') or hasAuthority('DEPOSITO:*') or hasAuthority('ESTOQUE:*')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
