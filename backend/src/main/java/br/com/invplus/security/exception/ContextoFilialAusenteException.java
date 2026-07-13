package br.com.invplus.security.exception;
import br.com.invplus.shared.domain.exception.BusinessException;
public class ContextoFilialAusenteException extends BusinessException {
    public ContextoFilialAusenteException() { super("Contexto de filial obrigatorio"); }
}
