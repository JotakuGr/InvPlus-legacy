# criar-estrutura-security.ps1
# Cria a estrutura inicial do módulo Security do invPlus

$projectPath = "C:\Users\User\OneDrive\Documentos\invPlus"
$basePath = "$projectPath\backend\src\main\java\br\com\invplus\security"

Write-Host "Criando estrutura do módulo Security..." -ForegroundColor Cyan

$folders = @(
    "entity",
    "repository",
    "service",
    "dto",
    "controller"
)

foreach ($folder in $folders) {
    New-Item -ItemType Directory -Force -Path "$basePath\$folder" | Out-Null
}

# Criar arquivos vazios das entidades
$entityFiles = @(
    "Usuario.java",
    "Perfil.java",
    "Permissao.java",
    "Auditoria.java",
    "UsuarioPerfil.java",
    "PerfilPermissao.java"
)

foreach ($file in $entityFiles) {
    $filePath = "$basePath\entity\$file"

    if (-not (Test-Path $filePath)) {
        New-Item -ItemType File -Path $filePath | Out-Null
    }
}

# Criar arquivos vazios dos repositories
$repositoryFiles = @(
    "UsuarioRepository.java",
    "PerfilRepository.java",
    "PermissaoRepository.java",
    "AuditoriaRepository.java"
)

foreach ($file in $repositoryFiles) {
    $filePath = "$basePath\repository\$file"

    if (-not (Test-Path $filePath)) {
        New-Item -ItemType File -Path $filePath | Out-Null
    }
}

# Criar arquivos base
$servicePath = "$basePath\service\SecurityService.java"
$controllerPath = "$basePath\controller\AuthController.java"

if (-not (Test-Path $servicePath)) {
    New-Item -ItemType File -Path $servicePath | Out-Null
}

if (-not (Test-Path $controllerPath)) {
    New-Item -ItemType File -Path $controllerPath | Out-Null
}

Write-Host ""
Write-Host "Estrutura criada com sucesso!" -ForegroundColor Green
Write-Host ""
Write-Host "Criado em:"
Write-Host $basePath -ForegroundColor Yellow