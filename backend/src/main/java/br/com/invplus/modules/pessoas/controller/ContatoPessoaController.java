package br.com.invplus.modules.pessoas.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.pessoas.dto.request.ContatoPessoaRequest;
import br.com.invplus.modules.pessoas.dto.response.ContatoPessoaResponse;
import br.com.invplus.modules.pessoas.service.ContatoPessoaService;
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
 * Endpoints REST para contatos operacionais de Pessoa.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/contatos-pessoa")
@RequiredArgsConstructor
public class ContatoPessoaController extends BaseCrudController {

    private final ContatoPessoaService contatoPessoaService;

    @GetMapping
    @PreAuthorize("hasAuthority('CONTATO_PESSOA:VISUALIZAR') or hasAuthority('CONTATO_PESSOA:*') or hasAuthority('PESSOAS:*')")
    public ResponseEntity<Page<ContatoPessoaResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(contatoPessoaService.listar(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTATO_PESSOA:VISUALIZAR') or hasAuthority('CONTATO_PESSOA:*') or hasAuthority('PESSOAS:*')")
    public ResponseEntity<ContatoPessoaResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(contatoPessoaService.buscar(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CONTATO_PESSOA:CRIAR') or hasAuthority('CONTATO_PESSOA:*') or hasAuthority('PESSOAS:*')")
    public ResponseEntity<ContatoPessoaResponse> criar(@Valid @RequestBody ContatoPessoaRequest request) {
        ContatoPessoaResponse response = contatoPessoaService.criar(request);
        return created(AppConstants.API_BASE_PATH + "/contatos-pessoa", response.id(), response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTATO_PESSOA:EDITAR') or hasAuthority('CONTATO_PESSOA:*') or hasAuthority('PESSOAS:*')")
    public ResponseEntity<ContatoPessoaResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody ContatoPessoaRequest request) {
        return ResponseEntity.ok(contatoPessoaService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTATO_PESSOA:EXCLUIR') or hasAuthority('CONTATO_PESSOA:*') or hasAuthority('PESSOAS:*')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        contatoPessoaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
