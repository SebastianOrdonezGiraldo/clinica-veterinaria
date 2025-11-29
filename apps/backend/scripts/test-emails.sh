#!/bin/bash

# Script para probar el envío de correos electrónicos
# Requiere: curl, jq (opcional para formatear JSON)

BASE_URL="http://localhost:8080"
EMAIL="${1:-test@ejemplo.com}"

echo "🧪 Probando envío de correos a: $EMAIL"
echo "=========================================="
echo ""

# Colores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Función para hacer login y obtener token
get_token() {
    echo "🔐 Obteniendo token de autenticación..."
    RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
        -H "Content-Type: application/json" \
        -d '{"email":"admin@clinica.com","password":"admin123"}')
    
    TOKEN=$(echo $RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
    
    if [ -z "$TOKEN" ]; then
        echo -e "${RED}✗ Error al obtener token${NC}"
        echo "Respuesta: $RESPONSE"
        exit 1
    fi
    
    echo -e "${GREEN}✓ Token obtenido${NC}"
    echo ""
}

# Función para probar un endpoint
test_endpoint() {
    local endpoint=$1
    local name=$2
    local data=$3
    
    echo "📧 Probando: $name"
    RESPONSE=$(curl -s -X POST "$BASE_URL/api/test/email/$endpoint" \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -d "$data")
    
    SUCCESS=$(echo $RESPONSE | grep -o '"success":[^,]*' | cut -d':' -f2)
    
    if [ "$SUCCESS" = "true" ]; then
        echo -e "${GREEN}✓ $name enviado exitosamente${NC}"
    else
        echo -e "${RED}✗ Error al enviar $name${NC}"
        echo "Respuesta: $RESPONSE"
    fi
    echo ""
    
    sleep 1
}

# Obtener token
get_token

# Probar cada tipo de correo
test_endpoint "bienvenida-usuario" "Bienvenida Usuario" "email=$EMAIL&nombre=Usuario de Prueba&rol=Veterinario"
test_endpoint "bienvenida-cliente" "Bienvenida Cliente" "email=$EMAIL&nombre=Cliente de Prueba"
test_endpoint "cambio-password-usuario" "Cambio Password Usuario" "email=$EMAIL&nombre=Usuario de Prueba&esResetAdmin=false"
test_endpoint "cambio-password-cliente" "Cambio Password Cliente" "email=$EMAIL&nombre=Cliente de Prueba"
test_endpoint "confirmacion-cita" "Confirmación Cita" "email=$EMAIL&nombrePropietario=Propietario&nombrePaciente=Mascota&profesionalNombre=Dra. María García"

echo "=========================================="
echo -e "${GREEN}✅ Pruebas completadas${NC}"
echo "Revisa tu bandeja de entrada (y spam) en: $EMAIL"

