package br.com.invplus.modules.pagamentos.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.pagamentos.dto.request.FormaPagamentoRequest;
import br.com.invplus.modules.pagamentos.dto.response.FormaPagamentoResponse;
import br.com.invplus.modules.pagamentos.enums.TipoFormaPagamento;
import br.com.invplus.modules.pagamentos.service.FormaPagamentoService;
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
 * Endpoints REST para meios de pagamento do ERP.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/formas-pagamento")
@RequiredArgsConstructor
public class FormaPagamentoController extends BaseCrudController {

    private final FormaPagamentoService service;

    @PostMapping
    @PreAuthorize("hasAuthority('FORMA_PAGAMENTO:CRIAR') or hasAuthority('FORMA_PAGAMENTO:*')")
    public ResponseEntity<FormaPagamentoResponse> criar(@Valid @RequestBody FormaPagamentoRequest request) {
        FormaPagamentoResponse response = service.criar(request);
        return created(AppConstants.API_BASE_PATH + "/formas-pagamento", response.id(), response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('FORMA_PAGAMENTO:VISUALIZAR') or hasAuthority('FORMA_PAGAMENTO:*')")
    public ResponseEntity<Page<FormaPagamentoResponse>> listar(
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) TipoFormaPagamento tipo,
            @RequestParam(required = false) Boolean ativo,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.listarComFiltros(codigo, nome, tipo, ativo, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('FORMA_PAGAMENTO:VISUALIZAR') or hasAuthority('FORMA_PAGAMENTO:*')")
    public ResponseEntity<FormaPagamentoResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('FORMA_PAGAMENTO:EDITAR') or hasAuthority('FORMA_PAGAMENTO:*')")
    public ResponseEntity<FormaPagamentoResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody FormaPagamentoRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('FORMA_PAGAMENTO:EXCLUIR') or hasAuthority('FORMA_PAGAMENTO:*')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
