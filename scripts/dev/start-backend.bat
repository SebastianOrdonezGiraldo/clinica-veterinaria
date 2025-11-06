@echo off
echo ========================================
echo  Iniciando Backend - Clinica Veterinaria
echo ========================================
echo.

:: Verificar que estamos en el directorio correcto
if not exist "apps\backend\pom.xml" (
    echo ERROR: No se encuentra el backend
    echo Por favor ejecuta este script desde la raiz del proyecto
    pause
    exit /b 1
)

:: Cambiar al directorio del backend
cd apps\backend

:: Iniciar Spring Boot
echo Iniciando Spring Boot...
echo.
mvn spring-boot:run

pause
