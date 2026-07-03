package br.com.invplus.modules.pessoas.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.modules.pessoas.dto.request.FuncionarioRequest;
import br.com.invplus.modules.pessoas.dto.response.FuncionarioResponse;
import br.com.invplus.modules.pessoas.service.FuncionarioService;
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
 * Endpoints REST para gerenciar o papel Funcionario de uma Pessoa.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/funcionarios")
@RequiredArgsConstructor
public class FuncionarioController extends BaseCrudController {

    private final FuncionarioService funcionarioService;

    @GetMapping
    @PreAuthorize("hasAuthority('FUNCIONARIO:VISUALIZAR') or hasAuthority('FUNCIONARIO:*') or hasAuthority('PESSOAS:*')")
    public ResponseEntity<Page<FuncionarioResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(funcionarioService.listar(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('FUNCIONARIO:VISUALIZAR') or hasAuthority('FUNCIONARIO:*') or hasAuthority('PESSOAS:*')")
    public ResponseEntity<FuncionarioResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(funcionarioService.buscar(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('FUNCIONARIO:CRIAR') or hasAuthority('FUNCIONARIO:*') or hasAuthority('PESSOAS:*')")
    public ResponseEntity<FuncionarioResponse> criar(@Valid @RequestBody FuncionarioRequest request) {
        FuncionarioResponse response = funcionarioService.criar(request);
        return created(AppConstants.API_BASE_PATH + "/funcionarios", response.id(), response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('FUNCIONARIO:EDITAR') or hasAuthority('FUNCIONARIO:*') or hasAuthority('PESSOAS:*')")
    public ResponseEntity<FuncionarioResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody FuncionarioRequest request) {
        return ResponseEntity.ok(funcionarioService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('FUNCIONARIO:EXCLUIR') or hasAuthority('FUNCIONARIO:*') or hasAuthority('PESSOAS:*')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        funcionarioService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
