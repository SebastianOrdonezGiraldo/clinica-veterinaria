# Script para eliminar credenciales SMTP del historial de Git
# Ejecutar desde la raíz del proyecto: .\scripts\security\limpiar-credenciales-git.ps1

Write-Host "🔐 Limpieza de Credenciales SMTP del Historial de Git" -ForegroundColor Yellow
Write-Host ""

# Verificar que estamos en el directorio correcto
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

Write-Host "📋 Buscando credenciales en el historial de Git..." -ForegroundColor Cyan

# Buscar commits que contienen las credenciales
$commits = git log --all --full-history -S "yywqbtcsrvgdxdzy" --pretty=format:"%H" --source
if ($commits) {
    Write-Host "⚠️  Encontrados commits con credenciales expuestas:" -ForegroundColor Yellow
    git log --all --full-history -S "yywqbtcsrvgdxdzy" --pretty=format:"  %H - %ai - %s" --source
    Write-Host ""
    
    Write-Host "🔧 Opciones para limpiar el historial:" -ForegroundColor Cyan
    Write-Host "  1. Usar git filter-repo (recomendado, más rápido y seguro)"
    Write-Host "  2. Usar git filter-branch (más lento pero incluido en Git)"
    Write-Host "  3. Reescribir commits específicos con git rebase"
    Write-Host ""
    
    $opcion = Read-Host "Selecciona una opción (1-3)"
    
    if ($opcion -eq "1") {
        Write-Host "📥 Instalando git-filter-repo..." -ForegroundColor Cyan
        Write-Host "   Descarga desde: https://github.com/newren/git-filter-repo"
        Write-Host "   O instala con: pip install git-filter-repo"
        Write-Host ""
        Write-Host "   Luego ejecuta:" -ForegroundColor Yellow
        Write-Host "   git filter-repo --path apps/backend/src/main/resources/application.properties --invert-paths"
        Write-Host "   git push origin --force --all"
    }
    elseif ($opcion -eq "2") {
        Write-Host "🔄 Ejecutando git filter-branch..." -ForegroundColor Cyan
        $env:FILTER_BRANCH_SQUELCH_WARNING = "1"
        git filter-branch --force --index-filter `
            "git rm --cached --ignore-unmatch apps/backend/src/main/resources/application.properties" `
            --prune-empty --tag-name-filter cat -- --all
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Historial limpiado exitosamente" -ForegroundColor Green
            Write-Host ""
            Write-Host "⚠️  IMPORTANTE: Ahora debes hacer force push:" -ForegroundColor Yellow
            Write-Host "   git push origin --force --all"
            Write-Host "   git push origin --force --tags"
        }
    }
    elseif ($opcion -eq "3") {
        Write-Host "📝 Para reescribir commits específicos:" -ForegroundColor Cyan
        Write-Host "   1. git rebase -i <commit-anterior-al-primero-con-credenciales>"
        Write-Host "   2. Cambia 'pick' por 'edit' en los commits problemáticos"
        Write-Host "   3. Edita application.properties para eliminar credenciales"
        Write-Host "   4. git commit --amend"
        Write-Host "   5. git rebase --continue"
    }
} else {
    Write-Host "✅ No se encontraron credenciales en el historial actual" -ForegroundColor Green
}

Write-Host ""
Write-Host "🔐 RECUERDA:" -ForegroundColor Yellow
Write-Host "   1. Generar nueva contraseña de aplicación de Gmail"
Write-Host "   2. Actualizar apps/backend/.env con la nueva contraseña"
Write-Host "   3. Reiniciar la aplicación backend"
Write-Host ""

