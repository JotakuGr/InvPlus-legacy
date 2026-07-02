package br.com.invplus.modules.cadastros.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.cadastros.dto.request.UnidadeMedidaRequest;
import br.com.invplus.modules.cadastros.dto.response.UnidadeMedidaResponse;
import br.com.invplus.modules.cadastros.service.UnidadeMedidaService;
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
 * Endpoints REST para unidades de medida do modulo Cadastros.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/unidades-medida")
@RequiredArgsConstructor
public class UnidadeMedidaController extends BaseCrudController {

    private final UnidadeMedidaService unidadeMedidaService;

    /**
     * Retorna todas as unidades de medida.
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('CADASTROS:*', 'UNIDADE_MEDIDA:VISUALIZAR')")
    public ResponseEntity<Page<UnidadeMedidaResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(unidadeMedidaService.listar(pageable));
    }

    /**
     * Retorna uma unidade pelo identificador.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CADASTROS:*', 'UNIDADE_MEDIDA:VISUALIZAR')")
    public ResponseEntity<UnidadeMedidaResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(unidadeMedidaService.buscar(id));
    }

    /**
     * Cria uma unidade de medida e devolve a URL do novo recurso.
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('CADASTROS:*', 'UNIDADE_MEDIDA:CRIAR')")
    public ResponseEntity<UnidadeMedidaResponse> criar(@Valid @RequestBody UnidadeMedidaRequest request) {
        UnidadeMedidaResponse response = unidadeMedidaService.criar(request);
        return created(AppConstants.API_BASE_PATH + "/unidades-medida", response.id(), response);
    }

    /**
     * Atualiza uma unidade de medida existente.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CADASTROS:*', 'UNIDADE_MEDIDA:EDITAR')")
    public ResponseEntity<UnidadeMedidaResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody UnidadeMedidaRequest request) {
        return ResponseEntity.ok(unidadeMedidaService.atualizar(id, request));
    }

    /**
     * Exclui logicamente uma unidade de medida.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CADASTROS:*', 'UNIDADE_MEDIDA:EXCLUIR')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        unidadeMedidaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
