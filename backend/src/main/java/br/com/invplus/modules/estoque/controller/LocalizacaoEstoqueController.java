package br.com.invplus.modules.estoque.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.estoque.dto.request.LocalizacaoEstoqueRequest;
import br.com.invplus.modules.estoque.dto.response.LocalizacaoEstoqueResponse;
import br.com.invplus.modules.estoque.service.LocalizacaoEstoqueService;
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
 * Endpoints REST para LocalizacaoEstoque.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/localizacoes-estoque")
@RequiredArgsConstructor
public class LocalizacaoEstoqueController extends BaseCrudController {

    private final LocalizacaoEstoqueService service;

    @GetMapping
    @PreAuthorize("hasAuthority('LOCALIZACAO_ESTOQUE:VISUALIZAR') or hasAuthority('LOCALIZACAO_ESTOQUE:*') or hasAuthority('ESTOQUE:*')")
    public ResponseEntity<Page<LocalizacaoEstoqueResponse>> listar(
            @RequestParam(required = false) UUID depositoId,
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) String rua,
            @RequestParam(required = false) String corredor,
            @RequestParam(required = false) String prateleira,
            @RequestParam(required = false) Boolean principal,
            @RequestParam(required = false) Boolean bloqueada,
            @RequestParam(required = false) Boolean ativo,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.listarComFiltros(depositoId, codigo, descricao, rua, corredor,
                prateleira, principal, bloqueada, ativo, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('LOCALIZACAO_ESTOQUE:VISUALIZAR') or hasAuthority('LOCALIZACAO_ESTOQUE:*') or hasAuthority('ESTOQUE:*')")
    public ResponseEntity<LocalizacaoEstoqueResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('LOCALIZACAO_ESTOQUE:CRIAR') or hasAuthority('LOCALIZACAO_ESTOQUE:*') or hasAuthority('ESTOQUE:*')")
    public ResponseEntity<LocalizacaoEstoqueResponse> criar(@Valid @RequestBody LocalizacaoEstoqueRequest request) {
        LocalizacaoEstoqueResponse response = service.criar(request);
        return created(AppConstants.API_BASE_PATH + "/localizacoes-estoque", response.id(), response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('LOCALIZACAO_ESTOQUE:EDITAR') or hasAuthority('LOCALIZACAO_ESTOQUE:*') or hasAuthority('ESTOQUE:*')")
    public ResponseEntity<LocalizacaoEstoqueResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody LocalizacaoEstoqueRequest request
    ) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('LOCALIZACAO_ESTOQUE:EXCLUIR') or hasAuthority('LOCALIZACAO_ESTOQUE:*') or hasAuthority('ESTOQUE:*')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
