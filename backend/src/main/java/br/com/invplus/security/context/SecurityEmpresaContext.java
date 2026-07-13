package br.com.invplus.security.context;

import br.com.invplus.security.exception.*;
import br.com.invplus.security.service.AuthenticatedUserDetails;
import java.util.*;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityEmpresaContext implements EmpresaContext {
    @Override
    public UUID getUsuarioId() { return principal().getId(); }

    @Override
    public UUID getEmpresaId() {
        if (principal().getEmpresaId()==null) throw new ContextoEmpresarialAusenteException();
        return principal().getEmpresaId();
    }

    @Override
    public Optional<UUID> getFilialId() { return Optional.ofNullable(principal().getFilialId()); }

    @Override
    public UUID requireFilialId() { return getFilialId().orElseThrow(ContextoFilialAusenteException::new); }
    private AuthenticatedUserDetails principal() {
        var auth=SecurityContextHolder.getContext().getAuthentication();
        if(auth==null||!auth.isAuthenticated()||!(auth.getPrincipal() instanceof AuthenticatedUserDetails p))
            throw new AuthenticationCredentialsNotFoundException("Usuario autenticado nao encontrado");
        return p;
    }
}
