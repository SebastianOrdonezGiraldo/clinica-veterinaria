# 🚀 Guía de Despliegue Gratuito - Clínica Veterinaria

Guía completa para desplegar la aplicación de forma gratuita en diferentes plataformas.

## 📋 Tabla de Contenidos

- [Opciones de Despliegue Gratuito](#opciones-de-despliegue-gratuito)
- [Railway (Recomendado)](#railway-recomendado)
- [Render](#render)
- [Fly.io](#flyio)
- [Configuración de Variables](#configuración-de-variables)
- [Troubleshooting](#troubleshooting)

## 🎯 Opciones de Despliegue Gratuito

| Plataforma | Tier Gratuito | Base de Datos | Recomendado Para |
|------------|---------------|---------------|------------------|
| **Railway** | $5 crédito/mes | ✅ PostgreSQL incluido | Principiantes |
| **Render** | 750 horas/mes | ✅ PostgreSQL incluido | Principiantes |
| **Fly.io** | 3 VMs compartidas | ⚠️ Requiere DB externa | Avanzados |

## 🚂 Railway (Recomendado)

### Ventajas
- ✅ Muy fácil de usar
- ✅ PostgreSQL incluido gratis
- ✅ Despliegue automático desde GitHub
- ✅ $5 crédito mensual gratuito
- ✅ HTTPS automático
- ✅ Dominio personalizado gratis

### Pasos de Despliegue

#### 1. Preparar el Repositorio

```bash
# Asegúrate de que tu código esté en GitHub
git add .
git commit -m "Preparado para despliegue"
git push origin main
```

#### 2. Crear Cuenta en Railway

1. Ve a [railway.app](https://railway.app)
2. Inicia sesión con GitHub
3. Haz clic en "New Project"
4. Selecciona "Deploy from GitHub repo"
5. Selecciona tu repositorio

#### 3. Desplegar Base de Datos

1. En el proyecto, haz clic en "+ New"
2. Selecciona "Database" → "Add PostgreSQL"
3. Railway creará automáticamente la base de datos
4. Copia las variables de conexión (aparecerán automáticamente)

#### 4. Desplegar Backend

1. Haz clic en "+ New" → "GitHub Repo"
2. Selecciona tu repositorio
3. Railway detectará automáticamente el Dockerfile
4. Configura las variables de entorno (ver sección de variables)
5. Configura el servicio:
   - **Root Directory**: `apps/backend`
   - **Build Command**: (dejar vacío, usa Dockerfile)
   - **Start Command**: (dejar vacío, usa Dockerfile)

#### 5. Desplegar Frontend

1. Haz clic en "+ New" → "GitHub Repo"
2. Selecciona tu repositorio
3. Configura el servicio:
   - **Root Directory**: `apps/frontend`
   - **Build Command**: `npm ci && npm run build`
   - **Start Command**: `npx serve -s dist -l 3000`
   - **Nixpacks Plan**: Usa Node.js

#### 6. Configurar Variables de Entorno

Para el **Backend**, agrega estas variables:

```env
SPRING_PROFILES_ACTIVE=prod
DB_URL=${{Postgres.DATABASE_URL}}
DB_USERNAME=${{Postgres.PGUSER}}
DB_PASSWORD=${{Postgres.PGPASSWORD}}
JWT_SECRET=tu_secreto_jwt_muy_largo_y_seguro_min_32_caracteres
JWT_EXPIRATION=86400000
CORS_ALLOWED_ORIGINS=https://tu-app.railway.app
SWAGGER_ENABLED=false
```

Para el **Frontend**, agrega:

```env
VITE_API_URL=https://tu-backend.railway.app/api
```

#### 7. Configurar Dominios

1. En cada servicio, ve a "Settings" → "Domains"
2. Railway proporciona un dominio automático
3. Opcional: Agrega tu dominio personalizado

### Costos Estimados

- **Base de datos**: Gratis (incluido en tier gratuito)
- **Backend**: ~$2-3/mes (dentro del crédito gratuito)
- **Frontend**: ~$1-2/mes (dentro del crédito gratuito)
- **Total**: Gratis con $5 crédito mensual

---

## 🎨 Render

### Ventajas
- ✅ 750 horas gratuitas/mes
- ✅ PostgreSQL incluido
- ✅ Despliegue automático
- ✅ HTTPS automático

### Pasos de Despliegue

#### 1. Crear Cuenta

1. Ve a [render.com](https://render.com)
2. Inicia sesión con GitHub

#### 2. Desplegar Base de Datos

1. Dashboard → "New +" → "PostgreSQL"
2. Configura:
   - **Name**: `clinica-veterinaria-db`
   - **Database**: `vetclinic`
   - **User**: `veterinaria`
   - **Region**: Elige el más cercano
   - **Plan**: Free
3. Copia las credenciales de conexión

#### 3. Desplegar Backend

1. Dashboard → "New +" → "Web Service"
2. Conecta tu repositorio de GitHub
3. Configura:
   - **Name**: `clinica-veterinaria-backend`
   - **Environment**: `Docker`
   - **Root Directory**: `apps/backend`
   - **Dockerfile Path**: `apps/backend/Dockerfile`
   - **Plan**: Free
4. Agrega variables de entorno (ver sección de variables)

#### 4. Desplegar Frontend

1. Dashboard → "New +" → "Static Site"
2. Conecta tu repositorio
3. Configura:
   - **Name**: `clinica-veterinaria-frontend`
   - **Build Command**: `cd apps/frontend && npm ci && npm run build`
   - **Publish Directory**: `apps/frontend/dist`
   - **Environment Variables**: `VITE_API_URL=https://tu-backend.onrender.com/api`

### Variables de Entorno (Backend)

```env
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:postgresql://tu-db-host:5432/vetclinic
DB_USERNAME=veterinaria
DB_PASSWORD=tu_password_de_render
JWT_SECRET=tu_secreto_jwt_muy_largo_y_seguro
JWT_EXPIRATION=86400000
CORS_ALLOWED_ORIGINS=https://tu-frontend.onrender.com
SWAGGER_ENABLED=false
```

### Limitaciones del Plan Gratuito

- ⚠️ Los servicios se "duermen" después de 15 minutos de inactividad
- ⚠️ El primer request después de dormir puede tardar ~30 segundos
- ⚠️ 750 horas/mes (suficiente para 1 servicio 24/7)

---

## 🪂 Fly.io

### Ventajas
- ✅ 3 VMs compartidas gratis
- ✅ Muy rápido
- ✅ Buena para Docker
- ⚠️ Requiere configuración más técnica

### Pasos de Despliegue

#### 1. Instalar Fly CLI

```bash
# Windows (PowerShell)
iwr https://fly.io/install.ps1 -useb | iex

# Mac/Linux
curl -L https://fly.io/install.sh | sh
```

#### 2. Iniciar Sesión

```bash
fly auth login
```

#### 3. Desplegar Backend

```bash
cd apps/backend
fly launch
# Sigue las instrucciones interactivas
```

#### 4. Configurar Base de Datos

Fly.io no incluye PostgreSQL gratis, usa:
- Railway PostgreSQL (gratis)
- Render PostgreSQL (gratis)
- Supabase (gratis)

#### 5. Configurar Variables

```bash
fly secrets set \
  SPRING_PROFILES_ACTIVE=prod \
  DB_URL=jdbc:postgresql://tu-db-host:5432/vetclinic \
  DB_USERNAME=usuario \
  DB_PASSWORD=password \
  JWT_SECRET=tu_secreto
```

---

## 🔐 Configuración de Variables

### Variables Requeridas para Backend

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Perfil de Spring | `prod` |
| `DB_URL` | URL de conexión a PostgreSQL | `jdbc:postgresql://host:5432/vetclinic` |
| `DB_USERNAME` | Usuario de la BD | `postgres` |
| `DB_PASSWORD` | Contraseña de la BD | `password_seguro` |
| `JWT_SECRET` | Secreto para JWT (min 32 chars) | Generar con `openssl rand -base64 32` |
| `JWT_EXPIRATION` | Expiración del token (ms) | `86400000` (24h) |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos | `https://tu-frontend.com` |
| `SWAGGER_ENABLED` | Habilitar Swagger | `false` (producción) |

### Variables Requeridas para Frontend

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `VITE_API_URL` | URL del backend | `https://tu-backend.railway.app/api` |

### Generar JWT Secret Seguro

```bash
# Linux/Mac
openssl rand -base64 32

# Windows (PowerShell)
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
```

---

## 🔧 Troubleshooting

### Backend no se conecta a la base de datos

1. Verifica que las variables de entorno estén correctas
2. Asegúrate de que la BD esté corriendo
3. Verifica que el firewall permita conexiones desde el backend

### Frontend no se conecta al backend

1. Verifica `VITE_API_URL` en el frontend
2. Asegúrate de que CORS esté configurado correctamente
3. Verifica que el backend esté accesible públicamente

### Error de CORS

Agrega el dominio del frontend a `CORS_ALLOWED_ORIGINS`:

```env
CORS_ALLOWED_ORIGINS=https://tu-frontend.railway.app,https://tu-frontend.onrender.com
```

### Servicios se duermen (Render)

- Usa un servicio de "ping" como [UptimeRobot](https://uptimerobot.com) (gratis)
- O considera Railway que no tiene este problema

---

## 📊 Comparación Rápida

| Característica | Railway | Render | Fly.io |
|----------------|---------|--------|--------|
| Facilidad | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ |
| PostgreSQL gratis | ✅ | ✅ | ❌ |
| Sin "sleep" | ✅ | ❌ | ✅ |
| HTTPS automático | ✅ | ✅ | ✅ |
| Dominio gratis | ✅ | ✅ | ✅ |
| Despliegue automático | ✅ | ✅ | ⚠️ Manual |

---

## 🎯 Recomendación

**Para principiantes**: Usa **Railway**
- Más fácil de configurar
- PostgreSQL incluido
- Sin problemas de "sleep"
- $5 crédito mensual suficiente

**Para proyectos pequeños**: Usa **Render**
- 750 horas/mes gratis
- PostgreSQL incluido
- Bueno si no te importa el "sleep"

**Para usuarios avanzados**: Usa **Fly.io**
- Más control
- Muy rápido
- Requiere más configuración

---

## 🛠️ Scripts de Ayuda

### Generar Variables de Entorno

Usa los scripts incluidos para generar las variables de entorno:

```bash
# Linux/Mac
./scripts/deployment/generate-env.sh railway

# Windows (PowerShell)
.\scripts\deployment\generate-env.ps1 railway
```

Esto generará un JWT_SECRET seguro y las variables necesarias para tu plataforma.

## ⚠️ Notas Importantes

### Seguridad

1. **NUNCA** commitees variables de entorno reales
2. **SIEMPRE** usa secretos seguros en producción
3. **GENERA** un nuevo JWT_SECRET para cada entorno
4. **DESHABILITA** Swagger en producción

### Límites del Plan Gratuito

- **Railway**: $5 crédito/mes (suficiente para proyectos pequeños)
- **Render**: 750 horas/mes (servicios se duermen después de 15 min)
- **Fly.io**: 3 VMs compartidas (requiere DB externa)

### Migración de Datos

Si necesitas migrar datos de desarrollo a producción:

1. Exporta la BD local:
   ```bash
   pg_dump -U postgres vetclinic > backup.sql
   ```

2. Importa en producción (según plataforma):
   - Railway: Usa el CLI o dashboard
   - Render: Usa `psql` con las credenciales
   - Fly.io: Usa `psql` con las credenciales

## 📚 Recursos Adicionales

- [Documentación Railway](https://docs.railway.app)
- [Documentación Render](https://render.com/docs)
- [Documentación Fly.io](https://fly.io/docs)
- [Guía de Docker](./DOCKER.md)

