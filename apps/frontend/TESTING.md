# 🧪 Guía de Pruebas - Frontend Mejorado

## ✅ Verificaciones Completadas

### Build
- ✅ Compilación exitosa sin errores
- ✅ Todos los imports resueltos correctamente
- ✅ Sin errores de TypeScript

### Endpoints Backend
- ✅ `/api/pacientes/search` - Existe y funciona
- ✅ `/api/propietarios/search` - Existe y funciona
- ✅ `/api/dashboard/stats` - Existe y funciona

## 🧪 Pruebas a Realizar

### 1. Dashboard
**URL:** `http://localhost:5173/`

**Qué probar:**
- [ ] Carga inicial muestra skeleton loading
- [ ] Estadísticas se cargan correctamente
- [ ] Gráficos se renderizan
- [ ] Refetch automático cada 30 segundos funciona
- [ ] Navegación a otras secciones desde las cards funciona

**Comportamiento esperado:**
- Loading state con skeletons durante carga inicial
- Datos se muestran después de ~1-2 segundos
- Los datos se actualizan automáticamente cada 30s

### 2. Pacientes
**URL:** `http://localhost:5173/pacientes`

**Qué probar:**
- [ ] Lista de pacientes carga con paginación
- [ ] Búsqueda con debounce (espera 500ms)
- [ ] Filtro por especie funciona
- [ ] Ordenamiento funciona
- [ ] Paginación funciona correctamente
- [ ] Eliminar paciente muestra confirmación y actualiza lista
- [ ] Navegación a detalle/editar funciona

**Comportamiento esperado:**
- Loading cards durante carga inicial
- Búsqueda no dispara request hasta 500ms después de escribir
- Cache: al volver a la página, datos se cargan instantáneamente
- Después de eliminar, lista se actualiza automáticamente

### 3. Propietarios
**URL:** `http://localhost:5173/propietarios`

**Qué probar:**
- [ ] Lista carga con paginación
- [ ] Búsqueda funciona
- [ ] CRUD completo funciona
- [ ] Cache funciona correctamente

### 4. React Query Features

**Qué verificar:**
- [ ] Cache: Navegar entre páginas y volver, datos cargan instantáneamente
- [ ] Refetch: Cambiar datos en otra pestaña, volver y ver actualización
- [ ] Loading states: Aparecen durante carga inicial
- [ ] Error handling: Desconectar backend, ver mensaje de error apropiado

### 5. Hooks Personalizados

**Verificar en consola del navegador:**
- [ ] `usePacientes` - Query key: `['pacientes', params]`
- [ ] `usePropietarios` - Query key: `['propietarios', params]`
- [ ] `useDashboard` - Query key: `['dashboard', 'stats']`
- [ ] `useDebounce` - Funciona correctamente (500ms delay)

## 🐛 Problemas Conocidos a Verificar

### Posibles Issues

1. **Endpoint `/propietarios/search`**
   - Verificar que existe en backend
   - Si no existe, usar endpoint alternativo

2. **Normalización de IDs**
   - Verificar que IDs numéricos del backend se convierten a strings

3. **Cache de React Query**
   - Verificar que `staleTime` y `gcTime` funcionan correctamente

## 📊 Métricas de Rendimiento

**Antes (sin React Query):**
- Cada navegación = nueva request
- Sin cache
- Loading states manuales

**Después (con React Query):**
- Cache automático
- Refetch inteligente
- Loading states automáticos
- Menos código

## 🔍 Debugging

### React Query DevTools (Opcional)
Para ver el estado de las queries en tiempo real, instalar:
```bash
npm install @tanstack/react-query-devtools
```

Luego agregar en `App.tsx`:
```tsx
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';

// Dentro del QueryClientProvider
<ReactQueryDevtools initialIsOpen={false} />
```

## ✅ Checklist Final

- [ ] Frontend compila sin errores
- [ ] Backend está corriendo en puerto 8080
- [ ] Frontend está corriendo en puerto 5173
- [ ] Login funciona correctamente
- [ ] Dashboard carga y muestra datos
- [ ] Pacientes lista funciona con paginación
- [ ] Búsqueda funciona con debounce
- [ ] Cache funciona (navegar y volver)
- [ ] Mutaciones (crear/editar/eliminar) funcionan
- [ ] Error handling muestra mensajes apropiados

