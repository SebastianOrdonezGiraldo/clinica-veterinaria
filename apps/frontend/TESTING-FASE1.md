# 🧪 Pruebas de la Fase 1 - Mejoras de Frontend

## ✅ Checklist de Pruebas

### 1. Lazy Loading de Rutas

#### Prueba 1.1: Verificar que las rutas lazy-loaded se cargan correctamente
- [ ] Abrir DevTools → Network tab
- [ ] Navegar a `/dashboard` (no debe estar en el bundle inicial)
- [ ] Verificar que se carga un chunk separado (ej: `Dashboard-xxx.js`)
- [ ] Verificar que aparece el `PageLoader` brevemente antes de cargar
- [ ] Repetir para otras rutas: `/pacientes`, `/agenda`, `/reportes`

#### Prueba 1.2: Verificar que las rutas críticas NO son lazy
- [ ] Verificar que `/login` y `/` (LandingPage) están en el bundle inicial
- [ ] No deben aparecer como chunks separados

#### Prueba 1.3: Verificar tamaño del bundle
- [ ] Ejecutar `npm run build`
- [ ] Verificar que el bundle principal (`index-xxx.js`) es más pequeño
- [ ] Verificar que hay múltiples chunks pequeños en lugar de uno grande

**Resultado esperado:**
- Bundle inicial reducido en ~30-40%
- Chunks separados para cada ruta lazy-loaded
- PageLoader aparece durante la carga

---

### 2. Mejoras de Accesibilidad

#### Prueba 2.1: ARIA Labels en Login
- [ ] Abrir `/login`
- [ ] Abrir DevTools → Elements → Inspeccionar el formulario
- [ ] Verificar que el form tiene `aria-labelledby="login-title"`
- [ ] Verificar que inputs tienen `aria-required="true"`
- [ ] Verificar que inputs tienen `aria-describedby` con descripciones

#### Prueba 2.2: Navegación por Teclado
- [ ] En `/login`, usar solo el teclado (Tab, Enter, Space)
- [ ] Verificar que se puede navegar entre campos con Tab
- [ ] Verificar que Enter envía el formulario
- [ ] Verificar que los iconos tienen `aria-hidden="true"`

#### Prueba 2.3: Screen Reader
- [ ] Activar un screen reader (NVDA, JAWS, o VoiceOver)
- [ ] Navegar por `/login` y verificar que anuncia correctamente:
  - "Página de inicio de sesión"
  - "Correo Electrónico, campo requerido"
  - "Contraseña, campo requerido"
  - "Ingrese su dirección de correo electrónico" (descripción)

#### Prueba 2.4: Sidebar Navigation
- [ ] Verificar que los enlaces del sidebar tienen `aria-label`
- [ ] Verificar que el enlace activo tiene `aria-current="page"`

**Resultado esperado:**
- Todos los elementos interactivos tienen ARIA labels
- Navegación por teclado funciona correctamente
- Screen reader anuncia correctamente los elementos

---

### 3. Memoización de Componentes

#### Prueba 3.1: Verificar que PacienteCard está memoizado
- [ ] Abrir `/pacientes`
- [ ] Abrir React DevTools → Components
- [ ] Seleccionar un `PacienteCard`
- [ ] Verificar que tiene `memo` en el nombre del componente
- [ ] Cambiar un filtro (ej: especie)
- [ ] Verificar en Profiler que solo se re-renderizan las cards que cambiaron

#### Prueba 3.2: Performance con muchos pacientes
- [ ] Crear o tener al menos 20+ pacientes
- [ ] Abrir `/pacientes`
- [ ] Abrir React DevTools → Profiler
- [ ] Grabar una interacción (cambiar filtro, buscar)
- [ ] Verificar que no todos los `PacienteCard` se re-renderizan
- [ ] Comparar tiempo de render antes/después (si es posible)

**Resultado esperado:**
- Solo los componentes que cambiaron se re-renderizan
- Mejor performance con listas grandes
- Menos trabajo de React

---

### 4. Sanitización de Inputs

#### Prueba 4.1: Sanitización en PacienteForm
- [ ] Ir a `/pacientes/nuevo`
- [ ] En el campo "Nombre", ingresar: `<script>alert('XSS')</script>Test`
- [ ] En el campo "Notas", ingresar: `<img src=x onerror=alert('XSS')>`
- [ ] Guardar el paciente
- [ ] Verificar en la consola del navegador que NO se ejecuta el script
- [ ] Verificar en la base de datos que el HTML fue eliminado
- [ ] Verificar que solo se guardó el texto seguro

#### Prueba 4.2: Sanitización en PropietarioForm
- [ ] Ir a `/propietarios/nuevo`
- [ ] En "Nombre", ingresar: `<b>Juan</b> <script>alert('XSS')</script>`
- [ ] En "Dirección", ingresar: `Calle 123 <iframe src="evil.com"></iframe>`
- [ ] Guardar
- [ ] Verificar que el script no se ejecuta
- [ ] Verificar que solo se guardó texto limpio

#### Prueba 4.3: Sanitización en CitaForm
- [ ] Ir a `/agenda/nuevo`
- [ ] En "Motivo", ingresar: `<script>alert('XSS')</script>Consulta`
- [ ] En "Observaciones", ingresar: `<img src=x onerror=alert('XSS')>`
- [ ] Guardar
- [ ] Verificar que no se ejecuta código malicioso

#### Prueba 4.4: Verificar que texto normal funciona
- [ ] En cualquier formulario, ingresar texto normal
- [ ] Verificar que se guarda correctamente sin cambios
- [ ] Verificar que caracteres especiales normales (ñ, acentos) se preservan

**Resultado esperado:**
- Scripts y HTML malicioso son eliminados
- Texto normal se preserva intacto
- No se ejecuta código JavaScript malicioso
- Datos en BD están limpios

---

### 5. Pruebas de Integración

#### Prueba 5.1: Flujo completo de creación de paciente
1. [ ] Ir a `/pacientes/nuevo`
2. [ ] Llenar formulario con datos normales
3. [ ] Verificar que PageLoader aparece brevemente (si aplica)
4. [ ] Verificar que se guarda correctamente
5. [ ] Verificar que aparece en la lista
6. [ ] Verificar que PacienteCard se renderiza correctamente

#### Prueba 5.2: Navegación entre rutas lazy-loaded
1. [ ] Empezar en `/dashboard`
2. [ ] Navegar a `/pacientes` → Verificar carga de chunk
3. [ ] Navegar a `/agenda` → Verificar carga de chunk
4. [ ] Volver a `/pacientes` → NO debe cargar chunk (ya está en cache)
5. [ ] Verificar que la navegación es fluida

#### Prueba 5.3: Login con accesibilidad
1. [ ] Ir a `/login`
2. [ ] Usar solo teclado para completar el formulario
3. [ ] Verificar que se puede enviar con Enter
4. [ ] Verificar que los mensajes de error son accesibles

---

## 🔍 Verificaciones Técnicas

### Verificar en el código:

```bash
# 1. Verificar que lazy loading está implementado
grep -r "React.lazy\|lazy(" apps/frontend/src/App.tsx

# 2. Verificar que Suspense está usado
grep -r "Suspense" apps/frontend/src/App.tsx

# 3. Verificar memoización
grep -r "React.memo\|memo(" apps/frontend/src/features/pacientes/components/

# 4. Verificar sanitización
grep -r "sanitize" apps/frontend/src/features/*/pages/*Form.tsx
```

### Verificar en el navegador:

1. **Network Tab:**
   - Abrir DevTools → Network
   - Filtrar por "JS"
   - Navegar entre páginas
   - Verificar chunks separados

2. **React DevTools:**
   - Instalar extensión React DevTools
   - Verificar componentes memoizados
   - Usar Profiler para medir renders

3. **Console:**
   - Verificar que no hay errores
   - Verificar que los logs del logger funcionan

---

## 📊 Métricas Esperadas

### Performance:
- **Bundle inicial:** Reducción de ~30-40%
- **Tiempo de carga inicial:** Mejora de ~20-30%
- **Re-renders:** Reducción de ~50% en listas grandes

### Accesibilidad:
- **ARIA labels:** 100% en formularios críticos
- **Navegación por teclado:** Funcional en todos los componentes
- **Screen reader:** Compatible con lectores principales

### Seguridad:
- **XSS prevention:** 100% de inputs sanitizados
- **Scripts bloqueados:** Todos los intentos de XSS bloqueados

---

## 🐛 Problemas Conocidos a Verificar

1. **Lazy Loading:**
   - [ ] Verificar que no hay errores de "Module not found"
   - [ ] Verificar que los chunks se cargan correctamente en producción

2. **Memoización:**
   - [ ] Verificar que la comparación personalizada funciona correctamente
   - [ ] Verificar que no causa problemas con actualizaciones

3. **Sanitización:**
   - [ ] Verificar que no elimina caracteres válidos (ñ, acentos)
   - [ ] Verificar que funciona en todos los navegadores

---

## ✅ Criterios de Aceptación

La Fase 1 se considera exitosa si:

- ✅ Todas las rutas lazy-loaded se cargan correctamente
- ✅ El bundle inicial es significativamente más pequeño
- ✅ Los formularios tienen ARIA labels completos
- ✅ La navegación por teclado funciona en todos los componentes
- ✅ Los componentes memoizados reducen re-renders
- ✅ La sanitización previene ataques XSS
- ✅ No hay errores en consola
- ✅ La aplicación funciona normalmente

---

## 📝 Notas de Prueba

**Fecha de prueba:** _______________
**Probado por:** _______________
**Navegador:** _______________
**Versión:** _______________

**Resultados:**
- [ ] Todas las pruebas pasaron
- [ ] Algunas pruebas fallaron (ver detalles abajo)
- [ ] Problemas encontrados:

_________________________________________________
_________________________________________________
_________________________________________________

