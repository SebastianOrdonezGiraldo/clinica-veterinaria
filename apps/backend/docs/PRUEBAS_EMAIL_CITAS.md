# Guía de Pruebas - Sistema de Correos para Citas

## 📋 Resumen

Este documento describe cómo probar el sistema de envío de correos electrónicos para citas veterinarias.

## ✅ Estado Actual

- ✅ Plantilla HTML para creación de citas (`cita-confirmacion.html`)
- ✅ Plantilla HTML para cancelación (`cita-cancelacion.html`)
- ✅ Plantilla HTML para actualización de estado (`cita-estado-actualizado.html`)
- ✅ Envío automático de correos en creación, cancelación y confirmación

## 🔧 Configuración Requerida

### 1. Variables de Entorno

Asegúrate de que el archivo `.env` en `apps/backend/` tenga las siguientes variables configuradas:

```env
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=tu-email@gmail.com
MAIL_PASSWORD=tu-contraseña-de-aplicacion
MAIL_FROM=tu-email@gmail.com
MAIL_FROM_NAME=Clínica Veterinaria
```

### 2. Verificar Configuración de Gmail

- ✅ Verificación en 2 pasos activada
- ✅ Contraseña de aplicación generada
- ✅ URL: https://myaccount.google.com/apppasswords

## 🧪 Casos de Prueba

### Prueba 1: Crear una Nueva Cita

**Objetivo:** Verificar que se envía un correo de confirmación al crear una cita.

**Pasos:**
1. Iniciar el backend
2. Crear una nueva cita desde el frontend o usando la API
3. Verificar que el propietario tenga un email válido
4. Revisar los logs del backend para confirmar el envío
5. Verificar la bandeja de entrada del propietario

**Endpoint:**
```
POST /api/citas
```

**Logs esperados:**
```
✓ Email de confirmación enviado a: [email]
```

**Resultado esperado:**
- ✅ Correo recibido con plantilla `cita-confirmacion.html`
- ✅ Asunto: "Confirmación de cita - [Nombre de la Mascota]"
- ✅ Contenido incluye: fecha, hora, mascota, veterinario, motivo

---

### Prueba 2: Cancelar una Cita

**Objetivo:** Verificar que se envía un correo de cancelación al cancelar una cita.

**Pasos:**
1. Tener una cita existente (PENDIENTE o CONFIRMADA)
2. Cambiar el estado de la cita a CANCELADA
3. Revisar los logs del backend
4. Verificar la bandeja de entrada del propietario

**Endpoint:**
```
PATCH /api/citas/{id}/estado
Body: { "estado": "CANCELADA" }
```

O actualizar la cita completa:
```
PUT /api/citas/{id}
Body: { ..., "estado": "CANCELADA" }
```

**Logs esperados:**
```
✓ Email de cancelación enviado a: [email]
```

**Resultado esperado:**
- ✅ Correo recibido con plantilla `cita-cancelacion.html`
- ✅ Asunto: "Cancelación de cita - [Nombre de la Mascota]"
- ✅ Contenido incluye razón de cancelación (si está en observaciones)

---

### Prueba 3: Confirmar una Cita

**Objetivo:** Verificar que se envía un correo de confirmación al confirmar una cita.

**Pasos:**
1. Tener una cita existente con estado PENDIENTE
2. Cambiar el estado de la cita a CONFIRMADA
3. Revisar los logs del backend
4. Verificar la bandeja de entrada del propietario

**Endpoint:**
```
PATCH /api/citas/{id}/estado
Body: { "estado": "CONFIRMADA" }
```

**Logs esperados:**
```
✓ Email de confirmación enviado a: [email]
```

**Resultado esperado:**
- ✅ Correo recibido con plantilla `cita-estado-actualizado.html`
- ✅ Asunto: "Cita confirmada - [Nombre de la Mascota]"
- ✅ Contenido incluye recordatorios importantes

---

### Prueba 4: Actualizar Estado (Otros Estados)

**Objetivo:** Verificar que se envía un correo al cambiar a otros estados.

**Pasos:**
1. Tener una cita existente
2. Cambiar el estado a ATENDIDA o cualquier otro estado
3. Revisar los logs del backend
4. Verificar la bandeja de entrada del propietario

**Endpoint:**
```
PATCH /api/citas/{id}/estado
Body: { "estado": "ATENDIDA" }
```

**Logs esperados:**
```
✓ Email de actualización de estado enviado a: [email] (Estado: ATENDIDA)
```

---

## 🔍 Verificación de Logs

### Comandos para revisar logs:

```powershell
# Ver logs recientes relacionados con email
cd apps/backend/logs
Get-Content application.log -Tail 100 | Select-String -Pattern "Email|email|sendCita"

# Ver errores de email
Get-Content application.log -Tail 200 | Select-String -Pattern "Error.*email|✗.*Email" -Context 2,2
```

### Logs de éxito esperados:
- `✓ Email HTML enviado exitosamente a: [email]`
- `✓ Email de confirmación enviado a: [email]`
- `✓ Email de cancelación enviado a: [email]`
- `✓ Email de actualización de estado enviado a: [email]`

### Logs de error comunes:
- `✗ Error al enviar email HTML a [email]: Authentication failed`
  - **Solución:** Verificar credenciales de Gmail en `.env`
- `✗ No se puede enviar email: app.mail.from no está configurado`
  - **Solución:** Verificar variable `MAIL_FROM` en `.env`
- `Propietario sin email, no se envía confirmación por correo`
  - **Solución:** Asegurarse de que el propietario tenga email registrado

## 🐛 Troubleshooting

### Problema: No se reciben correos

1. **Verificar configuración de Gmail:**
   - Revisar que la contraseña de aplicación sea correcta
   - Verificar que la verificación en 2 pasos esté activada
   - Generar una nueva contraseña de aplicación si es necesario

2. **Verificar logs del backend:**
   ```powershell
   Get-Content apps/backend/logs/application.log -Tail 50 | Select-String -Pattern "Email|Mail"
   ```

3. **Verificar que el propietario tenga email:**
   - El email debe estar registrado en la base de datos
   - El email debe ser válido

4. **Verificar carpeta de spam:**
   - Los correos pueden llegar a la carpeta de spam
   - Marcar como "No es spam" si es necesario

### Problema: Error de autenticación

**Error:** `Authentication failed`

**Solución:**
1. Ir a: https://myaccount.google.com/apppasswords
2. Generar una nueva contraseña de aplicación
3. Actualizar `MAIL_PASSWORD` en el archivo `.env`
4. Reiniciar el backend

### Problema: Plantilla no se renderiza

**Solución:**
1. Verificar que las plantillas estén en: `apps/backend/src/main/resources/templates/email/`
2. Verificar que Thymeleaf esté configurado en `application.properties`
3. Revisar logs para errores de renderizado

## 📝 Notas Importantes

- ⚠️ Los correos se envían de forma asíncrona y no bloquean la operación principal
- ⚠️ Si falla el envío de correo, la cita se crea/actualiza igualmente
- ⚠️ Los errores de correo se registran en los logs pero no interrumpen el flujo
- ✅ El sistema valida que el propietario tenga email antes de intentar enviar
- ✅ Los correos incluyen información completa de la cita

## 🎯 Checklist de Pruebas

- [ ] Crear nueva cita → Correo de confirmación recibido
- [ ] Cancelar cita → Correo de cancelación recibido
- [ ] Confirmar cita → Correo de confirmación recibido
- [ ] Cambiar estado a ATENDIDA → Correo de actualización recibido
- [ ] Verificar que los correos tienen el formato HTML correcto
- [ ] Verificar que los datos en los correos son correctos
- [ ] Verificar logs del backend para confirmar envíos exitosos

## 📞 Siguiente Paso

Una vez completadas las pruebas, puedes continuar con otras funcionalidades o ajustar las plantillas según tus necesidades.

