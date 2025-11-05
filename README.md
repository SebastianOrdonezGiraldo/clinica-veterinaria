# 🐾 VetClinic Pro

Sistema profesional de gestión para clínicas veterinarias desarrollado con tecnologías modernas de frontend.

## 📋 Descripción

VetClinic Pro es una aplicación web completa para la gestión integral de clínicas veterinarias que incluye:

- 📊 **Dashboard** con estadísticas y actividades del día
- 🐕 **Gestión de Pacientes** (mascotas) con historiales completos
- 👥 **Gestión de Propietarios** con información de contacto
- 📅 **Agenda de Citas** con estados y seguimiento
- 📋 **Historias Clínicas** detalladas con signos vitales, diagnósticos y procedimientos
- 💊 **Prescripciones Médicas** con recetas imprimibles
- 📈 **Reportes Operativos** con estadísticas y análisis
- 🔐 **Sistema de Seguridad** con roles y permisos (Admin, Veterinario, Recepción, Estudiante)

## 🚀 Tecnologías

Este proyecto está construido con:

- **Vite** - Build tool y dev server ultrarrápido
- **React 18** - Biblioteca de UI con hooks modernos
- **TypeScript** - Tipado estático para mayor seguridad
- **React Router v6** - Enrutamiento con protección de rutas
- **Tailwind CSS** - Framework de utilidades CSS
- **shadcn/ui** - Componentes UI basados en Radix UI
- **Lucide React** - Iconos modernos
- **TanStack Query** - Gestión de estado del servidor (preparado)
- **React Hook Form + Zod** - Validación de formularios (preparado)

## 📦 Instalación

### Requisitos previos

- Node.js 18+ y npm instalados - [Instalar con nvm](https://github.com/nvm-sh/nvm#installing-and-updating)

### Pasos de instalación

```bash
# 1. Clonar el repositorio
git clone <URL_DEL_REPOSITORIO>

# 2. Navegar al directorio del proyecto
cd clinica-veterinaria/clinica-veterinaria

# 3. Instalar dependencias
npm install

# 4. Iniciar el servidor de desarrollo
npm run dev
```

El servidor se iniciará en `http://localhost:8080`

## 🛠️ Scripts disponibles

```bash
# Desarrollo
npm run dev          # Inicia el servidor de desarrollo

# Producción
npm run build        # Construye la aplicación para producción
npm run build:dev    # Construye en modo desarrollo
npm run preview      # Previsualiza el build de producción

# Calidad de código
npm run lint         # Ejecuta ESLint
```

## 🔐 Sistema de Autenticación

La aplicación incluye un sistema de autenticación con roles. Usuarios de prueba:

| Email | Contraseña | Rol |
|-------|-----------|-----|
| admin@vetclinic.com | demo123 | Administrador |
| maria@vetclinic.com | demo123 | Veterinario |
| recepcion@vetclinic.com | demo123 | Recepcionista |
| estudiante@vetclinic.com | demo123 | Estudiante |

### Permisos por rol:

- **Administrador**: Acceso completo a todas las funcionalidades
- **Veterinario**: Gestión de pacientes, consultas, prescripciones y reportes
- **Recepcionista**: Gestión de citas, pacientes y propietarios
- **Estudiante**: Solo lectura de historias clínicas y agenda

## 📁 Estructura del proyecto

```
src/
├── components/          # Componentes reutilizables
│   ├── layout/         # Layout principal (Header, Sidebar)
│   ├── ui/             # Componentes UI de shadcn
│   └── ProtectedRoute.tsx
├── contexts/           # Contextos de React (Auth)
├── hooks/              # Custom hooks
├── lib/                # Utilidades y datos mock
├── pages/              # Páginas de la aplicación
├── types/              # Definiciones de TypeScript
├── App.tsx             # Componente principal con rutas
└── main.tsx            # Punto de entrada
```

## 🎨 Sistema de diseño

El proyecto utiliza un sistema de diseño consistente con:

- Tokens de color definidos en `src/index.css`
- Tema claro y oscuro (preparado)
- Paleta de colores médica (teal/cyan principal, verde salud secundario)
- Componentes accesibles de Radix UI

## 🚀 Despliegue

### Build de producción

```bash
npm run build
```

Los archivos optimizados se generarán en la carpeta `dist/`.

### Opciones de despliegue

- **Vercel**: Conecta tu repositorio de GitHub
- **Netlify**: Drag & drop de la carpeta `dist`
- **GitHub Pages**: Configura con GitHub Actions
- **Servidor propio**: Sirve la carpeta `dist` con cualquier servidor web

## 🔄 Próximas mejoras

- [ ] Integración con backend real (API REST)
- [ ] Subida de imágenes de pacientes
- [ ] Exportación de reportes a PDF
- [ ] Notificaciones en tiempo real
- [ ] Modo oscuro completo
- [ ] Internacionalización (i18n)
- [ ] Tests unitarios y de integración

## 📄 Licencia

Este proyecto es privado y de uso interno.

## 👨‍💻 Desarrollo

Para contribuir al proyecto, asegúrate de seguir las convenciones de código y ejecutar el linter antes de hacer commits.

```bash
npm run lint
```

---

Desarrollado con ❤️ para clínicas veterinarias modernas
