package br.com.invplus.modules.pessoas.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.pessoas.dto.request.PessoaRequest;
import br.com.invplus.modules.pessoas.dto.response.PessoaResponse;
import br.com.invplus.modules.pessoas.service.PessoaService;
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
 * Endpoints REST para o cadastro base de pessoas.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/pessoas")
@RequiredArgsConstructor
public class PessoaController extends BaseCrudController {

    private final PessoaService pessoaService;

    /**
     * Lista pessoas ativas em paginas.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('PESSOA:VISUALIZAR') or hasAuthority('PESSOA:*') or hasAuthority('PESSOAS:*')")
    public ResponseEntity<Page<PessoaResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(pessoaService.listar(pageable));
    }

    /**
     * Busca uma pessoa ativa pelo identificador.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PESSOA:VISUALIZAR') or hasAuthority('PESSOA:*') or hasAuthority('PESSOAS:*')")
    public ResponseEntity<PessoaResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(pessoaService.buscar(id));
    }

    /**
     * Cria uma pessoa e devolve a URL do novo recurso.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('PESSOA:CRIAR') or hasAuthority('PESSOA:*') or hasAuthority('PESSOAS:*')")
    public ResponseEntity<PessoaResponse> criar(@Valid @RequestBody PessoaRequest request) {
        PessoaResponse response = pessoaService.criar(request);
        return created(AppConstants.API_BASE_PATH + "/pessoas", response.id(), response);
    }

    /**
     * Atualiza uma pessoa existente.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PESSOA:EDITAR') or hasAuthority('PESSOA:*') or hasAuthority('PESSOAS:*')")
    public ResponseEntity<PessoaResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody PessoaRequest request) {
        return ResponseEntity.ok(pessoaService.atualizar(id, request));
    }

    /**
     * Exclui logicamente uma pessoa.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PESSOA:EXCLUIR') or hasAuthority('PESSOA:*') or hasAuthority('PESSOAS:*')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        pessoaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
