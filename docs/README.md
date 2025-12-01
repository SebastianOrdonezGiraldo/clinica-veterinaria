# 📚 Documentación - Sistema de Clínica Veterinaria

Bienvenido a la documentación completa del sistema de gestión para clínicas veterinarias.

## 📋 Sobre el Proyecto

Sistema completo de gestión para clínicas veterinarias que permite administrar pacientes, propietarios, citas médicas, historias clínicas, prescripciones y usuarios del sistema. Desarrollado con arquitectura moderna y mejores prácticas de desarrollo.

### 🎯 Características Principales

- 🔐 **Autenticación y Autorización**: Sistema de autenticación JWT con roles (ADMIN, VET, RECEPCIONISTA, AUXILIAR)
- 👥 **Gestión de Usuarios**: CRUD completo con diferentes roles y permisos
- 🐾 **Gestión de Pacientes**: Registro completo de mascotas con historial médico
- 👤 **Gestión de Propietarios**: Administración de dueños de mascotas
- 📅 **Sistema de Citas**: Programación y seguimiento de citas médicas
- 📋 **Historias Clínicas**: Registro detallado de consultas y diagnósticos
- 💊 **Prescripciones**: Gestión de recetas médicas y medicamentos
- 📊 **Reportes y Estadísticas**: Dashboard con métricas del sistema
- 🔍 **Búsquedas Avanzadas**: Filtros y búsquedas en todos los módulos
- 📱 **Interfaz Moderna**: Frontend responsive con React y TailwindCSS

### 🛠️ Tecnologías Utilizadas

#### Backend
- **Java 17** - Lenguaje de programación
- **Spring Boot 3.2.1** - Framework principal
- **Spring Security** - Seguridad y autenticación
- **Spring Data JPA** - Acceso a datos
- **PostgreSQL** - Base de datos relacional
- **JWT (JSON Web Tokens)** - Autenticación stateless
- **Maven** - Gestión de dependencias
- **Lombok** - Reducción de código boilerplate
- **Jakarta Validation** - Validación de datos

#### Frontend
- **React 18** - Biblioteca UI
- **TypeScript** - Tipado estático
- **Vite** - Build tool y dev server
- **TailwindCSS** - Framework CSS utility-first
- **shadcn/ui** - Componentes UI
- **React Router** - Enrutamiento
- **React Query (TanStack Query)** - Gestión de estado del servidor
- **Axios** - Cliente HTTP
- **React Hook Form** - Manejo de formularios
- **Zod** - Validación de esquemas

### 📋 Requisitos Previos

Antes de comenzar, asegúrate de tener instalado:

| Software | Versión Mínima | Descripción |
|----------|---------------|-------------|
| **Java JDK** | 17+ | [Descargar](https://adoptium.net/) |
| **Maven** | 3.8+ | [Descargar](https://maven.apache.org/download.cgi) |
| **Node.js** | 18+ | [Descargar](https://nodejs.org/) |
| **PostgreSQL** | 14+ | [Descargar](https://www.postgresql.org/download/) |
| **Git** | Cualquiera | [Descargar](https://git-scm.com/downloads) |

### ✅ Estado del Proyecto

- ✅ **Backend**: Completamente implementado y funcional
- ✅ **Frontend**: Implementado con todas las funcionalidades principales
- ✅ **Autenticación**: Sistema JWT completo
- ✅ **API REST**: 37+ endpoints documentados
- ✅ **Base de Datos**: Esquema completo con relaciones
- ✅ **Documentación**: Guías completas y detalladas
- ✅ **Testing**: Datos de prueba incluidos
- ✅ **Despliegue**: Desplegado en producción (Render)
- ✅ **CORS**: Configurado correctamente para producción
- ✅ **Docker**: Dockerfile optimizado para despliegue

## 📖 Índice de Documentación

### 🏗️ Arquitectura
- **[ARQUITECTURA.md](./architecture/ARQUITECTURA.md)** - Diseño arquitectónico del sistema
- **[PATRONES-RESUMEN.md](./architecture/PATRONES-RESUMEN.md)** - Patrones de diseño implementados

### 🔌 API
- **[POSTMAN_GUIDE.md](./api/POSTMAN_GUIDE.md)** - Guía de uso de la colección Postman
- **[Postman Collection](./api/postman/Clinica_Veterinaria_API.postman_collection.json)** - Colección de endpoints

### 🚀 Deployment
- **[DEPLOYMENT.md](./deployment/DEPLOYMENT.md)** - Guía de despliegue a producción

### 💻 Desarrollo
- **[SOLUCION_FRONTEND.md](./development/SOLUCION_FRONTEND.md)** - Soluciones y troubleshooting frontend
- **[TEST_GUIDE.md](./development/TEST_GUIDE.md)** - Guía de testing

### 🗄️ Base de Datos
- **[POSTGRESQL-SETUP.md](./database/POSTGRESQL-SETUP.md)** - Configuración de PostgreSQL

### 📘 Guías
- **[GUIA_INICIO.md](./guides/GUIA_INICIO.md)** - Guía de inicio completa
- **[INICIO-RAPIDO.md](./guides/INICIO-RAPIDO.md)** - Inicio rápido
- **[FRONTEND.md](./guides/FRONTEND.md)** - Guía del frontend
- **[RESUMEN-PROYECTO.md](./guides/RESUMEN-PROYECTO.md)** - Resumen del proyecto
- **[DOCUMENTACION.md](./guides/DOCUMENTACION.md)** - Documentación general

### 📝 Otros
- **[CHANGELOG.md](./CHANGELOG.md)** - Historial de cambios

---

## 🌐 Producción

El proyecto está desplegado y funcionando en producción:

- **Frontend**: [https://clinica-veterinaria-frontend-f9us.onrender.com](https://clinica-veterinaria-frontend-f9us.onrender.com)
- **Backend API**: [https://clinica-veterinaria-backend-1fut.onrender.com](https://clinica-veterinaria-backend-1fut.onrender.com)
- **Swagger UI**: [https://clinica-veterinaria-backend-1fut.onrender.com/swagger-ui](https://clinica-veterinaria-backend-1fut.onrender.com/swagger-ui)

### 🚀 Despliegue

El proyecto está configurado para desplegarse automáticamente en Render usando `render.yaml`. Para más detalles sobre el despliegue, consulta:

- **[DEPLOYMENT.md](./deployment/DEPLOYMENT.md)** - Guía completa de despliegue
- **render.yaml** - Configuración de servicios en Render

## 🚦 Inicio Rápido

Para comenzar con el proyecto, consulta:
1. **[Guía de Inicio](./guides/GUIA_INICIO.md)** - Setup completo paso a paso
2. **[Inicio Rápido](./guides/INICIO-RAPIDO.md)** - Si ya tienes todo configurado
3. **[Arquitectura](./architecture/ARQUITECTURA.md)** - Para entender el sistema

### ⚡ Instalación Rápida

```bash
# 1. Clonar el repositorio
git clone <url-del-repositorio>
cd clinica-veterinaria

# 2. Configurar base de datos PostgreSQL
# Ver: docs/database/POSTGRESQL-SETUP.md

# 3. Configurar variables de entorno
# Backend: Copiar apps/backend/env.example y configurar
# Frontend: Copiar apps/frontend/env.example y configurar

# 4. Iniciar Backend
cd apps/backend
mvn spring-boot:run

# 5. Iniciar Frontend (en otra terminal)
cd apps/frontend
npm install
npm run dev
```

### 🔑 Credenciales de Prueba

El sistema incluye usuarios de prueba preconfigurados:

| Rol | Email | Contraseña | Descripción |
|-----|-------|------------|-------------|
| **ADMIN** | admin@clinica.com | admin123 | Administrador del sistema |
| **VET** | vet@clinica.com | vet123 | Veterinario |
| **RECEPCIONISTA** | recepcion@clinica.com | recepcion123 | Recepcionista |
| **AUXILIAR** | auxiliar@clinica.com | auxiliar123 | Auxiliar veterinario |

## 🏗️ Estructura del Proyecto

```
clinica-veterinaria/
├── apps/
│   ├── backend/                    # API REST (Spring Boot)
│   │   ├── src/main/java/          # Código fuente Java
│   │   │   └── com/clinica/veterinaria/
│   │   │       ├── controller/     # Controladores REST
│   │   │       ├── service/        # Lógica de negocio
│   │   │       ├── repository/     # Acceso a datos
│   │   │       ├── entity/         # Entidades JPA
│   │   │       ├── dto/            # Data Transfer Objects
│   │   │       ├── security/       # Configuración seguridad
│   │   │       └── exception/      # Manejo de excepciones
│   │   ├── src/main/resources/     # Recursos y configuración
│   │   └── pom.xml                 # Dependencias Maven
│   │
│   └── frontend/                    # App Web (React + TypeScript)
│       ├── src/
│       │   ├── features/            # Módulos de funcionalidades
│       │   │   ├── auth/           # Autenticación
│       │   │   ├── pacientes/      # Gestión de pacientes
│       │   │   ├── propietarios/   # Gestión de propietarios
│       │   │   ├── citas/          # Sistema de citas
│       │   │   ├── historias/      # Historias clínicas
│       │   │   └── ...
│       │   ├── core/               # Funcionalidades core
│       │   ├── shared/            # Componentes compartidos
│       │   └── main.tsx           # Punto de entrada
│       └── package.json           # Dependencias npm
│
├── docs/                           # 📚 Documentación (estás aquí)
│   ├── architecture/              # Arquitectura y patrones
│   ├── api/                       # Documentación API
│   ├── deployment/                # Guías de despliegue
│   ├── development/               # Guías de desarrollo
│   ├── database/                  # Configuración BD
│   └── guides/                    # Guías generales
│
└── scripts/                       # Scripts de automatización
    ├── setup/                     # Configuración inicial
    ├── dev/                       # Desarrollo
    ├── db/                        # Base de datos
    └── deploy/                    # Despliegue
```

### 📦 Módulos del Sistema

#### Backend - Endpoints Disponibles

- **🔐 Autenticación** (`/api/auth/*`)
  - Login, validación de tokens
  
- **👥 Usuarios** (`/api/usuarios/*`)
  - CRUD completo, gestión de roles
  
- **👤 Propietarios** (`/api/propietarios/*`)
  - CRUD, búsquedas, paginación
  
- **🐾 Pacientes** (`/api/pacientes/*`)
  - CRUD, búsqueda por propietario, filtros por especie
  
- **📅 Citas** (`/api/citas/*`)
  - CRUD, filtros por fecha, profesional, paciente
  
- **📋 Consultas** (`/api/consultas/*`)
  - Historias clínicas, diagnósticos, seguimiento
  
- **💊 Prescripciones** (`/api/prescripciones/*`)
  - Recetas médicas, medicamentos

#### Frontend - Módulos Implementados

- **🔐 Autenticación**: Login, logout, gestión de sesión
- **📊 Dashboard**: Panel principal con estadísticas
- **🐾 Pacientes**: Listado, creación, edición, historial
- **👤 Propietarios**: Gestión completa de propietarios
- **📅 Agenda**: Sistema de citas y calendario
- **📋 Historias Clínicas**: Registro y consulta de historias
- **💊 Prescripciones**: Gestión de recetas médicas
- **👥 Usuarios**: Administración de usuarios (solo ADMIN)
- **📈 Reportes**: Estadísticas y reportes del sistema
- **🔔 Notificaciones**: Sistema de alertas y notificaciones

## 💡 Ayuda Rápida

### ¿Cómo...?

**...inicio el proyecto por primera vez?**
→ [GUIA_INICIO.md](./guides/GUIA_INICIO.md)

**...configuro la base de datos?**
→ [POSTGRESQL-SETUP.md](./database/POSTGRESQL-SETUP.md)

**...pruebo los endpoints de la API?**
→ [POSTMAN_GUIDE.md](./api/POSTMAN_GUIDE.md)

**...despliego a producción?**
→ [DEPLOYMENT.md](./deployment/DEPLOYMENT.md)

**...ejecuto los tests?**
→ [TEST_GUIDE.md](./development/TEST_GUIDE.md)

**...entiendo la arquitectura?**
→ [ARQUITECTURA.md](./architecture/ARQUITECTURA.md)

**...desarrollo nuevas funcionalidades?**
→ [RESUMEN-PROYECTO.md](./guides/RESUMEN-PROYECTO.md)

**...configuro el frontend?**
→ [FRONTEND.md](./guides/FRONTEND.md)

---

## 📊 Estadísticas del Proyecto

- **37+ Endpoints REST** documentados y funcionales
- **8 Entidades** principales en la base de datos
- **10+ Módulos** en el frontend
- **4 Roles** de usuario diferentes
- **50+ Métodos** de consulta en repositorios
- **100% Cobertura** de funcionalidades principales

## 🔒 Seguridad

El sistema implementa múltiples capas de seguridad:

- ✅ Autenticación JWT con tokens firmados
- ✅ Contraseñas encriptadas con BCrypt
- ✅ Autorización basada en roles (RBAC)
- ✅ Validación de entrada en todos los endpoints
- ✅ Protección CSRF (deshabilitada para API stateless)
- ✅ CORS configurado correctamente para producción
- ✅ Auditoría de intentos de login
- ✅ Filtros de seguridad en Spring Security
- ✅ Variables de entorno para secretos sensibles
- ✅ HTTPS habilitado en producción

## 🧪 Testing

- Datos de prueba incluidos automáticamente al iniciar
- Usuarios de prueba con diferentes roles
- Datos realistas para desarrollo y testing
- Colección Postman para pruebas de API

## 📚 Recursos Adicionales

- **[Colección Postman](./api/postman/Clinica_Veterinaria_API.postman_collection.json)** - Importa esta colección para probar todos los endpoints
- **[Guía de Variables de Entorno](./development/VARIABLES-ENTORNO.md)** - Configuración de variables de entorno
- **[Sistema de Logging](./development/LOGGING-SYSTEM.md)** - Documentación del sistema de logs
- **[Patrones de Diseño](./architecture/PATRONES-RESUMEN.md)** - Patrones implementados en el proyecto

## 🤝 Contribuir

Si deseas contribuir al proyecto:

1. Revisa la documentación de arquitectura
2. Sigue las convenciones de código establecidas
3. Asegúrate de que los tests pasen
4. Documenta los cambios realizados

## 📝 Licencia

Este proyecto es de uso privado. Todos los derechos reservados.

---

## 👨‍💻 Autor

**Sebastian Ordoñez**

- 📧 Email: sebastian789go@gmail.com
- 🔗 GitHub: https://github.com/SebastianOrdonezGiraldo
- 📅 Fecha de inicio: Noviembre 2025
- 🏗️ Arquitectura: Layered Architecture con Spring Boot y React

---

**Última actualización:** Diciembre 2025  
**Versión:** 2.0.0  
**Estado:** ✅ **Completado y en producción**

