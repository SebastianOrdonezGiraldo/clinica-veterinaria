# 🚀 Guía de Inicio - Clínica Veterinaria

Esta guía te ayudará a inicializar y ejecutar el proyecto completo desde cero.

## 📋 Requisitos Previos

Antes de comenzar, asegúrate de tener instalado:

### Software Necesario

| Software | Versión Mínima | Link de Descarga |
|----------|---------------|------------------|
| **Java JDK** | 17 o superior | https://adoptium.net/ |
| **Maven** | 3.8+ | https://maven.apache.org/download.cgi |
| **Node.js** | 18+ | https://nodejs.org/ |
| **PostgreSQL** | 14+ | https://www.postgresql.org/download/ |
| **Git** | Cualquiera | https://git-scm.com/downloads |

### Verificar Instalaciones

Abre una terminal y ejecuta estos comandos para verificar:

```bash
java -version        # Debe mostrar Java 17 o superior
mvn -version         # Debe mostrar Maven 3.8+
node -version        # Debe mostrar Node 18+
npm -version         # Debe mostrar npm 9+
psql --version       # Debe mostrar PostgreSQL 14+
```

---

## 📦 Paso 1: Clonar o Descargar el Proyecto

Si tienes el proyecto en un repositorio Git:

```bash
git clone <url-del-repositorio>
cd clinica-veterinaria
```

Si ya tienes el proyecto descargado, simplemente navega a la carpeta:

```bash
cd C:\Users\sebas\clinica-veterinaria
```

---

## 🗄️ Paso 2: Configurar PostgreSQL

### 2.1. Iniciar PostgreSQL

Asegúrate de que el servicio de PostgreSQL esté corriendo.

**En Windows (PowerShell como Administrador):**
```powershell
Start-Service postgresql-x64-*
# O busca el servicio específico:
Get-Service -Name "*postgresql*"
```

**En Windows (Servicios):**
1. Presiona `Win + R`
2. Escribe `services.msc` y presiona Enter
3. Busca el servicio de PostgreSQL
4. Haz clic derecho → Iniciar

### 2.2. Crear la Base de Datos

Abre **pgAdmin** o usa la terminal:

**Opción A: Usando pgAdmin (Recomendado)**

1. Abre pgAdmin
2. Conéctate al servidor PostgreSQL
3. Haz clic derecho en "Databases" → "Create" → "Database"
4. Nombre: `vetclinic_dev`
5. Owner: `postgres`
6. Haz clic en "Save"

**Opción B: Usando línea de comandos**

```bash
# Conectar a PostgreSQL (en el puerto 5433 según tu configuración)
psql -U postgres -p 5433

# Crear la base de datos
CREATE DATABASE vetclinic_dev;

# Salir
\q
```

### 2.3. Verificar la Configuración del Backend

El archivo `backend/src/main/resources/application-dev.properties` debe tener:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/vetclinic_dev
spring.datasource.username=postgres
spring.datasource.password=root
```

**⚠️ IMPORTANTE:** Si tu contraseña de PostgreSQL es diferente, cámbiala en este archivo.

---

## 🔧 Paso 3: Instalar Dependencias

### 3.1. Backend (Java/Maven)

```bash
# Navegar a la carpeta del backend
cd backend

# Limpiar y compilar el proyecto
mvn clean install

# Volver a la raíz del proyecto
cd ..
```

Esto descargará todas las dependencias de Java y compilará el proyecto. Puede tomar unos minutos la primera vez.

### 3.2. Frontend (React/Node)

```bash
# En la raíz del proyecto
npm install
```

Esto instalará todas las dependencias de Node.js para el frontend.

---

## 🚀 Paso 4: Iniciar la Aplicación

### 4.1. Iniciar el Backend (Puerto 8080)

Abre una **nueva terminal** en la carpeta del proyecto:

```bash
cd backend
mvn spring-boot:run
```

**Espera a ver este mensaje:**
```
Started ClinicaVeterinariaApplication in X.XXX seconds
```

El backend estará disponible en: **http://localhost:8080**

📚 **Documentación API (Swagger):** http://localhost:8080/swagger-ui.html

### 4.2. Iniciar el Frontend (Puerto 5173)

Abre **otra terminal nueva** (mantén la del backend corriendo):

```bash
npm run dev
```

**Espera a ver este mensaje:**
```
VITE vX.X.X  ready in XXX ms

➜  Local:   http://localhost:5173/
```

El frontend estará disponible en: **http://localhost:5173**

---

## 🔑 Paso 5: Acceder a la Aplicación

### 5.1. Abrir el Navegador

Ve a: **http://localhost:5173**

### 5.2. Iniciar Sesión

El sistema viene con usuarios de prueba pre-configurados:

| Usuario | Email | Password | Rol |
|---------|-------|----------|-----|
| Admin | `admin@vetclinic.com` | `admin123` | ADMIN |
| Veterinaria | `maria@vetclinic.com` | `admin123` | VET |
| Recepcionista | `recepcion@vetclinic.com` | `admin123` | RECEPCION |

**Usa estas credenciales para hacer login:**
- Email: `admin@vetclinic.com`
- Password: `admin123`

### 5.3. Verificar que Todo Funciona

Una vez que inicies sesión, deberías ver:

✅ Dashboard con estadísticas  
✅ Lista de Propietarios  
✅ Lista de Pacientes  
✅ Agenda de Citas  
✅ Todas las funciones CRUD funcionando  

---

## 📊 Resumen Visual

```
┌─────────────────────────────────────────────────────────┐
│                  ARQUITECTURA DEL SISTEMA               │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  NAVEGADOR (http://localhost:5173)                     │
│         │                                               │
│         │ Peticiones HTTP + JWT Token                  │
│         ▼                                               │
│  FRONTEND (React + Vite)                                │
│         │                                               │
│         │ Proxy /api → http://localhost:8080           │
│         ▼                                               │
│  BACKEND (Spring Boot)                                  │
│         │                                               │
│         │ JDBC                                          │
│         ▼                                               │
│  POSTGRESQL (Puerto 5433)                               │
│  Base de datos: vetclinic_dev                           │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## 🛠️ Solución de Problemas

### Problema 1: "Error al conectar con la base de datos"

**Solución:**
```bash
# Verifica que PostgreSQL esté corriendo
Get-Service -Name "*postgresql*"

# Si no está corriendo, inícialo
Start-Service postgresql-x64-16  # (ajusta el nombre según tu versión)
```

### Problema 2: "Puerto 8080 ya está en uso"

**Solución:**
```bash
# Ver qué está usando el puerto 8080
netstat -ano | findstr :8080

# Matar el proceso (reemplaza PID con el número que viste)
taskkill /PID <PID> /F

# O cambia el puerto en: backend/src/main/resources/application.properties
# server.port=8081
```

### Problema 3: "Puerto 5173 ya está en uso"

**Solución:**
```bash
# Ver qué está usando el puerto 5173
netstat -ano | findstr :5173

# Matar el proceso
taskkill /PID <PID> /F

# O Vite te ofrecerá automáticamente usar otro puerto
```

### Problema 4: "ECONNREFUSED al hacer login"

**Causas comunes:**
- El backend no está corriendo → Verifica la terminal del backend
- El backend está en un puerto diferente → Verifica `application.properties`
- PostgreSQL no está corriendo → Verifica el servicio de PostgreSQL

**Solución:**
```bash
# 1. Verifica que el backend esté corriendo
# En la terminal del backend deberías ver:
# "Started ClinicaVeterinariaApplication"

# 2. Verifica que responda
curl http://localhost:8080/swagger-ui.html
# O abre en el navegador: http://localhost:8080/swagger-ui.html
```

### Problema 5: "Cannot find module" en el frontend

**Solución:**
```bash
# Eliminar node_modules y reinstalar
rm -rf node_modules package-lock.json
npm install
```

### Problema 6: Maven no puede descargar dependencias

**Solución:**
```bash
# Limpiar caché de Maven
mvn clean
mvn dependency:purge-local-repository
mvn clean install -U
```

---

## 📝 Comandos Rápidos de Referencia

### Iniciar Todo (Modo Rápido)

**Terminal 1 - Backend:**
```bash
cd backend
mvn spring-boot:run
```

**Terminal 2 - Frontend:**
```bash
npm run dev
```

### Detener Todo

**Backend:** Presiona `Ctrl + C` en la terminal del backend

**Frontend:** Presiona `Ctrl + C` en la terminal del frontend

### Reiniciar desde Cero

```bash
# 1. Detener backend y frontend (Ctrl + C en ambas terminales)

# 2. Limpiar y reconstruir backend
cd backend
mvn clean install
cd ..

# 3. Limpiar y reinstalar frontend
rm -rf node_modules package-lock.json
npm install

# 4. Iniciar nuevamente (ver "Iniciar Todo" arriba)
```

---

## 🧪 Ejecutar Tests

### Tests del Backend

```bash
cd backend

# Ejecutar todos los tests
mvn test

# Ejecutar solo tests unitarios
mvn test -Dtest="*Test"

# Ejecutar solo tests de integración
mvn test -Dtest="*IntegrationTest"
```

### Tests del Frontend

```bash
# (Actualmente no hay tests configurados en el frontend)
npm test
```

---

## 📚 Documentación Adicional

- **Arquitectura del Sistema:** Ver `guias/ARQUITECTURA.md`
- **Guía de Tests:** Ver `guias/TEST_GUIDE.md`
- **Configuración de PostgreSQL:** Ver `guias/POSTGRESQL-SETUP.md`
- **Guía de Despliegue:** Ver `guias/DEPLOYMENT.md`
- **API Documentation:** http://localhost:8080/swagger-ui.html (cuando el backend esté corriendo)

---

## 🎯 Próximos Pasos

Ahora que tienes el proyecto corriendo:

1. ✅ Explora el Dashboard
2. ✅ Crea un nuevo propietario
3. ✅ Registra un paciente
4. ✅ Agenda una cita
5. ✅ Crea una consulta
6. ✅ Revisa la documentación de la API en Swagger

---

## 🆘 ¿Necesitas Ayuda?

Si encuentras algún problema que no está cubierto en esta guía:

1. Revisa los logs en las terminales del backend y frontend
2. Verifica que todos los servicios estén corriendo
3. Consulta la sección de "Solución de Problemas" arriba
4. Revisa la documentación adicional en la carpeta `guias/`

---

## ✨ ¡Felicidades!

Si llegaste hasta aquí y todo está funcionando, ¡felicitaciones! Ya tienes el sistema completo de gestión de clínica veterinaria corriendo en tu máquina local. 🎉

**Estado del Sistema:**
- ✅ PostgreSQL corriendo en puerto 5433
- ✅ Backend (Spring Boot) corriendo en puerto 8080
- ✅ Frontend (React + Vite) corriendo en puerto 5173
- ✅ Datos de prueba inicializados
- ✅ Sistema listo para usar

---

**Última actualización:** Noviembre 2024  
**Versión del proyecto:** 1.0.0

