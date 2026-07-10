package br.com.invplus.shared.api.exception;

import br.com.invplus.shared.api.response.ErrorResponse;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), List.of(ex.getMessage()), request);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, ex.getMessage(), List.of(ex.getMessage()), request);
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOperation(InvalidOperationException ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), List.of(ex.getMessage()), request);
    }

    @ExceptionHandler({
            OptimisticLockException.class,
            ObjectOptimisticLockingFailureException.class
    })
    public ResponseEntity<ErrorResponse> handleOptimisticLock(Exception ex, HttpServletRequest request) {
        return error(
                HttpStatus.CONFLICT,
                "Registro alterado por outro usuario. Atualize os dados e tente novamente.",
                List.of("Conflito de concorrencia"),
                request
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        return error(
                HttpStatus.CONFLICT,
                "Registro viola uma regra de unicidade ou integridade.",
                List.of("Conflito de integridade dos dados"),
                request
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), List.of(ex.getMessage()), request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, "Credenciais invalidas", List.of("Credenciais invalidas"), request);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDeniedException(AuthorizationDeniedException ex, HttpServletRequest request) {
        return error(HttpStatus.FORBIDDEN, "Acesso negado", List.of("Acesso negado"), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return error(HttpStatus.BAD_REQUEST, "Erro de validacao", errors, request);
    }

    @ExceptionHandler({
            MissingServletRequestPartException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ErrorResponse> handleMissingRequestValue(Exception ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "Campo obrigatorio nao informado", List.of(ex.getMessage()), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor", List.of(ex.getMessage()), request);
    }

    private ResponseEntity<ErrorResponse> error(
            HttpStatus status,
            String message,
            List<String> errors,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(status)
                .body(ErrorResponse.of(status.value(), titulo(status), message, request.getRequestURI(), errors));
    }

    private String titulo(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "Requisicao invalida";
            case UNAUTHORIZED -> "Nao autorizado";
            case FORBIDDEN -> "Acesso negado";
            case NOT_FOUND -> "Nao encontrado";
            case CONFLICT -> "Conflito";
            case INTERNAL_SERVER_ERROR -> "Erro interno";
            default -> status.getReasonPhrase();
        };
    }
}
