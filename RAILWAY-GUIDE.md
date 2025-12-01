# 🚂 Guía Paso a Paso - Despliegue en Railway

Guía detallada para desplegar Clínica Veterinaria en Railway.

## 📋 Prerrequisitos

- ✅ Cuenta de GitHub con el código subido
- ✅ Cuenta en Railway (gratis)
- ✅ ~15 minutos de tiempo

## 🚀 Paso 1: Preparar el Repositorio

### 1.1 Verificar que todo esté commiteado

```bash
# Verificar estado
git status

# Si hay cambios sin commitear
git add .
git commit -m "Preparado para despliegue en Railway"
git push origin main
```

### 1.2 Verificar estructura del proyecto

Asegúrate de que tengas:
- ✅ `apps/backend/Dockerfile`
- ✅ `apps/frontend/Dockerfile`
- ✅ `apps/backend/railway.json` (opcional, pero recomendado)

## 🚂 Paso 2: Crear Proyecto en Railway

### 2.1 Iniciar Sesión

1. Ve a [railway.app](https://railway.app)
2. Haz clic en "Start a New Project"
3. Inicia sesión con GitHub

### 2.2 Crear Proyecto

1. Selecciona "New Project"
2. Elige "Deploy from GitHub repo"
3. Autoriza Railway a acceder a tus repositorios
4. Selecciona tu repositorio `clinica-veterinaria`

## 🗄️ Paso 3: Desplegar Base de Datos PostgreSQL

### 3.1 Crear Servicio de Base de Datos

1. En el proyecto, haz clic en **"+ New"**
2. Selecciona **"Database"** → **"Add PostgreSQL"**
3. Railway creará automáticamente la base de datos
4. Espera a que el estado sea "Active" (verde)

### 3.2 Obtener Variables de Conexión

1. Haz clic en el servicio de PostgreSQL
2. Ve a la pestaña **"Variables"**
3. Verás las siguientes variables (las usaremos después):
   - `DATABASE_URL`
   - `PGHOST`
   - `PGPORT`
   - `PGUSER`
   - `PGPASSWORD`
   - `PGDATABASE`

**No necesitas copiarlas manualmente**, Railway las inyectará automáticamente.

## 🔧 Paso 4: Desplegar Backend

### 4.1 Crear Servicio Backend

1. Haz clic en **"+ New"** → **"GitHub Repo"**
2. Selecciona tu repositorio `clinica-veterinaria`
3. Railway detectará automáticamente el Dockerfile

### 4.2 Configurar Servicio Backend

1. Haz clic en el servicio recién creado
2. Ve a **"Settings"**
3. Configura:
   - **Root Directory**: `apps/backend`
   - **Build Command**: (dejar vacío, usa Dockerfile)
   - **Start Command**: (dejar vacío, usa Dockerfile)

### 4.3 Configurar Variables de Entorno

1. Ve a la pestaña **"Variables"**
2. Haz clic en **"+ New Variable"**
3. Agrega las siguientes variables:

```env
# Perfil de Spring
SPRING_PROFILES_ACTIVE=prod

# Base de datos (IMPORTANTE: NO usar DATABASE_URL automático de Railway)
# ⚠️ Railway genera DATABASE_URL automáticamente pero puede usar endpoint público (genera costos)
# ⚠️ NO uses: ${{Postgres.DATABASE_URL}} o ${{Postgres.DATABASE_PUBLIC_URL}}
# ✅ Construye DB_URL manualmente usando RAILWAY_PRIVATE_DOMAIN (gratis, conexión interna)
# Railway proporciona DATABASE_URL como postgresql:// pero Spring necesita jdbc:postgresql://
# Opción 1: Usar RAILWAY_PRIVATE_DOMAIN directamente (RECOMENDADO)
# Nota: Reemplaza 'Postgres' con el nombre exacto de tu servicio PostgreSQL
DB_URL=jdbc:postgresql://${{Postgres.RAILWAY_PRIVATE_DOMAIN}}:5432/railway
DB_USERNAME=${{Postgres.POSTGRES_USER}}
DB_PASSWORD=${{Postgres.POSTGRES_PASSWORD}}

# Nota: Si prefieres usar vetclinic_dev, primero créala en PostgreSQL:
# CREATE DATABASE vetclinic_dev;
# Luego cambia 'railway' por 'vetclinic_dev' en DB_URL

# Opción 2: Usar PGHOST (que apunta a RAILWAY_PRIVATE_DOMAIN)
# DB_URL=jdbc:postgresql://${{Postgres.PGHOST}}:5432/vetclinic_dev
# DB_USERNAME=${{Postgres.PGUSER}}
# DB_PASSWORD=${{Postgres.PGPASSWORD}}

# Opción 2: Si Railway ya proporciona DATABASE_URL, convertirla
# DB_URL=jdbc:${{Postgres.DATABASE_URL}}
# (Reemplaza postgresql:// por jdbc:postgresql://)

# JWT (genera uno seguro)
JWT_SECRET=GENERA_UN_SECRETO_SEGURO_AQUI_MIN_32_CARACTERES
JWT_EXPIRATION=86400000

# CORS (ajusta después de desplegar el frontend)
CORS_ALLOWED_ORIGINS=https://tu-frontend.railway.app

# Swagger (deshabilitado en producción)
SWAGGER_ENABLED=false
```

**⚠️ IMPORTANTE**: 
- Reemplaza `GENERA_UN_SECRETO_SEGURO_AQUI_MIN_32_CARACTERES` con un secreto real
- Para generar un secreto seguro, ejecuta:
  ```bash
  # Windows PowerShell
  [Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
  
  # Linux/Mac
  openssl rand -base64 32
  ```

### 4.4 Conectar con PostgreSQL

1. En el servicio Backend, ve a **"Settings"**
2. En **"Connect to Database"**, selecciona tu servicio PostgreSQL
3. Railway conectará automáticamente los servicios

### 4.5 Generar Dominio

1. Ve a la pestaña **"Settings"**
2. En **"Domains"**, haz clic en **"Generate Domain"**
3. Copia el dominio generado (ej: `clinica-veterinaria-backend.railway.app`)
4. **Guarda este dominio**, lo necesitarás para el frontend

## 🎨 Paso 5: Desplegar Frontend

### 5.1 Crear Servicio Frontend

1. Haz clic en **"+ New"** → **"GitHub Repo"**
2. Selecciona tu repositorio `clinica-veterinaria`

### 5.2 Configurar Servicio Frontend

1. Haz clic en el servicio recién creado
2. Ve a **"Settings"**
3. Configura:
   - **Root Directory**: `apps/frontend`
   - **Build Command**: `npm ci && npm run build`
   - **Start Command**: `npx serve -s dist -l 3000`
   - **Nixpacks Plan**: Selecciona "Node.js" si se muestra

**Alternativa con Docker**:
Si prefieres usar Docker (recomendado):
- **Root Directory**: `apps/frontend`
- **Build Command**: (dejar vacío)
- **Start Command**: (dejar vacío)
- Railway usará el Dockerfile automáticamente

### 5.3 Configurar Variables de Entorno

1. Ve a la pestaña **"Variables"**
2. Agrega:

```env
VITE_API_URL=https://TU-BACKEND-DOMAIN.railway.app/api
```

**⚠️ IMPORTANTE**: Reemplaza `TU-BACKEND-DOMAIN` con el dominio que generaste en el paso 4.5

### 5.4 Generar Dominio Frontend

1. Ve a **"Settings"** → **"Domains"**
2. Haz clic en **"Generate Domain"**
3. Copia el dominio (ej: `clinica-veterinaria-frontend.railway.app`)

### 5.5 Actualizar CORS en Backend

1. Vuelve al servicio Backend
2. Ve a **"Variables"**
3. Actualiza `CORS_ALLOWED_ORIGINS` con el dominio del frontend:

```env
CORS_ALLOWED_ORIGINS=https://clinica-veterinaria-frontend.railway.app
```

4. Railway reiniciará automáticamente el backend

## ✅ Paso 6: Verificar Despliegue

### 6.1 Verificar Backend

1. Abre el dominio del backend en tu navegador
2. Deberías ver una respuesta (puede ser un error 404, eso es normal)
3. Prueba el health check: `https://tu-backend.railway.app/actuator/health`
4. Deberías ver: `{"status":"UP"}`

### 6.2 Verificar Frontend

1. Abre el dominio del frontend
2. Deberías ver la aplicación funcionando
3. Intenta iniciar sesión o navegar

### 6.3 Verificar Logs

1. En cada servicio, ve a la pestaña **"Deployments"**
2. Haz clic en el deployment más reciente
3. Revisa los logs para ver si hay errores

## 🔍 Troubleshooting

### Backend no inicia

**Problema**: El backend falla al iniciar

**Solución**:
1. Revisa los logs en Railway
2. Verifica que todas las variables de entorno estén configuradas
3. Asegúrate de que `JWT_SECRET` tenga al menos 32 caracteres
4. **IMPORTANTE**: Verifica que `DB_URL` tenga el formato correcto:
   ```env
   DB_URL=jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}
   ```
   O si usas `DATABASE_URL`, conviértela:
   ```env
   DB_URL=jdbc:${{Postgres.DATABASE_URL}}
   ```
   (Esto reemplazará `postgresql://` por `jdbc:postgresql://`)

**Error común**: "Driver claims to not accept jdbcUrl"
- **Causa**: La URL no tiene el prefijo `jdbc:`
- **Solución**: Asegúrate de que `DB_URL` comience con `jdbc:postgresql://`

### Frontend no se conecta al backend

**Problema**: El frontend muestra errores de conexión

**Solución**:
1. Verifica que `VITE_API_URL` en el frontend sea correcto
2. Asegúrate de que el backend esté corriendo (verifica los logs)
3. Verifica que `CORS_ALLOWED_ORIGINS` incluya el dominio del frontend

### Error de CORS

**Problema**: Errores de CORS en la consola del navegador

**Solución**:
1. En el backend, actualiza `CORS_ALLOWED_ORIGINS` con el dominio exacto del frontend
2. Incluye el protocolo: `https://tu-frontend.railway.app`
3. Si tienes múltiples orígenes, sepáralos con comas

### Base de datos no conecta

**Problema**: El backend no puede conectarse a PostgreSQL

**Solución**:
1. Verifica que el servicio PostgreSQL esté "Active"
2. En el backend, asegúrate de usar las referencias de Railway con **RAILWAY_PRIVATE_DOMAIN**:
   ```env
   DB_URL=jdbc:postgresql://${{Postgres.RAILWAY_PRIVATE_DOMAIN}}:5432/vetclinic_dev
   DB_USERNAME=${{Postgres.POSTGRES_USER}}
   DB_PASSWORD=${{Postgres.POSTGRES_PASSWORD}}
   ```
3. **⚠️ IMPORTANTE**: NO uses `DATABASE_PUBLIC_URL` o `RAILWAY_TCP_PROXY_DOMAIN` (generan costos)
4. Verifica que los servicios estén conectados en "Settings"

### Error: "database does not exist"

**Problema**: `FATAL: database "vetclinic_dev" does not exist`

**Causa**: Railway creó la BD con nombre por defecto `railway`, pero estás intentando conectar a `vetclinic_dev`

**Solución 1 - Usar la BD existente (Rápido)**:
```env
# En Backend → Variables, cambia el nombre de la BD:
DB_URL=jdbc:postgresql://${{Postgres.RAILWAY_PRIVATE_DOMAIN}}:5432/railway
```

**Solución 2 - Crear la BD vetclinic_dev (Recomendado)**:

1. **Opción A: Usar Railway CLI**
   ```bash
   # Instalar Railway CLI
   npm i -g @railway/cli
   
   # Conectar a PostgreSQL
   railway connect postgres
   
   # Crear la BD
   psql -U postgres -c "CREATE DATABASE vetclinic_dev;"
   ```

2. **Opción B: Usar Query Tab en Railway**
   - Ve a tu servicio PostgreSQL
   - Haz clic en "Query" o "Connect"
   - Ejecuta: `CREATE DATABASE vetclinic_dev;`

3. **Opción C: Cambiar POSTGRES_DB antes de crear**
   - Ve a PostgreSQL → Settings → Variables
   - Cambia `POSTGRES_DB` de `railway` a `vetclinic_dev`
   - **⚠️ Esto recreará la BD** (pierdes datos si hay)
   - Railway recreará el servicio con el nuevo nombre

**Solución 3 - Usar script de inicialización**:
Crea un archivo SQL y configúralo en Railway para que se ejecute automáticamente.

### Advertencia de Egress Fees

**Problema**: Railway muestra advertencia sobre `DATABASE_PUBLIC_URL` o `RAILWAY_TCP_PROXY_DOMAIN`

**Solución**:
- **NO uses** `DATABASE_URL` automático de Railway (puede usar endpoint público)
- **NO uses** `DATABASE_PUBLIC_URL` (usa endpoint público, genera costos)
- **NO uses** `RAILWAY_TCP_PROXY_DOMAIN` (genera costos)
- **USA** `RAILWAY_PRIVATE_DOMAIN` o `PGHOST` (endpoint privado, gratis)
- Configuración correcta:
  ```env
  DB_URL=jdbc:postgresql://${{Postgres.RAILWAY_PRIVATE_DOMAIN}}:5432/vetclinic_dev
  DB_USERNAME=${{Postgres.POSTGRES_USER}}
  DB_PASSWORD=${{Postgres.POSTGRES_PASSWORD}}
  ```
- O usando PGHOST (que ya apunta a RAILWAY_PRIVATE_DOMAIN):
  ```env
  DB_URL=jdbc:postgresql://${{Postgres.PGHOST}}:5432/vetclinic_dev
  DB_USERNAME=${{Postgres.PGUSER}}
  DB_PASSWORD=${{Postgres.PGPASSWORD}}
  ```

## 📊 Monitoreo

### Ver Uso de Recursos

1. En cada servicio, ve a **"Metrics"**
2. Puedes ver:
   - Uso de CPU
   - Uso de memoria
   - Tráfico de red
   - Logs en tiempo real

### Ver Logs en Tiempo Real

1. En cada servicio, ve a **"Deployments"**
2. Haz clic en el deployment activo
3. Verás los logs en tiempo real

## 💰 Costos

### Plan Gratuito

Railway ofrece **$5 crédito mensual gratis**, que es suficiente para:
- ✅ 1 base de datos PostgreSQL pequeña
- ✅ 1 servicio backend (Spring Boot)
- ✅ 1 servicio frontend (React)

### Estimación de Costos

- **PostgreSQL**: ~$0.50-1/mes
- **Backend**: ~$2-3/mes
- **Frontend**: ~$1-2/mes
- **Total**: ~$3.50-6/mes (dentro del crédito gratuito)

### Monitorear Uso

1. Ve a tu perfil en Railway
2. Haz clic en **"Usage"**
3. Verás el uso actual y proyecciones

## 🔄 Actualizaciones

### Desplegar Cambios

Railway despliega automáticamente cuando haces push a GitHub:

```bash
git add .
git commit -m "Nuevas funcionalidades"
git push origin main
```

Railway detectará los cambios y desplegará automáticamente.

### Rollback

Si algo sale mal:

1. Ve a **"Deployments"** en el servicio
2. Encuentra el deployment anterior que funcionaba
3. Haz clic en los tres puntos → **"Redeploy"**

## 🌐 Dominio Personalizado

### Agregar Tu Dominio

1. En el servicio, ve a **"Settings"** → **"Domains"**
2. Haz clic en **"Custom Domain"**
3. Ingresa tu dominio
4. Sigue las instrucciones para configurar DNS

### Configurar DNS

Railway te dará un registro CNAME:
- **Tipo**: CNAME
- **Nombre**: `@` o `www`
- **Valor**: `tu-servicio.railway.app`

## 📚 Recursos Adicionales

- [Documentación Railway](https://docs.railway.app)
- [Railway Discord](https://discord.gg/railway)
- [Ejemplos Railway](https://github.com/railwayapp/starters)

## ✅ Checklist Final

Antes de considerar el despliegue completo:

- [ ] Backend está corriendo y responde
- [ ] Frontend está corriendo y se carga
- [ ] Frontend se conecta al backend (sin errores CORS)
- [ ] Puedes iniciar sesión en la aplicación
- [ ] La base de datos está funcionando
- [ ] Los logs no muestran errores críticos
- [ ] Los dominios están configurados
- [ ] Las variables de entorno están correctas

¡Felicitaciones! 🎉 Tu aplicación está desplegada en Railway.


Guía detallada para desplegar Clínica Veterinaria en Railway.

## 📋 Prerrequisitos

- ✅ Cuenta de GitHub con el código subido
- ✅ Cuenta en Railway (gratis)
- ✅ 10-15 minutos

## 🚀 Paso 1: Preparar el Repositorio

### 1.1 Verificar que todo esté commiteado

```bash
git status
```

### 1.2 Hacer commit de cambios pendientes

```bash
git add .
git commit -m "Preparado para despliegue en Railway"
git push origin main
```

## 🚂 Paso 2: Crear Proyecto en Railway

### 2.1 Iniciar Sesión

1. Ve a [railway.app](https://railway.app)
2. Haz clic en "Start a New Project"
3. Inicia sesión con tu cuenta de GitHub

### 2.2 Crear Nuevo Proyecto

1. Haz clic en **"New Project"**
2. Selecciona **"Deploy from GitHub repo"**
3. Autoriza Railway a acceder a tus repositorios (si es necesario)
4. Selecciona tu repositorio `clinica-veterinaria`

## 🗄️ Paso 3: Desplegar Base de Datos PostgreSQL

### 3.1 Agregar PostgreSQL

1. En el proyecto de Railway, haz clic en **"+ New"**
2. Selecciona **"Database"** → **"Add PostgreSQL"**
3. Railway creará automáticamente la base de datos
4. **IMPORTANTE**: Anota el nombre del servicio (ej: `Postgres`)

### 3.2 Verificar Variables de la BD

Las variables se generan automáticamente:
- `DATABASE_URL`
- `PGHOST`
- `PGPORT`
- `PGUSER`
- `PGPASSWORD`
- `PGDATABASE`

## 🔧 Paso 4: Desplegar Backend

### 4.1 Agregar Servicio Backend

1. Haz clic en **"+ New"** → **"GitHub Repo"**
2. Selecciona tu repositorio `clinica-veterinaria`
3. Railway detectará automáticamente el Dockerfile

### 4.2 Configurar el Servicio

1. Haz clic en el servicio recién creado
2. Ve a **"Settings"** → **"Source"**
3. Configura:
   - **Root Directory**: `apps/backend`
   - **Dockerfile Path**: `apps/backend/Dockerfile` (o deja en blanco si está en la raíz del root directory)

### 4.3 Configurar Variables de Entorno

Ve a **"Variables"** y agrega:

```env
SPRING_PROFILES_ACTIVE=prod
```

Luego, agrega las variables de la base de datos usando referencias:

```env
DB_URL=${{Postgres.DATABASE_URL}}
DB_USERNAME=${{Postgres.PGUSER}}
DB_PASSWORD=${{Postgres.PGPASSWORD}}
```

**Nota**: Reemplaza `Postgres` con el nombre exacto de tu servicio de PostgreSQL.

### 4.4 Agregar Variables Adicionales

```env
JWT_SECRET=tu_secreto_jwt_muy_largo_y_seguro_minimo_32_caracteres
JWT_EXPIRATION=86400000
CORS_ALLOWED_ORIGINS=https://tu-frontend.railway.app
SWAGGER_ENABLED=false
```

**Generar JWT_SECRET seguro:**

```bash
# Windows (PowerShell)
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))

# Linux/Mac
openssl rand -base64 32
```

### 4.5 Configurar Puerto

1. Ve a **"Settings"** → **"Networking"**
2. Railway asignará automáticamente un puerto
3. El backend usará el puerto interno 8080

### 4.6 Verificar Despliegue

1. Ve a **"Deployments"** para ver el progreso
2. Espera a que el build termine (puede tardar 5-10 minutos la primera vez)
3. Verifica los logs en **"Deployments"** → **"View Logs"**

## 🎨 Paso 5: Desplegar Frontend

### 5.1 Agregar Servicio Frontend

1. Haz clic en **"+ New"** → **"GitHub Repo"**
2. Selecciona tu repositorio `clinica-veterinaria`

### 5.2 Configurar el Servicio

1. Haz clic en el servicio recién creado
2. Ve a **"Settings"** → **"Source"**
3. Configura:
   - **Root Directory**: `apps/frontend`
   - **Build Command**: `npm ci && npm run build`
   - **Start Command**: `npx serve -s dist -l 3000`

**O usa Docker:**

1. Ve a **"Settings"** → **"Source"**
2. Cambia a **"Docker"**
3. Configura:
   - **Root Directory**: `apps/frontend`
   - **Dockerfile Path**: `apps/frontend/Dockerfile`

### 5.3 Configurar Variables de Entorno

Ve a **"Variables"** y agrega:

```env
VITE_API_URL=https://tu-backend.railway.app/api
```

**Nota**: Reemplaza `tu-backend` con el nombre real de tu servicio backend. Lo encontrarás en **"Settings"** → **"Networking"** → **"Public Domain"**.

### 5.4 Verificar Despliegue

1. Espera a que el build termine
2. Verifica los logs
3. El frontend debería estar disponible en el dominio proporcionado por Railway

## 🌐 Paso 6: Configurar Dominios

### 6.1 Dominio Automático

Railway proporciona un dominio automático para cada servicio:
- Backend: `tu-backend.railway.app`
- Frontend: `tu-frontend.railway.app`

### 6.2 Dominio Personalizado (Opcional)

1. Ve a **"Settings"** → **"Domains"**
2. Haz clic en **"Custom Domain"**
3. Agrega tu dominio
4. Configura los registros DNS según las instrucciones

## ✅ Paso 7: Verificar que Todo Funcione

### 7.1 Verificar Backend

1. Abre `https://tu-backend.railway.app/actuator/health`
2. Deberías ver: `{"status":"UP"}`

### 7.2 Verificar Frontend

1. Abre `https://tu-frontend.railway.app`
2. Deberías ver la aplicación funcionando

### 7.3 Verificar Conexión Frontend-Backend

1. Abre la consola del navegador (F12)
2. Intenta iniciar sesión
3. No debería haber errores de CORS

## 🔧 Troubleshooting

### Backend no inicia

1. **Verifica los logs**: Ve a **"Deployments"** → **"View Logs"**
2. **Verifica variables de entorno**: Asegúrate de que todas estén configuradas
3. **Verifica conexión a BD**: Revisa que `DB_URL` sea correcta

### Frontend no se conecta al backend

1. **Verifica `VITE_API_URL`**: Debe apuntar al dominio correcto del backend
2. **Verifica CORS**: Asegúrate de que `CORS_ALLOWED_ORIGINS` incluya el dominio del frontend
3. **Verifica que el backend esté corriendo**: Revisa los logs del backend

### Error de CORS

Agrega el dominio del frontend a `CORS_ALLOWED_ORIGINS`:

```env
CORS_ALLOWED_ORIGINS=https://tu-frontend.railway.app
```

### Base de datos no se conecta

1. Verifica que el servicio PostgreSQL esté corriendo
2. Verifica que las variables de referencia sean correctas: `${{Postgres.DATABASE_URL}}`
3. Asegúrate de que el nombre del servicio coincida exactamente

## 📊 Monitoreo

### Ver Logs en Tiempo Real

1. Ve a cualquier servicio
2. Haz clic en **"View Logs"**
3. Los logs se actualizan en tiempo real

### Ver Métricas

1. Ve a **"Metrics"** en cualquier servicio
2. Verás CPU, memoria, y tráfico de red

## 💰 Costos

### Plan Gratuito

- **$5 crédito mensual** (suficiente para proyectos pequeños)
- **PostgreSQL**: Incluido gratis
- **Backend**: ~$2-3/mes
- **Frontend**: ~$1-2/mes

### Monitorear Uso

1. Ve a **"Settings"** → **"Usage"**
2. Verás el consumo actual del mes

## 🎯 Checklist Final

- [ ] Repositorio en GitHub
- [ ] Proyecto creado en Railway
- [ ] PostgreSQL desplegado y corriendo
- [ ] Backend desplegado con todas las variables
- [ ] Frontend desplegado con `VITE_API_URL` configurado
- [ ] Backend accesible en `/actuator/health`
- [ ] Frontend accesible y funcionando
- [ ] Sin errores de CORS
- [ ] Aplicación funcionando completamente

## 📚 Recursos

- [Documentación Railway](https://docs.railway.app)
- [Railway Discord](https://discord.gg/railway)
- [Guía General de Despliegue](./DEPLOYMENT.md)

## 🆘 ¿Necesitas Ayuda?

Si encuentras problemas:

1. Revisa los logs en Railway
2. Verifica todas las variables de entorno
3. Consulta la sección de Troubleshooting
4. Revisa la [documentación oficial de Railway](https://docs.railway.app)

¡Buena suerte con tu despliegue! 🚀

