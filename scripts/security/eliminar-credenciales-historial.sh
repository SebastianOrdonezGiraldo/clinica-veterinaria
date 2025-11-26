#!/bin/bash
# Script para eliminar credenciales SMTP del historial de Git
# Ejecutar desde la raíz del proyecto

echo "🔐 Eliminando credenciales SMTP del historial de Git..."
echo ""

# Verificar que estamos en un repositorio Git
if [ ! -d ".git" ]; then
    echo "❌ Error: Este script debe ejecutarse desde la raíz del repositorio Git"
    exit 1
fi

# Verificar cambios sin commitear
if [ -n "$(git status --porcelain)" ]; then
    echo "⚠️  ADVERTENCIA: Tienes cambios sin commitear"
    git status --short
    echo ""
    read -p "¿Deseas hacer commit de estos cambios antes de continuar? (s/n): " response
    if [ "$response" = "s" ] || [ "$response" = "S" ]; then
        git add .
        read -p "Ingresa el mensaje del commit: " commitMsg
        git commit -m "$commitMsg"
    else
        echo "❌ Abortando. Por favor, haz commit o stash de tus cambios primero."
        exit 1
    fi
fi

# Crear archivo temporal con el reemplazo
cat > /tmp/fix-credentials.sh << 'EOF'
#!/bin/bash
# Reemplazar credenciales hardcodeadas por variables de entorno
sed -i 's/MAIL_USERNAME=sebastian789go@gmail.com/MAIL_USERNAME=${MAIL_USERNAME:}/g' "$1"
sed -i 's/MAIL_PASSWORD=yywqbtcsrvgdxdzy/MAIL_PASSWORD=${MAIL_PASSWORD:}/g' "$1"
sed -i 's/MAIL_FROM=sebastian789go@gmail.com/MAIL_FROM=${MAIL_FROM:noreply@clinica-veterinaria.com}/g' "$1"
EOF

chmod +x /tmp/fix-credentials.sh

echo "🔄 Reescribiendo historial de Git..."
export FILTER_BRANCH_SQUELCH_WARNING=1

git filter-branch --force --tree-filter '
    if [ -f apps/backend/src/main/resources/application.properties ]; then
        /tmp/fix-credentials.sh apps/backend/src/main/resources/application.properties
    fi
' --prune-empty --tag-name-filter cat -- --all

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Historial limpiado exitosamente"
    echo ""
    echo "⚠️  IMPORTANTE: Ahora debes hacer force push:"
    echo "   git push origin --force --all"
    echo "   git push origin --force --tags"
    echo ""
    echo "🔐 RECUERDA:"
    echo "   1. Generar nueva contraseña de aplicación de Gmail"
    echo "   2. Actualizar apps/backend/.env con la nueva contraseña"
    echo "   3. Reiniciar la aplicación backend"
else
    echo "❌ Error al limpiar el historial"
    exit 1
fi

# Limpiar archivo temporal
rm -f /tmp/fix-credentials.sh

