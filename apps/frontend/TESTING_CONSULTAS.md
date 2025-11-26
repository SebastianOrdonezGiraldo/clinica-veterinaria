# 🧪 Guía de Pruebas - Módulo de Consultas desde Citas

Esta guía te ayudará a probar el nuevo módulo de consultas que permite a los veterinarios crear consultas directamente desde las citas.

## 📋 Checklist de Pruebas

### 1. Preparación del Entorno

- [ ] Backend ejecutándose en `http://localhost:8080`
- [ ] Frontend ejecutándose en `http://localhost:5173`
- [ ] Usuario con rol **VET** o **ADMIN** autenticado
- [ ] Al menos una cita creada en el sistema (estado: PENDIENTE o CONFIRMADA)

### 2. Pruebas de Navegación

#### 2.1 Acceso desde Agenda
- [ ] Ir a `/agenda`
- [ ] Seleccionar una cita del día (estado PENDIENTE o CONFIRMADA)
- [ ] Verificar que aparece el botón **"Iniciar Consulta"**
- [ ] Hacer clic en "Iniciar Consulta"
- [ ] Verificar que navega a `/agenda/:citaId/consulta`

#### 2.2 Verificación de Permisos
- [ ] Cerrar sesión
- [ ] Iniciar sesión con usuario **RECEPCION** o **ESTUDIANTE**
- [ ] Ir a `/agenda` y seleccionar una cita
- [ ] Verificar que **NO** aparece el botón "Iniciar Consulta"
- [ ] Intentar acceder directamente a `/agenda/:citaId/consulta`
- [ ] Verificar que redirige o muestra error de permisos

### 3. Pruebas del Formulario de Consulta

#### 3.1 Carga de Datos
- [ ] Verificar que se cargan los datos de la cita correctamente
- [ ] Verificar que el panel lateral muestra:
  - [ ] Información del paciente (nombre, especie, raza)
  - [ ] Información del propietario (nombre, teléfono, email)
  - [ ] Fecha y hora de la cita
- [ ] Verificar que el historial clínico se carga en el panel lateral

#### 3.2 Campos del Formulario
- [ ] **Signos Vitales:**
  - [ ] FC (lpm) - acepta números enteros
  - [ ] FR (rpm) - acepta números enteros
  - [ ] Temperatura (°C) - acepta decimales
  - [ ] Peso (kg) - acepta decimales
- [ ] **Examen Físico:** Textarea funcional
- [ ] **Diagnóstico:** Textarea funcional
- [ ] **Tratamiento:** Textarea funcional
- [ ] **Observaciones:** Textarea funcional

#### 3.3 Validaciones
- [ ] Intentar guardar sin completar ningún campo
- [ ] Verificar que muestra error: "Debes completar al menos un campo"
- [ ] Completar solo un campo (ej: diagnóstico)
- [ ] Verificar que permite guardar

### 4. Pruebas de Funcionalidad

#### 4.1 Crear Consulta
- [ ] Completar todos los campos del formulario
- [ ] Verificar checkbox "Marcar cita como completada"
- [ ] Hacer clic en "Guardar Consulta"
- [ ] Verificar que muestra toast de éxito
- [ ] Verificar que navega de vuelta a `/agenda/:citaId`
- [ ] Verificar que la cita cambió a estado COMPLETADA (si estaba marcado)

#### 4.2 Crear Consulta sin Completar Cita
- [ ] Completar formulario de consulta
- [ ] Desmarcar checkbox "Marcar cita como completada"
- [ ] Guardar consulta
- [ ] Verificar que la consulta se guarda correctamente
- [ ] Verificar que la cita mantiene su estado original

#### 4.3 Historial Clínico
- [ ] En el panel lateral, verificar que se muestran las últimas 5 consultas
- [ ] Hacer clic en una consulta del historial
- [ ] Verificar que navega a `/historias/:consultaId`
- [ ] Hacer clic en "Ver Historial Completo"
- [ ] Verificar que navega a `/historias/:pacienteId`

### 5. Pruebas de Estados de Cita

#### 5.1 Cita Pendiente
- [ ] Crear consulta desde cita PENDIENTE
- [ ] Verificar que funciona correctamente

#### 5.2 Cita Confirmada
- [ ] Crear consulta desde cita CONFIRMADA
- [ ] Verificar que funciona correctamente

#### 5.3 Cita Cancelada
- [ ] Intentar crear consulta desde cita CANCELADA
- [ ] Verificar que muestra error: "No se puede crear una consulta para una cita cancelada"

#### 5.4 Cita Completada
- [ ] Verificar que NO aparece el botón "Iniciar Consulta" en citas COMPLETADAS

### 6. Pruebas de Manejo de Errores

#### 6.1 Errores de Red
- [ ] Desconectar el backend
- [ ] Intentar guardar consulta
- [ ] Verificar que muestra mensaje de error apropiado

#### 6.2 Errores de Validación
- [ ] Intentar ingresar valores negativos en signos vitales
- [ ] Verificar que muestra error de validación

#### 6.3 Cita No Encontrada
- [ ] Intentar acceder a `/agenda/99999/consulta` (ID inexistente)
- [ ] Verificar que muestra mensaje de error apropiado

### 7. Pruebas de UI/UX

#### 7.1 Responsive Design
- [ ] Probar en pantalla grande (desktop)
- [ ] Verificar layout de 2 columnas (formulario + panel lateral)
- [ ] Probar en tablet
- [ ] Verificar que el layout se adapta correctamente
- [ ] Probar en móvil
- [ ] Verificar que el panel lateral se muestra debajo del formulario

#### 7.2 Navegación
- [ ] Hacer clic en botón "Volver" (flecha atrás)
- [ ] Verificar que regresa a `/agenda/:citaId`
- [ ] Hacer clic en "Cancelar"
- [ ] Verificar que regresa a `/agenda/:citaId` sin guardar

#### 7.3 Estados de Carga
- [ ] Verificar skeleton loaders mientras carga datos
- [ ] Verificar spinner mientras guarda consulta
- [ ] Verificar que los botones se deshabilitan durante la carga

## 🐛 Problemas Conocidos y Soluciones

### Problema: No aparece el botón "Iniciar Consulta"
**Solución:** Verificar que:
- El usuario tiene rol VET o ADMIN
- La cita no está cancelada o completada
- El backend está funcionando

### Problema: Error al cargar datos del paciente
**Solución:** Verificar que:
- El paciente existe en el sistema
- El usuario tiene permisos para ver el paciente
- El backend está funcionando

### Problema: No se guarda la consulta
**Solución:** Verificar que:
- Se completó al menos un campo
- El backend está funcionando
- No hay errores en la consola del navegador

## 📝 Notas de Prueba

### Datos de Prueba Sugeridos

**Signos Vitales:**
- FC: 120 lpm
- FR: 30 rpm
- Temperatura: 38.5 °C
- Peso: 15.5 kg

**Examen Físico:**
```
Paciente en buen estado general, alerta y reactivo. 
Mucosas rosadas y húmedas. 
Tiempo de llenado capilar < 2 segundos.
```

**Diagnóstico:**
```
Control de salud rutinario. 
Estado general óptimo.
```

**Tratamiento:**
```
Continuar con dieta balanceada.
Ejercicio diario moderado.
Control en 6 meses.
```

## ✅ Criterios de Aceptación

- [ ] Los veterinarios pueden crear consultas desde citas
- [ ] Los datos de la cita se prellenan correctamente
- [ ] El historial clínico se muestra en el panel lateral
- [ ] La consulta se guarda correctamente
- [ ] La cita puede marcarse como completada automáticamente
- [ ] Los permisos funcionan correctamente
- [ ] El diseño es responsive
- [ ] Los errores se manejan apropiadamente

## 🎯 Próximos Pasos

Después de completar estas pruebas, considera:
1. Agregar pruebas unitarias para los componentes
2. Agregar pruebas de integración para el flujo completo
3. Optimizar la carga del historial clínico
4. Agregar plantillas de consulta frecuentes
5. Agregar autoguardado de borradores

