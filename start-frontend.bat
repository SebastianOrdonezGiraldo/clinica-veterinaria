@echo off
REM ============================================================
REM Script de Inicio - FRONTEND
REM ============================================================

echo.
echo ╔═══════════════════════════════════════════════════════════╗
echo ║             INICIANDO FRONTEND (React + Vite)             ║
echo ╚═══════════════════════════════════════════════════════════╝
echo.

if not exist "package.json" (
    echo [ERROR] No se encontro el archivo 'package.json'
    echo Por favor, ejecuta este script desde la raiz del proyecto
    pause
    exit /b 1
)

echo [INFO] Iniciando servidor de desarrollo Vite...
echo        El navegador se abrira automaticamente
echo.

npm run dev

pause

