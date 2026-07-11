package br.com.invplus.security.exception;
import org.springframework.security.access.AccessDeniedException;
public class AcessoEmpresaNegadoException extends AccessDeniedException {
    public AcessoEmpresaNegadoException() { super("Acesso a empresa negado"); }
}
