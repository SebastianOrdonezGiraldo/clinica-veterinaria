# 🔄 Instrucciones de Migración a Nueva Estructura

**Guía paso a paso para empezar a usar la nueva organización del proyecto**

---

## ⚠️ Importante: Lee Esto Primero

La reorganización YA ESTÁ COMPLETA. Los archivos han sido **copiados** (no movidos) a la nueva estructura, por lo que:

- ✅ La estructura antigua (`src/`, `backend/` en raíz, etc.) **sigue existiendo**
- ✅ La nueva estructura (`apps/`, `docs/`, `scripts/`) **ya está creada y funcional**
- ⚠️ Existen **archivos duplicados** temporalmente

### ¿Qué hacer?

**Opción 1: Empezar a usar la nueva estructura (Recomendado)**
- Usa los scripts en `scripts/dev/`
- Desarrolla en `apps/frontend/src/`
- Consulta docs en `docs/`

**Opción 2: Limpiar archivos antiguos (Después de probar)**
- Elimina carpetas antiguas cuando estés seguro
- Sigue las instrucciones en "Fase de Limpieza"

---

## 🚀 Inicio Rápido (5 minutos)

### 1. Verificar que Todo Funciona

```bash
# En la raíz del proyecto
cd C:\Users\sebas\clinica-veterinaria

# Verificar estructura nueva
dir apps
dir docs
dir scripts
```

### 2. Iniciar el Sistema con la Nueva Estructura

```bash
# Opción A: Usar script actualizado (Recomendado)
scripts\dev\start-all.bat

# Opción B: Manual
# Terminal 1
cd apps\backend
mvn spring-boot:run

# Terminal 2 (nueva ventana)
cd apps\frontend
npm install  # Solo primera vez
npm run dev
```

### 3. Verificar Acceso

- **Frontend:** http://localhost:5173
- **Backend:** http://localhost:8080
- **Swagger:** http://localhost:8080/swagger-ui.html

✅ **Si todo funciona, la migración está completa!**

---

## 📋 Plan de Migración Completo

### Fase 1: Exploración (1-2 días) ✅ HECHO

**Objetivo:** Familiarizarse con la nueva estructura

- [x] Revisar nueva estructura de carpetas
- [x] Leer documentación principal
- [x] Probar scripts actualizados
- [x] Explorar features organizadas

**Qué hacer:**

1. Lee los nuevos READMEs:
   ```bash
   # README principal
   type README.md
   
   # Frontend
   type apps\frontend\README.md
   
   # Guía de nueva estructura
   type docs\guides\NUEVA-ESTRUCTURA.md
   ```

2. Explora la estructura de features:
   ```bash
   cd apps\frontend\src\features
   dir
   # Verás: pacientes/, propietarios/, agenda/, etc.
   ```

3. Revisa un módulo completo:
   ```bash
   cd pacientes
   dir
   # components/, pages/, hooks/, services/, types/, README.md
   ```

---

### Fase 2: Trabajo en Nueva Estructura (Empezar ya)

**Objetivo:** Desarrollar usando la nueva organización

#### Para Desarrollo Frontend:

```bash
# Siempre trabaja en apps/frontend/
cd apps\frontend

# Instala dependencias (si no lo has hecho)
npm install

# Inicia desarrollo
npm run dev
```

#### Convenciones de Código:

```typescript
// ✅ Usa path aliases
import { Button } from '@shared/components/ui/button';
import { usePaciente } from '@features/pacientes/hooks/usePaciente';
import { AuthContext } from '@core/auth/AuthContext';

// ❌ Evita rutas relativas largas
import { Button } from '../../../shared/components/ui/button';
```

#### Agregar Nueva Funcionalidad:

1. **Identifica la feature:**
   - ¿Es específica de un módulo? → `features/{modulo}/`
   - ¿Es reutilizable? → `shared/`
   - ¿Es lógica central? → `core/`

2. **Crea el archivo en la ubicación correcta:**
   ```bash
   # Ejemplo: Nuevo componente de pacientes
   cd apps\frontend\src\features\pacientes\components
   # Crear NuevoComponente.tsx aquí
   ```

3. **Usa path aliases en imports:**
   ```typescript
   // En cualquier archivo
   import { NuevoComponente } from '@features/pacientes/components/NuevoComponente';
   ```

---

### Fase 3: Testing (1-2 semanas)

**Objetivo:** Asegurar que todo funciona correctamente

#### Checklist de Verificación:

- [ ] Frontend inicia sin errores
- [ ] Backend inicia sin errores
- [ ] Todos los módulos cargan correctamente
- [ ] Los path aliases funcionan
- [ ] Las rutas de React Router funcionan
- [ ] La autenticación funciona
- [ ] CRUD de pacientes funciona
- [ ] CRUD de propietarios funciona
- [ ] Sistema de citas funciona

#### Cómo Probar:

```bash
# Frontend
cd apps\frontend
npm run dev
# Abre http://localhost:5173 y navega por todas las secciones

# Backend
cd apps\backend
mvn spring-boot:run
mvn test  # Ejecutar tests
```

---

### Fase 4: Limpieza (Después de probar 1-2 semanas)

**⚠️ IMPORTANTE: Solo hazlo después de estar SEGURO que todo funciona**

#### Archivos/Carpetas a Eliminar (En Orden):

1. **Carpeta antigua de frontend en raíz:**
   ```bash
   # ⚠️ Hacer backup primero!
   # Eliminar SOLO DESPUÉS de verificar que apps/frontend funciona
   
   # Backup (opcional)
   xcopy src src_backup\ /E /I
   
   # Eliminar (después de backup)
   rmdir /s /q src
   rmdir /s /q public
   rmdir /s /q node_modules
   del package.json
   del package-lock.json
   del vite.config.ts
   del tailwind.config.ts
   del tsconfig.json
   del tsconfig.app.json
   del tsconfig.node.json
   del index.html
   del components.json
   del eslint.config.js
   del postcss.config.js
   ```

2. **Backend antiguo (si apps/backend funciona):**
   ```bash
   # ⚠️ Verificar que apps/backend funciona primero!
   rmdir /s /q backend
   ```

3. **Documentación antigua:**
   ```bash
   # Ya movida a docs/
   rmdir /s /q guias
   del GUIA_INICIO.md
   del POSTMAN_GUIDE.md
   del SOLUCION_FRONTEND.md
   ```

4. **Scripts antiguos:**
   ```bash
   # Ya movidos a scripts/
   del setup.bat
   del start-all.bat
   del start-backend.bat
   del start-frontend.bat
   ```

5. **Otros archivos de raíz:**
   ```bash
   # Verificar que no necesitas estos
   del bun.lockb  # Si no usas bun
   del Clinica_Veterinaria_API.postman_collection.json  # Ya en docs/api/
   ```

#### Resultado Final:

```
clinica-veterinaria/
├── apps/          # ✅ Aplicaciones
├── docs/          # ✅ Documentación
├── scripts/       # ✅ Scripts
├── docker/        # ✅ Docker (futuro)
├── .git/          # Git
├── .gitignore
├── README.md
└── LICENSE
```

---

## 🛠 Solución de Problemas Comunes

### Problema 1: "npm run dev no funciona"

**Solución:**
```bash
cd apps\frontend

# Reinstalar dependencias
rmdir /s /q node_modules
del package-lock.json
npm install

# Intentar de nuevo
npm run dev
```

---

### Problema 2: "Los imports con @ no funcionan"

**Causa:** Path aliases no configurados o servidor no reiniciado

**Solución:**
```bash
# 1. Verifica tsconfig.json
cd apps\frontend
type tsconfig.json
# Debe tener paths: { "@/*": ["./src/*"], ... }

# 2. Verifica vite.config.ts
type vite.config.ts
# Debe tener alias configurados

# 3. Reinicia el servidor
# Ctrl+C para detener
npm run dev
```

---

### Problema 3: "Backend no encuentra la base de datos"

**Solución:**
```bash
cd apps\backend

# Verifica configuración
type src\main\resources\application.properties
# Debe apuntar a tu PostgreSQL

# Si necesitas crear la BD:
# psql -U postgres
# CREATE DATABASE vetclinic_dev;
```

---

### Problema 4: "Página 404 en el frontend"

**Causa:** React Router no encuentra la ruta

**Solución:**
```typescript
// Verifica que la ruta esté en apps/frontend/src/App.tsx
// Y que el import sea correcto:
import Pacientes from './features/pacientes/pages/Pacientes';
```

---

### Problema 5: "Scripts .bat no funcionan"

**Solución:**
```bash
# Verifica que estás en la raíz del proyecto
cd C:\Users\sebas\clinica-veterinaria

# Ejecuta el script
scripts\dev\start-all.bat

# Si da error de "no se encuentra apps\backend":
# Asegúrate de ejecutar desde la raíz, no desde scripts/
```

---

## 📚 Recursos de Ayuda

### Documentación Principal
- **[README Principal](../README.md)** - Visión general
- **[Guía Nueva Estructura](guides/NUEVA-ESTRUCTURA.md)** - Detalles de organización
- **[Frontend README](../apps/frontend/README.md)** - Todo sobre frontend
- **[Resumen de Mejoras](RESUMEN-MEJORAS.md)** - Comparación antes/después

### Por Tarea
- **Agregar nueva feature:** Ver `docs/guides/NUEVA-ESTRUCTURA.md` sección "Estructura de una Feature"
- **Usar path aliases:** Ver `apps/frontend/README.md` sección "Path Aliases"
- **Configurar IDE:** Ver `.vscode/settings.json`
- **Problemas comunes:** Esta guía, sección "Solución de Problemas"

### Features Documentadas
- **[Pacientes](../apps/frontend/src/features/pacientes/README.md)** - Gestión de mascotas
- **Propietarios** - Por documentar
- **Agenda** - Por documentar

---

## ✅ Checklist de Migración Completa

### Pre-Migración
- [x] ✅ Estructura creada
- [x] ✅ Archivos copiados
- [x] ✅ Configuraciones actualizadas
- [x] ✅ Documentación creada
- [x] ✅ Scripts actualizados

### Tu Parte (Como Usuario)

#### Semana 1
- [ ] Leer documentación principal
- [ ] Probar scripts nuevos
- [ ] Iniciar frontend y backend con nueva estructura
- [ ] Explorar organización de features
- [ ] Identificar archivos/carpetas a eliminar

#### Semana 2
- [ ] Desarrollar nuevas funcionalidades en nueva estructura
- [ ] Documentar observaciones y problemas
- [ ] Completar testing de funcionalidades críticas
- [ ] Decidir si eliminar archivos antiguos

#### Semana 3-4
- [ ] Hacer backup de carpetas antiguas
- [ ] Eliminar carpetas/archivos antiguos (si todo funciona)
- [ ] Actualizar READMEs de features faltantes
- [ ] Capacitar al equipo (si aplica)

#### Opcional
- [ ] Implementar barrel exports (`index.ts`)
- [ ] Agregar tests por feature
- [ ] Dockerizar aplicación
- [ ] Setup CI/CD

---

## 🎯 Consejos Finales

### DO ✅

1. **Usa path aliases siempre**
   ```typescript
   import from '@features/...'
   import from '@shared/...'
   import from '@core/...'
   ```

2. **Mantén features autocontenidas**
   - Todo el código de pacientes en `features/pacientes/`
   - No importes entre features

3. **Documenta tu código**
   - Cada feature debe tener README.md
   - Comenta funciones complejas

4. **Sigue las convenciones**
   - PascalCase para componentes
   - camelCase para hooks y servicios
   - Singular para features

### DON'T ❌

1. **No uses rutas relativas largas**
   ```typescript
   // ❌ Evitar
   import from '../../../components/...'
   ```

2. **No mezcles código de features**
   ```typescript
   // ❌ Evitar
   import from '@features/citas/...' // Dentro de pacientes
   ```

3. **No pongas código compartido en features**
   - Si lo usas en 2+ features → `shared/`

4. **No elimines carpetas antiguas sin probar**
   - Prueba 1-2 semanas primero
   - Haz backup antes

---

## 🆘 ¿Necesitas Ayuda?

### Si algo no funciona:

1. **Consulta esta guía** - Sección "Solución de Problemas"
2. **Lee los READMEs** - Cada módulo tiene documentación
3. **Revisa la consola** - Los errores suelen ser descriptivos
4. **Verifica configuraciones** - `tsconfig.json`, `vite.config.ts`
5. **Reinstala dependencias** - `npm install`

### Canales de Soporte:

- 📖 Documentación: `docs/`
- 💬 Issues: GitHub Issues
- 📧 Email: [tu-email]
- 👥 Team: Slack/Discord

---

## 🎉 ¡Listo!

Si seguiste esta guía, tu proyecto ahora tiene:

✅ Estructura modular profesional  
✅ Documentación completa  
✅ Path aliases configurados  
✅ Scripts actualizados  
✅ Features organizadas  
✅ Código mantenible y escalable  

---

**¡Feliz desarrollo con la nueva estructura! 🚀**

---

**Autor:** Sebastian Ordoñez  
**Fecha:** Noviembre 6, 2025  
**Versión:** 2.0.0

