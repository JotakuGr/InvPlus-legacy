package br.com.invplus.modules.financeiro.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.financeiro.dto.request.CentroCustoRequest;
import br.com.invplus.modules.financeiro.dto.response.CentroCustoResponse;
import br.com.invplus.modules.financeiro.enums.TipoCentroCusto;
import br.com.invplus.modules.financeiro.service.CentroCustoService;
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
 * API de cadastro e consulta da arvore de centros de custo.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/centros-custo")
@RequiredArgsConstructor
public class CentroCustoController extends BaseCrudController {

    private final CentroCustoService service;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('CENTRO_CUSTO:*', 'CENTRO_CUSTO:CRIAR')")
    public ResponseEntity<CentroCustoResponse> criar(@Valid @RequestBody CentroCustoRequest request) {
        CentroCustoResponse response = service.criar(request);
        return created(AppConstants.API_BASE_PATH + "/centros-custo", response.id(), response);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('CENTRO_CUSTO:*', 'CENTRO_CUSTO:VISUALIZAR')")
    public ResponseEntity<Page<CentroCustoResponse>> listar(
            @RequestParam(required = false) UUID centroCustoPaiId,
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) TipoCentroCusto tipo,
            @RequestParam(required = false) Boolean ativo,
            Pageable pageable) {
        return ResponseEntity.ok(service.listar(centroCustoPaiId, codigo, nome, tipo, ativo, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CENTRO_CUSTO:*', 'CENTRO_CUSTO:VISUALIZAR')")
    public ResponseEntity<CentroCustoResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CENTRO_CUSTO:*', 'CENTRO_CUSTO:EDITAR')")
    public ResponseEntity<CentroCustoResponse> atualizar(
            @PathVariable UUID id, @Valid @RequestBody CentroCustoRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CENTRO_CUSTO:*', 'CENTRO_CUSTO:EXCLUIR')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
