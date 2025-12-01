#!/bin/bash

# Script para iniciar la aplicación con Docker

set -e

echo "🐳 Iniciando Clínica Veterinaria con Docker..."

# Verificar si existe .env
if [ ! -f .env ]; then
    echo "📝 Creando archivo .env desde .env.example..."
    cp .env.example .env
    echo "⚠️  Por favor, edita .env con tus configuraciones antes de continuar"
    echo "   Presiona Enter para continuar o Ctrl+C para cancelar..."
    read
fi

# Construir y levantar servicios
echo "🔨 Construyendo imágenes..."
docker-compose build

echo "🚀 Iniciando servicios..."
docker-compose up -d

echo "⏳ Esperando que los servicios estén listos..."
sleep 10

# Verificar estado
echo "📊 Estado de los servicios:"
docker-compose ps

echo ""
echo "✅ Servicios iniciados!"
echo ""
echo "📍 URLs:"
echo "   Frontend:  http://localhost:${FRONTEND_PORT:-5173}"
echo "   Backend:   http://localhost:${BACKEND_PORT:-8080}"
echo "   Swagger:   http://localhost:${BACKEND_PORT:-8080}/swagger-ui/index.html"
echo ""
echo "📋 Ver logs: docker-compose logs -f"
echo "🛑 Detener:  docker-compose down"

