package br.com.invplus.shared.config;

import br.com.invplus.security.service.AuthenticatedUserDetails;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Resolve o usuario autenticado para preencher automaticamente criadoPor e atualizadoPor.
 */
@Configuration
public class JpaAuditingConfig implements AuditorAware<UUID> {

    /**
     * Expoe o AuditorAware com nome estavel para o @EnableJpaAuditing.
     */
    @Bean("auditorAware")
    public AuditorAware<UUID> auditorAware() {
        return this;
    }

    /**
     * Garante que a auditoria use OffsetDateTime, o mesmo tipo das entidades base.
     */
    @Bean("auditingDateTimeProvider")
    public DateTimeProvider auditingDateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now());
    }

    /**
     * Retorna o UUID do principal autenticado quando a requisicao passou pelo Spring Security.
     */
    @Override
    public Optional<UUID> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedUserDetails userDetails) {
            return Optional.of(userDetails.getId());
        }

        return Optional.empty();
    }
}
