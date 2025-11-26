import { useState, useEffect } from 'react';
import { useNavigate, useParams, useLocation } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { ArrowLeft, Save, Plus } from 'lucide-react';
import { Button } from '@shared/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Input } from '@shared/components/ui/input';
import { Label } from '@shared/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@shared/components/ui/select';
import { Textarea } from '@shared/components/ui/textarea';
import { Skeleton } from '@shared/components/ui/skeleton';
import { toast } from 'sonner';
import { citaService, EstadoCita } from '@features/agenda/services/citaService';
import { pacienteService } from '@features/pacientes/services/pacienteService';
import { propietarioService } from '@features/propietarios/services/propietarioService';
import { usuarioService } from '@features/usuarios/services/usuarioService';
import { Paciente, Propietario, Usuario, Cita } from '@core/types';

const citaSchema = z.object({
  pacienteId: z.string().min(1, 'Paciente es requerido'),
  propietarioId: z.string().min(1, 'Propietario es requerido'),
  profesionalId: z.string().min(1, 'Profesional es requerido'),
  fecha: z.string().min(1, 'Fecha es requerida'),
  hora: z.string().min(1, 'Hora es requerida'),
  estado: z.enum(['PENDIENTE', 'CONFIRMADA', 'ATENDIDA', 'CANCELADA']),
  motivo: z.string().min(1, 'El motivo es requerido').max(500, 'El motivo no puede exceder 500 caracteres'),
  observaciones: z.string().max(1000).optional(),
});

type CitaFormData = z.infer<typeof citaSchema>;

export default function CitaForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const isEdit = !!id;

  const [pacientes, setPacientes] = useState<Paciente[]>([]);
  const [propietarios, setPropietarios] = useState<Propietario[]>([]);
  const [veterinarios, setVeterinarios] = useState<Usuario[]>([]);
  const [citasVeterinario, setCitasVeterinario] = useState<Cita[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isLoadingData, setIsLoadingData] = useState(true);
  const [isValidating, setIsValidating] = useState(false);

  const { register, handleSubmit, setValue, watch, formState: { errors }, reset } = useForm<CitaFormData>({
    resolver: zodResolver(citaSchema),
    defaultValues: {
      estado: 'PENDIENTE',
    },
  });

  const pacienteSeleccionado = watch('pacienteId');
  const horaSeleccionada = watch('hora');
  const fechaSeleccionada = watch('fecha');
  const profesionalSeleccionado = watch('profesionalId');

  // Función para formatear la hora para mostrar
  const formatHoraDisplay = (hora: string | undefined): string => {
    if (!hora) return '';
    const [hour, minute] = hora.split(':');
    const hourNum = parseInt(hour, 10);
    const hora12 = hourNum === 0 ? 12 : hourNum > 12 ? hourNum - 12 : hourNum;
    const ampm = hourNum < 12 ? 'AM' : 'PM';
    return `${hora12}:${minute} ${ampm}`;
  };

  useEffect(() => {
    loadInitialData();
  }, [id]);

  // Si hay un pacienteId en el state (viene de crear paciente), recargar pacientes y seleccionarlo
  useEffect(() => {
    const state = location.state as { pacienteId?: string } | null;
    if (state?.pacienteId) {
      // Recargar pacientes para asegurar que el nuevo esté disponible
      pacienteService.getAll().then(pacientesData => {
        setPacientes(pacientesData);
        const paciente = pacientesData.find(p => p.id === state.pacienteId);
        if (paciente) {
          setValue('pacienteId', paciente.id);
          setValue('propietarioId', paciente.propietarioId);
          toast.success(`Paciente "${paciente.nombre}" seleccionado`);
        }
        // Limpiar el state para evitar seleccionarlo de nuevo
        window.history.replaceState({}, document.title);
      }).catch(error => {
        console.error('Error al recargar pacientes:', error);
      });
    }
  }, [location.state, setValue]);

  // Cargar citas del veterinario cuando cambia el profesional o la fecha
  useEffect(() => {
    if (profesionalSeleccionado && fechaSeleccionada) {
      loadCitasVeterinario();
    } else {
      setCitasVeterinario([]);
    }
  }, [profesionalSeleccionado, fechaSeleccionada]);

  const loadInitialData = async () => {
    try {
      setIsLoadingData(true);
      // Usar getVeterinarios() en lugar de getAll() para evitar problemas de permisos
      // Los veterinarios no tienen acceso a getAll() que requiere rol ADMIN
      // El endpoint /api/usuarios/veterinarios está disponible para todos los usuarios autenticados
      const [pacientesData, propietariosData, veterinariosData] = await Promise.all([
        pacienteService.getAll(),
        propietarioService.getAll(),
        usuarioService.getVeterinarios(),
      ]);
      setPacientes(pacientesData);
      setPropietarios(propietariosData);
      
      // Los veterinarios ya vienen filtrados del backend (solo VET activos)
      // Nota: Si en el futuro necesitas incluir ADMIN como profesionales, 
      // se requeriría crear un nuevo endpoint en el backend
      const vets = veterinariosData.filter(u => u.activo !== false);
      setVeterinarios(vets);

      // Si es edición, cargar datos de la cita
      if (isEdit && id) {
        const cita = await citaService.getById(id);
        const fecha = new Date(cita.fecha);
        const fechaStr = fecha.toISOString().split('T')[0];
        const horaStr = fecha.toTimeString().slice(0, 5);
        
        reset({
          pacienteId: cita.pacienteId,
          propietarioId: cita.propietarioId,
          profesionalId: cita.profesionalId,
          fecha: fechaStr,
          hora: horaStr,
          estado: cita.estado,
          motivo: cita.motivo,
          observaciones: cita.observaciones || '',
        });
      }
    } catch (error: any) {
      console.error('Error al cargar datos:', error);
      const errorMessage = error?.response?.data?.message || 'Error al cargar los datos';
      toast.error(errorMessage);
    } finally {
      setIsLoadingData(false);
    }
  };

  const loadCitasVeterinario = async () => {
    if (!profesionalSeleccionado) return;
    
    try {
      const citas = await citaService.getByProfesional(profesionalSeleccionado);
      setCitasVeterinario(citas);
    } catch (error) {
      console.error('Error al cargar citas del veterinario:', error);
      // No mostrar error, solo log
    }
  };

  const handlePacienteChange = (pacienteId: string) => {
    setValue('pacienteId', pacienteId);
    const paciente = pacientes.find(p => p.id === pacienteId);
    if (paciente) {
      setValue('propietarioId', paciente.propietarioId);
    }
  };

  // Validar si hay conflicto de horario
  const validarDisponibilidad = (fechaHora: string, profesionalId: string): { hayConflicto: boolean; citaConflicto?: Cita } => {
    if (!fechaHora || !profesionalId) {
      return { hayConflicto: false };
    }

    const fechaHoraObj = new Date(fechaHora);
    const fechaHoraInicio = new Date(fechaHoraObj);
    fechaHoraInicio.setMinutes(fechaHoraInicio.getMinutes() - 30); // 30 min antes
    const fechaHoraFin = new Date(fechaHoraObj);
    fechaHoraFin.setMinutes(fechaHoraFin.getMinutes() + 30); // 30 min después

    // Buscar citas del mismo veterinario en el mismo rango de tiempo
    const citaConflicto = citasVeterinario.find(cita => {
      // Excluir la cita actual si estamos editando
      if (isEdit && id && cita.id === id) {
        return false;
      }

      // Excluir citas canceladas
      if (cita.estado === 'CANCELADA') {
        return false;
      }

      const citaFecha = new Date(cita.fecha);
      
      // Verificar si la cita está en el rango de conflicto (mismo día y hora similar)
      const mismoDia = citaFecha.toDateString() === fechaHoraObj.toDateString();
      const diferenciaMinutos = Math.abs(citaFecha.getTime() - fechaHoraObj.getTime()) / (1000 * 60);
      
      // Considerar conflicto si está en el mismo día y la diferencia es menor a 30 minutos
      return mismoDia && diferenciaMinutos < 30;
    });

    return {
      hayConflicto: !!citaConflicto,
      citaConflicto,
    };
  };

  const onSubmit = async (data: CitaFormData) => {
    try {
      setIsLoading(true);
      setIsValidating(true);
      
      // Combinar fecha y hora en formato ISO
      const fechaHora = `${data.fecha}T${data.hora}:00`;
      
      // Validar disponibilidad antes de crear/actualizar
      const validacion = validarDisponibilidad(fechaHora, data.profesionalId);
      
      if (validacion.hayConflicto && validacion.citaConflicto) {
        const citaFecha = new Date(validacion.citaConflicto.fecha);
        const horaFormateada = citaFecha.toLocaleTimeString('es-ES', { 
          hour: '2-digit', 
          minute: '2-digit',
          hour12: true 
        });
        const pacienteConflicto = pacientes.find(p => p.id === validacion.citaConflicto!.pacienteId);
        const nombrePaciente = pacienteConflicto?.nombre || 'otro paciente';
        
        toast.error(
          `El veterinario ya tiene una cita a las ${horaFormateada} con ${nombrePaciente}. Por favor, selecciona otra hora.`,
          { duration: 5000 }
        );
        setIsValidating(false);
        setIsLoading(false);
        return;
      }

      // Recargar citas del veterinario para asegurar datos actualizados
      await loadCitasVeterinario();
      
      // Validar nuevamente después de recargar
      const validacionFinal = validarDisponibilidad(fechaHora, data.profesionalId);
      if (validacionFinal.hayConflicto && validacionFinal.citaConflicto) {
        const citaFecha = new Date(validacionFinal.citaConflicto.fecha);
        const horaFormateada = citaFecha.toLocaleTimeString('es-ES', { 
          hour: '2-digit', 
          minute: '2-digit',
          hour12: true 
        });
        const pacienteConflicto = pacientes.find(p => p.id === validacionFinal.citaConflicto!.pacienteId);
        const nombrePaciente = pacienteConflicto?.nombre || 'otro paciente';
        
        toast.error(
          `El veterinario ya tiene una cita a las ${horaFormateada} con ${nombrePaciente}. Por favor, selecciona otra hora.`,
          { duration: 5000 }
        );
        setIsValidating(false);
        setIsLoading(false);
        return;
      }
      
      setIsValidating(false);
      
      const citaData = {
        pacienteId: data.pacienteId,
        propietarioId: data.propietarioId,
        profesionalId: data.profesionalId,
        fecha: fechaHora,
        motivo: data.motivo.trim(),
        observaciones: data.observaciones?.trim() || undefined,
        estado: data.estado as EstadoCita,
      };

      if (isEdit && id) {
        await citaService.update(id, citaData);
        toast.success('Cita actualizada exitosamente');
      } else {
        await citaService.create(citaData);
        toast.success('Cita agendada exitosamente');
      }
      
      navigate('/agenda');
    } catch (error: any) {
      console.error('Error al guardar cita:', error);
      const statusCode = error?.response?.status;
      const errorMessage = error?.response?.data?.message;
      
      if (statusCode === 400) {
        toast.error(errorMessage || 'Error de validación: Verifica que todos los campos sean correctos');
      } else if (statusCode === 403) {
        toast.error('No tienes permisos para crear o editar citas');
      } else if (statusCode === 404) {
        toast.error('La cita o alguno de los recursos relacionados no existe');
      } else if (statusCode === 500) {
        toast.error('Error del servidor. Por favor, intenta nuevamente');
      } else {
        toast.error(errorMessage || 'Error al guardar la cita. Por favor, intenta nuevamente');
      }
    } finally {
      setIsLoading(false);
      setIsValidating(false);
    }
  };

  if (isLoadingData) {
    return (
      <div className="space-y-6">
        <div className="flex items-center gap-4">
          <Skeleton className="h-10 w-10 rounded-md" />
          <div className="space-y-2">
            <Skeleton className="h-8 w-64" />
            <Skeleton className="h-4 w-48" />
          </div>
        </div>
        <Card>
          <CardHeader>
            <Skeleton className="h-6 w-48" />
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="grid gap-4 md:grid-cols-2">
              <Skeleton className="h-10 w-full" />
              <Skeleton className="h-10 w-full" />
              <Skeleton className="h-10 w-full" />
              <Skeleton className="h-10 w-full" />
              <Skeleton className="h-10 w-full" />
              <Skeleton className="h-10 w-full" />
            </div>
            <Skeleton className="h-24 w-full" />
            <Skeleton className="h-10 w-32 ml-auto" />
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate('/agenda')}>
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <div>
          <h1 className="text-3xl font-bold text-foreground">
            {isEdit ? 'Editar Cita' : 'Nueva Cita'}
          </h1>
          <p className="text-muted-foreground mt-1">Complete la información de la cita</p>
        </div>
      </div>

      <form onSubmit={handleSubmit(onSubmit)}>
        <Card>
          <CardHeader>
            <CardTitle>Información de la Cita</CardTitle>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="grid gap-4 md:grid-cols-2">
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <Label htmlFor="pacienteId">Paciente *</Label>
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    className="h-7 px-2 text-xs"
                    onClick={() => navigate('/pacientes/nuevo', { 
                      state: { returnTo: location.pathname } 
                    })}
                  >
                    <Plus className="h-3 w-3 mr-1" />
                    Nuevo
                  </Button>
                </div>
                <Select 
                  value={watch('pacienteId')} 
                  onValueChange={handlePacienteChange}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Seleccione paciente" />
                  </SelectTrigger>
                  <SelectContent>
                    {pacientes.map((paciente) => (
                      <SelectItem key={paciente.id} value={paciente.id}>
                        {paciente.nombre} ({paciente.especie})
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                {errors.pacienteId && (
                  <p className="text-sm text-destructive">{errors.pacienteId.message}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="propietarioId">Propietario *</Label>
                <Select 
                  value={watch('propietarioId')} 
                  onValueChange={(value) => setValue('propietarioId', value)}
                  disabled={!!pacienteSeleccionado}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Seleccione propietario" />
                  </SelectTrigger>
                  <SelectContent>
                    {propietarios.map((prop) => (
                      <SelectItem key={prop.id} value={prop.id}>
                        {prop.nombre}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                {errors.propietarioId && (
                  <p className="text-sm text-destructive">{errors.propietarioId.message}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="profesionalId">Veterinario *</Label>
                <Select 
                  value={watch('profesionalId')} 
                  onValueChange={(value) => setValue('profesionalId', value)}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Seleccione veterinario" />
                  </SelectTrigger>
                  <SelectContent>
                    {veterinarios.length > 0 ? (
                      veterinarios.map((vet) => (
                        <SelectItem key={vet.id} value={vet.id}>
                          {vet.nombre} {vet.rol === 'ADMIN' ? '(Admin)' : ''}
                        </SelectItem>
                      ))
                    ) : (
                      <SelectItem value="no-vets" disabled>
                        No hay veterinarios disponibles
                      </SelectItem>
                    )}
                  </SelectContent>
                </Select>
                {errors.profesionalId && (
                  <p className="text-sm text-destructive">{errors.profesionalId.message}</p>
                )}
                {veterinarios.length === 0 && !isLoadingData && (
                  <p className="text-sm text-muted-foreground">
                    No hay veterinarios registrados. Contacta al administrador.
                  </p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="estado">Estado</Label>
                <Select 
                  value={watch('estado')} 
                  onValueChange={(value) => setValue('estado', value as EstadoCita)}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="PENDIENTE">Pendiente</SelectItem>
                    <SelectItem value="CONFIRMADA">Confirmada</SelectItem>
                    <SelectItem value="ATENDIDA">Atendida</SelectItem>
                    <SelectItem value="CANCELADA">Cancelada</SelectItem>
                  </SelectContent>
                </Select>
                {errors.estado && (
                  <p className="text-sm text-destructive">{errors.estado.message}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="fecha">Fecha *</Label>
                <Input
                  id="fecha"
                  type="date"
                  {...register('fecha')}
                  min={new Date().toISOString().split('T')[0]}
                />
                {errors.fecha && (
                  <p className="text-sm text-destructive">{errors.fecha.message}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="hora">Hora *</Label>
                <Select
                  value={horaSeleccionada}
                  onValueChange={(value) => setValue('hora', value)}
                >
                  <SelectTrigger className="w-full">
                    <SelectValue placeholder="Seleccione hora" />
                  </SelectTrigger>
                  <SelectContent className="max-h-[300px]">
                    {Array.from({ length: 48 }, (_, i) => {
                      const hour = Math.floor(i / 2);
                      const minute = (i % 2) * 30;
                      const horaStr = `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`;
                      const hora12 = hour === 0 ? 12 : hour > 12 ? hour - 12 : hour;
                      const ampm = hour < 12 ? 'AM' : 'PM';
                      const displayHora = `${hora12}:${String(minute).padStart(2, '0')} ${ampm}`;
                      
                      // Verificar si esta hora tiene conflicto
                      const fechaHora = fechaSeleccionada && horaStr ? `${fechaSeleccionada}T${horaStr}:00` : '';
                      const tieneConflicto = profesionalSeleccionado && fechaHora 
                        ? validarDisponibilidad(fechaHora, profesionalSeleccionado).hayConflicto
                        : false;
                      
                      return (
                        <SelectItem 
                          key={horaStr} 
                          value={horaStr} 
                          className={tieneConflicto ? "cursor-pointer text-destructive" : "cursor-pointer"}
                        >
                          {displayHora} {tieneConflicto ? '(⚠️ Ocupada)' : ''}
                        </SelectItem>
                      );
                    })}
                  </SelectContent>
                </Select>
                {horaSeleccionada && fechaSeleccionada && profesionalSeleccionado && (() => {
                  const fechaHora = `${fechaSeleccionada}T${horaSeleccionada}:00`;
                  const validacion = validarDisponibilidad(fechaHora, profesionalSeleccionado);
                  if (validacion.hayConflicto && validacion.citaConflicto) {
                    const citaFecha = new Date(validacion.citaConflicto.fecha);
                    const horaFormateada = citaFecha.toLocaleTimeString('es-ES', { 
                      hour: '2-digit', 
                      minute: '2-digit',
                      hour12: true 
                    });
                    const pacienteConflicto = pacientes.find(p => p.id === validacion.citaConflicto!.pacienteId);
                    const nombrePaciente = pacienteConflicto?.nombre || 'otro paciente';
                    return (
                      <p className="text-sm text-destructive font-medium">
                        ⚠️ Esta hora está ocupada. El veterinario tiene una cita a las {horaFormateada} con {nombrePaciente}.
                      </p>
                    );
                  }
                  return (
                    <p className="text-sm text-muted-foreground">
                      Hora seleccionada: {formatHoraDisplay(horaSeleccionada)}
                    </p>
                  );
                })()}
                {errors.hora && (
                  <p className="text-sm text-destructive">{errors.hora.message}</p>
                )}
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="motivo">Motivo de la Consulta *</Label>
              <Textarea
                id="motivo"
                {...register('motivo')}
                placeholder="Describa el motivo de la consulta"
                rows={4}
              />
              {errors.motivo && (
                <p className="text-sm text-destructive">{errors.motivo.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="observaciones">Observaciones</Label>
              <Textarea
                id="observaciones"
                {...register('observaciones')}
                placeholder="Observaciones adicionales (opcional)"
                rows={3}
              />
              {errors.observaciones && (
                <p className="text-sm text-destructive">{errors.observaciones.message}</p>
              )}
            </div>

            <div className="flex justify-end gap-3 pt-4">
              <Button type="button" variant="outline" onClick={() => navigate('/agenda')} disabled={isLoading}>
                Cancelar
              </Button>
              <Button type="submit" className="gap-2" disabled={isLoading || isLoadingData}>
                <Save className="h-4 w-4" />
                {isLoading ? 'Guardando...' : isEdit ? 'Actualizar' : 'Agendar'}
              </Button>
            </div>
          </CardContent>
        </Card>
      </form>
    </div>
  );
}
