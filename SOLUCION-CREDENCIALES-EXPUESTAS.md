# 🔐 Solución para Credenciales SMTP Expuestas

## ⚠️ PROBLEMA CRÍTICO

GitGuardian detectó que las credenciales SMTP están expuestas en el historial de Git en los commits:
- `27ebfb77f51304017a185487fc7b83cb1c0210f9` (25 Nov 2025 20:29)
- `708adc59cf396eac98e98d687dde794a250fc11e` (25 Nov 2025 20:13)

**Las credenciales comprometidas son:**
- Email: `sebastian789go@gmail.com`
- Contraseña: `yywqbtcsrvgdxdzy` (contraseña de aplicación de Gmail)

## ✅ ESTADO ACTUAL

- ✅ El archivo `application.properties` **actualmente** solo usa variables de entorno (`${MAIL_USERNAME:}`, `${MAIL_PASSWORD:}`)
- ✅ El archivo `.env` está en `.gitignore` y no se commitea
- ❌ **Las credenciales aún están en el historial de Git** (commits anteriores)

## 🚨 ACCIONES INMEDIATAS REQUERIDAS

### 1. ROTAR CREDENCIALES DE GMAIL (URGENTE - HACER PRIMERO)

**La contraseña de aplicación actual está comprometida y debe ser revocada inmediatamente:**

1. **Revocar la contraseña de aplicación actual:**
   - Ve a: https://myaccount.google.com/apppasswords
   - Busca y elimina la contraseña de aplicación que termina en `...dzy`

2. **Generar una nueva contraseña de aplicación:**
   - En la misma página, genera una nueva contraseña
   - Selecciona "Correo" y "Otro (nombre personalizado)"
   - Ingresa "Clínica Veterinaria API - Nueva"
   - Copia la nueva contraseña (16 caracteres)

3. **Actualizar el archivo `.env`:**
   ```bash
   # Editar apps/backend/.env
   MAIL_PASSWORD=tu-nueva-contraseña-de-aplicacion
   ```

4. **Reiniciar la aplicación backend** para que cargue la nueva contraseña

### 2. LIMPIAR EL HISTORIAL DE GIT

Tienes **3 opciones** para eliminar las credenciales del historial:

#### Opción A: Usar git-filter-repo (Recomendado - Más rápido y seguro)

```bash
# 1. Instalar git-filter-repo
pip install git-filter-repo

# 2. Eliminar el archivo problemático del historial completo
git filter-repo --path apps/backend/src/main/resources/application.properties --invert-paths

# 3. Verificar que las credenciales fueron eliminadas
git log --all --full-history -S "yywqbtcsrvgdxdzy" --source
# No debería mostrar ningún resultado

# 4. Forzar push (⚠️ Coordina con tu equipo primero)
git push origin --force --all
git push origin --force --tags
```

#### Opción B: Usar el script PowerShell incluido

```powershell
# Ejecutar desde la raíz del proyecto
.\scripts\security\eliminar-credenciales-historial.ps1
```

#### Opción C: Reescribir commits específicos manualmente

```bash
# 1. Encontrar el commit anterior al primero con credenciales
git log --oneline --all
# Busca el commit anterior a cf3eab5

# 2. Hacer rebase interactivo
git rebase -i <commit-anterior-a-cf3eab5>

# 3. En el editor, cambiar 'pick' por 'edit' en:
#    - 708adc5 feat: Update email templates...
#    - 27ebfb7 feat: Refactor email templates...

# 4. Para cada commit editado, verificar que application.properties solo tenga variables:
git show HEAD:apps/backend/src/main/resources/application.properties

# 5. Si tiene credenciales hardcodeadas, reemplazarlas:
git checkout HEAD~1 -- apps/backend/src/main/resources/application.properties
git commit --amend --no-edit
git rebase --continue

# 6. Forzar push
git push origin --force --all
```

### 3. VERIFICAR QUE LAS CREDENCIALES FUERON ELIMINADAS

```bash
# Estos comandos NO deberían mostrar ningún resultado
git log --all --full-history -S "yywqbtcsrvgdxdzy" --source
git log --all --full-history -S "sebastian789go@gmail.com" --source
```

## 🔒 PREVENCIÓN FUTURA

- ✅ **NUNCA** commitees archivos `.env` con credenciales reales
- ✅ **NUNCA** hardcodees credenciales en `application.properties` o cualquier archivo
- ✅ Usa siempre variables de entorno: `${MAIL_PASSWORD:}`
- ✅ Usa `env.example` como plantilla (sin credenciales reales)
- ✅ Considera usar GitHub Secrets para CI/CD
- ✅ Revisa los cambios antes de hacer commit: `git diff`

## 📞 SI LAS CREDENCIALES YA FUERON USADAS MALICIOSAMENTE

1. **Cambia la contraseña de tu cuenta de Gmail inmediatamente**
2. **Revisa los logs de acceso:** https://myaccount.google.com/security
3. **Revoca todas las sesiones activas**
4. **Revisa si hay emails enviados desde tu cuenta sin tu conocimiento**
5. **Habilita la verificación en 2 pasos** si no está activada

## 📋 CHECKLIST DE VERIFICACIÓN

- [ ] Contraseña de aplicación de Gmail revocada
- [ ] Nueva contraseña de aplicación generada
- [ ] Archivo `.env` actualizado con nueva contraseña
- [ ] Aplicación backend reiniciada
- [ ] Historial de Git limpiado
- [ ] Verificación de que las credenciales fueron eliminadas del historial
- [ ] Force push realizado (si aplica)
- [ ] Equipo notificado sobre el force push (si aplica)

## 🛠️ ARCHIVOS CREADOS PARA AYUDAR

- `scripts/security/README-SEGURIDAD.md` - Guía completa de seguridad
- `scripts/security/eliminar-credenciales-historial.ps1` - Script PowerShell para limpiar historial
- `scripts/security/eliminar-credenciales-historial.sh` - Script Bash para limpiar historial
- `scripts/security/rotar-credenciales-email.md` - Guía de rotación de credenciales

