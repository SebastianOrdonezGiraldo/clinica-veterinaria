# Guía de Contribución

¡Gracias por tu interés en contribuir a Clínica Veterinaria! Esta guía te ayudará a configurar tu entorno y entender nuestros estándares de desarrollo.

## 📋 Tabla de Contenidos

- [Código de Conducta](#código-de-conducta)
- [Cómo Contribuir](#cómo-contribuir)
- [Configuración del Entorno](#configuración-del-entorno)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Estándares de Código](#estándares-de-código)
- [Testing](#testing)
- [Proceso de Pull Request](#proceso-de-pull-request)
- [Commits Convencionales](#commits-convencionales)

---

## 🤝 Código de Conducta

Este proyecto sigue un código de conducta que promueve un ambiente respetuoso y colaborativo. Por favor, lee y adhiérete a estas normas:

- Trata a todos con respeto y profesionalismo
- Acepta críticas constructivas con mente abierta
- Enfócate en lo que es mejor para la comunidad
- Muestra empatía hacia otros contribuidores

---

## 🚀 Cómo Contribuir

### 1. Fork y Clone

```bash
# Fork el repositorio desde GitHub
# Luego clona tu fork
git clone https://github.com/TU_USUARIO/clinica-veterinaria.git
cd clinica-veterinaria
```

### 2. Crear Branch

```bash
# Crea un branch descriptivo
git checkout -b feature/nombre-de-la-funcionalidad
# o
git checkout -b fix/descripcion-del-bug
```

### 3. Configurar el Entorno

```bash
# Backend (Java 17 + Spring Boot)
cd apps/backend
./mvnw clean install

# Frontend (React + TypeScript + Vite)
cd apps/frontend
npm install
```

### 4. Hacer Cambios

- Sigue los estándares de código descritos más adelante
- Escribe tests para nuevas funcionalidades
- Actualiza la documentación si es necesario

### 5. Testing

```bash
# Frontend
cd apps/frontend
npm run lint        # Verificar linting
npm run build       # Verificar build

# Backend
cd apps/backend
./mvnw test
```

### 6. Commit y Push

```bash
git add .
git commit -m "feat(scope): descripción del cambio"
git push origin feature/nombre-de-la-funcionalidad
```

### 7. Pull Request

- Abre un PR desde tu branch hacia `main`
- Describe claramente los cambios realizados
- Referencia issues relacionados si aplica

---

## 💻 Configuración del Entorno

### Requisitos

- **Node.js**: v18 o superior
- **Java**: JDK 17
- **Maven**: 3.9+
- **Git**: 2.30+

### Variables de Entorno

Copia los archivos de ejemplo y configura las variables:

```bash
# Frontend
cp apps/frontend/env.example apps/frontend/.env.local

# Backend
cp apps/backend/src/main/resources/application.example.properties \
   apps/backend/src/main/resources/application-local.properties
```

---

## 📁 Estructura del Proyecto

```
clinica-veterinaria/
├── apps/
│   ├── frontend/                 # React + TypeScript + Vite
│   │   ├── src/
│   │   │   ├── core/             # Configuración central
│   │   │   │   ├── api/          # Cliente HTTP (axios)
│   │   │   │   ├── auth/         # Autenticación
│   │   │   │   ├── logging/      # Sistema de logs
│   │   │   │   └── types/        # Tipos globales
│   │   │   ├── features/         # Módulos de funcionalidad
│   │   │   │   ├── pacientes/
│   │   │   │   ├── propietarios/
│   │   │   │   ├── consultas/
│   │   │   │   └── ...
│   │   │   └── shared/           # Código compartido
│   │   │       ├── components/   # UI components
│   │   │       ├── hooks/        # Custom hooks
│   │   │       └── utils/        # Utilidades
│   │   └── ...
│   └── backend/                  # Java + Spring Boot
├── docs/                         # Documentación
├── scripts/                      # Scripts de utilidad
└── .storybook/                   # Configuración Storybook
```

---

## 📝 Estándares de Código

### TypeScript

```typescript
// ✅ Usar tipos explícitos
function calcularEdad(fechaNacimiento: Date): number {
  // ...
}

// ❌ Evitar 'any'
function procesarDatos(data: any) {  // Malo
  // ...
}

// ✅ Interfaces para objetos
interface Paciente {
  id: string;
  nombre: string;
  especie: string;
  propietarioId: string;
}

// ✅ Types para uniones y tipos simples
type Estado = 'activo' | 'inactivo' | 'suspendido';
```

### React

```tsx
// ✅ Componentes funcionales
function PacienteCard({ paciente }: PacienteCardProps) {
  return <Card>...</Card>;
}

// ✅ Props con destructuring
function Button({ children, variant = 'default', ...props }: ButtonProps) {
  return <button {...props}>{children}</button>;
}

// ✅ Hooks personalizados para lógica reutilizable
function usePacientes() {
  const { data, isLoading } = useQuery({...});
  return { pacientes: data, isLoading };
}
```

### Convenciones de Nomenclatura

| Tipo | Convención | Ejemplo |
|------|------------|---------|
| Componentes | PascalCase | `PacienteCard.tsx` |
| Hooks | camelCase con 'use' | `usePacientes.ts` |
| Funciones | camelCase | `calcularEdad()` |
| Variables | camelCase | `const pacienteActual` |
| Constantes | UPPER_SNAKE_CASE | `const API_BASE_URL` |
| Interfaces | PascalCase con 'I' o sin prefijo | `Paciente` o `IPaciente` |
| Types | PascalCase | `EstadoCita` |
| Archivos componentes | PascalCase | `PacienteCard.tsx` |
| Archivos utilidades | camelCase | `formatDate.ts` |

### JSDoc

Todos los componentes y hooks públicos deben tener documentación JSDoc:

```typescript
/**
 * Componente para mostrar información de un paciente.
 *
 * Incluye datos básicos como nombre, especie, raza y propietario.
 *
 * @component
 *
 * @param {PacienteCardProps} props - Propiedades del componente
 * @param {Paciente} props.paciente - Datos del paciente
 * @param {Function} props.onDelete - Callback al eliminar
 *
 * @returns {JSX.Element} Tarjeta de paciente
 *
 * @example
 * ```tsx
 * <PacienteCard
 *   paciente={paciente}
 *   onDelete={(id) => handleDelete(id)}
 * />
 * ```
 */
```

---

## 🎨 ESLint y Prettier

### Ejecutar Linting

```bash
cd apps/frontend
npm run lint
```

### Configuración

El proyecto usa ESLint con las siguientes reglas principales:

- `@typescript-eslint/no-explicit-any`: Error - evitar uso de `any`
- `react-hooks/rules-of-hooks`: Error - reglas de hooks
- `react-hooks/exhaustive-deps`: Warning - dependencias de useEffect

---

## 🧪 Testing

### Frontend

```bash
# El proyecto usa Vitest (a implementar)
npm run test

# Watch mode
npm run test:watch

# Coverage
npm run test:coverage
```

### Guías de Testing

1. **Unit Tests**: Para funciones y hooks aislados
2. **Component Tests**: Para componentes React
3. **Integration Tests**: Para flujos completos

```typescript
// Ejemplo de test de componente
describe('PacienteCard', () => {
  it('muestra el nombre del paciente', () => {
    render(<PacienteCard paciente={mockPaciente} onDelete={vi.fn()} />);
    expect(screen.getByText('Max')).toBeInTheDocument();
  });
});
```

---

## 🔄 Proceso de Pull Request

1. **Título descriptivo**: Usa prefijos como `feat:`, `fix:`, `docs:`
2. **Descripción completa**: Explica qué, por qué y cómo
3. **Referencias**: Menciona issues relacionados (#123)
4. **Tests**: Asegúrate de que todos los tests pasen
5. **Review**: Espera al menos 1 aprobación

### Template de PR

```markdown
## Descripción
[Descripción clara del cambio]

## Tipo de Cambio
- [ ] Bug fix
- [ ] Nueva funcionalidad
- [ ] Breaking change
- [ ] Documentación

## Testing
- [ ] Tests unitarios
- [ ] Tests de integración
- [ ] Verificación manual

## Checklist
- [ ] El código sigue los estándares del proyecto
- [ ] La documentación está actualizada
- [ ] Los tests pasan localmente
```

---

## 📦 Commits Convencionales

Usamos [Conventional Commits](https://www.conventionalcommits.org/) para mensajes de commit claros y generación automática de changelogs.

### Formato

```
<tipo>(<scope>): <descripción>

[cuerpo opcional]

[footer opcional]
```

### Tipos

| Tipo | Descripción |
|------|-------------|
| `feat` | Nueva funcionalidad |
| `fix` | Corrección de bug |
| `docs` | Solo documentación |
| `style` | Formato (no afecta lógica) |
| `refactor` | Refactorización de código |
| `test` | Agregar o modificar tests |
| `chore` | Tareas de mantenimiento |
| `perf` | Mejoras de rendimiento |

### Ejemplos

```bash
# Nueva funcionalidad
git commit -m "feat(pacientes): agregar búsqueda por especie"

# Bug fix
git commit -m "fix(auth): corregir validación de token expirado"

# Documentación
git commit -m "docs(readme): actualizar instrucciones de instalación"

# Refactorización
git commit -m "refactor(hooks): extraer lógica de paginación a hook separado"
```

### Scopes Comunes

- `auth`, `api`, `logging` - Core
- `pacientes`, `propietarios`, `consultas`, `citas` - Features
- `ui`, `hooks`, `utils` - Shared
- `tests`, `docs`, `config` - Infraestructura

---

## ❓ Preguntas

Si tienes dudas, puedes:

1. Revisar la documentación en `/docs`
2. Buscar issues similares
3. Crear un issue con la etiqueta `question`

¡Gracias por contribuir! 🐾
