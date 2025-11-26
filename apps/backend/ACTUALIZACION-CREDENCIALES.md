# ✅ Credenciales SMTP Actualizadas

## Estado Actual

- ✅ **Nueva contraseña de aplicación configurada** en `apps/backend/.env`
- ✅ **Contraseña antigua revocada** (ya no funciona)
- ⚠️ **Pendiente**: Limpiar historial de Git para eliminar la contraseña antigua

## Próximos Pasos

### 1. Reiniciar la Aplicación Backend

La aplicación debe reiniciarse para cargar la nueva contraseña desde el archivo `.env`:

```bash
# Detener la aplicación actual (Ctrl+C)
# Luego reiniciarla
cd apps/backend
mvn spring-boot:run
```

### 2. Probar el Envío de Emails

Después de reiniciar, crea una cita de prueba para verificar que los emails se envían correctamente.

### 3. Limpiar el Historial de Git (IMPORTANTE)

La contraseña antigua aún está en el historial de Git. Debes eliminarla:

**Opción más simple:**
```powershell
.\scripts\security\eliminar-credenciales-historial.ps1
```

**O manualmente:**
```bash
# Instalar git-filter-repo (recomendado)
pip install git-filter-repo

# Eliminar credenciales del historial
git filter-repo --path apps/backend/src/main/resources/application.properties --invert-paths

# Verificar que fueron eliminadas
git log --all --full-history -S "yywqbtcsrvgdxdzy" --source
# No debería mostrar ningún resultado

# Forzar push (⚠️ Coordina con tu equipo primero)
git push origin --force --all
git push origin --force --tags
```

## Verificación

Para verificar que los emails funcionan:

1. Crea una cita desde el frontend
2. Revisa los logs del backend buscando:
   - `📧 Intentando enviar email a: ...`
   - `✓ Email HTML enviado exitosamente a: ...`
3. Verifica que el email llegue al propietario

## Notas de Seguridad

- ✅ La nueva contraseña está solo en `.env` (no commiteado)
- ✅ El archivo `.env` está en `.gitignore`
- ⚠️ La contraseña antigua aún está en el historial de Git (debe limpiarse)
- ✅ `application.properties` solo usa variables de entorno (no tiene credenciales hardcodeadas)

