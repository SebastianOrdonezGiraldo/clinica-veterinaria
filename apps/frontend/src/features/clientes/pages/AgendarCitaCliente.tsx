import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useAuth } from '@core/auth/AuthContext';
import { useQuery } from '@tanstack/react-query';
import { clienteService } from '../services/clienteService';
import { citaPublicaService } from '@features/agenda/services/citaPublicaService';
import { Usuario, Paciente } from '@core/types';
import { Button } from '@shared/components/ui/button';
import { Input } from '@shared/components/ui/input';
import { Label } from '@shared/components/ui/label';
import { Textarea } from '@shared/components/ui/textarea';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@shared/components/ui/select';
import { toast } from 'sonner';
import { Calendar, Clock, User, FileText, Dog, ArrowLeft, Plus, CheckCircle2 } from 'lucide-react';
import { useLogger } from '@shared/hooks/useLogger';

const citaClienteSchema = z.object({
  // Selección de mascota
  tipoMascota: z.enum(['existente', 'nueva']),
  pacienteId: z.string().optional(),
  // Datos de la cita
  fecha: z.string().min(1, 'La fecha es requerida'),
  hora: z.string().min(1, 'La hora es requerida'),
  motivo: z.string().min(1, 'El motivo es requerido').max(500, 'El motivo no puede exceder 500 caracteres'),
  observaciones: z.string().max(1000).optional(),
  profesionalId: z.string().min(1, 'El veterinario es requerido'),
  // Datos nuevos de paciente (solo si tipoMascota === 'nueva')
  pacienteNombre: z.string().optional(),
  pacienteEspecie: z.string().optional(),
  pacienteRaza: z.string().optional(),
  pacienteSexo: z.string().optional(),
  pacienteEdadMeses: z.string().optional(),
  pacientePesoKg: z.string().optional(),
  pacienteNotas: z.string().optional(),
}).refine((data) => {
  if (data.tipoMascota === 'existente') {
    return !!data.pacienteId;
  } else {
    return !!data.pacienteNombre && !!data.pacienteEspecie;
  }
}, {
  message: 'Complete todos los campos requeridos',
  path: ['tipoMascota'],
});

type CitaClienteFormData = z.infer<typeof citaClienteSchema>;

export default function AgendarCitaCliente() {
  const logger = useLogger('AgendarCitaCliente');
  const { cliente } = useAuth();
  const navigate = useNavigate();
  const [veterinarios, setVeterinarios] = useState<Usuario[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isLoadingVets, setIsLoadingVets] = useState(true);
  const [citaCreada, setCitaCreada] = useState(false);
  const [horasOcupadas, setHorasOcupadas] = useState<string[]>([]);
  const [isLoadingHoras, setIsLoadingHoras] = useState(false);

  const { data: mascotas, isLoading: isLoadingMascotas } = useQuery({
    queryKey: ['mis-mascotas'],
    queryFn: () => clienteService.getMisMascotas(),
  });

  const { register, handleSubmit, watch, setValue, formState: { errors } } = useForm<CitaClienteFormData>({
    resolver: zodResolver(citaClienteSchema),
    defaultValues: {
      tipoMascota: 'existente',
    },
  });

  const tipoMascota = watch('tipoMascota');
  const fechaSeleccionada = watch('fecha');
  const profesionalSeleccionado = watch('profesionalId');

  // Función para verificar si una fecha es domingo
  const esDomingo = (fechaStr: string): boolean => {
    if (!fechaStr) return false;
    const fecha = new Date(fechaStr + 'T00:00:00');
    return fecha.getDay() === 0;
  };

  // Función para verificar si una fecha es sábado
  const esSabado = (fechaStr: string): boolean => {
    if (!fechaStr) return false;
    const fecha = new Date(fechaStr + 'T00:00:00');
    return fecha.getDay() === 6;
  };

  // Función para obtener todas las horas posibles según el día
  const getHorasLaborales = (): string[] => {
    if (!fechaSeleccionada || esDomingo(fechaSeleccionada)) return [];
    
    const horas: string[] = [];
    
    if (esSabado(fechaSeleccionada)) {
      // Sábados: solo 8am-12m
      for (let hora = 8; hora < 12; hora++) {
        horas.push(`${hora.toString().padStart(2, '0')}:00`);
        horas.push(`${hora.toString().padStart(2, '0')}:30`);
      }
    } else {
      // Lunes a viernes: 8am-12m y 2pm-6pm
      for (let hora = 8; hora < 12; hora++) {
        horas.push(`${hora.toString().padStart(2, '0')}:00`);
        horas.push(`${hora.toString().padStart(2, '0')}:30`);
      }
      for (let hora = 14; hora < 18; hora++) {
        horas.push(`${hora.toString().padStart(2, '0')}:00`);
        horas.push(`${hora.toString().padStart(2, '0')}:30`);
      }
    }
    
    return horas;
  };

  // Función para obtener las horas disponibles
  const getHorasDisponibles = (): string[] => {
    const horasLaborales = getHorasLaborales();
    return horasLaborales.filter(hora => !horasOcupadas.includes(hora));
  };

  useEffect(() => {
    loadVeterinarios();
  }, []);

  useEffect(() => {
    if (profesionalSeleccionado && fechaSeleccionada && !esDomingo(fechaSeleccionada)) {
      loadHorasOcupadas();
    } else {
      setHorasOcupadas([]);
    }
  }, [profesionalSeleccionado, fechaSeleccionada]);

  const loadVeterinarios = async () => {
    try {
      setIsLoadingVets(true);
      const vets = await citaPublicaService.getVeterinarios();
      setVeterinarios(vets);
    } catch (error: any) {
      logger.error('Error al cargar veterinarios', error);
      toast.error('Error al cargar veterinarios disponibles');
    } finally {
      setIsLoadingVets(false);
    }
  };

  const loadHorasOcupadas = async () => {
    if (!profesionalSeleccionado || !fechaSeleccionada) return;
    
    try {
      setIsLoadingHoras(true);
      const horas = await citaPublicaService.getHorasOcupadas(
        profesionalSeleccionado,
        fechaSeleccionada
      );
      setHorasOcupadas(horas);
    } catch (error: any) {
      logger.error('Error al cargar horas ocupadas', error);
      toast.error('Error al cargar horas disponibles');
    } finally {
      setIsLoadingHoras(false);
    }
  };

  const onSubmit = async (data: CitaClienteFormData) => {
    if (!cliente) {
      toast.error('No se pudo obtener la información del cliente');
      return;
    }

    try {
      setIsLoading(true);

      // Combinar fecha y hora
      const fechaISO = `${data.fecha}T${data.hora}:00`;

      let pacienteId: string;

      if (data.tipoMascota === 'existente') {
        // Usar mascota existente
        pacienteId = data.pacienteId!;
      } else {
        // Crear nueva mascota primero
        // Nota: Necesitaríamos un endpoint para crear pacientes desde el cliente autenticado
        // Por ahora, usaremos el endpoint público que crea paciente y cita juntos
        toast.error('La creación de nuevas mascotas desde el portal del cliente aún no está disponible. Por favor, selecciona una mascota existente.');
        return;
      }

      // Crear la cita usando el endpoint público con IDs existentes
      // (El endpoint /api/citas requiere roles del sistema, pero /api/public/citas acepta IDs existentes)
      const cita = await citaPublicaService.crearCita({
        fecha: fechaISO,
        motivo: data.motivo,
        observaciones: data.observaciones,
        propietarioId: cliente.id,
        pacienteId: pacienteId,
        profesionalId: data.profesionalId,
      });

      setCitaCreada(true);
      toast.success('¡Cita agendada exitosamente!');
      
      // Redirigir al dashboard después de 2 segundos
      setTimeout(() => {
        navigate('/cliente/dashboard');
      }, 2000);
    } catch (error: any) {
      logger.error('Error al agendar cita', error);
      toast.error(error.response?.data?.message || 'Error al agendar la cita');
    } finally {
      setIsLoading(false);
    }
  };

  if (citaCreada) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-green-50 flex items-center justify-center p-4">
        <Card className="w-full max-w-md">
          <CardContent className="p-8 text-center">
            <div className="mb-4 flex justify-center">
              <div className="h-16 w-16 rounded-full bg-green-100 flex items-center justify-center">
                <CheckCircle2 className="h-8 w-8 text-green-600" />
              </div>
            </div>
            <h2 className="text-2xl font-bold mb-2">¡Cita Agendada!</h2>
            <p className="text-muted-foreground mb-6">
              Tu cita ha sido agendada exitosamente. Serás redirigido al dashboard en breve.
            </p>
            <Button onClick={() => navigate('/cliente/dashboard')}>
              Volver al Dashboard
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  const horasDisponibles = getHorasDisponibles();
  const minDate = new Date().toISOString().split('T')[0];

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-green-50">
      {/* Header */}
      <header className="sticky top-0 z-50 w-full border-b bg-white/80 backdrop-blur-sm">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <Button variant="ghost" size="sm" onClick={() => navigate('/cliente/dashboard')}>
                <ArrowLeft className="h-4 w-4 mr-2" />
                Volver
              </Button>
              <div>
                <h1 className="text-xl font-bold">Agendar Nueva Cita</h1>
                <p className="text-xs text-muted-foreground">Portal del Cliente</p>
              </div>
            </div>
          </div>
        </div>
      </header>

      <div className="container mx-auto px-4 py-8 max-w-3xl">
        <Card className="shadow-lg">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Calendar className="h-5 w-5" />
              Información de la Cita
            </CardTitle>
            <CardDescription>
              Completa los datos para agendar tu cita
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
              {/* Selección de Mascota */}
              <div className="space-y-4 p-4 bg-accent/50 rounded-lg">
                <div className="space-y-2">
                  <Label className="text-base font-semibold flex items-center gap-2">
                    <Dog className="h-4 w-4" />
                    Mascota
                  </Label>
                  <Select
                    value={tipoMascota}
                    onValueChange={(value) => {
                      setValue('tipoMascota', value as 'existente' | 'nueva');
                      if (value === 'existente') {
                        setValue('pacienteId', '');
                      }
                    }}
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="existente">Usar mascota existente</SelectItem>
                      <SelectItem value="nueva">Registrar nueva mascota</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                {tipoMascota === 'existente' && (
                  <div className="space-y-2">
                    <Label htmlFor="pacienteId">Selecciona tu mascota *</Label>
                    {isLoadingMascotas ? (
                      <div className="text-sm text-muted-foreground">Cargando mascotas...</div>
                    ) : mascotas && mascotas.length > 0 ? (
                      <Select
                        value={watch('pacienteId') || ''}
                        onValueChange={(value) => {
                          setValue('pacienteId', value, { shouldValidate: true });
                        }}
                        disabled={isLoading}
                      >
                        <SelectTrigger id="pacienteId" className="w-full">
                          <SelectValue placeholder="Selecciona una mascota" />
                        </SelectTrigger>
                        <SelectContent>
                          {mascotas.map((mascota) => (
                            <SelectItem key={mascota.id} value={String(mascota.id)}>
                              {mascota.nombre} - {mascota.especie} {mascota.raza && `(${mascota.raza})`}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    ) : (
                      <div className="text-sm text-muted-foreground p-3 bg-yellow-50 border border-yellow-200 rounded">
                        No tienes mascotas registradas. Por favor, regístrate primero en{' '}
                        <Button
                          type="button"
                          variant="link"
                          className="p-0 h-auto"
                          onClick={() => navigate('/agendar-cita')}
                        >
                          el formulario público
                        </Button>
                      </div>
                    )}
                    {errors.pacienteId && (
                      <p className="text-sm text-red-500">{errors.pacienteId.message}</p>
                    )}
                  </div>
                )}

                {tipoMascota === 'nueva' && (
                  <div className="space-y-4 p-4 bg-yellow-50 border border-yellow-200 rounded">
                    <p className="text-sm text-yellow-800">
                      Para registrar una nueva mascota, por favor usa el{' '}
                      <Button
                        type="button"
                        variant="link"
                        className="p-0 h-auto text-yellow-800 underline"
                        onClick={() => navigate('/agendar-cita')}
                      >
                        formulario público de agendamiento
                      </Button>
                      {' '}donde puedes crear la mascota y agendar la cita al mismo tiempo.
                    </p>
                  </div>
                )}
              </div>

              {/* Fecha y Hora */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="fecha" className="flex items-center gap-2">
                    <Calendar className="h-4 w-4" />
                    Fecha *
                  </Label>
                  <Input
                    id="fecha"
                    type="date"
                    min={minDate}
                    {...register('fecha')}
                    disabled={isLoading}
                  />
                  {errors.fecha && (
                    <p className="text-sm text-red-500">{errors.fecha.message}</p>
                  )}
                </div>

                <div className="space-y-2">
                  <Label htmlFor="hora" className="flex items-center gap-2">
                    <Clock className="h-4 w-4" />
                    Hora *
                  </Label>
                  {isLoadingHoras ? (
                    <div className="text-sm text-muted-foreground">Cargando horarios...</div>
                  ) : horasDisponibles.length > 0 ? (
                    <Select
                      value={watch('hora') || ''}
                      onValueChange={(value) => setValue('hora', value)}
                      disabled={!fechaSeleccionada || !profesionalSeleccionado || isLoadingHoras}
                    >
                      <SelectTrigger id="hora">
                        <SelectValue placeholder="Selecciona una hora" />
                      </SelectTrigger>
                      <SelectContent>
                        {horasDisponibles.map((hora) => (
                          <SelectItem key={hora} value={hora}>
                            {hora}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  ) : (
                    <Input
                      id="hora"
                      type="time"
                      {...register('hora')}
                      disabled={!fechaSeleccionada || !profesionalSeleccionado || isLoadingHoras}
                    />
                  )}
                  {errors.hora && (
                    <p className="text-sm text-red-500">{errors.hora.message}</p>
                  )}
                </div>
              </div>

              {/* Veterinario */}
              <div className="space-y-2">
                <Label htmlFor="profesionalId" className="flex items-center gap-2">
                  <User className="h-4 w-4" />
                  Veterinario *
                </Label>
                {isLoadingVets ? (
                  <div className="text-sm text-muted-foreground">Cargando veterinarios...</div>
                ) : (
                  <Select
                    value={watch('profesionalId') || ''}
                    onValueChange={(value) => setValue('profesionalId', value)}
                    disabled={isLoading}
                  >
                    <SelectTrigger id="profesionalId">
                      <SelectValue placeholder="Selecciona un veterinario" />
                    </SelectTrigger>
                    <SelectContent>
                      {veterinarios.map((vet) => (
                        <SelectItem key={vet.id} value={vet.id}>
                          {vet.nombre}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                )}
                {errors.profesionalId && (
                  <p className="text-sm text-red-500">{errors.profesionalId.message}</p>
                )}
              </div>

              {/* Motivo */}
              <div className="space-y-2">
                <Label htmlFor="motivo" className="flex items-center gap-2">
                  <FileText className="h-4 w-4" />
                  Motivo de la Consulta *
                </Label>
                <Input
                  id="motivo"
                  placeholder="Ej: Control general, Vacunación, Urgencia..."
                  {...register('motivo')}
                  disabled={isLoading}
                />
                {errors.motivo && (
                  <p className="text-sm text-red-500">{errors.motivo.message}</p>
                )}
              </div>

              {/* Observaciones */}
              <div className="space-y-2">
                <Label htmlFor="observaciones">Observaciones (opcional)</Label>
                <Textarea
                  id="observaciones"
                  placeholder="Información adicional que consideres importante..."
                  {...register('observaciones')}
                  rows={3}
                  disabled={isLoading}
                />
              </div>

              {/* Botones */}
              <div className="flex gap-4 pt-4">
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => navigate('/cliente/dashboard')}
                  disabled={isLoading}
                  className="flex-1"
                >
                  Cancelar
                </Button>
                <Button
                  type="submit"
                  disabled={isLoading || tipoMascota === 'nueva'}
                  className="flex-1"
                >
                  {isLoading ? 'Agendando...' : 'Agendar Cita'}
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

