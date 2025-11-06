# 🐾 Feature: Pacientes

Módulo de gestión completa de pacientes (mascotas) de la clínica veterinaria.

## 📋 Descripción

Este módulo permite registrar, consultar, actualizar y eliminar información de los pacientes (mascotas) que atiende la clínica. Incluye datos básicos, relación con propietarios y acceso a su historial clínico completo.

## 🗂️ Estructura

```
pacientes/
├── components/         # Componentes específicos
├── pages/              # Páginas del módulo
│   ├── Pacientes.tsx          # Lista principal
│   ├── PacienteDetalle.tsx    # Vista detallada
│   └── PacienteForm.tsx       # Formulario crear/editar
├── hooks/              # Hooks personalizados
├── services/           # Servicios API
│   └── pacienteService.ts
├── types/              # TypeScript types
└── README.md           # Esta documentación
```

## 📄 Páginas

### Pacientes.tsx (Lista)
**Ruta:** `/pacientes`

- **Funcionalidad:**
  - Lista paginada de todos los pacientes
  - Búsqueda por nombre
  - Filtro por especie
  - Ordenamiento
  - Acceso rápido a detalles

- **Acceso:** Todos los roles autenticados

### PacienteDetalle.tsx
**Ruta:** `/pacientes/:id`

- **Funcionalidad:**
  - Vista completa de información del paciente
  - Datos del propietario
  - Historial de citas
  - Historial de consultas
  - Acciones: Editar, Eliminar (ADMIN), Nueva cita

- **Acceso:** Todos los roles autenticados

### PacienteForm.tsx
**Rutas:** 
- `/pacientes/nuevo` (crear)
- `/pacientes/:id/editar` (editar)

- **Funcionalidad:**
  - Formulario completo validado
  - Campos: nombre, especie, raza, sexo, edad, peso, microchip, notas
  - Selección de propietario
  - Validaciones con Zod

- **Acceso:** ADMIN, VET, RECEPCION

## 🎣 Hooks

### usePacientes()
```typescript
const { data, isLoading, error, refetch } = usePacientes();
```
Obtiene la lista completa de pacientes.

### usePaciente(id)
```typescript
const { data, isLoading, error } = usePaciente(pacienteId);
```
Obtiene detalles de un paciente específico.

### usePacientesByPropietario(propietarioId)
```typescript
const { data } = usePacientesByPropietario(propietarioId);
```
Obtiene pacientes de un propietario.

## 🔌 Servicios API

### pacienteService.ts

```typescript
// Obtener todos
await pacienteService.getAll();

// Obtener por ID
await pacienteService.getById(id);

// Buscar por nombre
await pacienteService.search(nombre);

// Por propietario
await pacienteService.getByPropietario(propietarioId);

// Crear
await pacienteService.create(data);

// Actualizar
await pacienteService.update(id, data);

// Eliminar
await pacienteService.delete(id);
```

## 📦 Types

### Paciente
```typescript
interface Paciente {
  id: string;
  nombre: string;
  especie: string;
  raza?: string;
  sexo?: 'M' | 'F';
  edadMeses?: number;
  pesoKg?: number;
  propietarioId: string;
  microchip?: string;
  observaciones?: string;
  propietario?: Propietario;
}
```

## 🎨 Componentes

### PacienteCard
Tarjeta de resumen de paciente.

```typescript
<PacienteCard paciente={paciente} onClick={() => navigate(`/pacientes/${id}`)} />
```

### PacienteTable
Tabla con funcionalidades avanzadas.

```typescript
<PacienteTable 
  pacientes={pacientes} 
  onEdit={handleEdit}
  onDelete={handleDelete}
  onView={handleView}
/>
```

## 🔒 Permisos

| Acción | ADMIN | VET | RECEPCION | ESTUDIANTE |
|--------|-------|-----|-----------|------------|
| Ver lista | ✅ | ✅ | ✅ | ✅ |
| Ver detalle | ✅ | ✅ | ✅ | ✅ |
| Crear | ✅ | ✅ | ✅ | ❌ |
| Editar | ✅ | ✅ | ✅ | ❌ |
| Eliminar | ✅ | ❌ | ❌ | ❌ |

## 🔄 Flujo de Uso

1. Usuario accede a `/pacientes`
2. Ve lista de pacientes con opciones de búsqueda
3. Puede:
   - Hacer clic en un paciente para ver detalles
   - Usar botón "Nuevo Paciente" (si tiene permisos)
   - Buscar por nombre o filtrar por especie
4. En detalle puede ver toda la información y acceder a:
   - Historial de citas
   - Historial clínico
   - Editar información
   - Agendar nueva cita

## 🧪 Testing

```bash
# Tests unitarios
npm test pacientes

# Tests E2E
npm run test:e2e -- features/pacientes
```

## 📝 Mejoras Futuras

- [ ] Subida de fotos de pacientes
- [ ] Gráficos de evolución de peso
- [ ] Alertas de vacunación
- [ ] Exportar historial a PDF
- [ ] Línea de tiempo visual de eventos médicos

---

**Última actualización:** Noviembre 2025
