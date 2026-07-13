package br.com.invplus.security.controller;
import br.com.invplus.common.constants.AppConstants;import br.com.invplus.security.dto.*;import br.com.invplus.security.service.UsuarioEmpresaService;
import jakarta.validation.Valid;import java.util.*;import lombok.RequiredArgsConstructor;import org.springframework.http.*;import org.springframework.security.access.prepost.PreAuthorize;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping(AppConstants.API_BASE_PATH+"/usuarios-empresas") @RequiredArgsConstructor
@PreAuthorize("hasAuthority('CONTEXTO_EMPRESARIAL:*')") public class UsuarioEmpresaController {
 private final UsuarioEmpresaService service;
 @PostMapping public ResponseEntity<UsuarioEmpresaResponse> criar(@Valid @RequestBody UsuarioEmpresaRequest r){return ResponseEntity.status(201).body(service.criar(r));}
 @GetMapping public ResponseEntity<List<UsuarioEmpresaResponse>> listar(@RequestParam UUID usuarioId){return ResponseEntity.ok(service.listar(usuarioId));}
 @PostMapping("/{id}/desativar") public ResponseEntity<UsuarioEmpresaResponse> desativar(@PathVariable UUID id){return ResponseEntity.ok(service.desativar(id));}
}
