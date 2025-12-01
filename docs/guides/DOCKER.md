# 🐳 Dockerización - Clínica Veterinaria

Guía completa para ejecutar la aplicación usando Docker y Docker Compose.

## 📋 Requisitos Previos

- Docker Desktop (o Docker Engine + Docker Compose)
- Git
- **IMPORTANTE**: Docker Desktop debe estar corriendo antes de ejecutar los comandos

### Verificar Docker

```bash
# Verificar que Docker esté corriendo
docker info

# Si no está corriendo, inicia Docker Desktop y espera a que esté listo
```

## 🚀 Inicio Rápido

### 1. Configurar Variables de Entorno

```bash
# Copiar archivo de ejemplo
cp .env.example .env

# Editar .env con tus valores
# (Opcional, los valores por defecto funcionan para desarrollo)
```

### 2. Iniciar Todos los Servicios

```bash
# Construir y levantar todos los servicios
docker-compose up -d

# Ver logs
docker-compose logs -f

# Ver logs de un servicio específico
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f postgres
```

### 3. Acceder a la Aplicación

- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **PostgreSQL**: localhost:5432

## 🛠️ Comandos Útiles

### Gestión de Servicios

```bash
# Iniciar servicios
docker-compose up -d

# Detener servicios
docker-compose down

# Detener y eliminar volúmenes (⚠️ elimina datos)
docker-compose down -v

# Reiniciar un servicio específico
docker-compose restart backend

# Ver estado de servicios
docker-compose ps

# Ver logs en tiempo real
docker-compose logs -f

# Reconstruir imágenes
docker-compose build --no-cache

# Reconstruir y levantar
docker-compose up -d --build
```

### Base de Datos

```bash
# Acceder a PostgreSQL
docker-compose exec postgres psql -U postgres -d vetclinic

# Hacer backup
docker-compose exec postgres pg_dump -U postgres vetclinic > backup.sql

# Restaurar backup
docker-compose exec -T postgres psql -U postgres vetclinic < backup.sql
```

### Desarrollo

```bash
# Solo levantar la base de datos (para desarrollo local)
docker-compose -f docker-compose.dev.yml up -d postgres

# Ver logs del backend
docker-compose logs -f backend

# Ejecutar comandos dentro del contenedor
docker-compose exec backend sh
docker-compose exec frontend sh
```

## 📁 Estructura de Archivos Docker

```
clinica-veterinaria/
├── docker-compose.yml          # Configuración principal
├── docker-compose.dev.yml       # Solo BD para desarrollo
├── .env.example                 # Ejemplo de variables de entorno
├── apps/
│   ├── backend/
│   │   ├── Dockerfile          # Imagen del backend
│   │   └── .dockerignore       # Archivos a ignorar
│   └── frontend/
│       ├── Dockerfile          # Imagen del frontend
│       ├── nginx.conf          # Configuración de Nginx
│       └── .dockerignore       # Archivos a ignorar
```

## 🔧 Configuración

### Variables de Entorno

Las variables se pueden configurar en:
1. Archivo `.env` en la raíz del proyecto
2. Variables de entorno del sistema
3. En `docker-compose.yml` directamente

**Prioridad**: Variables de entorno del sistema > `.env` > valores por defecto

### Puertos

Por defecto:
- **Frontend**: 5173
- **Backend**: 8080
- **PostgreSQL**: 5432

Puedes cambiarlos en el archivo `.env`:

```env
FRONTEND_PORT=3000
BACKEND_PORT=8080
DB_PORT=5432
```

### Base de Datos

La base de datos se inicializa automáticamente con el script `database-setup.sql` si existe.

Los datos persisten en un volumen Docker llamado `postgres_data`.

## 🐛 Solución de Problemas

### El backend no se conecta a la base de datos

```bash
# Verificar que PostgreSQL esté corriendo
docker-compose ps postgres

# Ver logs de PostgreSQL
docker-compose logs postgres

# Verificar variables de entorno
docker-compose exec backend env | grep DB_
```

### El frontend no se conecta al backend

1. Verificar que `VITE_API_URL` en `.env` apunte al backend correcto
2. Reconstruir la imagen del frontend:
   ```bash
   docker-compose build --no-cache frontend
   docker-compose up -d frontend
   ```

### Limpiar Todo y Empezar de Nuevo

```bash
# Detener y eliminar contenedores, redes y volúmenes
docker-compose down -v

# Eliminar imágenes
docker-compose rm -f

# Limpiar sistema Docker (⚠️ elimina todo)
docker system prune -a --volumes
```

## 📦 Producción

Para producción, considera:

1. **Variables de entorno seguras**: Usa un gestor de secretos
2. **HTTPS**: Configura un reverse proxy (nginx/traefik)
3. **Backups**: Configura backups automáticos de la BD
4. **Monitoreo**: Agrega herramientas de monitoreo
5. **Logs**: Configura rotación de logs

### Ejemplo de docker-compose.prod.yml

```yaml
version: '3.8'

services:
  postgres:
    # ... configuración de producción
    volumes:
      - /data/postgres:/var/lib/postgresql/data  # Volumen persistente
    restart: always

  backend:
    # ... configuración de producción
    restart: always
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 2G

  frontend:
    # ... configuración de producción
    restart: always
```

## 🔒 Seguridad

- ✅ Usuarios no-root en contenedores
- ✅ Health checks configurados
- ✅ Redes aisladas
- ✅ Variables de entorno para secretos
- ⚠️ **IMPORTANTE**: Cambia todas las contraseñas por defecto en producción

## 🧪 Pruebas

### Verificar que todo funciona

```bash
# Verificar que los contenedores estén corriendo
docker-compose ps

# Verificar logs del backend
docker-compose logs backend | grep "Started VeterinariaApplication"

# Verificar conexión a la base de datos
docker-compose exec postgres psql -U postgres -d vetclinic -c "SELECT 1;"

# Probar health check del backend
curl http://localhost:8080/actuator/health

# Probar API
curl http://localhost:8080/api/auth/validate

# Probar frontend
curl http://localhost:5173
```

## 📚 Recursos Adicionales

- [Documentación Docker](https://docs.docker.com/)
- [Docker Compose](https://docs.docker.com/compose/)
- [Spring Boot Docker](https://spring.io/guides/gs/spring-boot-docker/)
- [React Docker](https://mherman.org/blog/dockerizing-a-react-app/)

