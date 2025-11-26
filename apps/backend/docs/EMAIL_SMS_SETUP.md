# 📧 Configuración de Email y SMS para Confirmación de Citas

Este documento explica cómo configurar el envío automático de emails y SMS cuando se crea una nueva cita.

## 📧 Configuración de Email

### Para Gmail (Recomendado para desarrollo)

1. **Activar verificación en 2 pasos:**
   - Ve a tu cuenta de Google: https://myaccount.google.com/
   - Seguridad → Verificación en 2 pasos → Activar

2. **Generar contraseña de aplicación:**
   - Ve a: https://myaccount.google.com/apppasswords
   - Selecciona "Correo" y "Otro (nombre personalizado)"
   - Ingresa "Clínica Veterinaria API"
   - Copia la contraseña generada (16 caracteres)

3. **Configurar variables de entorno:**
   ```bash
   MAIL_HOST=smtp.gmail.com
   MAIL_PORT=587
   MAIL_USERNAME=tu-email@gmail.com
   MAIL_PASSWORD=la-contraseña-de-aplicación-generada
   MAIL_FROM=noreply@clinica-veterinaria.com
   MAIL_FROM_NAME=Clínica Veterinaria
   ```

### Para otros proveedores

#### Outlook/Hotmail
```bash
MAIL_HOST=smtp-mail.outlook.com
MAIL_PORT=587
MAIL_USERNAME=tu-email@outlook.com
MAIL_PASSWORD=tu-contraseña
```

#### SendGrid
```bash
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=tu-api-key-de-sendgrid
```

#### Amazon SES
```bash
MAIL_HOST=email-smtp.us-east-1.amazonaws.com
MAIL_PORT=587
MAIL_USERNAME=tu-access-key-id
MAIL_PASSWORD=tu-secret-access-key
```

## 📱 Configuración de SMS (Opcional)

### Twilio (Recomendado)

1. **Crear cuenta en Twilio:**
   - Ve a: https://www.twilio.com/
   - Regístrate y verifica tu cuenta
   - Obtén tu Account SID y Auth Token del dashboard

2. **Obtener número de teléfono:**
   - En el dashboard de Twilio, ve a "Phone Numbers"
   - Compra o usa un número de prueba (gratis para desarrollo)

3. **Agregar dependencia de Twilio al pom.xml:**
   ```xml
   <dependency>
       <groupId>com.twilio.sdk</groupId>
       <artifactId>twilio</artifactId>
       <version>9.14.0</version>
   </dependency>
   ```

4. **Descomentar código en SMSService.java:**
   - Abre `SMSService.java`
   - Descomenta el código en el método `sendSMSViaTwilio()`
   - Elimina el log de "modo desarrollo"

5. **Configurar variables de entorno:**
   ```bash
   SMS_ENABLED=true
   SMS_PROVIDER=twilio
   TWILIO_ACCOUNT_SID=tu-account-sid
   TWILIO_AUTH_TOKEN=tu-auth-token
   TWILIO_FROM_NUMBER=+1234567890
   ```

### Otros proveedores de SMS

- **AWS SNS:** Requiere configuración de AWS SDK
- **Vonage (Nexmo):** Similar a Twilio
- **Plivo:** Requiere SDK específico
- **MessageBird:** Requiere SDK específico

## 🧪 Pruebas

### Probar Email

1. Configura las variables de entorno de email
2. Crea una cita desde el frontend o API
3. Verifica que el propietario reciba el email

### Probar SMS

1. Configura las variables de entorno de SMS
2. Asegúrate de que el propietario tenga un teléfono válido
3. Crea una cita
4. Verifica que se envíe el SMS

## 🔍 Troubleshooting

### Email no se envía

- Verifica que las credenciales sean correctas
- Para Gmail, asegúrate de usar contraseña de aplicación, no la contraseña normal
- Revisa los logs del backend para ver errores específicos
- Verifica que el propietario tenga un email válido

### SMS no se envía

- Verifica que `SMS_ENABLED=true`
- Asegúrate de tener la dependencia de Twilio agregada
- Verifica que el número de teléfono esté en formato internacional (+XX...)
- Revisa los logs del backend

### Plantilla de email no se renderiza

- Verifica que la plantilla esté en: `src/main/resources/templates/email/cita-confirmacion.html`
- Asegúrate de que Thymeleaf esté configurado correctamente
- Revisa los logs para errores de renderizado

## 📝 Notas

- Los emails y SMS se envían de forma asíncrona y no bloquean la creación de la cita
- Si falla el envío, se registra en los logs pero no se lanza excepción
- En desarrollo, puedes usar servicios como Mailtrap o MailHog para probar emails sin enviarlos realmente
- Para SMS en desarrollo, Twilio ofrece números de prueba gratuitos

