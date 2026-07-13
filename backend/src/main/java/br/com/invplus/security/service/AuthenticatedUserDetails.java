package br.com.invplus.security.service;

import java.util.Collection;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Principal autenticado da aplicacao, mantendo o UUID do usuario para auditoria automatica.
 */
public class AuthenticatedUserDetails implements UserDetails {

    private final UUID id;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;
    private final UUID empresaId;
    private final UUID filialId;

    public AuthenticatedUserDetails(
            UUID id,
            String username,
            String password,
            Collection<? extends GrantedAuthority> authorities,
            boolean enabled
    ) {
        this(id, username, password, authorities, enabled, null, null);
    }

    public AuthenticatedUserDetails(UUID id, String username, String password,
            Collection<? extends GrantedAuthority> authorities, boolean enabled,
            UUID empresaId, UUID filialId) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.enabled = enabled;
        this.empresaId = empresaId;
        this.filialId = filialId;
    }

    /**
     * Identificador usado pelo AuditorAware para preencher os campos de auditoria.
     */
    public UUID getId() {
        return id;
    }
    public UUID getEmpresaId() { return empresaId; }
    public UUID getFilialId() { return filialId; }
    public boolean possuiContextoEmpresarial() { return empresaId != null; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
