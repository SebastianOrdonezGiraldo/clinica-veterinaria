# Script PowerShell para probar el envío de correos electrónicos

param(
    [string]$Email = "test@ejemplo.com"
)

$BaseUrl = "http://localhost:8080"

Write-Host "🧪 Probando envío de correos a: $Email" -ForegroundColor Cyan
Write-Host "=========================================="
Write-Host ""

# Función para hacer login y obtener token
function Get-Token {
    Write-Host "🔐 Obteniendo token de autenticación..." -ForegroundColor Yellow
    
    $body = @{
        email = "admin@clinica.com"
        password = "admin123"
    } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" `
            -Method POST `
            -ContentType "application/json" `
            -Body $body
        
        $script:Token = $response.token
        
        if ($Token) {
            Write-Host "✓ Token obtenido" -ForegroundColor Green
            Write-Host ""
        } else {
            Write-Host "✗ Error al obtener token" -ForegroundColor Red
            exit 1
        }
    } catch {
        Write-Host "✗ Error al obtener token: $_" -ForegroundColor Red
        exit 1
    }
}

# Función para probar un endpoint
function Test-Endpoint {
    param(
        [string]$Endpoint,
        [string]$Name,
        [hashtable]$Data
    )
    
    Write-Host "📧 Probando: $Name" -ForegroundColor Yellow
    
    $formData = $Data.GetEnumerator() | ForEach-Object { 
        "$($_.Key)=$($_.Value)" 
    } | Join-String -Separator "&"
    
    $headers = @{
        "Authorization" = "Bearer $Token"
        "Content-Type" = "application/x-www-form-urlencoded"
    }
    
    try {
        $response = Invoke-RestMethod -Uri "$BaseUrl/api/test/email/$Endpoint" `
            -Method POST `
            -Headers $headers `
            -Body $formData
        
        if ($response.success) {
            Write-Host "✓ $Name enviado exitosamente" -ForegroundColor Green
        } else {
            Write-Host "✗ Error al enviar $Name" -ForegroundColor Red
            Write-Host "Respuesta: $($response | ConvertTo-Json)" -ForegroundColor Red
        }
    } catch {
        Write-Host "✗ Error al enviar $Name: $_" -ForegroundColor Red
    }
    
    Write-Host ""
    Start-Sleep -Seconds 1
}

# Obtener token
Get-Token

# Probar cada tipo de correo
Test-Endpoint -Endpoint "bienvenida-usuario" `
    -Name "Bienvenida Usuario" `
    -Data @{
        email = $Email
        nombre = "Usuario de Prueba"
        rol = "Veterinario"
    }

Test-Endpoint -Endpoint "bienvenida-cliente" `
    -Name "Bienvenida Cliente" `
    -Data @{
        email = $Email
        nombre = "Cliente de Prueba"
    }

Test-Endpoint -Endpoint "cambio-password-usuario" `
    -Name "Cambio Password Usuario" `
    -Data @{
        email = $Email
        nombre = "Usuario de Prueba"
        esResetAdmin = "false"
    }

Test-Endpoint -Endpoint "cambio-password-cliente" `
    -Name "Cambio Password Cliente" `
    -Data @{
        email = $Email
        nombre = "Cliente de Prueba"
    }

Test-Endpoint -Endpoint "confirmacion-cita" `
    -Name "Confirmación Cita" `
    -Data @{
        email = $Email
        nombrePropietario = "Propietario"
        nombrePaciente = "Mascota"
        profesionalNombre = "Dra. María García"
    }

Write-Host "=========================================="
Write-Host "✅ Pruebas completadas" -ForegroundColor Green
Write-Host "Revisa tu bandeja de entrada (y spam) en: $Email" -ForegroundColor Cyan

