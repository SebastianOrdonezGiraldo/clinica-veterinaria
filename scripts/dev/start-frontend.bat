@echo off
echo ========================================
echo  Iniciando Frontend - Clinica Veterinaria
echo ========================================
echo.

:: Verificar que estamos en el directorio correcto
if not exist "apps\frontend\package.json" (
    echo ERROR: No se encuentra el frontend
    echo Por favor ejecuta este script desde la raiz del proyecto
    pause
    exit /b 1
)

:: Cambiar al directorio raíz primero
cd /d "%~dp0..\.."

:: Cambiar al directorio del frontend
cd apps\frontend

:: Verificar si node_modules existe
if not exist "node_modules" (
    echo Instalando dependencias...
    npm install
    echo.
)

:: Iniciar Vite
echo Iniciando servidor de desarrollo...
echo.
echo Frontend corriendo en: http://localhost:5173
echo.
npm run dev

pause
