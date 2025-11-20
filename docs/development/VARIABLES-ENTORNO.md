# 🔐 Variables de Entorno - Configuración

Este documento explica cómo configurar las variables de entorno para el proyecto Clínica Veterinaria.

## 📋 Tabla de Contenidos

- [Backend](#backend)
- [Frontend](#frontend)
- [Seguridad](#seguridad)
- [Troubleshooting](#troubleshooting)

---

## 🔧 Backend

### Configuración Rápida

1. **Copia el archivo de ejemplo:**
   ```bash
   cd apps/backend
   cp env.example .env
   ```

2. **Edita el archivo `.env`** con tus valores:
   ```bash
   # Usa tu editor favorito
   notepad .env  # Windows
   nano .env     # Linux/Mac
   ```

3. **Configura las variables necesarias:**
   ```env
   SERVER_PORT=8080
   DB_URL=jdbc:postgresql://localhost:5433/vetclinic
   DB_USERNAME=postgres
   DB_PASSWORD=tu_password_seguro
   JWT_SECRET=tu_secreto_jwt_muy_largo_y_seguro
   ```

### Variables Disponibles

| Variable | Descripción | Valor por Defecto | Requerido |
|----------|-------------|-------------------|-----------|
| `SERVER_PORT` | Puerto del servidor Spring Boot | `8080` | No |
| `DB_URL` | URL de conexión a PostgreSQL | `jdbc:postgresql://localhost:5433/vetclinic` | Sí |
| `DB_USERNAME` | Usuario de la base de datos | `postgres` | Sí |
| `DB_PASSWORD` | Contraseña de la base de datos | - | Sí |
| `JWT_SECRET` | Secreto para firmar tokens JWT | - | Sí (producción) |
| `JWT_EXPIRATION` | Tiempo de expiración del token (ms) | `86400000` (24h) | No |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos para CORS | `*` | No |
| `SWAGGER_ENABLED` | Habilitar Swagger UI | `true` | No |
| `SPRING_PROFILES_ACTIVE` | Perfil activo de Spring | `dev` | No |

### Generar Secreto JWT Seguro

**En producción, es CRÍTICO usar un secreto seguro:**

```bash
# Linux/Mac
openssl rand -base64 32

# Windows (PowerShell)
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
```

El secreto debe tener **al menos 32 caracteres** para seguridad adecuada.

---

## 🎨 Frontend

### Configuración Rápida

1. **Copia el archivo de ejemplo:**
   ```bash
   cd apps/frontend
   cp env.example .env.local
   ```

2. **Edita el archivo `.env.local`** con tus valores:
   ```env
   VITE_API_BASE_URL=http://localhost:8080/api
   VITE_APP_ENV=development
   ```

### Variables Disponibles

| Variable | Descripción | Valor por Defecto | Requerido |
|----------|-------------|-------------------|-----------|
| `VITE_API_BASE_URL` | URL base de la API backend | `http://localhost:8080/api` | Sí |
| `VITE_APP_ENV` | Entorno de la aplicación | `development` | No |
| `VITE_APP_NAME` | Nombre de la aplicación | `Clínica Veterinaria` | No |
| `VITE_ENABLE_LOGGING` | Habilitar logging detallado | `true` | No |
| `VITE_LOG_LEVEL` | Nivel de logging | `debug` | No |

### Nota sobre Vite

Las variables de entorno en Vite **deben comenzar con `VITE_`** para ser accesibles en el código del frontend.

**Ejemplo de uso:**
```typescript
const apiUrl = import.meta.env.VITE_API_BASE_URL;
```

---

## 🔒 Seguridad

### ⚠️ IMPORTANTE: Nunca commitees archivos sensibles

Los siguientes archivos están en `.gitignore` y **NUNCA** deben ser commiteados:

- `.env`
- `.env.local`
- `.env.production`
- `*.key`
- `*.pem`
- `secrets/`
- `credentials/`

### ✅ Archivos que SÍ se commitean (plantillas):

- `env.example`
- `.env.example`
- `application.example.properties`

### Checklist de Seguridad

- [ ] El archivo `.env` está en `.gitignore`
- [ ] No hay credenciales hardcodeadas en el código
- [ ] El secreto JWT tiene al menos 32 caracteres
- [ ] Las contraseñas de base de datos son seguras
- [ ] En producción, Swagger está deshabilitado
- [ ] CORS está configurado correctamente para producción

---

## 🐛 Troubleshooting

### El backend no lee las variables de entorno

**Problema:** Spring Boot no está leyendo las variables del archivo `.env`

**Solución:**
- Spring Boot no lee `.env` por defecto
- Usa variables de entorno del sistema operativo
- O usa un plugin como `spring-dotenv` (agregar dependencia)

**Alternativa:** Configura las variables directamente en `application.properties`:
```properties
spring.datasource.password=${DB_PASSWORD:password_por_defecto}
```

### El frontend no encuentra las variables

**Problema:** `import.meta.env.VITE_*` es `undefined`

**Soluciones:**
1. Verifica que las variables comienzan con `VITE_`
2. Reinicia el servidor de desarrollo (`npm run dev`)
3. Verifica que el archivo se llama `.env.local` o `.env`
4. Limpia la caché: `rm -rf node_modules/.vite`

### Error de conexión a la base de datos

**Problema:** No puede conectar a PostgreSQL

**Verifica:**
- PostgreSQL está corriendo
- El puerto es correcto (por defecto 5432, pero puede ser 5433)
- El usuario y contraseña son correctos
- La base de datos existe
- El firewall permite la conexión

---

## 📚 Recursos Adicionales

- [Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [Vite Environment Variables](https://vitejs.dev/guide/env-and-mode.html)
- [12 Factor App - Config](https://12factor.net/config)

---

**Última actualización:** Noviembre 2025  
**Versión:** 1.0.0

