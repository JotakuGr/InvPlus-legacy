package br.com.invplus.security.controller;
import br.com.invplus.common.constants.AppConstants;import br.com.invplus.security.dto.*;import br.com.invplus.security.service.UsuarioEmpresaFilialService;
import jakarta.validation.Valid;import java.util.*;import lombok.RequiredArgsConstructor;import org.springframework.http.*;import org.springframework.security.access.prepost.PreAuthorize;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping(AppConstants.API_BASE_PATH+"/usuarios-empresas-filiais") @RequiredArgsConstructor
@PreAuthorize("hasAuthority('CONTEXTO_EMPRESARIAL:*')") public class UsuarioEmpresaFilialController {
 private final UsuarioEmpresaFilialService service;
 @PostMapping public ResponseEntity<UsuarioEmpresaFilialResponse> criar(@Valid @RequestBody UsuarioEmpresaFilialRequest r){return ResponseEntity.status(201).body(service.criar(r));}
 @GetMapping public ResponseEntity<List<UsuarioEmpresaFilialResponse>> listar(@RequestParam UUID usuarioEmpresaId){return ResponseEntity.ok(service.listar(usuarioEmpresaId));}
 @PostMapping("/{id}/desativar") public ResponseEntity<UsuarioEmpresaFilialResponse> desativar(@PathVariable UUID id){return ResponseEntity.ok(service.desativar(id));}
}
