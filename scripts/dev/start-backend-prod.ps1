# Script para iniciar backend en modo PRODUCCION
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Iniciando Backend - PRODUCCION" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Verificar que estamos en el directorio correcto
if (-not (Test-Path "apps\backend\pom.xml")) {
    Write-Host "ERROR: No se encuentra el backend" -ForegroundColor Red
    Write-Host "Por favor ejecuta este script desde la raiz del proyecto" -ForegroundColor Red
    Read-Host "Presiona Enter para salir"
    exit 1
}

# Configurar variables de entorno para producción
Write-Host "Configurando variables de entorno..." -ForegroundColor Yellow
$env:JWT_SECRET = "52gT0AUUx9MjHgh7KH1cSZfZVqAaxq8hQ6mBjsbqpCY="
$env:SWAGGER_ENABLED = "true"
Write-Host "JWT_SECRET configurado" -ForegroundColor Green
Write-Host "Swagger habilitado para pruebas" -ForegroundColor Green
Write-Host ""

# Cambiar al directorio del backend
Set-Location apps\backend

# Iniciar Spring Boot con perfil de producción
Write-Host "Iniciando Spring Boot en modo PRODUCCION..." -ForegroundColor Yellow
Write-Host ""
mvn spring-boot:run "-Dspring.profiles.active=prod"

