package br.com.invplus.support.context;

import br.com.invplus.security.context.EmpresaContext;
import br.com.invplus.security.exception.ContextoEmpresarialAusenteException;
import br.com.invplus.security.exception.ContextoFilialAusenteException;
import br.com.invplus.security.service.AuthenticatedUserDetails;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Contexto empresarial controlavel e isolado por thread para testes de integracao. */
@Component
@Primary
@Profile("test")
public class TestEmpresaContext implements EmpresaContext {

    private final ThreadLocal<UUID> usuarioId = new ThreadLocal<>();
    private final ThreadLocal<UUID> empresaId = new ThreadLocal<>();
    private final ThreadLocal<UUID> filialId = new ThreadLocal<>();

    public void ativar(UUID usuarioId, UUID empresaId, UUID filialId) {
        this.usuarioId.set(usuarioId);
        this.empresaId.set(empresaId);
        if (filialId == null) {
            this.filialId.remove();
        } else {
            this.filialId.set(filialId);
        }
    }

    public void ativarEmpresa(UUID usuarioId, UUID empresaId) {
        ativar(usuarioId, empresaId, null);
    }

    public void limpar() {
        usuarioId.remove();
        empresaId.remove();
        filialId.remove();
    }

    @Override
    public UUID getUsuarioId() {
        UUID configurado = usuarioId.get();
        return configurado != null ? configurado : principal().getId();
    }

    @Override
    public UUID getEmpresaId() {
        UUID configurado = empresaId.get();
        if (configurado != null) {
            return configurado;
        }
        UUID autenticada = principal().getEmpresaId();
        if (autenticada == null) {
            throw new ContextoEmpresarialAusenteException();
        }
        return autenticada;
    }

    @Override
    public Optional<UUID> getFilialId() {
        UUID configurada = filialId.get();
        if (configurada != null) {
            return Optional.of(configurada);
        }
        return Optional.ofNullable(principalOpcional()).map(AuthenticatedUserDetails::getFilialId);
    }

    @Override
    public UUID requireFilialId() {
        return getFilialId().orElseThrow(ContextoFilialAusenteException::new);
    }

    private AuthenticatedUserDetails principal() {
        AuthenticatedUserDetails principal = principalOpcional();
        if (principal == null) {
            throw new AuthenticationCredentialsNotFoundException(
                    "Contexto de teste ou usuario autenticado nao configurado");
        }
        return principal;
    }

    private AuthenticatedUserDetails principalOpcional() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof AuthenticatedUserDetails principal
                        ? principal
                        : null;
    }
}
