package br.com.invplus.modules.financeiro.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.financeiro.dto.request.ExtratoBancarioRequest;
import br.com.invplus.modules.financeiro.dto.response.ExtratoBancarioResponse;
import br.com.invplus.modules.financeiro.enums.OrigemExtratoBancario;
import br.com.invplus.modules.financeiro.enums.StatusExtratoBancario;
import br.com.invplus.modules.financeiro.service.ExtratoBancarioService;
import br.com.invplus.shared.api.controller.BaseCrudController;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
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
 * API de importacao, consulta e cancelamento de extratos bancarios.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/extratos-bancarios")
@RequiredArgsConstructor
public class ExtratoBancarioController extends BaseCrudController {

    private final ExtratoBancarioService service;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('EXTRATO_BANCARIO:*', 'EXTRATO_BANCARIO:IMPORTAR')")
    public ResponseEntity<ExtratoBancarioResponse> importar(@Valid @RequestBody ExtratoBancarioRequest request) {
        ExtratoBancarioResponse response = service.importar(request);
        return created(AppConstants.API_BASE_PATH + "/extratos-bancarios", response.id(), response);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('EXTRATO_BANCARIO:*', 'EXTRATO_BANCARIO:VISUALIZAR')")
    public ResponseEntity<Page<ExtratoBancarioResponse>> listar(
            @RequestParam(required = false) UUID contaBancariaId,
            @RequestParam(required = false) OrigemExtratoBancario origem,
            @RequestParam(required = false) StatusExtratoBancario status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicioDe,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicioAte,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFimDe,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFimAte,
            @RequestParam(required = false) Boolean ativo,
            Pageable pageable) {
        return ResponseEntity.ok(service.listar(contaBancariaId, origem, status, dataInicioDe,
                dataInicioAte, dataFimDe, dataFimAte, ativo, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('EXTRATO_BANCARIO:*', 'EXTRATO_BANCARIO:VISUALIZAR')")
    public ResponseEntity<ExtratoBancarioResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyAuthority('EXTRATO_BANCARIO:*', 'EXTRATO_BANCARIO:CANCELAR')")
    public ResponseEntity<ExtratoBancarioResponse> cancelar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.cancelar(id));
    }
}
