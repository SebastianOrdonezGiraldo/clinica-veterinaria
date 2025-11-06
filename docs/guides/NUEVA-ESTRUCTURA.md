# 📂 Guía de la Nueva Estructura del Proyecto

Esta guía explica la reorganización del proyecto y cómo trabajar con la nueva estructura modular.

## 🎯 Objetivos de la Reorganización

1. **Modularidad**: Agrupar código relacionado por features
2. **Escalabilidad**: Facilitar el crecimiento del proyecto
3. **Mantenibilidad**: Código más fácil de encontrar y modificar
4. **Colaboración**: Estructura clara para múltiples desarrolladores

---

## 📁 Estructura General

```
clinica-veterinaria/
├── apps/          # Aplicaciones (backend y frontend)
├── docs/          # Documentación organizada
├── scripts/       # Scripts de automatización
├── docker/        # Configuración Docker
└── README.md      # Documentación principal
```

---

## 🎨 Frontend - Organización por Features

### Antes (Estructura Plana)
```
src/
├── components/
│   ├── ui/
│   └── layout/
├── pages/
├── services/
├── types/
└── contexts/
```

**Problemas:**
- ❌ Difícil encontrar código relacionado
- ❌ Imports largos y confusos
- ❌ No está claro qué es reutilizable

### Después (Feature-Sliced Design)
```
src/
├── core/              # Lógica central
│   ├── api/           # Cliente API, interceptores
│   ├── auth/          # Autenticación y contexto
│   ├── router/        # Configuración de rutas
│   └── types/         # Types globales
│
├── features/          # Módulos de negocio
│   ├── pacientes/     # Todo sobre pacientes
│   │   ├── components/
│   │   ├── pages/
│   │   ├── hooks/
│   │   ├── services/
│   │   ├── types/
│   │   └── README.md
│   ├── propietarios/
│   ├── agenda/
│   └── ...
│
└── shared/            # Código compartido
    ├── components/
    │   ├── ui/        # shadcn/ui
    │   ├── layout/    # Layout components
    │   └── common/    # Componentes comunes
    ├── hooks/
    ├── utils/
    └── constants/
```

**Beneficios:**
- ✅ Todo el código de una feature en un lugar
- ✅ Fácil saber qué es reutilizable (shared)
- ✅ Imports más claros
- ✅ Code splitting natural

---

## 🔍 Convenciones de Nomenclatura

### Features (Singular)
```typescript
features/paciente/     ✅ Correcto
features/pacientes/    ❌ Incorrecto
```

### Componentes (PascalCase)
```typescript
PacienteCard.tsx       ✅
paciente-card.tsx      ❌
```

### Servicios (camelCase + .service)
```typescript
pacienteService.ts     ✅
PacienteService.ts     ❌
```

### Hooks (use + PascalCase)
```typescript
usePaciente.ts         ✅
pacienteHook.ts        ❌
```

### Types (camelCase + .types)
```typescript
paciente.types.ts      ✅
types.ts               ❌
```

---

## 📦 Path Aliases

El proyecto usa path aliases para imports más limpios:

### tsconfig.json
```json
{
  "compilerOptions": {
    "baseUrl": ".",
    "paths": {
      "@/*": ["./src/*"],
      "@core/*": ["./src/core/*"],
      "@features/*": ["./src/features/*"],
      "@shared/*": ["./src/shared/*"]
    }
  }
}
```

### Uso
```typescript
// ❌ Antes
import { Button } from '../../../../components/ui/button';
import { usePaciente } from '../../../hooks/usePaciente';

// ✅ Ahora
import { Button } from '@shared/components/ui/button';
import { usePaciente } from '@features/pacientes/hooks/usePaciente';
```

---

## 🗂️ Estructura de una Feature

Cada feature sigue esta estructura estándar:

```
features/paciente/
├── components/           # Componentes específicos
│   ├── PacienteCard.tsx
│   └── PacienteTable.tsx
│
├── pages/                # Páginas (rutas)
│   ├── Pacientes.tsx
│   ├── PacienteDetalle.tsx
│   └── PacienteForm.tsx
│
├── hooks/                # Hooks personalizados
│   ├── usePacientes.ts
│   └── usePaciente.ts
│
├── services/             # Llamadas a API
│   └── pacienteService.ts
│
├── types/                # TypeScript interfaces
│   └── paciente.types.ts
│
├── index.ts              # Barrel export (opcional)
└── README.md             # Documentación del módulo
```

---

## 📤 Barrel Exports (index.ts)

Para simplificar imports, cada feature puede tener un `index.ts`:

```typescript
// features/paciente/index.ts
export * from './components/PacienteCard';
export * from './components/PacienteTable';
export * from './hooks/usePacientes';
export * from './hooks/usePaciente';
export * from './services/pacienteService';
export * from './types/paciente.types';
```

**Uso:**
```typescript
// En lugar de múltiples imports
import { PacienteCard } from '@features/pacientes/components/PacienteCard';
import { usePaciente } from '@features/pacientes/hooks/usePaciente';
import type { Paciente } from '@features/pacientes/types/paciente.types';

// Un solo import
import { 
  PacienteCard, 
  usePaciente, 
  type Paciente 
} from '@features/pacientes';
```

---

## 🔄 Migrando Código Existente

### Paso 1: Identificar la Feature
¿El archivo pertenece a una feature específica (pacientes, citas, etc.) o es compartido?

### Paso 2: Mover a la Ubicación Correcta
- **Feature específica** → `features/{nombre}/`
- **Compartido** → `shared/`
- **Core** → `core/`

### Paso 3: Actualizar Imports
Usa los nuevos path aliases:

```typescript
// Antes
import { AuthContext } from '../../contexts/AuthContext';

// Después
import { AuthContext } from '@core/auth/AuthContext';
```

### Paso 4: Probar
```bash
npm run dev
npm run build
```

---

## 📚 Documentación

### Por Feature
Cada feature tiene su propio `README.md` que explica:
- Qué hace el módulo
- Estructura de archivos
- Componentes disponibles
- Hooks y servicios
- Permisos necesarios
- Ejemplos de uso

### General
La documentación general está en `docs/`:
- **architecture/**: Decisiones arquitectónicas
- **api/**: Documentación de API
- **guides/**: Guías de uso
- **development/**: Para contributors

---

## 🎯 Reglas de Dependencias

### ✅ Permitido
```
features/paciente → shared/components
features/paciente → core/api
features/paciente → core/auth
shared/components → shared/utils
```

### ❌ No permitido
```
shared → features         (shared no depende de features)
features/paciente → features/cita  (features no dependen entre sí)
core/types → features     (core no depende de features)
```

**Regla de oro:** Las dependencias fluyen hacia adentro (core ← features ← pages)

---

## 🚀 Comandos Actualizados

### Desarrollo
```bash
# Frontend
cd apps/frontend
npm run dev

# Backend
cd apps/backend
mvn spring-boot:run

# Todo (con scripts)
scripts/dev/start-all.bat
```

### Build
```bash
cd apps/frontend
npm run build

cd apps/backend
mvn clean package
```

### Tests
```bash
# Frontend
cd apps/frontend
npm test

# Backend
cd apps/backend
mvn test
```

---

## 🐛 Problemas Comunes

### Error: Cannot find module '@/...'
**Solución:** Reinicia el servidor de desarrollo
```bash
npm run dev
```

### Error: Path alias no funciona
**Solución:** Verifica `tsconfig.json` tenga los paths correctos

### Imports rotos después de mover archivos
**Solución:** Usa el refactor de VS Code (F2) o buscar/reemplazar

---

## 💡 Mejores Prácticas

### 1. Un Componente = Un Archivo
```typescript
// ✅ Bueno
PacienteCard.tsx

// ❌ Malo
components.tsx (con múltiples componentes)
```

### 2. Coloca Código Compartido en `shared`
Si un componente se usa en 2+ features → `shared/`

### 3. Documenta tus Features
Crea/actualiza el `README.md` de la feature

### 4. Usa TypeScript
Define types claros en `types/`

### 5. Mantén Features Independientes
No importes de otras features

---

## 📖 Recursos

- [Feature-Sliced Design](https://feature-sliced.design/)
- [React Folder Structure](https://reactjs.org/docs/faq-structure.html)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)

---

## 🤝 Contribuyendo

Al agregar nuevas features:

1. Crea la estructura estándar
2. Agrega un `README.md`
3. Usa path aliases
4. Sigue las convenciones de nomenclatura
5. Documenta los componentes principales

---

**¿Preguntas?** Abre un issue o contacta al equipo.

---

**Última actualización:** Noviembre 2025  
**Versión:** 2.0.0
