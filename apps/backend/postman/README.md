# 📬 Colección de Postman - Pruebas de Correos

Esta carpeta contiene la colección de Postman completa para probar el sistema de correos electrónicos de la Clínica Veterinaria.

## 📁 Archivos

- **`Clinica-Veterinaria-Email-Tests.postman_collection.json`** - Colección principal con todos los endpoints de prueba
- **`Clinica-Veterinaria-Environment.postman_environment.json`** - Variables de entorno para desarrollo
- **`README.md`** - Este archivo

## 🚀 Cómo Usar

### 1. Importar en Postman

1. Abre Postman
2. Haz clic en **Import** (botón superior izquierdo)
3. Arrastra los archivos JSON o selecciona **Upload Files**
4. Importa ambos archivos:
   - La colección (`*.postman_collection.json`)
   - El entorno (`*.postman_environment.json`)

### 2. Configurar Variables

1. Selecciona el entorno **"Clínica Veterinaria - Desarrollo"** en el selector de entornos (esquina superior derecha)
2. Edita las variables:
   - **`test_email`**: Cambia a tu email real donde quieres recibir los correos de prueba
   - **`base_url`**: Debe ser `http://localhost:8080` (o tu URL si es diferente)

### 3. Ejecutar Pruebas

#### Opción A: Paso a Paso (Recomendado)

1. **Primero, autenticarse:**
   - Abre la carpeta **"1. Autenticación"**
   - Ejecuta **"Login - Admin"**
   - El token se guardará automáticamente en la variable `auth_token`

2. **Luego, probar correos:**
   - Abre la carpeta **"2. Pruebas de Correos"**
   - Ejecuta cada endpoint individualmente
   - Revisa tu bandeja de entrada después de cada envío

#### Opción B: Probar Todos de Una Vez

1. Autentícate primero (paso 1 de Opción A)
2. Ejecuta **"Probar Todos los Correos"**
3. Espera unos segundos (envía 5 correos con 1 segundo de espera entre cada uno)
4. Revisa tu bandeja de entrada

## 📋 Endpoints Incluidos

### Autenticación
- ✅ Login - Admin
- ✅ Validar Token

### Pruebas de Correos
- ✅ Bienvenida Usuario Sistema
- ✅ Bienvenida Cliente/Propietario
- ✅ Cambio Contraseña - Usuario Sistema (Por Usuario)
- ✅ Cambio Contraseña - Usuario Sistema (Por Admin)
- ✅ Cambio Contraseña - Cliente/Propietario
- ✅ Confirmación de Cita
- ✅ Probar Todos los Correos

## 🔧 Variables de Entorno

| Variable | Descripción | Valor por Defecto |
|----------|-------------|-------------------|
| `base_url` | URL base del backend | `http://localhost:8080` |
| `test_email` | Email donde recibir correos de prueba | `tu-email@gmail.com` |
| `auth_token` | Token JWT (se llena automáticamente) | (vacío) |
| `user_id` | ID del usuario autenticado | (vacío) |
| `admin_email` | Email del admin para login | `admin@clinica.com` |
| `admin_password` | Contraseña del admin | `admin123` |

## ✅ Verificación

Después de ejecutar cada prueba:

1. **Revisa la respuesta JSON:**
   ```json
   {
     "success": true,
     "message": "Email enviado exitosamente",
     "email": "tu-email@gmail.com",
     "tipo": "bienvenida-usuario"
   }
   ```

2. **Revisa los logs del servidor:**
   - Deberías ver: `✓ Email HTML enviado exitosamente a: tu-email@gmail.com`

3. **Revisa tu bandeja de entrada:**
   - Incluye la carpeta de spam
   - Verifica el formato del correo
   - Verifica que los enlaces funcionen

## 🐛 Troubleshooting

### Error 401 Unauthorized

**Problema:** El token expiró o no está configurado.

**Solución:**
1. Ejecuta nuevamente "Login - Admin"
2. Verifica que el entorno esté seleccionado
3. Verifica que `auth_token` tenga un valor

### Error 403 Forbidden

**Problema:** El usuario no tiene rol ADMIN.

**Solución:**
- Asegúrate de usar las credenciales de admin:
  - Email: `admin@clinica.com`
  - Password: `admin123`

### Error de Conexión

**Problema:** El servidor no está corriendo o la URL es incorrecta.

**Solución:**
- Verifica que el backend esté corriendo en `http://localhost:8080`
- Verifica la variable `base_url` en el entorno

### El correo no llega

**Verifica:**
1. Revisa la carpeta de spam
2. Verifica que `test_email` sea un email válido
3. Revisa los logs del servidor para errores
4. Verifica la configuración SMTP en el archivo `.env`

## 📝 Notas

- Los endpoints de prueba solo están disponibles en perfil `dev`
- El token JWT tiene una duración limitada (por defecto 24 horas)
- Si el token expira, simplemente ejecuta "Login - Admin" nuevamente
- Los correos se envían de forma asíncrona, puede haber un pequeño retraso

## 🔒 Seguridad

- **NO** compartas el archivo de entorno con credenciales reales
- **NO** subas estos archivos a repositorios públicos
- Los endpoints de prueba están protegidos y solo accesibles para ADMIN
- En producción, estos endpoints estarán deshabilitados automáticamente

---

**Última actualización:** Diciembre 2024

