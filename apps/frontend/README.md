# 💻 Frontend - Clínica Veterinaria

Aplicación web desarrollada con React, TypeScript y Vite, organizada con arquitectura modular por features.

## 🚀 Inicio Rápido

```bash
# Instalar dependencias
npm install

# Iniciar servidor de desarrollo
npm run dev

# Acceder a: http://localhost:5173
```

## 📁 Estructura del Proyecto

```
src/
├── core/                      # Lógica central de la aplicación
│   ├── api/                   # Configuración de API y servicios
│   │   ├── axios.ts           # Cliente HTTP configurado
│   │   ├── citaService.ts
│   │   ├── consultaService.ts
│   │   └── usuarioService.ts
│   ├── auth/                  # Autenticación
│   │   ├── AuthContext.tsx    # Context de autenticación
│   │   └── authService.ts     # Servicio de auth
│   ├── router/                # Configuración de rutas
│   └── types/                 # Types globales
│       └── index.ts
│
├── features/                  # Módulos de negocio
│   ├── auth/                  # Login y autenticación
│   │   └── pages/
│   │       └── Login.tsx
│   │
│   ├── dashboard/             # Panel principal
│   │   └── pages/
│   │       └── Dashboard.tsx
│   │
│   ├── pacientes/             # Gestión de pacientes
│   │   ├── components/        # Componentes específicos
│   │   ├── pages/             # Páginas del módulo
│   │   │   ├── Pacientes.tsx
│   │   │   ├── PacienteDetalle.tsx
│   │   │   └── PacienteForm.tsx
│   │   ├── hooks/             # Hooks personalizados
│   │   ├── services/          # Servicio API
│   │   ├── types/             # Types del módulo
│   │   └── README.md          # Documentación
│   │
│   ├── propietarios/          # Gestión de propietarios
│   ├── agenda/                # Citas médicas
│   ├── historias/             # Historias clínicas
│   ├── prescripciones/        # Prescripciones médicas
│   └── usuarios/              # Gestión de usuarios
│
└── shared/                    # Código compartido
    ├── components/            # Componentes reutilizables
    │   ├── ui/                # shadcn/ui components (40+)
    │   ├── layout/            # Layout components
    │   │   ├── AppLayout.tsx
    │   │   ├── AppHeader.tsx
    │   │   └── AppSidebar.tsx
    │   └── common/            # Componentes comunes
    │       ├── Breadcrumbs.tsx
    │       ├── LoadingCards.tsx
    │       ├── Pagination.tsx
    │       └── ProtectedRoute.tsx
    ├── hooks/                 # Hooks compartidos
    │   ├── use-mobile.tsx
    │   └── use-toast.ts
    ├── utils/                 # Utilidades
    │   ├── utils.ts
    │   └── mockData.ts
    └── constants/             # Constantes
```

## 🛠 Tecnologías

### Core
- **React 18.3** - Librería UI
- **TypeScript 5.8** - Type safety
- **Vite 5.4** - Build tool ultra-rápido

### UI y Estilos
- **shadcn/ui** - Componentes UI de alta calidad
- **Radix UI** - Primitivos accesibles
- **Tailwind CSS 3.4** - Utility-first CSS
- **Lucide React** - Iconos modernos

### Routing y Data
- **React Router v6** - Routing declarativo
- **TanStack Query** - Data fetching y cache

### Formularios
- **React Hook Form** - Gestión de formularios
- **Zod** - Validación de schemas

### Gráficos
- **Recharts** - Visualización de datos

## 🎯 Features Disponibles

| Feature | Ruta Base | Descripción |
|---------|-----------|-------------|
| **Auth** | `/login` | Autenticación de usuarios |
| **Dashboard** | `/` | Panel principal con estadísticas |
| **Pacientes** | `/pacientes` | Gestión de mascotas |
| **Propietarios** | `/propietarios` | Gestión de dueños |
| **Agenda** | `/agenda` | Sistema de citas |
| **Historias** | `/historias` | Historias clínicas |
| **Prescripciones** | `/prescripciones` | Recetas médicas (VET+) |
| **Usuarios** | `/seguridad` | Gestión de usuarios (ADMIN) |

## 📦 Scripts Disponibles

```bash
# Desarrollo
npm run dev              # Inicia servidor de desarrollo

# Build
npm run build            # Build de producción
npm run build:dev        # Build de desarrollo

# Linting
npm run lint             # Ejecuta ESLint

# Preview
npm run preview          # Preview del build de producción
```

## 🔧 Configuración

### Variables de Entorno

Crea un archivo `.env.local` (opcional):

```env
# URL del backend (por defecto usa proxy)
VITE_API_URL=http://localhost:8080/api

# Otras configuraciones
VITE_APP_NAME=Clínica Veterinaria
```

### Path Aliases

El proyecto usa path aliases para imports más limpios:

```typescript
// tsconfig.json & vite.config.ts
{
  "@/*": ["./src/*"],
  "@core/*": ["./src/core/*"],
  "@features/*": ["./src/features/*"],
  "@shared/*": ["./src/shared/*"]
}
```

**Ejemplo de uso:**

```typescript
// ❌ Antes
import { Button } from '../../../shared/components/ui/button';

// ✅ Ahora
import { Button } from '@shared/components/ui/button';
```

## 🎨 Agregar Componentes UI

El proyecto usa **shadcn/ui**. Para agregar nuevos componentes:

```bash
# Ejemplo: agregar Select
npx shadcn-ui@latest add select

# Ver componentes disponibles
npx shadcn-ui@latest
```

Los componentes se instalan automáticamente en `src/shared/components/ui/`.

## 🏗️ Crear una Nueva Feature

1. **Crear estructura:**

```bash
mkdir -p src/features/mi-feature/{components,pages,hooks,services,types}
```

2. **Crear archivos básicos:**

```typescript
// src/features/mi-feature/pages/MiFeature.tsx
export default function MiFeature() {
  return <div>Mi Nueva Feature</div>
}

// src/features/mi-feature/services/miFeatureService.ts
import axios from '@core/api/axios';

export const miFeatureService = {
  async getAll() {
    const response = await axios.get('/mi-feature');
    return response.data;
  }
};
```

3. **Agregar ruta en App.tsx:**

```typescript
import MiFeature from './features/mi-feature/pages/MiFeature';

<Route path="/mi-feature" element={<MiFeature />} />
```

4. **Documentar:**

Crea `src/features/mi-feature/README.md` explicando el módulo.

## 🔒 Protección de Rutas

### Proteger una ruta completa:

```typescript
<Route path="/admin" element={
  <ProtectedRoute allowedRoles={['ADMIN']}>
    <AdminPage />
  </ProtectedRoute>
} />
```

### Múltiples roles:

```typescript
<ProtectedRoute allowedRoles={['ADMIN', 'VET']}>
  <Reportes />
</ProtectedRoute>
```

## 🎣 Hooks Personalizados

### useAuth()

```typescript
import { useAuth } from '@core/auth/AuthContext';

function MiComponente() {
  const { user, logout } = useAuth();
  
  return (
    <div>
      <p>Usuario: {user?.nombre}</p>
      <button onClick={logout}>Salir</button>
    </div>
  );
}
```

### usePacientes()

```typescript
import { usePacientes } from '@features/pacientes/hooks/usePacientes';

function Lista() {
  const { data, isLoading, error } = usePacientes();
  
  if (isLoading) return <LoadingCards />;
  if (error) return <div>Error</div>;
  
  return <PacienteTable pacientes={data} />;
}
```

## 📝 Convenciones de Código

### Componentes

```typescript
// PascalCase para componentes
export function MiComponente({ prop1, prop2 }: Props) {
  return <div>...</div>
}
```

### Hooks

```typescript
// use + PascalCase
export function useMiHook() {
  const [state, setState] = useState();
  return { state, setState };
}
```

### Servicios

```typescript
// camelCase + Service
export const miService = {
  async metodo() {
    // ...
  }
};
```

### Types

```typescript
// PascalCase para interfaces/types
export interface MiType {
  id: string;
  nombre: string;
}
```

## 🐛 Solución de Problemas

### Error: Cannot find module '@/...'

**Solución:** Reinicia el servidor de desarrollo

```bash
npm run dev
```

### Componentes UI no funcionan

**Solución:** Verifica que Tailwind esté configurado correctamente

```bash
# Reinstalar dependencias
rm -rf node_modules package-lock.json
npm install
```

### Error de tipos TypeScript

**Solución:** Regenera los tipos

```bash
npx tsc --noEmit
```

## 📚 Recursos y Documentación

- **[Guía de Nueva Estructura](../../docs/guides/NUEVA-ESTRUCTURA.md)** - Cómo trabajar con la organización modular
- **[Documentación de Features](./src/features/)** - Cada feature tiene su README
- **[React Docs](https://react.dev/)** - Documentación oficial de React
- **[Vite Guide](https://vitejs.dev/guide/)** - Guía de Vite
- **[shadcn/ui](https://ui.shadcn.com/)** - Documentación de componentes
- **[Tailwind CSS](https://tailwindcss.com/docs)** - Documentación de Tailwind

## 🧪 Testing (Próximamente)

```bash
# Instalar dependencias de testing
npm install -D vitest @testing-library/react @testing-library/jest-dom

# Ejecutar tests
npm test
```

## 🚀 Build y Deploy

### Build de Producción

```bash
npm run build
# Output: dist/
```

### Preview del Build

```bash
npm run preview
# Accede a: http://localhost:4173
```

### Deploy en Vercel

```bash
# Instalar Vercel CLI
npm i -g vercel

# Deploy
vercel

# Producción
vercel --prod
```

### Deploy en Netlify

```bash
# Build command: npm run build
# Publish directory: dist
```

## 👥 Contribuir

1. Crea una rama para tu feature
2. Sigue las convenciones de código
3. Documenta tus cambios
4. Crea un Pull Request

## 📄 Licencia

MIT License - Ver [LICENSE](../../LICENSE)

---

**¿Preguntas?** Consulta la [documentación principal](../../README.md) o abre un issue.

---

**Última actualización:** Noviembre 2025  
**Versión:** 2.0.0
