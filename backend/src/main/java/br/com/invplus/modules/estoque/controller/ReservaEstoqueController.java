package br.com.invplus.modules.estoque.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.estoque.dto.request.ReservaEstoqueRequest;
import br.com.invplus.modules.estoque.dto.response.ReservaEstoqueResponse;
import br.com.invplus.modules.estoque.enums.MovimentacaoOrigem;
import br.com.invplus.modules.estoque.enums.StatusReservaEstoque;
import br.com.invplus.modules.estoque.service.ReservaEstoqueService;
import br.com.invplus.shared.api.controller.BaseCrudController;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints REST do fluxo de reservas de estoque.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/reservas-estoque")
@RequiredArgsConstructor
public class ReservaEstoqueController extends BaseCrudController {

    private final ReservaEstoqueService service;

    @GetMapping
    @PreAuthorize("hasAuthority('RESERVA_ESTOQUE:VISUALIZAR') or hasAuthority('RESERVA_ESTOQUE:*') or hasAuthority('ESTOQUE:*')")
    public ResponseEntity<Page<ReservaEstoqueResponse>> listar(
            @RequestParam(required = false) UUID estoqueId,
            @RequestParam(required = false) UUID produtoId,
            @RequestParam(required = false) UUID empresaId,
            @RequestParam(required = false) UUID filialId,
            @RequestParam(required = false) UUID depositoId,
            @RequestParam(required = false) UUID localizacaoId,
            @RequestParam(required = false) StatusReservaEstoque status,
            @RequestParam(required = false) MovimentacaoOrigem origem,
            @RequestParam(required = false) UUID origemId,
            @RequestParam(required = false) Boolean ativo,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.listarComFiltros(estoqueId, produtoId, empresaId, filialId,
                depositoId, localizacaoId, status, origem, origemId, ativo, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('RESERVA_ESTOQUE:VISUALIZAR') or hasAuthority('RESERVA_ESTOQUE:*') or hasAuthority('ESTOQUE:*')")
    public ResponseEntity<ReservaEstoqueResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('RESERVA_ESTOQUE:CRIAR') or hasAuthority('RESERVA_ESTOQUE:*') or hasAuthority('ESTOQUE:*')")
    public ResponseEntity<ReservaEstoqueResponse> criar(@Valid @RequestBody ReservaEstoqueRequest request) {
        ReservaEstoqueResponse response = service.criar(request);
        return created(AppConstants.API_BASE_PATH + "/reservas-estoque", response.id(), response);
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('RESERVA_ESTOQUE:CANCELAR') or hasAuthority('RESERVA_ESTOQUE:*') or hasAuthority('ESTOQUE:*')")
    public ResponseEntity<ReservaEstoqueResponse> cancelar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.cancelar(id));
    }

    @PostMapping("/{id}/baixar")
    @PreAuthorize("hasAuthority('RESERVA_ESTOQUE:BAIXAR') or hasAuthority('RESERVA_ESTOQUE:*') or hasAuthority('ESTOQUE:*')")
    public ResponseEntity<ReservaEstoqueResponse> baixar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.baixar(id));
    }
}
