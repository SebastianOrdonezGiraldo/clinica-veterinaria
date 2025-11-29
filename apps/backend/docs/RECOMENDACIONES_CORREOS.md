# 📧 Recomendaciones para Sistema de Correos Electrónicos

## 📋 Estado Actual

### ✅ Correos Implementados
- ✅ Confirmación de cita
- ✅ Cancelación de cita
- ✅ Cambio de estado de cita

### ❌ Correos Faltantes
- ❌ Bienvenida al crear cuenta de usuario (sistema)
- ❌ Bienvenida al crear cuenta de propietario/cliente
- ❌ Notificación de cambio de contraseña (usuario sistema)
- ❌ Notificación de cambio de contraseña (propietario/cliente)
- ❌ Confirmación de establecimiento de contraseña (propietario/cliente)

---

## 🎯 Recomendaciones

### 1. **Correos de Bienvenida**

#### Para Usuarios del Sistema (ADMIN, VET, RECEPCION, ESTUDIANTE)
**Cuándo enviar:** Al crear un nuevo usuario en `UsuarioService.create()`

**Contenido sugerido:**
- Mensaje de bienvenida personalizado
- Credenciales de acceso (email y contraseña temporal si aplica)
- Enlace al portal de acceso
- Información sobre el rol asignado
- Instrucciones de seguridad (cambiar contraseña en primer acceso)

#### Para Propietarios/Clientes
**Cuándo enviar:** 
- Al crear un propietario con contraseña en `PropietarioService.createWithPassword()`
- Al establecer contraseña en `PropietarioService.establecerPassword()`

**Contenido sugerido:**
- Mensaje de bienvenida
- Enlace al portal del cliente
- Información sobre cómo acceder
- Beneficios del portal (ver citas, historial, etc.)

### 2. **Correos de Cambio de Contraseña**

#### Para Usuarios del Sistema
**Cuándo enviar:**
- Cuando un admin resetea la contraseña (`UsuarioService.resetPassword()`)
- Cuando el usuario cambia su propia contraseña (`UsuarioService.updateMyProfile()` con password)

**Contenido sugerido:**
- Notificación de cambio de contraseña
- Fecha y hora del cambio
- Si fue por admin: indicar que fue un reset administrativo
- Si fue por el usuario: confirmación del cambio
- Advertencia de seguridad si no fue el usuario quien lo hizo

#### Para Propietarios/Clientes
**Cuándo enviar:**
- Cuando se establece una contraseña inicial (`PropietarioService.establecerPassword()`)
- Cuando se cambia la contraseña (si se implementa en el futuro)

**Contenido sugerido:**
- Confirmación de establecimiento/cambio de contraseña
- Enlace al portal del cliente
- Recordatorio de seguridad

### 3. **Mejoras Adicionales Recomendadas**

#### A. Plantilla Base Reutilizable
- Crear una plantilla base con header, footer y estilos comunes
- Usar fragmentos de Thymeleaf para reutilizar código

#### B. Personalización por Tipo de Usuario
- Diferentes tonos y estilos según el destinatario
- Usuarios sistema: más formal y técnico
- Clientes: más amigable y accesible

#### C. Configuración de Envío
- Permitir deshabilitar correos en desarrollo
- Configurar retry automático para fallos
- Logging detallado de envíos

#### D. Internacionalización
- Preparar plantillas para múltiples idiomas
- Usar `Locale` de Spring para seleccionar idioma

#### E. Seguridad
- **NUNCA** enviar contraseñas en texto plano
- Si se envía contraseña temporal, usar enlace seguro con token
- Incluir advertencias de seguridad en correos

---

## 📝 Plan de Implementación

### Fase 1: Correos de Bienvenida
1. ✅ Plantillas existentes (citas)
2. ⏳ Plantilla bienvenida usuario sistema
3. ⏳ Plantilla bienvenida propietario/cliente
4. ⏳ Integrar en `UsuarioService.create()`
5. ⏳ Integrar en `PropietarioService.createWithPassword()`
6. ⏳ Integrar en `PropietarioService.establecerPassword()`

### Fase 2: Correos de Cambio de Contraseña
1. ⏳ Plantilla cambio contraseña usuario sistema
2. ⏳ Plantilla cambio contraseña propietario/cliente
3. ⏳ Integrar en `UsuarioService.resetPassword()`
4. ⏳ Integrar en `UsuarioService.updateMyProfile()` (si cambia password)
5. ⏳ Integrar en `PropietarioService.establecerPassword()`

### Fase 3: Mejoras y Optimizaciones
1. ⏳ Crear plantilla base reutilizable
2. ⏳ Agregar configuración para deshabilitar en desarrollo
3. ⏳ Mejorar logging y monitoreo
4. ⏳ Agregar tests para correos

---

## 🔒 Consideraciones de Seguridad

### ⚠️ IMPORTANTE: Nunca Enviar Contraseñas en Texto Plano

**Opción 1: Enlace con Token Temporal (Recomendado)**
```
1. Generar token único y temporal (ej: 24 horas)
2. Guardar token hasheado en BD
3. Enviar enlace: https://clinica.com/set-password?token=ABC123
4. Usuario hace clic y establece su contraseña
5. Invalidar token después de uso
```

**Opción 2: Contraseña Temporal (Menos Seguro)**
```
1. Generar contraseña temporal aleatoria
2. Enviar en correo
3. Forzar cambio en primer acceso
4. Marcar como "temporal" en BD
```

**Opción 3: Sin Contraseña en Correo (Más Seguro)**
```
1. Solo enviar notificación de creación de cuenta
2. Usuario debe usar "Olvidé mi contraseña"
3. O admin proporciona contraseña por otro canal seguro
```

---

## 📊 Métricas Recomendadas

- Tasa de entrega de correos
- Tasa de apertura (si se implementa tracking)
- Tasa de clics en enlaces
- Errores de envío
- Tiempo de entrega

---

## 🛠️ Herramientas y Tecnologías

### Actual
- ✅ Spring Mail (JavaMailSender)
- ✅ Thymeleaf para plantillas HTML
- ✅ Gmail SMTP (configurado)

### Recomendaciones Futuras
- Considerar servicios especializados (SendGrid, Mailgun, AWS SES)
- Para producción: usar servicios con mejor deliverability
- Implementar cola de correos para no bloquear requests
- Agregar retry automático con backoff exponencial

---

## ✅ Checklist de Implementación

- [ ] Crear plantillas HTML para todos los correos
- [ ] Agregar métodos en `EmailService`
- [ ] Integrar envíos en servicios correspondientes
- [ ] Agregar configuración para deshabilitar en desarrollo
- [ ] Agregar logging detallado
- [ ] Crear tests unitarios
- [ ] Documentar en README
- [ ] Probar envíos reales
- [ ] Verificar formato en diferentes clientes de correo
- [ ] Optimizar para móviles

---

**Última actualización:** Diciembre 2024

