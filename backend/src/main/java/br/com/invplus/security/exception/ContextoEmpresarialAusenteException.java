package br.com.invplus.security.exception;
import org.springframework.security.core.AuthenticationException;
public class ContextoEmpresarialAusenteException extends AuthenticationException {
    public ContextoEmpresarialAusenteException() { super("Contexto empresarial ausente"); }
}
