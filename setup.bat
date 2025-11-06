@echo off
REM ============================================================
REM Script de Configuración Inicial - Primera Instalación
REM ============================================================

echo.
echo ╔═══════════════════════════════════════════════════════════╗
echo ║     CONFIGURACION INICIAL - CLINICA VETERINARIA           ║
echo ║     Este proceso puede tomar varios minutos...            ║
echo ╚═══════════════════════════════════════════════════════════╝
echo.

REM Verificar estructura del proyecto
if not exist "backend" (
    echo [ERROR] No se encontro la carpeta 'backend'
    pause
    exit /b 1
)

if not exist "package.json" (
    echo [ERROR] No se encontro el archivo 'package.json'
    pause
    exit /b 1
)

echo [1/4] Instalando dependencias del BACKEND (Maven)...
echo       Esto descargara las librerias de Java/Spring Boot
echo.
cd backend
call mvn clean install -DskipTests
if %errorlevel% neq 0 (
    echo [ERROR] Fallo la instalacion del backend
    cd ..
    pause
    exit /b 1
)
cd ..
echo [OK] Backend configurado correctamente
echo.

echo [2/4] Instalando dependencias del FRONTEND (Node.js)...
echo       Esto descargara las librerias de React/Vite
echo.
call npm install
if %errorlevel% neq 0 (
    echo [ERROR] Fallo la instalacion del frontend
    pause
    exit /b 1
)
echo [OK] Frontend configurado correctamente
echo.

echo [3/4] Verificando configuracion de PostgreSQL...
echo.
echo IMPORTANTE: Asegurate de que PostgreSQL este instalado y corriendo
echo            con las siguientes configuraciones:
echo.
echo   Puerto:         5433
echo   Usuario:        postgres
echo   Password:       root
echo   Base de datos:  vetclinic_dev
echo.

choice /C SN /M "Has creado la base de datos 'vetclinic_dev' en PostgreSQL?"
if %errorlevel% equ 2 (
    echo.
    echo [ACCION REQUERIDA] Crea la base de datos antes de continuar:
    echo.
    echo 1. Abre pgAdmin
    echo 2. Conectate al servidor PostgreSQL
    echo 3. Click derecho en "Databases" -^> "Create" -^> "Database"
    echo 4. Nombre: vetclinic_dev
    echo 5. Click en "Save"
    echo.
    pause
    exit /b 0
)

echo [OK] Base de datos configurada
echo.

echo [4/4] Configuracion completada!
echo.
echo ╔═══════════════════════════════════════════════════════════╗
echo ║                INSTALACION COMPLETADA                     ║
echo ╚═══════════════════════════════════════════════════════════╝
echo.
echo Proximos pasos:
echo.
echo 1. Ejecuta 'start-all.bat' para iniciar todo el sistema
echo    O ejecuta manualmente:
echo    - 'start-backend.bat' para el backend
echo    - 'start-frontend.bat' para el frontend
echo.
echo 2. Abre tu navegador en: http://localhost:5173
echo.
echo 3. Inicia sesion con:
echo    Email:    admin@vetclinic.com
echo    Password: admin123
echo.
echo Para mas informacion, lee: GUIA_INICIO.md
echo.
pause

