#!/bin/bash

# Script para generar variables de entorno para despliegue
# Uso: ./generate-env.sh [railway|render|fly]

PLATFORM=${1:-railway}

echo "🔐 Generando variables de entorno para $PLATFORM..."

# Generar JWT Secret
JWT_SECRET=$(openssl rand -base64 32)

echo ""
echo "📋 Variables de entorno para BACKEND:"
echo "=================================="
echo ""
echo "SPRING_PROFILES_ACTIVE=prod"
echo "JWT_SECRET=$JWT_SECRET"
echo "JWT_EXPIRATION=86400000"
echo "SWAGGER_ENABLED=false"
echo ""

case $PLATFORM in
  railway)
    echo "# Railway - Usa las variables de referencia:"
    echo "DB_URL=\${{Postgres.DATABASE_URL}}"
    echo "DB_USERNAME=\${{Postgres.PGUSER}}"
    echo "DB_PASSWORD=\${{Postgres.PGPASSWORD}}"
    echo "CORS_ALLOWED_ORIGINS=https://tu-frontend.railway.app"
    ;;
  render)
    echo "# Render - Configura en el dashboard:"
    echo "DB_URL=jdbc:postgresql://tu-db-host:5432/vetclinic"
    echo "DB_USERNAME=veterinaria"
    echo "DB_PASSWORD=tu_password_de_render"
    echo "CORS_ALLOWED_ORIGINS=https://tu-frontend.onrender.com"
    ;;
  fly)
    echo "# Fly.io - Usa: fly secrets set"
    echo "DB_URL=jdbc:postgresql://tu-db-host:5432/vetclinic"
    echo "DB_USERNAME=usuario"
    echo "DB_PASSWORD=password"
    echo "CORS_ALLOWED_ORIGINS=https://tu-frontend.fly.dev"
    ;;
esac

echo ""
echo "📋 Variables de entorno para FRONTEND:"
echo "=================================="
echo ""

case $PLATFORM in
  railway)
    echo "VITE_API_URL=https://tu-backend.railway.app/api"
    ;;
  render)
    echo "VITE_API_URL=https://tu-backend.onrender.com/api"
    ;;
  fly)
    echo "VITE_API_URL=https://tu-backend.fly.dev/api"
    ;;
esac

echo ""
echo "✅ Variables generadas!"
echo "💡 Copia y pega estas variables en tu plataforma de despliegue"

