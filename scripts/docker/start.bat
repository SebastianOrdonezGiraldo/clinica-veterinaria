@echo off
REM Script para iniciar la aplicación con Docker en Windows

echo 🐳 Iniciando Clínica Veterinaria con Docker...

REM Verificar si existe .env
if not exist .env (
    echo 📝 Creando archivo .env desde .env.example...
    copy .env.example .env
    echo ⚠️  Por favor, edita .env con tus configuraciones antes de continuar
    pause
)

REM Construir y levantar servicios
echo 🔨 Construyendo imágenes...
docker-compose build

echo 🚀 Iniciando servicios...
docker-compose up -d

echo ⏳ Esperando que los servicios estén listos...
timeout /t 10 /nobreak >nul

REM Verificar estado
echo 📊 Estado de los servicios:
docker-compose ps

echo.
echo ✅ Servicios iniciados!
echo.
echo 📍 URLs:
echo    Frontend:  http://localhost:5173
echo    Backend:   http://localhost:8080
echo    Swagger:   http://localhost:8080/swagger-ui/index.html
echo.
echo 📋 Ver logs: docker-compose logs -f
echo 🛑 Detener:  docker-compose down

pause

