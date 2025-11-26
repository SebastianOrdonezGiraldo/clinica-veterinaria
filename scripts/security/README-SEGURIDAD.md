# 🔐 Solución para Credenciales SMTP Expuestas

## ⚠️ PROBLEMA DETECTADO

GitGuardian detectó que las credenciales SMTP están expuestas en el historial de Git en los commits:
- `27ebfb77f51304017a185487fc7b83cb1c0210f9` (25 Nov 2025)
- `708adc59cf396eac98e98d687dde794a250fc11e` (25 Nov 2025)

## ✅ ESTADO ACTUAL

- ✅ El archivo `application.properties` actualmente solo usa variables de entorno
- ✅ El archivo `.env` está en `.gitignore` y no se commitea
- ❌ Las credenciales aún están en el historial de Git (commits anteriores)

## 🚨 ACCIONES INMEDIATAS REQUERIDAS

### 1. Rotar las Credenciales de Gmail (URGENTE)

**La contraseña de aplicación actual está comprometida. Debes generar una nueva:**

1. Ve a: https://myaccount.google.com/apppasswords
2. Genera una nueva contraseña de aplicación
3. Actualiza `apps/backend/.env`:
   ```bash
   MAIL_PASSWORD=tu-nueva-contraseña-de-aplicacion
   ```
4. Reinicia la aplicación backend

### 2. Limpiar el Historial de Git

Tienes dos opciones:

#### Opción A: Usar git-filter-repo (Recomendado)

```bash
# Instalar git-filter-repo
pip install git-filter-repo

# Eliminar el archivo del historial completo
git filter-repo --path apps/backend/src/main/resources/application.properties --invert-paths

# Forzar push (⚠️ Coordina con tu equipo primero)
git push origin --force --all
git push origin --force --tags
```

#### Opción B: Reescribir Commits Específicos

```bash
# Encontrar el commit anterior al primero con credenciales
git log --oneline --all | Select-String "cf3eab5"

# Hacer rebase interactivo
git rebase -i <commit-anterior>

# En el editor, cambiar 'pick' por 'edit' en los commits:
# - 708adc5
# - 27ebfb7

# Para cada commit editado:
git checkout HEAD~1 -- apps/backend/src/main/resources/application.properties
git commit --amend --no-edit
git rebase --continue

# Forzar push
git push origin --force --all
```

### 3. Verificar que las Credenciales fueron Eliminadas

```bash
# Esto no debería mostrar ningún resultado
git log --all --full-history -S "yywqbtcsrvgdxdzy" --source
git log --all --full-history -S "sebastian789go@gmail.com" --source
```

## 🔒 Prevención Futura

- ✅ **NUNCA** commitees archivos `.env` con credenciales reales
- ✅ **NUNCA** hardcodees credenciales en `application.properties`
- ✅ Usa siempre variables de entorno: `${MAIL_PASSWORD:}`
- ✅ Usa `env.example` como plantilla (sin credenciales reales)
- ✅ Considera usar GitHub Secrets para CI/CD

## 📞 Si las Credenciales ya fueron Usadas

1. Cambia la contraseña de tu cuenta de Gmail inmediatamente
2. Revisa los logs de acceso: https://myaccount.google.com/security
3. Revoca todas las sesiones activas
4. Revisa si hay emails enviados desde tu cuenta sin tu conocimiento

