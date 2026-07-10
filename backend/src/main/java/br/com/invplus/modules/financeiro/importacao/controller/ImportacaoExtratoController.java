package br.com.invplus.modules.financeiro.importacao.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.financeiro.importacao.dto.ImportacaoExtratoResponse;
import br.com.invplus.modules.financeiro.importacao.dto.ImportacaoExtratoResumoResponse;
import br.com.invplus.modules.financeiro.importacao.enums.FormatoArquivoExtrato;
import br.com.invplus.modules.financeiro.importacao.enums.StatusImportacaoExtrato;
import br.com.invplus.modules.financeiro.importacao.service.ImportacaoExtratoService;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** API de upload OFX e acompanhamento das importacoes. */
@RestController
@RequiredArgsConstructor
public class ImportacaoExtratoController {

    private final ImportacaoExtratoService service;

    @PostMapping(value = AppConstants.API_BASE_PATH + "/extratos-bancarios/importar/ofx",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('IMPORTACAO_EXTRATO:*', 'IMPORTACAO_EXTRATO:IMPORTAR')")
    public ResponseEntity<ImportacaoExtratoResponse> importarOfx(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam(required = false) UUID contaBancariaId,
            @RequestParam(required = false) String observacao) {
        ImportacaoExtratoResponse response = service.importarOfx(arquivo, contaBancariaId, observacao);
        return ResponseEntity.created(URI.create(AppConstants.API_BASE_PATH + "/importacoes-extratos/"
                + response.importacaoId())).body(response);
    }

    @GetMapping(AppConstants.API_BASE_PATH + "/importacoes-extratos")
    @PreAuthorize("hasAnyAuthority('IMPORTACAO_EXTRATO:*', 'IMPORTACAO_EXTRATO:VISUALIZAR')")
    public ResponseEntity<Page<ImportacaoExtratoResumoResponse>> listar(
            @RequestParam(required = false) UUID contaBancariaId,
            @RequestParam(required = false) FormatoArquivoExtrato formato,
            @RequestParam(required = false) StatusImportacaoExtrato status,
            @RequestParam(required = false) String arquivoHash,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime iniciadaEmDe,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime iniciadaEmAte,
            Pageable pageable) {
        return ResponseEntity.ok(service.listar(contaBancariaId, formato, status, arquivoHash,
                iniciadaEmDe, iniciadaEmAte, pageable));
    }

    @GetMapping(AppConstants.API_BASE_PATH + "/importacoes-extratos/{id}")
    @PreAuthorize("hasAnyAuthority('IMPORTACAO_EXTRATO:*', 'IMPORTACAO_EXTRATO:VISUALIZAR')")
    public ResponseEntity<ImportacaoExtratoResumoResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscar(id));
    }
}
