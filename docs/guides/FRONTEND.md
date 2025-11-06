# 💻 Guía del Frontend - Clínica Veterinaria

Sistema de interfaz web desarrollado con React, TypeScript y shadcn/ui.

## 📋 Tabla de Contenidos

- [Tecnologías](#tecnologías)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Instalación y Configuración](#instalación-y-configuración)
- [Desarrollo](#desarrollo)
- [Componentes Principales](#componentes-principales)
- [Routing y Navegación](#routing-y-navegación)
- [Autenticación](#autenticación)
- [Gestión de Estado](#gestión-de-estado)
- [Estilos y Temas](#estilos-y-temas)
- [Build y Deployment](#build-y-deployment)

## 🛠 Tecnologías

### Core
- **React 18.3** - Librería UI
- **TypeScript 5.8** - Type safety
- **Vite 5.4** - Build tool y dev server

### Routing y Data Fetching
- **React Router v6** - Client-side routing
- **TanStack Query (React Query)** - Server state management

### UI y Estilos
- **shadcn/ui** - Componentes UI reutilizables
- **Radix UI** - Componentes primitivos accesibles
- **Tailwind CSS 3.4** - Utility-first CSS
- **Lucide React** - Iconos

### Formularios
- **React Hook Form 7.61** - Gestión de formularios
- **Zod 3.25** - Validación de schemas

### Utilidades
- **date-fns** - Manipulación de fechas
- **recharts** - Gráficas y charts
- **class-variance-authority** - Variantes de clases CSS
- **clsx** + **tailwind-merge** - Utilidades de CSS

## 📁 Estructura del Proyecto

```
src/
├── components/
│   ├── layout/              # Componentes de layout
│   │   ├── AppHeader.tsx    # Header con navegación
│   │   ├── AppLayout.tsx    # Layout principal
│   │   └── AppSidebar.tsx   # Sidebar con menú
│   ├── ui/                  # Componentes shadcn/ui
│   │   ├── button.tsx
│   │   ├── input.tsx
│   │   ├── card.tsx
│   │   ├── dialog.tsx
│   │   ├── form.tsx
│   │   ├── table.tsx
│   │   └── ... (40+ componentes)
│   ├── Breadcrumbs.tsx      # Navegación breadcrumb
│   ├── LoadingCards.tsx     # Skeletons de carga
│   ├── NavLink.tsx          # Links de navegación
│   ├── Pagination.tsx       # Paginación de tablas
│   └── ProtectedRoute.tsx   # HOC para rutas protegidas
│
├── contexts/
│   └── AuthContext.tsx      # Context de autenticación
│
├── hooks/
│   ├── use-mobile.tsx       # Hook para detectar móvil
│   └── use-toast.ts         # Hook para notificaciones
│
├── lib/
│   ├── mockData.ts          # Datos de prueba
│   └── utils.ts             # Utilidades generales
│
├── pages/                   # Páginas de la aplicación
│   ├── Dashboard.tsx        # 📊 Panel principal
│   ├── Login.tsx            # 🔐 Página de login
│   │
│   ├── Pacientes.tsx        # 🐾 Lista de pacientes
│   ├── PacienteDetalle.tsx  # 🐾 Detalle del paciente
│   ├── PacienteForm.tsx     # 🐾 Formulario paciente
│   │
│   ├── Propietarios.tsx          # 👨‍👩‍👧 Lista de propietarios
│   ├── PropietarioDetalle.tsx    # 👨‍👩‍👧 Detalle del propietario
│   ├── PropietarioForm.tsx       # 👨‍👩‍👧 Formulario propietario
│   │
│   ├── Agenda.tsx           # 📅 Gestión de citas
│   ├── CitaForm.tsx         # 📅 Formulario de cita
│   │
│   ├── HistoriasClinicas.tsx     # 📋 Historias clínicas
│   ├── HistoriaDetalle.tsx       # 📋 Detalle de historia
│   ├── ConsultaForm.tsx          # 📋 Nueva consulta
│   │
│   ├── Prescripciones.tsx        # 💊 Lista de prescripciones
│   ├── PrescripcionDetalle.tsx   # 💊 Detalle prescripción
│   ├── PrescripcionForm.tsx      # 💊 Formulario prescripción
│   │
│   ├── Reportes.tsx              # 📈 Reportes y estadísticas
│   │
│   ├── SeguridadRoles.tsx        # 🔒 Gestión de roles
│   ├── SeguridadUsuarios.tsx     # 🔒 Gestión de usuarios
│   │
│   └── NotFound.tsx              # ❌ Página 404
│
├── types/
│   └── index.ts             # TypeScript types y interfaces
│
├── App.tsx                  # Componente raíz
├── main.tsx                 # Entry point
├── App.css                  # Estilos globales
└── index.css                # Estilos base + Tailwind
```

## 🚀 Instalación y Configuración

### 1. Instalar Dependencias

```bash
# Con npm
npm install

# Con bun (recomendado para mayor velocidad)
bun install
```

### 2. Configurar Variables de Entorno (Opcional)

Crear archivo `.env.local`:

```env
# URL del backend (por defecto usa proxy en vite.config.ts)
VITE_API_URL=http://localhost:8080/api

# Otras configuraciones
VITE_APP_NAME=Clínica Veterinaria
```

### 3. Iniciar Desarrollo

```bash
npm run dev
# o
bun dev
```

La aplicación estará en `http://localhost:8080`

## 💻 Desarrollo

### Scripts Disponibles

```json
{
  "dev": "vite",              // Servidor de desarrollo
  "build": "vite build",      // Build de producción
  "build:dev": "vite build --mode development", // Build de desarrollo
  "lint": "eslint .",         // Linter
  "preview": "vite preview"   // Preview del build
}
```

### Agregar Nuevos Componentes UI

El proyecto usa **shadcn/ui**. Para agregar componentes:

```bash
# Ejemplo: agregar componente Select
npx shadcn-ui@latest add select

# Ver componentes disponibles
npx shadcn-ui@latest
```

### Estructura de un Componente Típico

```tsx
// src/components/MiComponente.tsx
import { Button } from "@/components/ui/button"
import { Card } from "@/components/ui/card"

interface MiComponenteProps {
  title: string
  onAction?: () => void
}

export function MiComponente({ title, onAction }: MiComponenteProps) {
  return (
    <Card className="p-6">
      <h2 className="text-2xl font-bold">{title}</h2>
      <Button onClick={onAction}>Acción</Button>
    </Card>
  )
}
```

## 🧩 Componentes Principales

### AppLayout

Layout principal con sidebar y header:

```tsx
// Uso automático en rutas protegidas
<Route element={<AppLayout />}>
  <Route path="/" element={<Dashboard />} />
</Route>
```

### ProtectedRoute

Protección de rutas por rol:

```tsx
// Solo ADMIN
<Route path="/admin" element={
  <ProtectedRoute allowedRoles={['ADMIN']}>
    <AdminPage />
  </ProtectedRoute>
} />

// ADMIN y VET
<Route path="/reportes" element={
  <ProtectedRoute allowedRoles={['ADMIN', 'VET']}>
    <Reportes />
  </ProtectedRoute>
} />
```

### Formularios con React Hook Form + Zod

```tsx
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import * as z from "zod"

const formSchema = z.object({
  nombre: z.string().min(2, "Nombre muy corto"),
  email: z.string().email("Email inválido")
})

function MiFormulario() {
  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      nombre: "",
      email: ""
    }
  })

  function onSubmit(values: z.infer<typeof formSchema>) {
    console.log(values)
  }

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)}>
        {/* Campos del formulario */}
      </form>
    </Form>
  )
}
```

## 🛣️ Routing y Navegación

### Estructura de Rutas

```tsx
<Routes>
  {/* Pública */}
  <Route path="/login" element={<Login />} />
  
  {/* Protegidas */}
  <Route element={<ProtectedRoute><AppLayout /></ProtectedRoute>}>
    <Route path="/" element={<Dashboard />} />
    <Route path="/pacientes" element={<Pacientes />} />
    <Route path="/pacientes/nuevo" element={<PacienteForm />} />
    <Route path="/pacientes/:id" element={<PacienteDetalle />} />
    
    {/* Solo roles específicos */}
    <Route path="/reportes" element={
      <ProtectedRoute allowedRoles={['ADMIN', 'VET']}>
        <Reportes />
      </ProtectedRoute>
    } />
  </Route>
  
  {/* 404 */}
  <Route path="*" element={<NotFound />} />
</Routes>
```

### Navegación Programática

```tsx
import { useNavigate } from 'react-router-dom'

function MiComponente() {
  const navigate = useNavigate()
  
  const handleClick = () => {
    navigate('/pacientes')
  }
  
  return <button onClick={handleClick}>Ir a Pacientes</button>
}
```

## 🔐 Autenticación

### AuthContext

Maneja el estado de autenticación globalmente:

```tsx
// src/contexts/AuthContext.tsx
export interface AuthContextType {
  user: Usuario | null
  token: string | null
  login: (email: string, password: string) => Promise<void>
  logout: () => void
  loading: boolean
}

// Uso en componentes
import { useAuth } from '@/contexts/AuthContext'

function MiComponente() {
  const { user, logout } = useAuth()
  
  return (
    <div>
      <p>Hola, {user?.nombre}</p>
      <button onClick={logout}>Cerrar Sesión</button>
    </div>
  )
}
```

### Flujo de Login

```tsx
// 1. Usuario ingresa credenciales
// 2. AuthContext.login() llama al backend
// 3. Backend retorna JWT + datos de usuario
// 4. Se guardan en localStorage
// 5. Se actualiza el estado global
// 6. Redirección al dashboard
```

### Interceptor de Requests (Ejemplo con fetch)

```tsx
async function apiRequest(url: string, options: RequestInit = {}) {
  const token = localStorage.getItem('token')
  
  const response = await fetch(url, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` }),
      ...options.headers
    }
  })
  
  if (response.status === 401) {
    // Logout automático si el token expiró
    localStorage.clear()
    window.location.href = '/login'
  }
  
  return response
}
```

## 📊 Gestión de Estado

### TanStack Query (React Query)

Para data fetching y caché:

```tsx
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'

// GET - Listar pacientes
function usePacientes() {
  return useQuery({
    queryKey: ['pacientes'],
    queryFn: async () => {
      const res = await fetch('/api/pacientes')
      return res.json()
    }
  })
}

// POST - Crear paciente
function useCreatePaciente() {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: async (data) => {
      const res = await fetch('/api/pacientes', {
        method: 'POST',
        body: JSON.stringify(data)
      })
      return res.json()
    },
    onSuccess: () => {
      // Invalidar caché para refrescar la lista
      queryClient.invalidateQueries({ queryKey: ['pacientes'] })
    }
  })
}

// Uso en componente
function Pacientes() {
  const { data, isLoading, error } = usePacientes()
  const createMutation = useCreatePaciente()
  
  if (isLoading) return <div>Cargando...</div>
  if (error) return <div>Error</div>
  
  return (
    <div>
      {data.map(paciente => (
        <div key={paciente.id}>{paciente.nombre}</div>
      ))}
    </div>
  )
}
```

## 🎨 Estilos y Temas

### Tailwind CSS

Clases utility-first:

```tsx
<div className="flex items-center justify-between p-4 bg-white rounded-lg shadow-md">
  <h1 className="text-2xl font-bold text-gray-800">Título</h1>
  <Button className="bg-blue-500 hover:bg-blue-600">Acción</Button>
</div>
```

### Variables CSS

Definidas en `index.css`:

```css
@layer base {
  :root {
    --background: 0 0% 100%;
    --foreground: 222.2 84% 4.9%;
    --primary: 221.2 83.2% 53.3%;
    --secondary: 210 40% 96.1%;
    /* ... más variables */
  }
  
  .dark {
    --background: 222.2 84% 4.9%;
    --foreground: 210 40% 98%;
    /* ... modo oscuro */
  }
}
```

### Componentes con Variantes

```tsx
import { cva } from "class-variance-authority"

const buttonVariants = cva(
  "inline-flex items-center justify-center rounded-md",
  {
    variants: {
      variant: {
        default: "bg-primary text-white",
        destructive: "bg-red-500 text-white",
        outline: "border border-gray-300"
      },
      size: {
        sm: "h-9 px-3 text-sm",
        md: "h-10 px-4",
        lg: "h-11 px-8"
      }
    }
  }
)

<Button variant="destructive" size="lg">Eliminar</Button>
```

## 📦 Build y Deployment

### Build de Producción

```bash
# Compilar
npm run build
# o
bun run build

# Los archivos estarán en /dist
```

### Optimizaciones Automáticas

Vite incluye:
- ✅ Tree-shaking
- ✅ Code splitting
- ✅ Minificación
- ✅ Asset optimization
- ✅ CSS purging (via Tailwind)

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
# Build command
npm run build

# Publish directory
dist
```

### Variables de Entorno en Producción

```bash
# Vercel
vercel env add VITE_API_URL

# Netlify
netlify env:set VITE_API_URL https://api.tu-dominio.com
```

## 🔧 Configuración Vite

```ts
// vite.config.ts
export default defineConfig({
  server: {
    host: "::",
    port: 8080,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  plugins: [react()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  build: {
    outDir: 'dist',
    sourcemap: true
  }
})
```

## 🧪 Testing (Futuro)

### Recomendaciones

```bash
# Instalar Vitest + Testing Library
npm install -D vitest @testing-library/react @testing-library/jest-dom

# Configurar en vite.config.ts
test: {
  globals: true,
  environment: 'jsdom',
  setupFiles: './src/test/setup.ts'
}
```

## 📚 Recursos Adicionales

- [React Docs](https://react.dev/)
- [TypeScript Handbook](https://www.typescriptlang.org/docs/)
- [Vite Guide](https://vitejs.dev/guide/)
- [shadcn/ui Docs](https://ui.shadcn.com/)
- [Tailwind CSS](https://tailwindcss.com/docs)
- [TanStack Query](https://tanstack.com/query/latest)
- [React Router](https://reactrouter.com/)

---

**Última actualización**: Noviembre 2025
**Versión**: 1.0.0

