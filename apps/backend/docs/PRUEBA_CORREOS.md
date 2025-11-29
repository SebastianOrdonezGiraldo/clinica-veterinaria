# 🧪 Guía de Prueba de Correos Electrónicos

## 📋 Endpoints de Prueba

Se ha creado un controlador de prueba (`EmailTestController`) con los siguientes endpoints:

**Base URL:** `http://localhost:8080/api/test/email`

**Autenticación:** Requiere rol ADMIN (debes estar autenticado como administrador)

### 1. Bienvenida Usuario del Sistema

```bash
POST /api/test/email/bienvenida-usuario
Content-Type: application/x-www-form-urlencoded

email=tu-email@ejemplo.com&nombre=Tu Nombre&rol=Veterinario
```

**Parámetros:**
- `email` (requerido): Email de destino
- `nombre` (opcional, default: "Usuario de Prueba"): Nombre del usuario
- `rol` (opcional, default: "Veterinario"): Rol del usuario

### 2. Bienvenida Cliente/Propietario

```bash
POST /api/test/email/bienvenida-cliente
Content-Type: application/x-www-form-urlencoded

email=tu-email@ejemplo.com&nombre=Cliente de Prueba
```

**Parámetros:**
- `email` (requerido): Email de destino
- `nombre` (opcional, default: "Cliente de Prueba"): Nombre del propietario

### 3. Cambio de Contraseña - Usuario Sistema

```bash
POST /api/test/email/cambio-password-usuario
Content-Type: application/x-www-form-urlencoded

email=tu-email@ejemplo.com&nombre=Usuario de Prueba&esResetAdmin=false
```

**Parámetros:**
- `email` (requerido): Email de destino
- `nombre` (opcional, default: "Usuario de Prueba"): Nombre del usuario
- `esResetAdmin` (opcional, default: false): true si es reset por admin, false si es cambio por el usuario

### 4. Cambio de Contraseña - Cliente

```bash
POST /api/test/email/cambio-password-cliente
Content-Type: application/x-www-form-urlencoded

email=tu-email@ejemplo.com&nombre=Cliente de Prueba
```

**Parámetros:**
- `email` (requerido): Email de destino
- `nombre` (opcional, default: "Cliente de Prueba"): Nombre del propietario

### 5. Confirmación de Cita

```bash
POST /api/test/email/confirmacion-cita
Content-Type: application/x-www-form-urlencoded

email=tu-email@ejemplo.com&nombrePropietario=Propietario&nombrePaciente=Mascota&profesionalNombre=Dra. María García
```

**Parámetros:**
- `email` (requerido): Email de destino
- `nombrePropietario` (opcional): Nombre del propietario
- `nombrePaciente` (opcional): Nombre de la mascota
- `profesionalNombre` (opcional): Nombre del veterinario

### 6. Probar Todos los Correos

```bash
POST /api/test/email/todos
Content-Type: application/x-www-form-urlencoded

email=tu-email@ejemplo.com
```

Envía todos los tipos de correos en secuencia (con 1 segundo de espera entre cada uno).

---

## 🔧 Configuración Requerida

### 1. Variables de Entorno

Asegúrate de tener configurado tu archivo `.env` con:

```env
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=tu-email@gmail.com
MAIL_PASSWORD=tu-contraseña-de-aplicacion
MAIL_FROM=tu-email@gmail.com
MAIL_FROM_NAME=Clínica Veterinaria Universitaria Humboldt
MAIL_BASE_URL=http://localhost:8080
MAIL_LOGO_URL=
```

### 2. Gmail - Contraseña de Aplicación

Si usas Gmail, necesitas generar una contraseña de aplicación:

1. Activa la verificación en 2 pasos en tu cuenta de Google
2. Ve a: https://myaccount.google.com/apppasswords
3. Genera una contraseña de aplicación para "Correo"
4. Usa esa contraseña en `MAIL_PASSWORD`

---

## 📝 Ejemplos de Uso

### Usando cURL

```bash
# 1. Obtener token de autenticación (como ADMIN)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@clinica.com","password":"admin123"}'

# 2. Probar envío de bienvenida usuario
curl -X POST http://localhost:8080/api/test/email/bienvenida-usuario \
  -H "Authorization: Bearer TU_TOKEN_AQUI" \
  -d "email=tu-email@ejemplo.com&nombre=Juan Pérez&rol=Veterinario"

# 3. Probar envío de bienvenida cliente
curl -X POST http://localhost:8080/api/test/email/bienvenida-cliente \
  -H "Authorization: Bearer TU_TOKEN_AQUI" \
  -d "email=tu-email@ejemplo.com&nombre=María González"

# 4. Probar todos los correos
curl -X POST http://localhost:8080/api/test/email/todos \
  -H "Authorization: Bearer TU_TOKEN_AQUI" \
  -d "email=tu-email@ejemplo.com"
```

### Usando Postman

1. **Configurar autenticación:**
   - Ve a la pestaña "Authorization"
   - Selecciona "Bearer Token"
   - Pega tu token JWT

2. **Crear request:**
   - Método: POST
   - URL: `http://localhost:8080/api/test/email/bienvenida-usuario`
   - Body: `x-www-form-urlencoded`
   - Agregar parámetros:
     - `email`: tu-email@ejemplo.com
     - `nombre`: Tu Nombre
     - `rol`: Veterinario

3. **Enviar request y verificar:**
   - Revisa la respuesta JSON
   - Verifica tu bandeja de entrada (y spam)

### Usando Swagger UI

1. Ve a: `http://localhost:8080/swagger-ui`
2. Busca el endpoint `/api/test/email/**`
3. Haz clic en "Try it out"
4. Completa los parámetros
5. Ejecuta y revisa la respuesta

---

## ✅ Verificación

### Respuesta Exitosa

```json
{
  "success": true,
  "message": "Email de bienvenida enviado exitosamente",
  "email": "tu-email@ejemplo.com",
  "tipo": "bienvenida-usuario"
}
```

### Respuesta con Error

```json
{
  "success": false,
  "message": "Error: ...",
  "error": "ExceptionClass",
  "email": "tu-email@ejemplo.com"
}
```

### Logs del Servidor

Revisa los logs del servidor para ver detalles:

```
📧 Enviando email de bienvenida a usuario: tu-email@ejemplo.com
✓ Email HTML enviado exitosamente a: tu-email@ejemplo.com
```

---

## 🔍 Troubleshooting

### Error: "No se puede enviar email: app.mail.from no está configurado"

**Solución:** Verifica que `MAIL_FROM` esté configurado en tu `.env`

### Error: "Authentication failed"

**Solución:** 
- Verifica que `MAIL_USERNAME` y `MAIL_PASSWORD` sean correctos
- Si usas Gmail, asegúrate de usar una contraseña de aplicación

### Error: "Connection timeout"

**Solución:**
- Verifica que `MAIL_HOST` y `MAIL_PORT` sean correctos
- Verifica tu conexión a internet
- Si estás detrás de un firewall, verifica que el puerto 587 esté abierto

### El correo no llega

**Verifica:**
1. Revisa la carpeta de spam
2. Verifica que el email de destino sea válido
3. Revisa los logs del servidor para errores
4. Verifica que la configuración SMTP sea correcta

### Error 403 Forbidden

**Solución:** Asegúrate de estar autenticado como ADMIN y que el token JWT sea válido

---

## 🗑️ Eliminar en Producción

**IMPORTANTE:** Este controlador es solo para desarrollo. Antes de desplegar a producción:

1. Elimina el archivo `EmailTestController.java`
2. O agrega una condición para deshabilitarlo en producción:

```java
@Profile("dev") // Solo disponible en desarrollo
@RestController
public class EmailTestController {
    // ...
}
```

---

## 📊 Pruebas Recomendadas

1. ✅ Probar cada tipo de correo individualmente
2. ✅ Verificar que los correos lleguen correctamente
3. ✅ Verificar formato en diferentes clientes (Gmail, Outlook, etc.)
4. ✅ Verificar que los enlaces funcionen
5. ✅ Verificar que las imágenes se muestren (si aplica)
6. ✅ Probar en dispositivos móviles

---

**Última actualización:** Diciembre 2024

