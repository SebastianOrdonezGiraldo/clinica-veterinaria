@echo off
echo ========================================
echo  Iniciando Backend - PRODUCCION
echo ========================================
echo.

:: Verificar que estamos en el directorio correcto
if not exist "apps\backend\pom.xml" (
    echo ERROR: No se encuentra el backend
    echo Por favor ejecuta este script desde la raiz del proyecto
    pause
    exit /b 1
)

:: Configurar variables de entorno para producción
echo Configurando variables de entorno...
set JWT_SECRET=52gT0AUUx9MjHgh7KH1cSZfZVqAaxq8hQ6mBjsbqpCY=
set SWAGGER_ENABLED=true
echo JWT_SECRET configurado
echo Swagger habilitado para pruebas
echo.

:: Cambiar al directorio del backend
cd apps\backend

:: Iniciar Spring Boot con perfil de producción
echo Iniciando Spring Boot en modo PRODUCCION...
echo.
mvn spring-boot:run -Dspring.profiles.active=prod

pause

