package br.com.invplus.modules.estoque.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.estoque.dto.response.MovimentacaoEstoqueResponse;
import br.com.invplus.modules.estoque.enums.MovimentacaoOrigem;
import br.com.invplus.modules.estoque.enums.TipoMovimentacaoEstoque;
import br.com.invplus.modules.estoque.service.MovimentacaoEstoqueService;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints somente-leitura para historico de movimentacoes de estoque.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/movimentacoes-estoque")
@RequiredArgsConstructor
public class MovimentacaoEstoqueController {

    private final MovimentacaoEstoqueService service;

    @GetMapping
    @PreAuthorize("hasAuthority('MOVIMENTACAO_ESTOQUE:VISUALIZAR') or hasAuthority('MOVIMENTACAO_ESTOQUE:*') or hasAuthority('ESTOQUE:*')")
    public ResponseEntity<Page<MovimentacaoEstoqueResponse>> listar(
            @RequestParam(required = false) UUID estoqueId,
            @RequestParam(required = false) UUID produtoId,
            @RequestParam(required = false) UUID depositoId,
            @RequestParam(required = false) UUID localizacaoId,
            @RequestParam(required = false) TipoMovimentacaoEstoque tipoMovimentacao,
            @RequestParam(required = false) MovimentacaoOrigem origem,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dataInicial,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dataFinal,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.listarComFiltros(estoqueId, produtoId, depositoId, localizacaoId,
                tipoMovimentacao, origem, dataInicial, dataFinal, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MOVIMENTACAO_ESTOQUE:VISUALIZAR') or hasAuthority('MOVIMENTACAO_ESTOQUE:*') or hasAuthority('ESTOQUE:*')")
    public ResponseEntity<MovimentacaoEstoqueResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscar(id));
    }
}
