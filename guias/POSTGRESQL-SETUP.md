# 🐘 Configuración de PostgreSQL

Guía paso a paso para configurar PostgreSQL para el proyecto Clínica Veterinaria.

## 📋 Requisitos Previos

### Windows
1. Descargar PostgreSQL desde: https://www.postgresql.org/download/windows/
2. Ejecutar el instalador (recomendado: versión 15 o superior)
3. Durante la instalación:
   - Puerto por defecto: **5432**
   - Usuario: **postgres**
   - Contraseña: **root** (o la que prefieras)

## 🗄️ Crear la Base de Datos

### Opción 1: Usando pgAdmin (GUI)
1. Abrir pgAdmin 4
2. Conectarse al servidor local
3. Click derecho en "Databases" → "Create" → "Database"
4. Nombre: `vetclinic`
5. Owner: `postgres`
6. Click "Save"

## ⚙️ Configuración de Credenciales

Las credenciales por defecto están en `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/vetclinic
spring.datasource.username=postgres
spring.datasource.password=postgres
```

### Cambiar credenciales

#### Opción 1: Editar application.properties
```properties
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña
```

#### Opción 2: Variables de entorno (recomendado para producción)
```bash
# Windows
set DB_USERNAME=tu_usuario
set DB_PASSWORD=tu_contraseña

# Linux/macOS
export DB_USERNAME=tu_usuario
export DB_PASSWORD=tu_contraseña
```

Luego en `application.properties`:
```properties
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}
```

## 🔍 Verificar Conexión

### 1. Verificar que PostgreSQL está corriendo

#### Windows
```cmd
# Abrir Servicios (services.msc)
# Buscar "postgresql" y verificar que está "En ejecución"

# O desde PowerShell
Get-Service -Name postgresql*
```

#### Linux
```bash
sudo systemctl status postgresql
```

#### macOS
```bash
brew services list | grep postgresql
```

### 2. Verificar conexión desde la aplicación

Iniciar el backend:
```bash
cd backend
mvn spring-boot:run
```

Si ves en los logs:
```
Hibernate: create table ...
```
¡La conexión es exitosa! ✅

## 🛠️ Herramientas Recomendadas

### pgAdmin 4
- Interfaz gráfica oficial para PostgreSQL
- Incluida en la instalación de Windows
- Descarga: https://www.pgadmin.org/

### DBeaver
- Cliente universal de bases de datos
- Gratuito y multiplataforma
- Descarga: https://dbeaver.io/

### VS Code Extensions
- **PostgreSQL** by Chris Kolkman
- Permite ejecutar queries directamente desde VS Code

## 🔧 Comandos Útiles de PostgreSQL

```sql
-- Listar todas las bases de datos
\l

-- Conectarse a una base de datos
\c vetclinic

-- Listar todas las tablas
\dt

-- Ver estructura de una tabla
\d nombre_tabla

-- Listar usuarios
\du

-- Ver conexiones activas
SELECT * FROM pg_stat_activity;

-- Terminar una conexión
SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE pid = <numero>;
```

## 🐛 Solución de Problemas

### Error: "password authentication failed"
```bash
# 1. Resetear contraseña del usuario postgres
sudo -u postgres psql
ALTER USER postgres PASSWORD 'nueva_contraseña';
\q
```

### Error: "database does not exist"
```bash
# Crear la base de datos
psql -U postgres
CREATE DATABASE vetclinic;
\q
```

### Error: "Connection refused" o "could not connect to server"
```bash
# Verificar que PostgreSQL está corriendo
# Windows
Get-Service postgresql*

# Linux
sudo systemctl start postgresql
```

### Cambiar puerto de PostgreSQL
Editar `postgresql.conf`:
```
# Windows: C:\Program Files\PostgreSQL\15\data\postgresql.conf
# Linux: /etc/postgresql/15/main/postgresql.conf

port = 5433  # Cambiar a otro puerto si 5432 está ocupado
```

Reiniciar PostgreSQL y actualizar `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/vetclinic
```

## 📊 Esquema de Base de Datos

Las tablas se crearán automáticamente al iniciar la aplicación gracias a:
```properties
spring.jpa.hibernate.ddl-auto=update
```

Tablas principales que se crearán:
- `usuarios` - Usuarios del sistema
- `propietarios` - Dueños de mascotas
- `pacientes` - Mascotas/Pacientes
- `citas` - Citas médicas
- `consultas` - Historias clínicas
- `prescripciones` - Recetas médicas
- `items_prescripcion` - Medicamentos recetados

## 🔐 Seguridad

### Para Producción:
1. **Cambiar credenciales por defecto**
2. **Usar variables de entorno** para contraseñas
3. **Configurar SSL** para conexiones
4. **Crear usuario específico** para la aplicación (no usar postgres)

```sql
-- Crear usuario específico para la app
CREATE USER vetclinic_user WITH PASSWORD 'contraseña_segura';
GRANT ALL PRIVILEGES ON DATABASE vetclinic TO vetclinic_user;
```

## 📚 Recursos Adicionales

- [Documentación oficial PostgreSQL](https://www.postgresql.org/docs/)
- [Spring Data JPA Documentation](https://spring.io/projects/spring-data-jpa)
- [Hibernate PostgreSQL Dialect](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#database)

---

✅ Una vez configurado PostgreSQL, el backend estará listo para crear las tablas automáticamente al iniciar.

