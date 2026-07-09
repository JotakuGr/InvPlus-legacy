package br.com.invplus.modules.financeiro.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.financeiro.dto.request.PlanoContaRequest;
import br.com.invplus.modules.financeiro.dto.response.PlanoContaResponse;
import br.com.invplus.modules.financeiro.enums.NaturezaPlanoConta;
import br.com.invplus.modules.financeiro.enums.TipoPlanoConta;
import br.com.invplus.modules.financeiro.service.PlanoContaService;
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
 * API de cadastro e consulta da arvore do plano de contas.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/planos-contas")
@RequiredArgsConstructor
public class PlanoContaController extends BaseCrudController {

    private final PlanoContaService service;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('PLANO_CONTA:*', 'PLANO_CONTA:CRIAR')")
    public ResponseEntity<PlanoContaResponse> criar(@Valid @RequestBody PlanoContaRequest request) {
        PlanoContaResponse response = service.criar(request);
        return created(AppConstants.API_BASE_PATH + "/planos-contas", response.id(), response);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PLANO_CONTA:*', 'PLANO_CONTA:VISUALIZAR')")
    public ResponseEntity<Page<PlanoContaResponse>> listar(
            @RequestParam(required = false) UUID empresaId,
            @RequestParam(required = false) UUID planoContaPaiId,
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) TipoPlanoConta tipo,
            @RequestParam(required = false) NaturezaPlanoConta natureza,
            @RequestParam(required = false) Boolean ativo,
            Pageable pageable) {
        return ResponseEntity.ok(service.listar(empresaId, planoContaPaiId, codigo,
                nome, tipo, natureza, ativo, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PLANO_CONTA:*', 'PLANO_CONTA:VISUALIZAR')")
    public ResponseEntity<PlanoContaResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PLANO_CONTA:*', 'PLANO_CONTA:EDITAR')")
    public ResponseEntity<PlanoContaResponse> atualizar(
            @PathVariable UUID id, @Valid @RequestBody PlanoContaRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PLANO_CONTA:*', 'PLANO_CONTA:EXCLUIR')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
