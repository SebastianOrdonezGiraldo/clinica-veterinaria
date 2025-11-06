# 🚀 INICIO RÁPIDO - Clínica Veterinaria

## ⚡ Solución Rápida al Error de Vite

Si ves el error: `'vite' is not recognized as an internal or external command`

**Causa:** Las dependencias no están instaladas en `apps/frontend/`

---

## ✅ SOLUCIÓN (3 pasos):

### **Paso 1: Instalar Dependencias**

```bash
cd apps\frontend
npm install
```

Espera a que termine (puede tomar 1-2 minutos).

### **Paso 2: Iniciar Frontend**

```bash
npm run dev
```

### **Paso 3: Abrir Navegador**

Abre: **http://localhost:5173**

---

## 🎯 ALTERNATIVA: Usar Script Automatizado

Desde la raíz del proyecto:

```bash
scripts\dev\start-frontend.bat
```

Este script:
- ✅ Verifica dependencias
- ✅ Las instala si no existen
- ✅ Inicia el servidor automáticamente

---

## 📋 COMANDOS ÚTILES

### Iniciar Solo Frontend

```bash
# Opción 1: Desde raíz
scripts\dev\start-frontend.bat

# Opción 2: Manual
cd apps\frontend
npm install  # Solo primera vez
npm run dev
```

### Iniciar Solo Backend

```bash
# Opción 1: Desde raíz
scripts\dev\start-backend.bat

# Opción 2: Manual
cd apps\backend
mvn spring-boot:run
```

### Iniciar Todo el Sistema

```bash
scripts\dev\start-all.bat
```

---

## 🔍 VERIFICACIÓN

### Frontend está corriendo si ves:

```
  VITE v5.4.19  ready in XXX ms

  ➜  Local:   http://localhost:5173/
  ➜  Network: use --host to expose
  ➜  press h + enter to show help
```

### Backend está corriendo si ves:

```
Started VeterinariaApplication in X.XXX seconds
Tomcat started on port(s): 8080
```

---

## ⚠️ PROBLEMAS COMUNES

### 1. "vite is not recognized"

**Solución:**
```bash
cd apps\frontend
rm -rf node_modules package-lock.json
npm install
npm run dev
```

### 2. "Cannot find module"

**Solución:**
```bash
cd apps\frontend
npm install
npm run dev
```

### 3. "Port 5173 already in use"

**Solución:**
- Detén otro proceso de Vite (Ctrl+C)
- O cambia el puerto en `vite.config.ts`

### 4. "Backend no conecta"

**Verifica:**
- Backend está corriendo en http://localhost:8080
- PostgreSQL está iniciado
- Base de datos existe

---

## 📚 DOCUMENTACIÓN

### Guías Completas:
- **[README.md](README.md)** - Documentación principal
- **[REORGANIZACION-COMPLETADA.md](REORGANIZACION-COMPLETADA.md)** - Resumen de cambios
- **[apps/frontend/README.md](apps/frontend/README.md)** - Frontend específico
- **[docs/guides/NUEVA-ESTRUCTURA.md](docs/guides/NUEVA-ESTRUCTURA.md)** - Guía de estructura

---

## 🎯 CHECKLIST PRIMERA VEZ

- [ ] Instalar dependencias frontend: `cd apps\frontend && npm install`
- [ ] Iniciar frontend: `npm run dev`
- [ ] Verificar: http://localhost:5173 carga
- [ ] Instalar dependencias backend (si no está): `cd apps\backend && mvn clean install`
- [ ] Iniciar backend: `mvn spring-boot:run`
- [ ] Verificar: http://localhost:8080 responde
- [ ] Verificar PostgreSQL está corriendo
- [ ] Probar login con: `admin@clinica.com` / `admin123`

---

## 🆘 ¿SIGUE SIN FUNCIONAR?

### 1. Reinstalar Dependencias

```bash
cd apps\frontend
rmdir /s /q node_modules
del package-lock.json
npm install
npm run dev
```

### 2. Verificar Node.js

```bash
node --version  # Debe ser v18 o superior
npm --version   # Debe ser v9 o superior
```

### 3. Limpiar Caché

```bash
cd apps\frontend
npm cache clean --force
npm install
npm run dev
```

---

## ✅ TODO FUNCIONA SI VES:

### Frontend:
```
✓ Vite server running
✓ http://localhost:5173/
✓ Página de login carga
```

### Backend:
```
✓ Spring Boot started
✓ http://localhost:8080
✓ Swagger UI accesible
```

---

## 🎉 ¡LISTO!

Una vez que ambos estén corriendo:

1. Abre: http://localhost:5173
2. Login: `admin@clinica.com` / `admin123`
3. ¡Empieza a desarrollar! 🚀

---

**Creado:** Noviembre 6, 2025  
**Propósito:** Solución rápida para iniciar el proyecto  
**Tiempo:** 3 minutos

