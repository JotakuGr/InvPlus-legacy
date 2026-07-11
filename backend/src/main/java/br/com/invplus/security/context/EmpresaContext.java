package br.com.invplus.security.context;
import java.util.Optional;
import java.util.UUID;
public interface EmpresaContext {
    UUID getUsuarioId();
    UUID getEmpresaId();
    Optional<UUID> getFilialId();
    UUID requireFilialId();
}
