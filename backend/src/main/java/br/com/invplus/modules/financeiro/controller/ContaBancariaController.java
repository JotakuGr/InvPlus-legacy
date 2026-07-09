package br.com.invplus.modules.financeiro.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.financeiro.dto.request.ContaBancariaRequest;
import br.com.invplus.modules.financeiro.dto.response.ContaBancariaResponse;
import br.com.invplus.modules.financeiro.enums.TipoContaBancaria;
import br.com.invplus.modules.financeiro.service.ContaBancariaService;
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
 * API de cadastro e consulta de contas bancarias do financeiro.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/contas-bancarias")
@RequiredArgsConstructor
public class ContaBancariaController extends BaseCrudController {

    private final ContaBancariaService service;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('CONTA_BANCARIA:*', 'CONTA_BANCARIA:CRIAR')")
    public ResponseEntity<ContaBancariaResponse> criar(@Valid @RequestBody ContaBancariaRequest request) {
        ContaBancariaResponse response = service.criar(request);
        return created(AppConstants.API_BASE_PATH + "/contas-bancarias", response.id(), response);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('CONTA_BANCARIA:*', 'CONTA_BANCARIA:VISUALIZAR')")
    public ResponseEntity<Page<ContaBancariaResponse>> listar(
            @RequestParam(required = false) UUID empresaId,
            @RequestParam(required = false) UUID filialId,
            @RequestParam(required = false) String bancoCodigo,
            @RequestParam(required = false) String bancoNome,
            @RequestParam(required = false) TipoContaBancaria tipoConta,
            @RequestParam(required = false) Boolean principal,
            @RequestParam(required = false) Boolean ativo,
            Pageable pageable) {
        return ResponseEntity.ok(service.listar(empresaId, filialId, bancoCodigo, bancoNome,
                tipoConta, principal, ativo, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CONTA_BANCARIA:*', 'CONTA_BANCARIA:VISUALIZAR')")
    public ResponseEntity<ContaBancariaResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CONTA_BANCARIA:*', 'CONTA_BANCARIA:EDITAR')")
    public ResponseEntity<ContaBancariaResponse> atualizar(
            @PathVariable UUID id, @Valid @RequestBody ContaBancariaRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CONTA_BANCARIA:*', 'CONTA_BANCARIA:EXCLUIR')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
