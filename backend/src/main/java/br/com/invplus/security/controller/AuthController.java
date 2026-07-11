package br.com.invplus.security.controller;

import br.com.invplus.common.constants.AppConstants;
import br.com.invplus.security.dto.LoginRequest;
import br.com.invplus.security.dto.LoginResponse;
import br.com.invplus.security.dto.LogoutRequest;
import br.com.invplus.security.dto.LogoutResponse;
import br.com.invplus.security.dto.RefreshTokenRequest;
import br.com.invplus.security.service.SecurityService;
import br.com.invplus.security.service.ContextoEmpresarialService;
import br.com.invplus.security.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;
import java.util.List;

@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SecurityService securityService;
    private final ContextoEmpresarialService contextoService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        return ResponseEntity.ok(securityService.login(request, ip(servletRequest), userAgent(servletRequest)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest servletRequest
    ) {
        return ResponseEntity.ok(securityService.refresh(
                request.refreshToken(),
                ip(servletRequest),
                userAgent(servletRequest)
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(@Valid @RequestBody LogoutRequest request) {
        securityService.logout(request.refreshToken());
        return ResponseEntity.ok(new LogoutResponse(true, "Logout realizado com sucesso"));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('SEGURANCA:*')")
    public ResponseEntity<String> me(Authentication authentication) {
        return ResponseEntity.ok(authentication.getName());
    }

    @PostMapping("/contexto/selecionar")
    public ResponseEntity<TokenContextoResponse> selecionar(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody SelecionarContextoRequest request, HttpServletRequest servletRequest) {
        return ResponseEntity.ok(contextoService.selecionar(bearer(authorization),request,
                ip(servletRequest),userAgent(servletRequest)));
    }

    @PostMapping("/contexto/trocar")
    @PreAuthorize("hasAnyAuthority('CONTEXTO_EMPRESARIAL:*','CONTEXTO_EMPRESARIAL:TROCAR')")
    public ResponseEntity<TokenContextoResponse> trocar(@Valid @RequestBody SelecionarContextoRequest request,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(contextoService.trocar(request,ip(servletRequest),userAgent(servletRequest)));
    }

    @GetMapping("/contexto")
    @PreAuthorize("hasAnyAuthority('CONTEXTO_EMPRESARIAL:*','CONTEXTO_EMPRESARIAL:VISUALIZAR')")
    public ResponseEntity<ContextoAtualResponse> contexto() { return ResponseEntity.ok(contextoService.atual()); }

    @GetMapping("/contexto/empresas")
    @PreAuthorize("hasAnyAuthority('CONTEXTO_EMPRESARIAL:*','CONTEXTO_EMPRESARIAL:VISUALIZAR')")
    public ResponseEntity<List<EmpresaDisponivelResponse>> empresas() {
        return ResponseEntity.ok(contextoService.empresasAtuais());
    }

    private String bearer(String value) {
        if(value==null||!value.startsWith("Bearer "))
            throw new org.springframework.security.authentication.BadCredentialsException("Selection token ausente");
        return value.substring(7);
    }

    private String ip(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String userAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
}
