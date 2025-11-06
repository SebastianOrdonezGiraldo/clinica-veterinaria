@echo off
REM ============================================================
REM Script de Inicio - BACKEND
REM ============================================================

echo.
echo ╔═══════════════════════════════════════════════════════════╗
echo ║              INICIANDO BACKEND (Spring Boot)              ║
echo ╚═══════════════════════════════════════════════════════════╝
echo.

if not exist "backend" (
    echo [ERROR] No se encontro la carpeta 'backend'
    echo Por favor, ejecuta este script desde la raiz del proyecto
    pause
    exit /b 1
)

echo [INFO] Navegando a la carpeta backend...
cd backend

echo [INFO] Iniciando Spring Boot...
echo        Esto puede tomar 30-60 segundos...
echo.

mvn spring-boot:run

pause

