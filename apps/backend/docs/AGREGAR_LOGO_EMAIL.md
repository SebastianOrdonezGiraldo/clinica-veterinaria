# Cómo Agregar el Logo de la Clínica a los Emails

## 📋 Resumen

Este documento explica cómo agregar el logo real de la Clínica Veterinaria Universitaria Humboldt a las plantillas de correo electrónico.

## 🎯 Opciones para Agregar el Logo

### Opción 1: URL Externa (Recomendado)

Si tienes el logo alojado en un servidor web o CDN:

1. **Obtén la URL pública del logo**
   - Ejemplo: `https://tu-servidor.com/images/logo-clinica.png`
   - O: `https://cdn.tu-dominio.com/logo-clinica.png`

2. **Agrega la URL al archivo `.env`**:
   ```env
   MAIL_LOGO_URL=https://tu-servidor.com/images/logo-clinica.png
   ```

3. **Reinicia el backend** para que cargue la nueva configuración

### Opción 2: Logo desde el Backend (Spring Boot Static Resources)

Si quieres servir el logo desde el mismo backend:

1. **Coloca el logo en la carpeta de recursos estáticos**:
   ```
   apps/backend/src/main/resources/static/images/logo-clinica.png
   ```

2. **Configura una URL base en `application.properties`**:
   ```properties
   app.mail.base-url=http://localhost:8080
   ```

3. **Agrega la URL completa al `.env`**:
   ```env
   MAIL_LOGO_URL=http://localhost:8080/images/logo-clinica.png
   ```

   **Nota:** En producción, reemplaza `localhost:8080` con tu dominio real.

### Opción 3: Logo como Imagen Base64 (Solo para logos pequeños)

Si el logo es pequeño (< 50KB), puedes incrustarlo directamente en el HTML:

1. Convierte la imagen a Base64
2. Actualiza las plantillas HTML directamente

**⚠️ No recomendado** para logos grandes porque aumenta mucho el tamaño del email.

## 📝 Pasos Detallados

### Paso 1: Preparar el Logo

- **Formato recomendado:** PNG con fondo transparente o JPG
- **Tamaño recomendado:** 
  - Header: 200x80 píxeles (ancho x alto)
  - Footer: 150x60 píxeles
- **Peso máximo:** < 100KB para mejor rendimiento

### Paso 2: Subir el Logo

**Si usas URL externa:**
- Sube el logo a tu servidor web, CDN, o servicio de almacenamiento
- Asegúrate de que la URL sea accesible públicamente (sin autenticación)
- Prueba la URL en un navegador para verificar que funciona

**Si usas recursos estáticos del backend:**
- Coloca el archivo en: `apps/backend/src/main/resources/static/images/`
- El logo estará disponible en: `http://localhost:8080/images/logo-clinica.png`

### Paso 3: Configurar la Variable de Entorno

Edita el archivo `.env` en `apps/backend/`:

```env
# URL del logo para emails (debe ser accesible públicamente)
MAIL_LOGO_URL=https://tu-servidor.com/images/logo-clinica.png
```

### Paso 4: Verificar la Configuración

1. Reinicia el backend
2. Crea una cita de prueba
3. Verifica que el logo aparezca en el correo recibido

## 🔍 Verificación

### Verificar que la URL del logo funciona:

```bash
# En PowerShell
Invoke-WebRequest -Uri "https://tu-servidor.com/images/logo-clinica.png" -Method Head
```

### Verificar en los logs:

Si el logo no aparece, revisa los logs del backend:
```powershell
Get-Content apps/backend/logs/application.log -Tail 50 | Select-String -Pattern "logo|Logo"
```

## 🐛 Troubleshooting

### Problema: El logo no aparece en el email

**Posibles causas:**
1. La URL no es accesible públicamente
2. La variable `MAIL_LOGO_URL` no está configurada correctamente
3. El cliente de correo bloquea imágenes externas (normal en algunos clientes)

**Soluciones:**
- Verifica que la URL sea accesible desde un navegador
- Verifica que `MAIL_LOGO_URL` esté en el archivo `.env`
- Reinicia el backend después de cambiar `.env`
- Algunos clientes de correo (como Gmail) bloquean imágenes por defecto, pero el usuario puede habilitarlas

### Problema: El logo se ve muy grande o muy pequeño

**Solución:**
- Ajusta el tamaño del logo en las plantillas HTML
- Busca las clases `.logo` en las plantillas y ajusta `max-width`

### Problema: Error 404 al cargar el logo

**Solución:**
- Verifica que la URL sea correcta
- Asegúrate de que el servidor donde está alojado el logo esté funcionando
- Verifica permisos de acceso al archivo

## 📌 Notas Importantes

- ⚠️ **URLs públicas:** El logo debe estar en una URL accesible públicamente. Los clientes de correo no pueden acceder a recursos locales o protegidos.
- ⚠️ **HTTPS recomendado:** Usa HTTPS para las URLs del logo cuando sea posible.
- ✅ **Fallback:** Si no configuras `MAIL_LOGO_URL`, se usará un placeholder temporal.
- ✅ **Tamaño del email:** Mantén el logo pequeño para no aumentar demasiado el tamaño del email.

## 🎨 Ejemplo de Configuración Completa

```env
# Configuración de Email
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=sebastian789go@gmail.com
MAIL_PASSWORD=yywqbtcsrvgdxdzy
MAIL_FROM=sebastian789go@gmail.com
MAIL_FROM_NAME=Clínica Veterinaria Universitaria Humboldt

# URL del logo (reemplaza con tu URL real)
MAIL_LOGO_URL=https://tu-servidor.com/images/logo-clinica-humboldt.png
```

## 📞 Siguiente Paso

Una vez configurado el logo, prueba creando una cita y verificando que el logo aparezca correctamente en el correo recibido.

