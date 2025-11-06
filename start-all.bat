@echo off
REM ============================================================
REM Script de Inicio - Sistema Clínica Veterinaria
REM ============================================================
REM Este script inicia el backend y el frontend automáticamente
REM ============================================================

echo.
echo ╔═══════════════════════════════════════════════════════════╗
echo ║   SISTEMA DE GESTION CLINICA VETERINARIA                  ║
echo ║   Iniciando Backend y Frontend...                         ║
echo ╚═══════════════════════════════════════════════════════════╝
echo.

REM Verificar que estamos en el directorio correcto
if not exist "backend" (
    echo [ERROR] No se encontro la carpeta 'backend'
    echo Por favor, ejecuta este script desde la raiz del proyecto
    pause
    exit /b 1
)

if not exist "package.json" (
    echo [ERROR] No se encontro el archivo 'package.json'
    echo Por favor, ejecuta este script desde la raiz del proyecto
    pause
    exit /b 1
)

echo [1/4] Verificando PostgreSQL...
timeout /t 2 /nobreak >nul

REM Intentar iniciar el servicio de PostgreSQL si no está corriendo
sc query "postgresql-x64-16" >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] PostgreSQL encontrado
) else (
    echo [ADVERTENCIA] No se pudo verificar PostgreSQL
    echo Asegurate de que PostgreSQL este corriendo en el puerto 5433
)

echo.
echo [2/4] Iniciando BACKEND (Spring Boot)...
echo Esto puede tomar 30-60 segundos la primera vez...
echo.

REM Iniciar el backend en una nueva ventana
start "Backend - Spring Boot" cmd /k "cd backend && mvn spring-boot:run"

echo [OK] Backend iniciando en segundo plano...
echo      Ventana del backend abierta en otra consola
echo.
echo [3/4] Esperando a que el backend inicie...
timeout /t 30 /nobreak >nul

echo.
echo [4/4] Iniciando FRONTEND (React + Vite)...
echo.

REM Iniciar el frontend en una nueva ventana
start "Frontend - React" cmd /k "npm run dev"

echo [OK] Frontend iniciando en segundo plano...
echo      Ventana del frontend abierta en otra consola
echo.
echo ╔═══════════════════════════════════════════════════════════╗
echo ║                    INICIO COMPLETADO                      ║
echo ╚═══════════════════════════════════════════════════════════╝
echo.
echo Servicios iniciados:
echo.
echo   Backend:  http://localhost:8080
echo   Frontend: http://localhost:5173
echo   Swagger:  http://localhost:8080/swagger-ui.html
echo.
echo Credenciales de prueba:
echo   Email:    admin@vetclinic.com
echo   Password: admin123
echo.
echo NOTA: Espera 1-2 minutos para que todo este completamente listo
echo       El navegador se abrira automaticamente cuando este listo
echo.
timeout /t 5 /nobreak >nul

REM Abrir el navegador automáticamente
start http://localhost:5173

echo.
echo Presiona cualquier tecla para cerrar esta ventana...
echo (Las ventanas del backend y frontend seguiran abiertas)
pause >nul

