package br.com.invplus.modules.financeiro.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.financeiro.dto.request.ExtratoBancarioLancamentoRequest;
import br.com.invplus.modules.financeiro.dto.response.ExtratoBancarioLancamentoResponse;
import br.com.invplus.modules.financeiro.enums.CategoriaLancamentoBancario;
import br.com.invplus.modules.financeiro.enums.TipoLancamentoBancario;
import br.com.invplus.modules.financeiro.service.ExtratoBancarioLancamentoService;
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
 * API de manutencao dos lancamentos importados em extratos bancarios.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/extratos-bancarios-lancamentos")
@RequiredArgsConstructor
public class ExtratoBancarioLancamentoController extends BaseCrudController {

    private final ExtratoBancarioLancamentoService service;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('EXTRATO_BANCARIO_LANCAMENTO:*', 'EXTRATO_BANCARIO_LANCAMENTO:CRIAR')")
    public ResponseEntity<ExtratoBancarioLancamentoResponse> criar(
            @Valid @RequestBody ExtratoBancarioLancamentoRequest request) {
        ExtratoBancarioLancamentoResponse response = service.criar(request);
        return created(AppConstants.API_BASE_PATH + "/extratos-bancarios-lancamentos", response.id(), response);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('EXTRATO_BANCARIO_LANCAMENTO:*', 'EXTRATO_BANCARIO_LANCAMENTO:VISUALIZAR')")
    public ResponseEntity<Page<ExtratoBancarioLancamentoResponse>> listar(
            @RequestParam(required = false) UUID extratoBancarioId,
            @RequestParam(required = false) UUID contaBancariaId,
            @RequestParam(required = false) TipoLancamentoBancario tipo,
            @RequestParam(required = false) CategoriaLancamentoBancario categoriaBancaria,
            @RequestParam(required = false) Boolean conciliado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataLancamentoDe,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataLancamentoAte,
            @RequestParam(required = false) Boolean ativo,
            Pageable pageable) {
        return ResponseEntity.ok(service.listar(extratoBancarioId, contaBancariaId, tipo,
                categoriaBancaria, conciliado, dataLancamentoDe, dataLancamentoAte, ativo, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('EXTRATO_BANCARIO_LANCAMENTO:*', 'EXTRATO_BANCARIO_LANCAMENTO:VISUALIZAR')")
    public ResponseEntity<ExtratoBancarioLancamentoResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('EXTRATO_BANCARIO_LANCAMENTO:*', 'EXTRATO_BANCARIO_LANCAMENTO:EDITAR')")
    public ResponseEntity<ExtratoBancarioLancamentoResponse> atualizar(
            @PathVariable UUID id, @Valid @RequestBody ExtratoBancarioLancamentoRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('EXTRATO_BANCARIO_LANCAMENTO:*', 'EXTRATO_BANCARIO_LANCAMENTO:EXCLUIR')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
