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

```bash
# Terminal 1: Iniciar Backend
cd apps/backend
mvn spring-boot:run

# Terminal 2: Iniciar Frontend
cd apps/frontend
npm run dev
```

**Acceder a:**
- 🌐 **Frontend:** http://localhost:5173
- 🔧 **Backend API:** http://localhost:8080
- 📚 **Swagger UI:** http://localhost:8080/swagger-ui.html

**Credenciales de prueba:**
- Email: `admin@clinica.com`
- Password: `admin123`

---

## 📁 Estructura del Proyecto (Nueva Organización)

```
clinica-veterinaria/
│
├── 📁 apps/                    # Aplicaciones del proyecto
│   ├── backend/                # API REST (Spring Boot)
│   └── frontend/               # Aplicación web (React + TypeScript)
│       ├── src/
│       │   ├── core/           # Lógica central (auth, api, router)
│       │   ├── features/       # Características por módulo
│       │   │   ├── auth/
│       │   │   ├── pacientes/
│       │   │   ├── propietarios/
│       │   │   ├── agenda/
│       │   │   ├── historias/
│       │   │   ├── prescripciones/
│       │   │   └── usuarios/
│       │   └── shared/         # Código compartido
│       │       ├── components/ # Componentes UI
│       │       ├── hooks/      # Hooks personalizados
│       │       └── utils/      # Utilidades
│       └── ...
│
├── 📁 docs/                    # Documentación organizada
│   ├── architecture/           # Arquitectura y patrones
│   ├── api/                    # Documentación de API
│   ├── deployment/             # Guías de deployment
│   ├── development/            # Guías de desarrollo
│   ├── database/               # Documentación de BD
│   └── guides/                 # Guías generales
│
├── 📁 scripts/                 # Scripts de automatización
│   ├── setup/                  # Configuración inicial
│   ├── dev/                    # Desarrollo local
│   ├── db/                     # Base de datos
│   └── deploy/                 # Despliegue
│
├── 📁 docker/                  # Configuración Docker
│
└── README.md                   # Este archivo
```

---

## ✨ Características

### Gestión Completa
- 👥 **Usuarios**: Gestión de usuarios con roles (ADMIN, VET, RECEPCION, ESTUDIANTE)
- 🐾 **Pacientes**: Registro completo de mascotas con historial médico
- 👨‍👩‍👧 **Propietarios**: Administración de dueños de mascotas
- 📅 **Citas**: Sistema de agendamiento con estados y seguimiento
- 🏥 **Consultas**: Registro detallado de consultas médicas
- 💊 **Prescripciones**: Recetas médicas digitales

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

---

## 🛠 Tecnologías

### Frontend
- **React 18.3** con TypeScript
- **Vite 5.4** (Build tool)
- **React Router v6** (Routing)
- **TanStack Query** (Data fetching)
- **shadcn/ui** (Componentes UI)
- **Tailwind CSS** (Estilos)

### Backend
- **Java 17**
- **Spring Boot 3.2.1**
  - Spring Data JPA
  - Spring Security
  - Spring Web
- **PostgreSQL 15**
- **JWT (io.jsonwebtoken)**

---

## 📦 Instalación

### 1. Clonar el repositorio

```bash
git clone https://github.com/tu-usuario/clinica-veterinaria.git
cd clinica-veterinaria
```

### 2. Configurar la base de datos

```sql
CREATE DATABASE vetclinic_dev;
CREATE DATABASE vetclinic_prod;
```

### 3. Configurar Backend

```bash
cd apps/backend
# Editar src/main/resources/application.properties
mvn clean install
```

### 4. Configurar Frontend

```bash
cd apps/frontend
npm install
```

---

## 🏃 Ejecución

### Con Scripts (Windows)

```bash
# Iniciar todo el sistema
scripts\dev\start-all.bat

# O iniciar individualmente
scripts\dev\start-backend.bat
scripts\dev\start-frontend.bat
```

### Manual

```bash
# Backend
cd apps/backend
mvn spring-boot:run

# Frontend (en otra terminal)
cd apps/frontend
npm run dev
```

---

## 🧪 Testing

```bash
# Backend
cd apps/backend
mvn test

# Cobertura
mvn clean test jacoco:report
```

---

## 📚 Documentación

La documentación está organizada por categorías en la carpeta `docs/`:

- **[Arquitectura](docs/architecture/)** - Patrones de diseño y decisiones arquitectónicas
- **[API](docs/api/)** - Documentación de endpoints y Postman
- **[Guías](docs/guides/)** - Guías de inicio rápido y tutoriales
- **[Base de Datos](docs/database/)** - Configuración y migraciones
- **[Desarrollo](docs/development/)** - Guías para contributors
- **[Deployment](docs/deployment/)** - Guías de despliegue

### Documentación Rápida

- [📖 Guía de Inicio](docs/guides/GUIA_INICIO.md)
- [🏗️ Arquitectura del Sistema](docs/architecture/ARQUITECTURA.md)
- [🔌 Documentación de API](docs/api/DOCUMENTACION.md)
- [💻 Guía Frontend](docs/guides/FRONTEND.md)

---

## 👥 Roles y Permisos

| Rol | Permisos |
|-----|----------|
| **ADMIN** | Acceso total al sistema |
| **VET** | Gestión de pacientes, citas y consultas |
| **RECEPCION** | Gestión de citas y propietarios |
| **ESTUDIANTE** | Solo lectura |

---

## 🔑 Usuarios de Prueba

| Email | Password | Rol |
|-------|----------|-----|
| admin@clinica.com | admin123 | ADMIN |
| maria@clinica.com | vet123 | VET |
| carlos@clinica.com | vet123 | VET |
| ana@clinica.com | recep123 | RECEPCION |
| juan@clinica.com | est123 | ESTUDIANTE |

---

## 🎯 Beneficios de la Nueva Estructura

### ✅ Organización Modular
- **Features autocontenidas**: Cada módulo (pacientes, citas, etc.) tiene sus propios componentes, páginas, servicios y hooks
- **Fácil navegación**: Encuentra todo lo relacionado con una feature en un solo lugar
- **Escalabilidad**: Agregar nuevas features sin afectar las existentes

### ✅ Separación Clara
- **Core**: Lógica central compartida (auth, api, routing)
- **Features**: Funcionalidades específicas del negocio
- **Shared**: Componentes y utilidades reutilizables

### ✅ Documentación Estructurada
- **Por categorías**: Encuentra fácilmente la información que necesitas
- **Centralizada**: Todo en la carpeta `docs/`

### ✅ Scripts Organizados
- **Por propósito**: Setup, desarrollo, base de datos, deploy
- **Fácil acceso**: Todos en la carpeta `scripts/`

---

## 🤝 Contribuir

1. Fork del proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

Lee la [Guía de Contribución](docs/development/CONTRIBUTING.md) para más detalles.

---

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

---

## 👨‍💻 Autor

**Sebastian Ordoñez**

---

⭐ Si este proyecto te fue útil, considera darle una estrella en GitHub
