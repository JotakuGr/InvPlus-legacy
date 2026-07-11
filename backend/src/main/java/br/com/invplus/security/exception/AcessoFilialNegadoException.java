package br.com.invplus.security.exception;
import org.springframework.security.access.AccessDeniedException;
public class AcessoFilialNegadoException extends AccessDeniedException {
    public AcessoFilialNegadoException() { super("Acesso a filial negado"); }
}
