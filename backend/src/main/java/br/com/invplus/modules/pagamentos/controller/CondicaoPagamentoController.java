package br.com.invplus.modules.pagamentos.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.pagamentos.dto.request.CondicaoPagamentoRequest;
import br.com.invplus.modules.pagamentos.dto.request.SimulacaoCondicaoPagamentoRequest;
import br.com.invplus.modules.pagamentos.dto.response.CondicaoPagamentoResponse;
import br.com.invplus.modules.financeiro.service.ParcelaGerada;
import br.com.invplus.modules.pagamentos.service.CondicaoPagamentoService;
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
 * Endpoints REST para regras de parcelamento e vencimento.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/condicoes-pagamento")
@RequiredArgsConstructor
public class CondicaoPagamentoController extends BaseCrudController {

    private final CondicaoPagamentoService service;

    @PostMapping
    @PreAuthorize("hasAuthority('CONDICAO_PAGAMENTO:CRIAR') or hasAuthority('CONDICAO_PAGAMENTO:*')")
    public ResponseEntity<CondicaoPagamentoResponse> criar(@Valid @RequestBody CondicaoPagamentoRequest request) {
        CondicaoPagamentoResponse response = service.criar(request);
        return created(AppConstants.API_BASE_PATH + "/condicoes-pagamento", response.id(), response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CONDICAO_PAGAMENTO:VISUALIZAR') or hasAuthority('CONDICAO_PAGAMENTO:*')")
    public ResponseEntity<Page<CondicaoPagamentoResponse>> listar(
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Boolean ativo,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.listarComFiltros(codigo, nome, ativo, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CONDICAO_PAGAMENTO:VISUALIZAR') or hasAuthority('CONDICAO_PAGAMENTO:*')")
    public ResponseEntity<CondicaoPagamentoResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CONDICAO_PAGAMENTO:EDITAR') or hasAuthority('CONDICAO_PAGAMENTO:*')")
    public ResponseEntity<CondicaoPagamentoResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody CondicaoPagamentoRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CONDICAO_PAGAMENTO:EXCLUIR') or hasAuthority('CONDICAO_PAGAMENTO:*')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/simular")
    @PreAuthorize("hasAuthority('CONDICAO_PAGAMENTO:VISUALIZAR') or hasAuthority('CONDICAO_PAGAMENTO:*')")
    public ResponseEntity<java.util.List<ParcelaGerada>> simular(
            @PathVariable UUID id,
            @Valid @RequestBody SimulacaoCondicaoPagamentoRequest request
    ) {
        return ResponseEntity.ok(service.simular(id, request));
    }
}
