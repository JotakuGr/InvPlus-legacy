package br.com.invplus.modules.estoque.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.estoque.dto.request.EstoqueRequest;
import br.com.invplus.modules.estoque.dto.response.EstoqueCompletoResponse;
import br.com.invplus.modules.estoque.dto.response.EstoqueDisponivelResponse;
import br.com.invplus.modules.estoque.dto.response.EstoqueResponse;
import br.com.invplus.modules.estoque.service.EstoqueAggregateService;
import br.com.invplus.modules.estoque.service.EstoqueService;
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
 * Endpoints REST para saldo atual de estoque.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/estoques")
@RequiredArgsConstructor
public class EstoqueController extends BaseCrudController {

    private final EstoqueService service;
    private final EstoqueAggregateService aggregateService;

    @GetMapping
    @PreAuthorize("hasAuthority('ESTOQUE_SALDO:VISUALIZAR') or hasAuthority('ESTOQUE_SALDO:*') or hasAuthority('ESTOQUE:*')")
    public ResponseEntity<Page<EstoqueResponse>> listar(
            @RequestParam(required = false) UUID produtoId,
            @RequestParam(required = false) UUID filialId,
            @RequestParam(required = false) UUID depositoId,
            @RequestParam(required = false) UUID localizacaoId,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) Boolean estoqueBaixo,
            @RequestParam(required = false) Boolean estoqueZerado,
            @RequestParam(required = false) Boolean comSaldo,
            @RequestParam(required = false) Boolean semSaldo,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.listarComFiltros(produtoId, filialId, depositoId,
                localizacaoId, ativo, estoqueBaixo, estoqueZerado, comSaldo, semSaldo, pageable));
    }

    @GetMapping("/saldo")
    @PreAuthorize("hasAuthority('ESTOQUE_SALDO:VISUALIZAR') or hasAuthority('ESTOQUE_SALDO:*') or hasAuthority('ESTOQUE:*')")
    public ResponseEntity<EstoqueResponse> buscarSaldo(
            @RequestParam UUID produtoId,
            @RequestParam UUID depositoId,
            @RequestParam UUID localizacaoId
    ) {
        return ResponseEntity.ok(service.buscarSaldo(produtoId, depositoId, localizacaoId));
    }

    @GetMapping("/disponivel")
    @PreAuthorize("hasAuthority('ESTOQUE_SALDO:VISUALIZAR') or hasAuthority('ESTOQUE_SALDO:*') or hasAuthority('ESTOQUE:*')")
    public ResponseEntity<EstoqueDisponivelResponse> buscarDisponivel(
            @RequestParam UUID produtoId,
            @RequestParam UUID depositoId,
            @RequestParam UUID localizacaoId
    ) {
        return ResponseEntity.ok(aggregateService.buscarDisponivel(produtoId, depositoId, localizacaoId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ESTOQUE_SALDO:VISUALIZAR') or hasAuthority('ESTOQUE_SALDO:*') or hasAuthority('ESTOQUE:*')")
    public ResponseEntity<EstoqueResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @GetMapping("/{id}/completo")
    @PreAuthorize("hasAuthority('ESTOQUE_SALDO:VISUALIZAR') or hasAuthority('ESTOQUE_SALDO:*') or hasAuthority('ESTOQUE:*')")
    public ResponseEntity<EstoqueCompletoResponse> buscarCompleto(@PathVariable UUID id) {
        return ResponseEntity.ok(aggregateService.buscar(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ESTOQUE_SALDO:CRIAR') or hasAuthority('ESTOQUE_SALDO:*') or hasAuthority('ESTOQUE:*')")
    public ResponseEntity<EstoqueResponse> criar(@Valid @RequestBody EstoqueRequest request) {
        EstoqueResponse response = service.criar(request);
        return created(AppConstants.API_BASE_PATH + "/estoques", response.id(), response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ESTOQUE_SALDO:EDITAR') or hasAuthority('ESTOQUE_SALDO:*') or hasAuthority('ESTOQUE:*')")
    public ResponseEntity<EstoqueResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody EstoqueRequest request
    ) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ESTOQUE_SALDO:EXCLUIR') or hasAuthority('ESTOQUE_SALDO:*') or hasAuthority('ESTOQUE:*')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
