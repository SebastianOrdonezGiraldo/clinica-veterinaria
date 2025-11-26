# 🔐 Rotación de Credenciales SMTP Expuestas

## ⚠️ ACCIÓN INMEDIATA REQUERIDA

Las credenciales SMTP fueron expuestas en el repositorio de GitHub. Debes:

1. **Generar una nueva contraseña de aplicación de Gmail** (la actual está comprometida)
2. **Eliminar las credenciales del historial de Git**
3. **Actualizar el archivo .env con las nuevas credenciales**

## 📋 Pasos para Rotar las Credenciales

### 1. Generar Nueva Contraseña de Aplicación de Gmail

1. Ve a: https://myaccount.google.com/apppasswords
2. Selecciona "Correo" y "Otro (nombre personalizado)"
3. Ingresa "Clínica Veterinaria API - Nueva"
4. Copia la nueva contraseña generada (16 caracteres)

### 2. Actualizar el archivo .env

Edita `apps/backend/.env` y actualiza:

```bash
MAIL_PASSWORD=tu-nueva-contraseña-de-aplicacion
```

### 3. Eliminar Credenciales del Historial de Git

Ejecuta estos comandos desde la raíz del proyecto:

```bash
# Opción 1: Usando git filter-branch (más seguro)
git filter-branch --force --index-filter \
  "git rm --cached --ignore-unmatch apps/backend/src/main/resources/application.properties" \
  --prune-empty --tag-name-filter cat -- --all

# Opción 2: Si la opción 1 no funciona, usar BFG Repo-Cleaner (más rápido)
# Descargar BFG: https://rtyley.github.io/bfg-repo-cleaner/
# java -jar bfg.jar --replace-text passwords.txt
```

### 4. Forzar Push al Repositorio Remoto

⚠️ **ADVERTENCIA**: Esto reescribirá el historial de Git. Asegúrate de coordinar con tu equipo.

```bash
git push origin --force --all
git push origin --force --tags
```

### 5. Verificar que las Credenciales fueron Eliminadas

```bash
git log --all --full-history -S "yywqbtcsrvgdxdzy" --source
# No debería mostrar ningún resultado
```

## 🔒 Prevención Futura

- ✅ El archivo `.env` ya está en `.gitignore`
- ✅ `application.properties` solo usa variables de entorno
- ⚠️ **NUNCA** commitees archivos con credenciales reales
- ⚠️ Usa siempre `env.example` como plantilla

## 📞 Si las Credenciales ya fueron Usadas Maliciosamente

1. Cambia la contraseña de tu cuenta de Gmail inmediatamente
2. Revisa los logs de acceso de tu cuenta de Gmail
3. Revoca todas las sesiones activas
4. Genera una nueva contraseña de aplicación

