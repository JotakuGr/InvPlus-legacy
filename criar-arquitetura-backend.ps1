# criar-arquitetura-backend.ps1
# Cria a arquitetura base do backend invPlus

$projectPath = "C:\Users\User\OneDrive\Documentos\invPlus"
$basePath = "$projectPath\backend\src\main\java\br\com\invplus"

Write-Host "Criando arquitetura base do backend invPlus..." -ForegroundColor Cyan

$folders = @(
    "config",
    "shared",
    "shared\domain",
    "shared\domain\entity",
    "shared\domain\exception",
    "shared\domain\valueobject",
    "shared\api",
    "shared\api\response",
    "shared\api\exception",
    "shared\util",

    "common",
    "common\enums",
    "common\constants",

    "security",
    "security\config",
    "security\controller",
    "security\dto",
    "security\entity",
    "security\enums",
    "security\exception",
    "security\mapper",
    "security\repository",
    "security\service",
    "security\validator",

    "modules",
    "modules\cadastros",
    "modules\pessoas",
    "modules\produtos",
    "modules\estoque",
    "modules\compras",
    "modules\vendas",
    "modules\pagamentos",
    "modules\faturamento",
    "modules\financeiro",
    "modules\fiscal",
    "modules\crm"
)

foreach ($folder in $folders) {
    New-Item -ItemType Directory -Force -Path "$basePath\$folder" | Out-Null
}

# BaseEntity
$baseEntityPath = "$basePath\shared\domain\entity\BaseEntity.java"

@(
"package br.com.invplus.shared.domain.entity;",
"",
"import jakarta.persistence.*;",
"import lombok.Getter;",
"import lombok.Setter;",
"",
"import java.io.Serializable;",
"import java.time.OffsetDateTime;",
"import java.util.UUID;",
"",
"@Getter",
"@Setter",
"@MappedSuperclass",
"public abstract class BaseEntity implements Serializable {",
"",
"    @Id",
"    @GeneratedValue(strategy = GenerationType.UUID)",
"    @Column(name = ""id"", nullable = false, updatable = false)",
"    private UUID id;",
"",
"    @Column(name = ""criado_em"", nullable = false, updatable = false)",
"    private OffsetDateTime criadoEm;",
"",
"    @Column(name = ""atualizado_em"", nullable = false)",
"    private OffsetDateTime atualizadoEm;",
"",
"    @Column(name = ""criado_por"")",
"    private UUID criadoPor;",
"",
"    @Column(name = ""atualizado_por"")",
"    private UUID atualizadoPor;",
"",
"    @PrePersist",
"    protected void prePersist() {",
"        OffsetDateTime now = OffsetDateTime.now();",
"        this.criadoEm = now;",
"        this.atualizadoEm = now;",
"    }",
"",
"    @PreUpdate",
"    protected void preUpdate() {",
"        this.atualizadoEm = OffsetDateTime.now();",
"    }",
"}"
) | Set-Content -Path $baseEntityPath -Encoding UTF8

# SoftDeleteEntity
$softDeletePath = "$basePath\shared\domain\entity\SoftDeleteEntity.java"

@(
"package br.com.invplus.shared.domain.entity;",
"",
"import jakarta.persistence.Column;",
"import jakarta.persistence.MappedSuperclass;",
"import lombok.Getter;",
"import lombok.Setter;",
"",
"import java.time.OffsetDateTime;",
"",
"@Getter",
"@Setter",
"@MappedSuperclass",
"public abstract class SoftDeleteEntity extends BaseEntity {",
"",
"    @Column(name = ""ativo"", nullable = false)",
"    private Boolean ativo = true;",
"",
"    @Column(name = ""deleted_at"")",
"    private OffsetDateTime deletedAt;",
"",
"    public void ativar() {",
"        this.ativo = true;",
"        this.deletedAt = null;",
"    }",
"",
"    public void desativar() {",
"        this.ativo = false;",
"    }",
"",
"    public void excluirLogicamente() {",
"        this.ativo = false;",
"        this.deletedAt = OffsetDateTime.now();",
"    }",
"",
"    public boolean estaExcluido() {",
"        return this.deletedAt != null;",
"    }",
"}"
) | Set-Content -Path $softDeletePath -Encoding UTF8

# BusinessException
$businessExceptionPath = "$basePath\shared\domain\exception\BusinessException.java"

@(
"package br.com.invplus.shared.domain.exception;",
"",
"public class BusinessException extends RuntimeException {",
"",
"    public BusinessException(String message) {",
"        super(message);",
"    }",
"",
"    public BusinessException(String message, Throwable cause) {",
"        super(message, cause);",
"    }",
"}"
) | Set-Content -Path $businessExceptionPath -Encoding UTF8

# ResourceNotFoundException
$notFoundPath = "$basePath\shared\domain\exception\ResourceNotFoundException.java"

@(
"package br.com.invplus.shared.domain.exception;",
"",
"public class ResourceNotFoundException extends RuntimeException {",
"",
"    public ResourceNotFoundException(String message) {",
"        super(message);",
"    }",
"}"
) | Set-Content -Path $notFoundPath -Encoding UTF8

# ApiResponse
$apiResponsePath = "$basePath\shared\api\response\ApiResponse.java"

@(
"package br.com.invplus.shared.api.response;",
"",
"import java.time.OffsetDateTime;",
"",
"public record ApiResponse<T>(",
"        boolean success,",
"        String message,",
"        T data,",
"        OffsetDateTime timestamp",
") {",
"    public static <T> ApiResponse<T> success(String message, T data) {",
"        return new ApiResponse<>(true, message, data, OffsetDateTime.now());",
"    }",
"",
"    public static <T> ApiResponse<T> error(String message) {",
"        return new ApiResponse<>(false, message, null, OffsetDateTime.now());",
"    }",
"}"
) | Set-Content -Path $apiResponsePath -Encoding UTF8

# ErrorResponse
$errorResponsePath = "$basePath\shared\api\response\ErrorResponse.java"

@(
"package br.com.invplus.shared.api.response;",
"",
"import java.time.OffsetDateTime;",
"import java.util.List;",
"",
"public record ErrorResponse(",
"        boolean success,",
"        String message,",
"        List<String> errors,",
"        OffsetDateTime timestamp",
") {",
"    public static ErrorResponse of(String message, List<String> errors) {",
"        return new ErrorResponse(false, message, errors, OffsetDateTime.now());",
"    }",
"}"
) | Set-Content -Path $errorResponsePath -Encoding UTF8

# GlobalExceptionHandler
$handlerPath = "$basePath\shared\api\exception\GlobalExceptionHandler.java"

@(
"package br.com.invplus.shared.api.exception;",
"",
"import br.com.invplus.shared.api.response.ErrorResponse;",
"import br.com.invplus.shared.domain.exception.BusinessException;",
"import br.com.invplus.shared.domain.exception.ResourceNotFoundException;",
"import org.springframework.http.HttpStatus;",
"import org.springframework.http.ResponseEntity;",
"import org.springframework.web.bind.MethodArgumentNotValidException;",
"import org.springframework.web.bind.annotation.ExceptionHandler;",
"import org.springframework.web.bind.annotation.RestControllerAdvice;",
"",
"import java.util.List;",
"",
"@RestControllerAdvice",
"public class GlobalExceptionHandler {",
"",
"    @ExceptionHandler(BusinessException.class)",
"    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {",
"        return ResponseEntity",
"                .status(HttpStatus.BAD_REQUEST)",
"                .body(ErrorResponse.of(ex.getMessage(), List.of(ex.getMessage())));",
"    }",
"",
"    @ExceptionHandler(ResourceNotFoundException.class)",
"    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {",
"        return ResponseEntity",
"                .status(HttpStatus.NOT_FOUND)",
"                .body(ErrorResponse.of(ex.getMessage(), List.of(ex.getMessage())));",
"    }",
"",
"    @ExceptionHandler(MethodArgumentNotValidException.class)",
"    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {",
"        List<String> errors = ex.getBindingResult()",
"                .getFieldErrors()",
"                .stream()",
"                .map(error -> error.getField() + "": "" + error.getDefaultMessage())",
"                .toList();",
"",
"        return ResponseEntity",
"                .status(HttpStatus.BAD_REQUEST)",
"                .body(ErrorResponse.of(""Erro de validação"", errors));",
"    }",
"",
"    @ExceptionHandler(Exception.class)",
"    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {",
"        return ResponseEntity",
"                .status(HttpStatus.INTERNAL_SERVER_ERROR)",
"                .body(ErrorResponse.of(""Erro interno no servidor"", List.of(ex.getMessage())));",
"    }",
"}"
) | Set-Content -Path $handlerPath -Encoding UTF8

# Enums comuns
$statusEnumPath = "$basePath\common\enums\StatusRegistro.java"

@(
"package br.com.invplus.common.enums;",
"",
"public enum StatusRegistro {",
"    ATIVO,",
"    INATIVO,",
"    EXCLUIDO",
"}"
) | Set-Content -Path $statusEnumPath -Encoding UTF8

$tipoPessoaPath = "$basePath\common\enums\TipoPessoa.java"

@(
"package br.com.invplus.common.enums;",
"",
"public enum TipoPessoa {",
"    FISICA,",
"    JURIDICA",
"}"
) | Set-Content -Path $tipoPessoaPath -Encoding UTF8

# Constantes
$appConstantsPath = "$basePath\common\constants\AppConstants.java"

@(
"package br.com.invplus.common.constants;",
"",
"public final class AppConstants {",
"",
"    private AppConstants() {",
"    }",
"",
"    public static final String API_BASE_PATH = ""/api/v1"";",
"    public static final String DEFAULT_TIMEZONE = ""America/Sao_Paulo"";",
"}"
) | Set-Content -Path $appConstantsPath -Encoding UTF8

Write-Host ""
Write-Host "Arquitetura base criada com sucesso!" -ForegroundColor Green
Write-Host "Base criada em: $basePath" -ForegroundColor Yellow
Write-Host ""
Write-Host "Próximo passo: compilar o projeto com .\mvnw clean compile" -ForegroundColor Cyan