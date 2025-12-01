# Script para generar variables de entorno para despliegue
# Uso: .\generate-env.ps1 [railway|render|fly]

param(
    [string]$Platform = "railway"
)

Write-Host "🔐 Generando variables de entorno para $Platform..." -ForegroundColor Cyan

# Generar JWT Secret
$bytes = 1..32 | ForEach-Object { Get-Random -Maximum 256 }
$jwtSecret = [Convert]::ToBase64String($bytes)

Write-Host ""
Write-Host "📋 Variables de entorno para BACKEND:" -ForegroundColor Yellow
Write-Host "==================================" -ForegroundColor Yellow
Write-Host ""
Write-Host "SPRING_PROFILES_ACTIVE=prod"
Write-Host "JWT_SECRET=$jwtSecret"
Write-Host "JWT_EXPIRATION=86400000"
Write-Host "SWAGGER_ENABLED=false"
Write-Host ""

switch ($Platform) {
    "railway" {
        Write-Host "# Railway - Usa las variables de referencia:" -ForegroundColor Gray
        Write-Host "DB_URL=`${{Postgres.DATABASE_URL}}"
        Write-Host "DB_USERNAME=`${{Postgres.PGUSER}}"
        Write-Host "DB_PASSWORD=`${{Postgres.PGPASSWORD}}"
        Write-Host "CORS_ALLOWED_ORIGINS=https://tu-frontend.railway.app"
    }
    "render" {
        Write-Host "# Render - Configura en el dashboard:" -ForegroundColor Gray
        Write-Host "DB_URL=jdbc:postgresql://tu-db-host:5432/vetclinic"
        Write-Host "DB_USERNAME=veterinaria"
        Write-Host "DB_PASSWORD=tu_password_de_render"
        Write-Host "CORS_ALLOWED_ORIGINS=https://tu-frontend.onrender.com"
    }
    "fly" {
        Write-Host "# Fly.io - Usa: fly secrets set" -ForegroundColor Gray
        Write-Host "DB_URL=jdbc:postgresql://tu-db-host:5432/vetclinic"
        Write-Host "DB_USERNAME=usuario"
        Write-Host "DB_PASSWORD=password"
        Write-Host "CORS_ALLOWED_ORIGINS=https://tu-frontend.fly.dev"
    }
}

Write-Host ""
Write-Host "📋 Variables de entorno para FRONTEND:" -ForegroundColor Yellow
Write-Host "==================================" -ForegroundColor Yellow
Write-Host ""

switch ($Platform) {
    "railway" {
        Write-Host "VITE_API_URL=https://tu-backend.railway.app/api"
    }
    "render" {
        Write-Host "VITE_API_URL=https://tu-backend.onrender.com/api"
    }
    "fly" {
        Write-Host "VITE_API_URL=https://tu-backend.fly.dev/api"
    }
}

Write-Host ""
Write-Host "✅ Variables generadas!" -ForegroundColor Green
Write-Host "💡 Copia y pega estas variables en tu plataforma de despliegue" -ForegroundColor Gray

