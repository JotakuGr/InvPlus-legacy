$folders = @(
    "src/app/core/auth",
    "src/app/core/guards",
    "src/app/core/interceptors",
    "src/app/core/services",

    "src/app/shared/models",
    "src/app/shared/components",
    "src/app/shared/directives",
    "src/app/shared/pipes",

    "src/app/layout/sidebar",
    "src/app/layout/toolbar",

    "src/app/modules/auth",
    "src/app/modules/dashboard",
    "src/app/modules/cadastros",
    "src/app/modules/pessoas",
    "src/app/modules/produtos",
    "src/app/modules/estoque"
)

foreach ($folder in $folders) {
    if (!(Test-Path $folder)) {
        New-Item -ItemType Directory -Path $folder | Out-Null
        Write-Host "Criado: $folder" -ForegroundColor Green
    } else {
        Write-Host "Já existe: $folder" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "Estrutura do frontend criada com sucesso!" -ForegroundColor Cyan