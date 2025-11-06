# 🏥 Sistema de Gestión para Clínica Veterinaria

Sistema completo de gestión para clínicas veterinarias desarrollado con Spring Boot (Backend) + React (Frontend) y arquitectura REST.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-brightgreen)
![React](https://img.shields.io/badge/React-18.3-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![Tests](https://img.shields.io/badge/Tests-60%20passed-success)
![License](https://img.shields.io/badge/License-MIT-yellow)

---

## 🚀 Inicio Rápido

¿Primera vez usando este proyecto? Lee la **[📖 Guía de Inicio Completa](GUIA_INICIO.md)** para instrucciones paso a paso.

### Inicio Express (si ya tienes todo configurado)

```bash
# Terminal 1: Iniciar Backend
cd backend
mvn spring-boot:run

# Terminal 2: Iniciar Frontend
npm run dev
```

**Acceder a:**
- 🌐 **Frontend:** http://localhost:5173
- 🔧 **Backend API:** http://localhost:8080
- 📚 **Swagger UI:** http://localhost:8080/swagger-ui.html

**Credenciales de prueba:**
- Email: `admin@vetclinic.com`
- Password: `admin123`

---

## 📋 Tabla de Contenidos

- [Características](#-características)
- [Tecnologías](#-tecnologías)
- [Requisitos Previos](#-requisitos-previos)
- [Instalación](#-instalación)
- [Configuración](#-configuración)
- [Ejecución](#-ejecución)
- [Testing](#-testing)
- [Documentación API](#-documentación-api)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Contribuir](#-contribuir)

## ✨ Características

### Gestión Completa
- 👥 **Usuarios**: Gestión de usuarios con roles (ADMIN, VET, RECEPCION, ESTUDIANTE)
- 🐾 **Pacientes**: Registro completo de mascotas con historial médico
- 👨‍👩‍👧 **Propietarios**: Administración de dueños de mascotas
- 📅 **Citas**: Sistema de agendamiento con estados y seguimiento
- 🏥 **Consultas**: Registro detallado de consultas médicas

### Seguridad
- 🔐 Autenticación JWT
- 🛡️ Control de acceso basado en roles (RBAC)
- 🔒 Encriptación de contraseñas con BCrypt
- 🚫 Protección CSRF

### Características Técnicas
- 📊 API REST completa
- 🗄️ Base de datos PostgreSQL
- ✅ 60 tests (unitarios e integración)
- 📝 Documentación Swagger/OpenAPI
- 🐳 Docker ready
- 🔄 Perfiles de configuración (dev, prod, test)

## 🛠 Tecnologías

### Frontend
- **React 18.3**
- **TypeScript 5.8**
- **Vite 5.4** (Build tool)
- **React Router v6** (Routing)
- **TanStack Query** (Data fetching)
- **shadcn/ui** (Componentes UI)
- **Tailwind CSS** (Estilos)
- **Radix UI** (Componentes primitivos)
- **React Hook Form** + **Zod** (Formularios y validación)
- **Lucide React** (Iconos)
- **Recharts** (Gráficas)

### Backend
- **Java 17**
- **Spring Boot 3.2.1**
  - Spring Data JPA
  - Spring Security
  - Spring Web
- **PostgreSQL 15**
- **JWT (io.jsonwebtoken)**
- **Lombok**
- **Maven**

### Testing
- **JUnit 5**
- **Mockito**
- **Spring Boot Test**
- **H2 Database** (tests)

## 📦 Requisitos Previos

Antes de comenzar, asegúrate de tener instalado:

### Backend
- **Java 17** o superior
- **Maven 3.8** o superior
- **PostgreSQL 15** o superior

### Frontend
- **Node.js 18** o superior
- **npm** o **bun**

### General
- **Git**

## 🚀 Instalación

### 1. Clonar el repositorio

```bash
git clone https://github.com/tu-usuario/clinica-veterinaria.git
cd clinica-veterinaria
```

### 2. Configurar la base de datos

Crea las bases de datos en PostgreSQL:

```sql
-- Base de datos de desarrollo
CREATE DATABASE vetclinic_dev;

-- Base de datos de producción
CREATE DATABASE vetclinic_prod;
```

### 3. Configurar variables de entorno (Producción)

Para producción, configura las siguientes variables de entorno:

```bash
export DB_HOST=localhost
export DB_PORT=5433
export DB_NAME=vetclinic_prod
export DB_USERNAME=tu_usuario
export DB_PASSWORD=tu_contraseña
export JWT_SECRET=tu_secret_key_muy_segura_de_al_menos_256_bits
export JWT_EXPIRATION=36000000
```

## ⚙️ Configuración

### Perfiles de Configuración

El proyecto utiliza tres perfiles:

#### Development (`application-dev.properties`)
```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/vetclinic_dev
spring.jpa.hibernate.ddl-auto=update
```

#### Production (`application-prod.properties`)
```properties
spring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
spring.jpa.hibernate.ddl-auto=validate
```

#### Test (`application-test.properties`)
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
```

### Activar un perfil

En `application.properties`:
```properties
spring.profiles.active=dev
```

## 🏃 Ejecución

### Backend (Modo Desarrollo)

```bash
cd backend
mvn spring-boot:run
```

El backend estará disponible en: `http://localhost:8080`

### Frontend (Modo Desarrollo)

En otra terminal:

```bash
# Instalar dependencias (solo la primera vez)
npm install
# o con bun
bun install

# Iniciar servidor de desarrollo
npm run dev
# o con bun
bun dev
```

El frontend estará disponible en: `http://localhost:8080`

### Iniciar todo el stack

```bash
# Terminal 1 - Backend
cd backend && mvn spring-boot:run

# Terminal 2 - Frontend
npm run dev
```

### Modo Producción

```bash
cd backend
mvn clean package -DskipTests
java -jar target/veterinaria-1.0.0.jar --spring.profiles.active=prod
```

### Con Docker

```bash
docker-compose up -d
```

## 🧪 Testing

### Ejecutar todos los tests

```bash
cd backend
mvn test
```

### Solo tests unitarios

```bash
mvn test -Dtest="*ServiceTest"
```

### Solo tests de integración

```bash
mvn test -Dtest="*IntegrationTest"
```

### Cobertura de Tests

```bash
mvn clean test jacoco:report
```

El reporte estará en: `backend/target/site/jacoco/index.html`

## 📚 Documentación API

### Swagger UI

Una vez iniciada la aplicación, accede a:

```
http://localhost:8080/swagger-ui.html
```

### Endpoints Principales

#### Autenticación
```http
POST /api/auth/login
POST /api/auth/register
GET  /api/auth/validate
```

#### Usuarios
```http
GET    /api/usuarios
GET    /api/usuarios/{id}
POST   /api/usuarios
PUT    /api/usuarios/{id}
DELETE /api/usuarios/{id}
```

#### Propietarios
```http
GET    /api/propietarios
GET    /api/propietarios/{id}
POST   /api/propietarios
PUT    /api/propietarios/{id}
DELETE /api/propietarios/{id}
GET    /api/propietarios/buscar?nombre={nombre}
```

#### Pacientes
```http
GET    /api/pacientes
GET    /api/pacientes/{id}
POST   /api/pacientes
PUT    /api/pacientes/{id}
DELETE /api/pacientes/{id}
GET    /api/pacientes/propietario/{id}
GET    /api/pacientes/buscar?nombre={nombre}
```

#### Citas
```http
GET    /api/citas
GET    /api/citas/{id}
POST   /api/citas
PUT    /api/citas/{id}
DELETE /api/citas/{id}
GET    /api/citas/estado/{estado}
GET    /api/citas/paciente/{id}
GET    /api/citas/profesional/{id}
```

#### Consultas
```http
GET    /api/consultas
GET    /api/consultas/{id}
POST   /api/consultas
PUT    /api/consultas/{id}
DELETE /api/consultas/{id}
GET    /api/consultas/paciente/{id}
```

### Autenticación

La API utiliza JWT Bearer Token:

```bash
# 1. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@clinica.com","password":"admin123"}'

# 2. Usar el token
curl -X GET http://localhost:8080/api/usuarios \
  -H "Authorization: Bearer {tu_token_jwt}"
```

## 📁 Estructura del Proyecto

```
clinica-veterinaria/
├── backend/                          # API REST (Spring Boot)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/clinica/veterinaria/
│   │   │   │   ├── config/          # Configuración
│   │   │   │   ├── controller/      # Controladores REST
│   │   │   │   ├── dto/             # Data Transfer Objects
│   │   │   │   ├── entity/          # Entidades JPA
│   │   │   │   ├── exception/       # Manejo de excepciones
│   │   │   │   ├── repository/      # Repositorios JPA
│   │   │   │   ├── security/        # Configuración de seguridad
│   │   │   │   └── service/         # Lógica de negocio
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       ├── application-dev.properties
│   │   │       ├── application-prod.properties
│   │   │       └── application-test.properties
│   │   └── test/
│   │       ├── java/com/clinica/veterinaria/
│   │       │   ├── integration/     # Tests de integración
│   │       │   └── service/         # Tests unitarios
│   │       └── resources/
│   └── pom.xml
│
├── src/                              # Frontend (React + TypeScript)
│   ├── components/
│   │   ├── layout/                  # Layout components
│   │   │   ├── AppHeader.tsx
│   │   │   ├── AppLayout.tsx
│   │   │   └── AppSidebar.tsx
│   │   ├── ui/                      # shadcn/ui components
│   │   └── ProtectedRoute.tsx
│   ├── contexts/                    # React Context
│   │   └── AuthContext.tsx          # Autenticación
│   ├── hooks/                       # Custom Hooks
│   ├── lib/                         # Utilidades
│   ├── pages/                       # Páginas de la aplicación
│   │   ├── Dashboard.tsx            # Panel principal
│   │   ├── Login.tsx                # Login
│   │   ├── Pacientes.tsx            # Gestión de pacientes
│   │   ├── Propietarios.tsx         # Gestión de propietarios
│   │   ├── Agenda.tsx               # Gestión de citas
│   │   ├── HistoriasClinicas.tsx    # Historias clínicas
│   │   ├── Prescripciones.tsx       # Prescripciones
│   │   ├── Reportes.tsx             # Reportes
│   │   ├── SeguridadRoles.tsx       # Gestión de roles
│   │   └── SeguridadUsuarios.tsx    # Gestión de usuarios
│   ├── types/                       # TypeScript types
│   ├── App.tsx                      # Componente principal
│   └── main.tsx                     # Entry point
│
├── guias/                           # Documentación
│   ├── ARQUITECTURA.md
│   ├── DEPLOYMENT.md
│   └── TEST_GUIDE.md
│
├── package.json                     # Dependencias frontend
├── vite.config.ts                   # Configuración Vite
├── tailwind.config.ts               # Configuración Tailwind
├── tsconfig.json                    # Configuración TypeScript
└── README.md
```

## 👥 Roles y Permisos

| Rol | Permisos |
|-----|----------|
| **ADMIN** | Acceso total al sistema |
| **VET** | Gestión de pacientes, citas y consultas |
| **RECEPCION** | Gestión de citas y propietarios |
| **ESTUDIANTE** | Solo lectura |

## 🔑 Usuarios de Prueba

Al iniciar la aplicación por primera vez, se crean usuarios de prueba:

| Email | Password | Rol |
|-------|----------|-----|
| admin@clinica.com | admin123 | ADMIN |
| maria@clinica.com | vet123 | VET |
| carlos@clinica.com | vet123 | VET |
| ana@clinica.com | recep123 | RECEPCION |
| juan@clinica.com | est123 | ESTUDIANTE |

## 🐛 Solución de Problemas

### Error de conexión a PostgreSQL

```bash
# Verificar que PostgreSQL esté corriendo
sudo systemctl status postgresql

# Verificar el puerto
netstat -an | grep 5433
```

### Error de compilación

```bash
# Limpiar y recompilar
mvn clean install -DskipTests
```

### Tests fallando

```bash
# Verificar perfil de test
cat backend/src/test/resources/application-test.properties

# Ejecutar tests con más información
mvn test -X
```

## 🤝 Contribuir

Las contribuciones son bienvenidas. Por favor:

1. Haz fork del proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📝 Convenciones de Código

- Usar Java Code Conventions
- Comentarios en español
- Tests para nuevas funcionalidades
- Documentación de endpoints

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

## 👨‍💻 Autor

**Sebastian Ordoñez**

## 🙏 Agradecimientos

- Spring Boot Team
- Comunidad de desarrolladores Java
- Todos los contribuidores

## 📞 Contacto

- Email: tu-email@ejemplo.com
- GitHub: [@tu-usuario](https://github.com/tu-usuario)

---

⭐ Si este proyecto te fue útil, considera darle una estrella en GitHub
