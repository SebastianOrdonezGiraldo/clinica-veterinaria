@echo off
echo ========================================
echo  Iniciando Sistema Clinica Veterinaria
echo ========================================
echo.

:: Verificar que estamos en el directorio correcto
if not exist "apps\backend\pom.xml" (
    echo ERROR: No se encuentra el backend
    echo Por favor ejecuta este script desde la raiz del proyecto
    pause
    exit /b 1
)

if not exist "apps\frontend\package.json" (
    echo ERROR: No se encuentra el frontend
    echo Por favor ejecuta este script desde la raiz del proyecto
    pause
    exit /b 1
)

:: Iniciar Backend en una nueva ventana
echo [1/2] Iniciando Backend (Spring Boot)...
start "Backend - Clinica Veterinaria" cmd /c "cd apps\backend && mvn spring-boot:run"

:: Esperar 5 segundos
timeout /t 5 /nobreak > nul

:: Iniciar Frontend en una nueva ventana
echo [2/2] Iniciando Frontend (React)...
start "Frontend - Clinica Veterinaria" cmd /c "cd apps\frontend && npm run dev"

echo.
echo ========================================
echo  Sistema iniciado correctamente
echo ========================================
echo.
echo  Backend:  http://localhost:8080
echo  Frontend: http://localhost:5173
echo  Swagger:  http://localhost:8080/swagger-ui.html
echo.
echo  Presiona cualquier tecla para cerrar esta ventana
echo  (Las aplicaciones seguiran ejecutandose)
echo.
pause > nul
