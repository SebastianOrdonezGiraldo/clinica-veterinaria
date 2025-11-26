# Script PowerShell para eliminar credenciales SMTP del historial de Git
# Ejecutar desde la raíz del proyecto: .\scripts\security\eliminar-credenciales-historial.ps1

Write-Host "🔐 Eliminando credenciales SMTP del historial de Git..." -ForegroundColor Yellow
Write-Host ""

# Verificar que estamos en un repositorio Git
if (-not (Test-Path ".git")) {
    Write-Host "❌ Error: Este script debe ejecutarse desde la raíz del repositorio Git" -ForegroundColor Red
    exit 1
}

# Verificar cambios sin commitear
$status = git status --porcelain
if ($status) {
    Write-Host "⚠️  ADVERTENCIA: Tienes cambios sin commitear:" -ForegroundColor Yellow
    Write-Host $status
    Write-Host ""
    $response = Read-Host "¿Deseas hacer commit de estos cambios antes de continuar? (s/n)"
    if ($response -eq "s" -or $response -eq "S") {
        git add .
        $commitMsg = Read-Host "Ingresa el mensaje del commit"
        git commit -m $commitMsg
    } else {
        Write-Host "❌ Abortando. Por favor, haz commit o stash de tus cambios primero." -ForegroundColor Red
        exit 1
    }
}

Write-Host "📋 Buscando commits con credenciales..." -ForegroundColor Cyan
$commits = git log --all --full-history -S "yywqbtcsrvgdxdzy" --pretty=format:"%H" --source
if (-not $commits) {
    Write-Host "✅ No se encontraron credenciales en el historial" -ForegroundColor Green
    exit 0
}

Write-Host "⚠️  Encontrados commits con credenciales:" -ForegroundColor Yellow
git log --all --full-history -S "yywqbtcsrvgdxdzy" --pretty=format:"  %H - %ai - %s" --source
Write-Host ""

$confirm = Read-Host "¿Deseas continuar con la limpieza del historial? Esto reescribirá el historial de Git. (s/n)"
if ($confirm -ne "s" -and $confirm -ne "S") {
    Write-Host "❌ Operación cancelada" -ForegroundColor Red
    exit 0
}

Write-Host "🔄 Reescribiendo historial de Git..." -ForegroundColor Cyan
Write-Host "   Esto puede tomar varios minutos..." -ForegroundColor Yellow

$env:FILTER_BRANCH_SQUELCH_WARNING = "1"

# Crear script temporal para reemplazar credenciales
$fixScript = @"
`$content = Get-Content `$args[0] -Raw
`$content = `$content -replace 'MAIL_USERNAME=sebastian789go@gmail.com', 'MAIL_USERNAME=`${MAIL_USERNAME:}'
`$content = `$content -replace 'MAIL_PASSWORD=yywqbtcsrvgdxdzy', 'MAIL_PASSWORD=`${MAIL_PASSWORD:}'
`$content = `$content -replace 'MAIL_FROM=sebastian789go@gmail.com', 'MAIL_FROM=`${MAIL_FROM:noreply@clinica-veterinaria.com}'
Set-Content -Path `$args[0] -Value `$content -NoNewline
"@

$fixScriptPath = Join-Path $env:TEMP "fix-credentials.ps1"
Set-Content -Path $fixScriptPath -Value $fixScript

# Ejecutar filter-branch
git filter-branch --force --tree-filter "powershell -File $fixScriptPath apps/backend/src/main/resources/application.properties 2>`$null" --prune-empty --tag-name-filter cat -- --all

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ Historial limpiado exitosamente" -ForegroundColor Green
    Write-Host ""
    Write-Host "⚠️  IMPORTANTE: Ahora debes hacer force push:" -ForegroundColor Yellow
    Write-Host "   git push origin --force --all" -ForegroundColor Cyan
    Write-Host "   git push origin --force --tags" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "🔐 RECUERDA:" -ForegroundColor Yellow
    Write-Host "   1. Generar nueva contraseña de aplicación de Gmail" -ForegroundColor White
    Write-Host "   2. Actualizar apps/backend/.env con la nueva contraseña" -ForegroundColor White
    Write-Host "   3. Reiniciar la aplicación backend" -ForegroundColor White
    Write-Host ""
    Write-Host "📋 Verificar que las credenciales fueron eliminadas:" -ForegroundColor Cyan
    Write-Host "   git log --all --full-history -S 'yywqbtcsrvgdxdzy' --source" -ForegroundColor Gray
} else {
    Write-Host "❌ Error al limpiar el historial" -ForegroundColor Red
    Write-Host "   Considera usar git-filter-repo en su lugar" -ForegroundColor Yellow
}

# Limpiar archivo temporal
Remove-Item -Path $fixScriptPath -ErrorAction SilentlyContinue

