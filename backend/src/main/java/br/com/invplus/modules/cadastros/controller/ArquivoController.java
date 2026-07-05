package br.com.invplus.modules.cadastros.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.cadastros.dto.request.ArquivoRequest;
import br.com.invplus.modules.cadastros.dto.response.ArquivoResponse;
import br.com.invplus.modules.cadastros.service.ArquivoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

/**
 * Endpoints REST para metadados de arquivos do modulo Cadastros.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/arquivos")
@RequiredArgsConstructor
public class ArquivoController {

    private final ArquivoService arquivoService;

    /**
     * Lista arquivos, opcionalmente filtrando por entidade vinculada.
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ARQUIVO:VISUALIZAR', 'ARQUIVO:*', 'CADASTROS:*')")
    public ResponseEntity<Page<ArquivoResponse>> listar(
            @RequestParam(required = false) String entidade,
            @RequestParam(required = false) UUID entidadeId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(arquivoService.listar(entidade, entidadeId, pageable));
    }

    /**
     * Retorna um arquivo pelo identificador.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ARQUIVO:VISUALIZAR', 'ARQUIVO:*', 'CADASTROS:*')")
    public ResponseEntity<ArquivoResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(arquivoService.buscar(id));
    }

    /**
     * Cria metadados de arquivo e devolve a URL do novo recurso.
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ARQUIVO:CRIAR', 'ARQUIVO:*', 'CADASTROS:*')")
    public ResponseEntity<ArquivoResponse> criar(@Valid @RequestBody ArquivoRequest request) {
        ArquivoResponse response = arquivoService.criar(request);
        return ResponseEntity.created(URI.create(AppConstants.API_BASE_PATH + "/arquivos/" + response.id()))
                .body(response);
    }

    /**
     * Faz upload do binario e registra os metadados do arquivo salvo.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('ARQUIVO:CRIAR', 'ARQUIVO:*', 'CADASTROS:*')")
    public ResponseEntity<ArquivoResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam String entidade,
            @RequestParam UUID entidadeId
    ) {
        ArquivoResponse response = arquivoService.upload(file, entidade, entidadeId);
        return ResponseEntity.created(URI.create(AppConstants.API_BASE_PATH + "/arquivos/" + response.id()))
                .body(response);
    }

    /**
     * Atualiza metadados de arquivo existentes.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ARQUIVO:EDITAR', 'ARQUIVO:*', 'CADASTROS:*')")
    public ResponseEntity<ArquivoResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody ArquivoRequest request) {
        return ResponseEntity.ok(arquivoService.atualizar(id, request));
    }

    /**
     * Exclui logicamente os metadados de arquivo.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ARQUIVO:EXCLUIR', 'ARQUIVO:*', 'CADASTROS:*')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        arquivoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
