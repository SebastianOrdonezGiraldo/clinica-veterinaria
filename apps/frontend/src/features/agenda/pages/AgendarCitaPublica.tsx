import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { Calendar, Clock, User, Mail, Phone, MapPin, FileText, Dog, UserPlus, CheckCircle2, Lock, LogIn, ArrowRight, ArrowLeft, Sparkles, AlertCircle, Info, Badge as BadgeIcon } from 'lucide-react';
import { Link } from 'react-router-dom';
import { Button } from '@shared/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Input } from '@shared/components/ui/input';
import { Label } from '@shared/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@shared/components/ui/select';
import { Textarea } from '@shared/components/ui/textarea';
import { RadioGroup, RadioGroupItem } from '@shared/components/ui/radio-group';
import { toast } from 'sonner';
import { citaPublicaService, CitaPublicaRequestDTO } from '@features/agenda/services/citaPublicaService';
import { Usuario } from '@core/types';
import { Skeleton } from '@shared/components/ui/skeleton';
import { Badge } from '@shared/components/ui/badge';
import { Alert, AlertDescription } from '@shared/components/ui/alert';
import { useLogger } from '@shared/hooks/useLogger';

const citaPublicaSchema = z.object({
  tipoRegistro: z.enum(['existente', 'nuevo']),
  // Datos de la cita
  fecha: z.string().min(1, 'La fecha es requerida'),
  hora: z.string().min(1, 'La hora es requerida'),
  motivo: z.string().min(1, 'El motivo es requerido').max(500, 'El motivo no puede exceder 500 caracteres'),
  observaciones: z.string().max(1000).optional(),
  profesionalId: z.string().min(1, 'El veterinario es requerido'),
  // IDs existentes
  propietarioId: z.string().optional(),
  pacienteId: z.string().optional(),
  // Datos nuevos de propietario
  propietarioNombre: z.string().optional(),
  propietarioEmail: z.string().email('Email inválido').optional().or(z.literal('')),
  propietarioTelefono: z.string().optional(),
  propietarioDocumento: z.string().optional(),
  propietarioDireccion: z.string().optional(),
  propietarioPassword: z.string().min(6, 'La contraseña debe tener al menos 6 caracteres'),
  // Datos nuevos de paciente
  pacienteNombre: z.string().optional(),
  pacienteEspecie: z.string().optional(),
  pacienteRaza: z.string().optional(),
  pacienteSexo: z.string().optional(),
  pacienteEdadMeses: z.string().optional(),
  pacientePesoKg: z.string().optional(),
  pacienteNotas: z.string().optional(),
}).refine((data) => {
  if (data.tipoRegistro === 'existente') {
    return !!data.propietarioId && !!data.pacienteId;
  } else {
    return !!data.propietarioNombre && !!data.propietarioEmail && !!data.propietarioPassword && !!data.pacienteNombre && !!data.pacienteEspecie;
  }
}, {
  message: 'Complete todos los campos requeridos',
  path: ['tipoRegistro'],
});

type CitaPublicaFormData = z.infer<typeof citaPublicaSchema>;

export default function AgendarCitaPublica() {
  const logger = useLogger('AgendarCitaPublica');
  const [veterinarios, setVeterinarios] = useState<Usuario[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isLoadingVets, setIsLoadingVets] = useState(true);
  const [citaCreada, setCitaCreada] = useState(false);
  const [citaId, setCitaId] = useState<string | null>(null);
  const [horasOcupadas, setHorasOcupadas] = useState<string[]>([]);
  const [isLoadingHoras, setIsLoadingHoras] = useState(false);
  const [currentStep, setCurrentStep] = useState(1);
  const [formData, setFormData] = useState<Partial<CitaPublicaFormData>>({});

  const { register, handleSubmit, watch, setValue, formState: { errors } } = useForm<CitaPublicaFormData>({
    resolver: zodResolver(citaPublicaSchema),
    defaultValues: {
      tipoRegistro: 'nuevo',
    },
  });

  const tipoRegistro = watch('tipoRegistro');
  const fechaSeleccionada = watch('fecha');
  const profesionalSeleccionado = watch('profesionalId');

  // Función para verificar si una fecha es domingo
  const esDomingo = (fechaStr: string): boolean => {
    if (!fechaStr) return false;
    const fecha = new Date(fechaStr + 'T00:00:00');
    return fecha.getDay() === 0; // 0 = Domingo
  };

  // Función para verificar si una fecha es sábado
  const esSabado = (fechaStr: string): boolean => {
    if (!fechaStr) return false;
    const fecha = new Date(fechaStr + 'T00:00:00');
    return fecha.getDay() === 6; // 6 = Sábado
  };

  // Función para formatear hora en formato 12 horas
  const formatHora12h = (hora24: string): string => {
    const [hora, minutos] = hora24.split(':');
    const horaNum = parseInt(hora, 10);
    const ampm = horaNum < 12 ? 'AM' : 'PM';
    const hora12 = horaNum === 0 ? 12 : horaNum > 12 ? horaNum - 12 : horaNum;
    return `${hora12}:${minutos} ${ampm}`;
  };

  // Función para obtener todas las horas posibles según el día (horario laboral)
  const getHorasLaborales = (): string[] => {
    if (!fechaSeleccionada || esDomingo(fechaSeleccionada)) return [];
    
    const horas: string[] = [];
    
    if (esSabado(fechaSeleccionada)) {
      // Sábados: solo 8am-12m (cada 30 minutos)
      for (let hora = 8; hora < 12; hora++) {
        horas.push(`${hora.toString().padStart(2, '0')}:00`);
        horas.push(`${hora.toString().padStart(2, '0')}:30`);
      }
    } else {
      // Lunes a viernes: 8am-12m y 2pm-6pm
      // Mañana: 8am-12m
      for (let hora = 8; hora < 12; hora++) {
        horas.push(`${hora.toString().padStart(2, '0')}:00`);
        horas.push(`${hora.toString().padStart(2, '0')}:30`);
      }
      // Tarde: 2pm-6pm
      for (let hora = 14; hora < 18; hora++) {
        horas.push(`${hora.toString().padStart(2, '0')}:00`);
        horas.push(`${hora.toString().padStart(2, '0')}:30`);
      }
    }
    
    return horas;
  };

  // Función para obtener las horas disponibles (horario laboral menos horas ocupadas)
  const getHorasDisponibles = (): string[] => {
    const horasLaborales = getHorasLaborales();
    
    // Filtrar las horas ocupadas
    return horasLaborales.filter(hora => !horasOcupadas.includes(hora));
  };

  // Función para deshabilitar domingos en el input de fecha
  const isDateDisabled = (date: Date): boolean => {
    return date.getDay() === 0; // Deshabilitar domingos
  };

  useEffect(() => {
    loadVeterinarios();
  }, []);

  // Cargar horas ocupadas cuando cambia el veterinario o la fecha
  useEffect(() => {
    if (profesionalSeleccionado && fechaSeleccionada && !esDomingo(fechaSeleccionada)) {
      loadHorasOcupadas();
    } else {
      setHorasOcupadas([]);
    }
  }, [profesionalSeleccionado, fechaSeleccionada]);

  // Resetear hora cuando cambian las horas disponibles
  useEffect(() => {
    if (fechaSeleccionada) {
      const horasDisponibles = getHorasDisponibles();
      const horaActual = watch('hora');
      
      // Si la hora actual no está en las horas disponibles, resetear
      if (horaActual && !horasDisponibles.includes(horaActual)) {
        setValue('hora', '');
      }
    }
  }, [horasOcupadas, fechaSeleccionada]);

  const loadVeterinarios = async () => {
    try {
      setIsLoadingVets(true);
      const vets = await citaPublicaService.getVeterinarios();
      setVeterinarios(vets);
    } catch (error: any) {
      logger.error('Error al cargar veterinarios disponibles', error, {
        action: 'loadVeterinarios',
      });
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
      logger.warn('Error al cargar horas ocupadas', {
        action: 'loadHorasOcupadas',
        profesionalId: profesionalSeleccionado,
        fecha: fechaSeleccionada,
      });
      // No mostrar error al usuario, solo log
      setHorasOcupadas([]);
    } finally {
      setIsLoadingHoras(false);
    }
  };

  const onSubmit = async (data: CitaPublicaFormData) => {
    try {
      setIsLoading(true);

      // Combinar fecha y hora en formato ISO
      const fechaHora = new Date(`${data.fecha}T${data.hora}`);
      const fechaISO = fechaHora.toISOString();

      let request: CitaPublicaRequestDTO;

      if (data.tipoRegistro === 'existente') {
        // Usar IDs existentes
        request = {
          fecha: fechaISO,
          motivo: data.motivo,
          observaciones: data.observaciones,
          profesionalId: data.profesionalId,
          propietarioId: data.propietarioId!,
          pacienteId: data.pacienteId!,
        };
      } else {
        // Crear nuevos registros
        request = {
          fecha: fechaISO,
          motivo: data.motivo,
          observaciones: data.observaciones,
          profesionalId: data.profesionalId,
          propietarioNuevo: {
            nombre: data.propietarioNombre!,
            email: data.propietarioEmail!,
            telefono: data.propietarioTelefono || undefined,
            documento: data.propietarioDocumento || undefined,
            direccion: data.propietarioDireccion || undefined,
            password: data.propietarioPassword,
          },
          pacienteNuevo: {
            nombre: data.pacienteNombre!,
            especie: data.pacienteEspecie!,
            raza: data.pacienteRaza || undefined,
            sexo: data.pacienteSexo || undefined,
            edadMeses: data.pacienteEdadMeses ? parseInt(data.pacienteEdadMeses) : undefined,
            pesoKg: data.pacientePesoKg ? parseFloat(data.pacientePesoKg) : undefined,
            notas: data.pacienteNotas || undefined,
          },
        };
      }

      const cita = await citaPublicaService.crearCita(request);
      setCitaId(cita.id);
      setCitaCreada(true);
      
      toast.success('¡Cita agendada exitosamente!');
    } catch (error: any) {
      logger.error('Error al agendar cita pública', error, {
        action: 'agendarCita',
        tipoRegistro: tipoRegistro,
        profesionalId: data.profesionalId,
        fecha: data.fecha,
      });
      const errorMessage = error.response?.data?.mensaje || error.response?.data?.message || 'Error al agendar la cita';
      toast.error(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  if (citaCreada) {
    return (
      <div className="min-h-screen bg-gradient-to-b from-primary/5 via-white to-primary/5 py-12 px-4">
        <div className="container mx-auto max-w-2xl">
          <Card className="border-2 border-green-200 bg-gradient-to-br from-green-50 to-white shadow-2xl overflow-hidden">
            <div className="absolute top-0 left-0 right-0 h-2 bg-gradient-to-r from-green-500 to-green-600"></div>
            <CardHeader className="text-center pt-8 pb-6">
              <div className="flex justify-center mb-6">
                <div className="relative">
                  <div className="absolute inset-0 bg-green-500 rounded-full blur-2xl opacity-30 animate-pulse"></div>
                  <div className="relative h-20 w-20 rounded-full bg-gradient-to-br from-green-500 to-green-600 flex items-center justify-center shadow-xl">
                    <CheckCircle2 className="h-12 w-12 text-white" />
                  </div>
                </div>
              </div>
              <CardTitle className="text-3xl lg:text-4xl font-bold text-green-800 mb-2">
                ¡Cita Agendada Exitosamente!
              </CardTitle>
              <CardDescription className="text-lg mt-2">
                Su cita ha sido registrada con el ID:{' '}
                <Badge variant="outline" className="text-base px-3 py-1 font-mono">
                  {citaId}
                </Badge>
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-6 pb-8">
              <Alert className="border-green-200 bg-green-50">
                <Info className="h-4 w-4 text-green-600" />
                <AlertDescription className="text-green-800">
                  Recibirá una confirmación por correo electrónico. Por favor, llegue 10 minutos antes de su cita.
                </AlertDescription>
              </Alert>
              
              <Card className="border-2 border-primary/20 bg-gradient-to-br from-primary/5 to-white shadow-lg">
                <CardContent className="p-6">
                  <div className="flex items-start gap-4">
                    <div className="h-12 w-12 rounded-xl bg-gradient-to-br from-primary to-primary/70 flex items-center justify-center flex-shrink-0 shadow-md">
                      <Lock className="h-6 w-6 text-white" />
                    </div>
                    <div className="flex-1">
                      <h4 className="font-bold text-lg mb-2 text-foreground">
                        Accede al portal del cliente
                      </h4>
                      <p className="text-sm text-muted-foreground mb-4 leading-relaxed">
                        Ya puedes iniciar sesión en el portal del cliente con tu email y la contraseña que ingresaste para ver tus citas y mascotas.
                      </p>
                      <Link to="/cliente/login">
                        <Button className="w-full sm:w-auto shadow-md hover:shadow-lg transition-all">
                          <LogIn className="h-4 w-4 mr-2" />
                          Ir al Portal del Cliente
                        </Button>
                      </Link>
                    </div>
                  </div>
                </CardContent>
              </Card>
              
              <div className="flex flex-col sm:flex-row justify-center gap-4 pt-4">
                <Button 
                  onClick={() => window.location.reload()}
                  size="lg"
                  className="shadow-lg hover:shadow-xl transition-all hover:scale-105"
                >
                  <Calendar className="h-4 w-4 mr-2" />
                  Agendar Otra Cita
                </Button>
                <Button 
                  variant="outline" 
                  size="lg"
                  onClick={() => window.location.href = '/'}
                  className="shadow-md hover:shadow-lg transition-all"
                >
                  Volver al Inicio
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    );
  }

  const steps = [
    { number: 1, title: 'Información de la Cita', icon: Calendar },
    { number: 2, title: tipoRegistro === 'nuevo' ? 'Datos del Cliente' : 'Datos Existentes', icon: User },
    { number: 3, title: tipoRegistro === 'nuevo' ? 'Datos de la Mascota' : 'Confirmar', icon: Dog },
  ];

  // Validar paso actual antes de avanzar
  const validateStep = (step: number): boolean => {
    switch (step) {
      case 1:
        return !!(fechaSeleccionada && profesionalSeleccionado && watch('hora') && watch('motivo') && tipoRegistro);
      case 2:
        if (tipoRegistro === 'nuevo') {
          return !!(watch('propietarioNombre') && watch('propietarioEmail') && watch('propietarioPassword'));
        } else {
          return !!(watch('propietarioId') && watch('pacienteId'));
        }
      case 3:
        if (tipoRegistro === 'nuevo') {
          return !!(watch('pacienteNombre') && watch('pacienteEspecie'));
        }
        return true;
      default:
        return false;
    }
  };

  const handleNext = () => {
    if (validateStep(currentStep)) {
      setCurrentStep(prev => Math.min(prev + 1, steps.length));
    } else {
      toast.error('Por favor, complete todos los campos requeridos antes de continuar');
    }
  };

  const handlePrevious = () => {
    setCurrentStep(prev => Math.max(prev - 1, 1));
  };

  const canGoNext = validateStep(currentStep);

  return (
    <div className="min-h-screen bg-gradient-to-b from-primary/5 via-white to-primary/5 py-8 px-4">
      <div className="container mx-auto max-w-4xl">
        {/* Header mejorado */}
        <div className="mb-8 text-center">
          <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-primary/10 border border-primary/20 mb-4">
            <Sparkles className="h-4 w-4 text-primary" />
            <span className="text-sm font-semibold text-primary">Agendar Cita</span>
          </div>
          <h1 className="text-4xl lg:text-5xl font-bold mb-3 bg-gradient-to-r from-foreground to-foreground/70 bg-clip-text text-transparent">
            Agendar una Cita
          </h1>
          <p className="text-lg text-muted-foreground max-w-2xl mx-auto">
            Complete el formulario para agendar una cita con nuestros veterinarios
          </p>
        </div>

        {/* Indicador de progreso */}
        <Card className="mb-6 border-2 border-primary/10 shadow-lg bg-white/90 backdrop-blur-sm">
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              {steps.map((step, index) => {
                const Icon = step.icon;
                const isActive = currentStep >= step.number;
                const isCurrent = currentStep === step.number;
                
                return (
                  <div key={step.number} className="flex items-center flex-1">
                    <div className="flex flex-col items-center flex-1">
                      <div className={`relative flex items-center justify-center w-12 h-12 rounded-full transition-all duration-300 ${
                        isActive 
                          ? 'bg-gradient-to-br from-primary to-primary/70 text-white shadow-lg scale-110' 
                          : 'bg-muted text-muted-foreground'
                      } ${isCurrent ? 'ring-4 ring-primary/20' : ''}`}>
                        <Icon className="h-6 w-6" />
                        {isActive && currentStep > step.number && (
                          <CheckCircle2 className="absolute -top-1 -right-1 h-5 w-5 text-green-500 bg-white rounded-full" />
                        )}
                      </div>
                      <div className={`mt-2 text-xs font-medium text-center max-w-[100px] ${
                        isActive ? 'text-foreground' : 'text-muted-foreground'
                      }`}>
                        {step.title}
                      </div>
                    </div>
                    {index < steps.length - 1 && (
                      <div className={`flex-1 h-1 mx-2 rounded-full transition-all duration-300 ${
                        currentStep > step.number ? 'bg-gradient-to-r from-primary to-primary/70' : 'bg-muted'
                      }`} />
                    )}
                  </div>
                );
              })}
            </div>
          </CardContent>
        </Card>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
        {/* Paso 1: Información de la Cita y Tipo de Registro */}
        {currentStep === 1 && (
          <>
          <Card className="border-2 border-primary/10 shadow-xl hover:shadow-2xl transition-all duration-300 bg-white/90 backdrop-blur-sm">
          <CardHeader className="bg-gradient-to-r from-primary/5 to-transparent border-b border-primary/10">
            <CardTitle className="flex items-center gap-3 text-2xl">
              <div className="h-10 w-10 rounded-xl bg-gradient-to-br from-primary to-primary/70 flex items-center justify-center shadow-md">
                <Calendar className="h-5 w-5 text-white" />
              </div>
              Información de la Cita
            </CardTitle>
            <CardDescription className="text-base mt-2">
              Seleccione la fecha, hora y veterinario para su cita
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="fecha">Fecha *</Label>
                <Input
                  id="fecha"
                  type="date"
                  min={new Date().toISOString().split('T')[0]}
                  {...register('fecha', {
                    onChange: (e) => {
                      const fecha = e.target.value;
                      if (esDomingo(fecha)) {
                        toast.error('Los domingos la clínica está cerrada. Por favor, seleccione otro día.');
                        setValue('fecha', '');
                      }
                    }
                  })}
                />
                {errors.fecha && (
                  <p className="text-sm text-destructive">{errors.fecha.message}</p>
                )}
                {fechaSeleccionada && esDomingo(fechaSeleccionada) && (
                  <Alert className="mt-2 border-destructive/50 bg-destructive/10">
                    <AlertCircle className="h-4 w-4 text-destructive" />
                    <AlertDescription className="text-sm text-destructive">
                      Los domingos la clínica está cerrada. Por favor, seleccione otro día.
                    </AlertDescription>
                  </Alert>
                )}
                <Alert className="mt-2 border-primary/20 bg-primary/5">
                  <Info className="h-4 w-4 text-primary" />
                  <AlertDescription className="text-xs">
                    <strong>Horarios disponibles:</strong> Lunes-Viernes 8am-12m y 2pm-6pm | Sábados 8am-12m | Domingos cerrado
                  </AlertDescription>
                </Alert>
              </div>

              <div className="space-y-2">
                <Label htmlFor="hora">Hora *</Label>
                {isLoadingHoras ? (
                  <Skeleton className="h-10 w-full" />
                ) : fechaSeleccionada && !esDomingo(fechaSeleccionada) ? (
                  <Select
                    value={watch('hora')}
                    onValueChange={(value) => setValue('hora', value)}
                    disabled={!profesionalSeleccionado || isLoadingHoras}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder={
                        !profesionalSeleccionado 
                          ? "Seleccione primero un veterinario" 
                          : "Seleccione una hora"
                      } />
                    </SelectTrigger>
                    <SelectContent>
                      {getHorasDisponibles().length > 0 ? (
                        getHorasDisponibles().map((hora) => (
                          <SelectItem key={hora} value={hora}>
                            {formatHora12h(hora)}
                          </SelectItem>
                        ))
                      ) : (
                        <SelectItem value="no-horas" disabled>
                          No hay horas disponibles
                        </SelectItem>
                      )}
                    </SelectContent>
                  </Select>
                ) : (
                  <Input
                    id="hora"
                    type="text"
                    placeholder="Seleccione primero una fecha"
                    disabled
                    {...register('hora')}
                  />
                )}
                {errors.hora && (
                  <p className="text-sm text-destructive">{errors.hora.message}</p>
                )}
                {horasOcupadas.length > 0 && (
                  <Alert className="mt-2 border-amber-200 bg-amber-50">
                    <AlertCircle className="h-4 w-4 text-amber-600" />
                    <AlertDescription className="text-xs text-amber-800">
                      {horasOcupadas.length} hora(s) ocupada(s) excluida(s) de las opciones disponibles
                    </AlertDescription>
                  </Alert>
                )}
                {fechaSeleccionada && esSabado(fechaSeleccionada) && (
                  <Alert className="mt-2 border-blue-200 bg-blue-50">
                    <Info className="h-4 w-4 text-blue-600" />
                    <AlertDescription className="text-xs text-blue-800">
                      <strong>Nota:</strong> Sábados: solo disponible de 8:00 AM a 12:00 PM
                    </AlertDescription>
                  </Alert>
                )}
                {fechaSeleccionada && !esSabado(fechaSeleccionada) && !esDomingo(fechaSeleccionada) && (
                  <Alert className="mt-2 border-green-200 bg-green-50">
                    <Info className="h-4 w-4 text-green-600" />
                    <AlertDescription className="text-xs text-green-800">
                      Horarios disponibles: 8:00 AM - 12:00 PM y 2:00 PM - 6:00 PM
                    </AlertDescription>
                  </Alert>
                )}
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="profesionalId">Veterinario *</Label>
              {isLoadingVets ? (
                <Skeleton className="h-10 w-full" />
              ) : (
                <Select
                  value={watch('profesionalId')}
                  onValueChange={(value) => setValue('profesionalId', value)}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Seleccione un veterinario" />
                  </SelectTrigger>
                  <SelectContent>
                    {veterinarios.length > 0 ? (
                      veterinarios.map((vet) => (
                        <SelectItem key={vet.id} value={vet.id}>
                          {vet.nombre}
                        </SelectItem>
                      ))
                    ) : (
                      <SelectItem value="no-vets" disabled>
                        No hay veterinarios disponibles
                      </SelectItem>
                    )}
                  </SelectContent>
                </Select>
              )}
              {errors.profesionalId && (
                <p className="text-sm text-destructive">{errors.profesionalId.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="motivo">Motivo de la Consulta *</Label>
              <Textarea
                id="motivo"
                placeholder="Ej: Control anual, vacunación, consulta general..."
                {...register('motivo')}
                rows={3}
              />
              {errors.motivo && (
                <p className="text-sm text-destructive">{errors.motivo.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="observaciones">Observaciones (Opcional)</Label>
              <Textarea
                id="observaciones"
                placeholder="Información adicional que considere importante..."
                {...register('observaciones')}
                rows={2}
              />
            </div>
          </CardContent>
        </Card>

        {/* Tipo de Registro */}
        <Card className="border-2 border-primary/10 shadow-xl hover:shadow-2xl transition-all duration-300 bg-white/90 backdrop-blur-sm">
          <CardHeader className="bg-gradient-to-r from-primary/5 to-transparent border-b border-primary/10">
            <CardTitle className="flex items-center gap-3 text-2xl">
              <div className="h-10 w-10 rounded-xl bg-gradient-to-br from-secondary to-secondary/70 flex items-center justify-center shadow-md">
                <UserPlus className="h-5 w-5 text-white" />
              </div>
              Tipo de Registro
            </CardTitle>
            <CardDescription className="text-base mt-2">
              ¿Ya está registrado en nuestro sistema o es su primera vez?
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <RadioGroup
              value={tipoRegistro}
              onValueChange={(value) => setValue('tipoRegistro', value as 'existente' | 'nuevo')}
            >
              <div className="flex items-center space-x-2">
                <RadioGroupItem value="nuevo" id="nuevo" />
                <Label htmlFor="nuevo" className="cursor-pointer">
                  Cliente nuevo (registrar datos)
                </Label>
              </div>
              <div className="flex items-center space-x-2">
                <RadioGroupItem value="existente" id="existente" />
                <Label htmlFor="existente" className="cursor-pointer">
                  Ya estoy registrado (usar datos existentes)
                </Label>
              </div>
            </RadioGroup>
            {tipoRegistro === 'existente' && (
              <Alert className="mt-4 border-primary/20 bg-primary/5">
                <LogIn className="h-4 w-4 text-primary" />
                <AlertDescription>
                  <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
                    <p className="text-sm text-foreground">
                      Si ya tienes una cuenta con contraseña, puedes iniciar sesión para ver tus citas y mascotas.
                    </p>
                    <Link to="/cliente/login">
                      <Button variant="outline" size="sm" className="w-full sm:w-auto">
                        <LogIn className="h-4 w-4 mr-2" />
                        Iniciar sesión
                      </Button>
                    </Link>
                  </div>
                </AlertDescription>
              </Alert>
            )}
          </CardContent>
        </Card>
        </>
        )}

        {/* Paso 2: Datos del Cliente */}
        {currentStep === 2 && (
          <>
            {tipoRegistro === 'nuevo' ? (
              <>
            {/* Datos del Propietario */}
            <Card className="border-2 border-primary/10 shadow-xl hover:shadow-2xl transition-all duration-300 bg-white/90 backdrop-blur-sm">
              <CardHeader className="bg-gradient-to-r from-primary/5 to-transparent border-b border-primary/10">
                <CardTitle className="flex items-center gap-3 text-2xl">
                  <div className="h-10 w-10 rounded-xl bg-gradient-to-br from-primary to-primary/70 flex items-center justify-center shadow-md">
                    <User className="h-5 w-5 text-white" />
                  </div>
                  Datos del Propietario
                </CardTitle>
                <CardDescription className="text-base mt-2">
                  Complete sus datos de contacto
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="propietarioNombre">Nombre Completo *</Label>
                    <Input
                      id="propietarioNombre"
                      placeholder="Juan Pérez"
                      {...register('propietarioNombre')}
                    />
                    {errors.propietarioNombre && (
                      <p className="text-sm text-destructive">{errors.propietarioNombre.message}</p>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="propietarioEmail">
                      <Mail className="inline h-4 w-4 mr-1" />
                      Email *
                    </Label>
                    <Input
                      id="propietarioEmail"
                      type="email"
                      placeholder="juan@email.com"
                      {...register('propietarioEmail')}
                    />
                    {errors.propietarioEmail && (
                      <p className="text-sm text-destructive">{errors.propietarioEmail.message}</p>
                    )}
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="propietarioTelefono">
                      <Phone className="inline h-4 w-4 mr-1" />
                      Teléfono
                    </Label>
                    <Input
                      id="propietarioTelefono"
                      placeholder="555-1234"
                      {...register('propietarioTelefono')}
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="propietarioDocumento">Documento de Identidad</Label>
                    <Input
                      id="propietarioDocumento"
                      placeholder="12345678"
                      {...register('propietarioDocumento')}
                    />
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="propietarioDireccion">
                    <MapPin className="inline h-4 w-4 mr-1" />
                    Dirección
                  </Label>
                  <Input
                    id="propietarioDireccion"
                    placeholder="Calle Principal 123"
                    {...register('propietarioDireccion')}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="propietarioPassword">
                    <Lock className="inline h-4 w-4 mr-1" />
                    Contraseña *
                  </Label>
                  <Input
                    id="propietarioPassword"
                    type="password"
                    placeholder="Mínimo 6 caracteres"
                    {...register('propietarioPassword')}
                  />
                  <Alert className="border-green-200 bg-green-50">
                    <Info className="h-4 w-4 text-green-600" />
                    <AlertDescription className="text-xs text-green-800">
                      Con esta contraseña podrás iniciar sesión en el portal del cliente para ver tus citas y mascotas.
                    </AlertDescription>
                  </Alert>
                  {errors.propietarioPassword && (
                    <p className="text-sm text-destructive">{errors.propietarioPassword.message}</p>
                  )}
                </div>
              </CardContent>
            </Card>
          </>
            ) : (
              <Card className="border-2 border-primary/10 shadow-xl hover:shadow-2xl transition-all duration-300 bg-white/90 backdrop-blur-sm">
                <CardHeader className="bg-gradient-to-r from-primary/5 to-transparent border-b border-primary/10">
                  <CardTitle className="flex items-center gap-3 text-2xl">
                    <div className="h-10 w-10 rounded-xl bg-gradient-to-br from-primary to-primary/70 flex items-center justify-center shadow-md">
                      <BadgeIcon className="h-5 w-5 text-white" />
                    </div>
                    Datos Existentes
                  </CardTitle>
                  <CardDescription className="text-base mt-2">
                    Si ya está registrado, por favor contacte a la clínica para obtener sus IDs de propietario y paciente.
                  </CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label htmlFor="propietarioId">ID de Propietario *</Label>
                      <Input
                        id="propietarioId"
                        placeholder="Ingrese su ID de propietario"
                        {...register('propietarioId')}
                      />
                      {errors.propietarioId && (
                        <p className="text-sm text-destructive">{errors.propietarioId.message}</p>
                      )}
                    </div>

                    <div className="space-y-2">
                      <Label htmlFor="pacienteId">ID de Paciente *</Label>
                      <Input
                        id="pacienteId"
                        placeholder="Ingrese el ID de su mascota"
                        {...register('pacienteId')}
                      />
                      {errors.pacienteId && (
                        <p className="text-sm text-destructive">{errors.pacienteId.message}</p>
                      )}
                    </div>
                  </div>
                </CardContent>
              </Card>
            )}
          </>
        )}

        {/* Paso 3: Datos de la Mascota (solo para nuevos) */}
        {currentStep === 3 && tipoRegistro === 'nuevo' && (
          <Card className="border-2 border-primary/10 shadow-xl hover:shadow-2xl transition-all duration-300 bg-white/90 backdrop-blur-sm">
            <CardHeader className="bg-gradient-to-r from-primary/5 to-transparent border-b border-primary/10">
              <CardTitle className="flex items-center gap-3 text-2xl">
                <div className="h-10 w-10 rounded-xl bg-gradient-to-br from-secondary to-secondary/70 flex items-center justify-center shadow-md">
                  <Dog className="h-5 w-5 text-white" />
                </div>
                Datos de la Mascota
              </CardTitle>
              <CardDescription className="text-base mt-2">
                Información sobre su mascota
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="pacienteNombre">Nombre de la Mascota *</Label>
                  <Input
                    id="pacienteNombre"
                    placeholder="Max"
                    {...register('pacienteNombre')}
                  />
                  {errors.pacienteNombre && (
                    <p className="text-sm text-destructive">{errors.pacienteNombre.message}</p>
                  )}
                </div>

                <div className="space-y-2">
                  <Label htmlFor="pacienteEspecie">Especie *</Label>
                  <Select
                    value={watch('pacienteEspecie')}
                    onValueChange={(value) => setValue('pacienteEspecie', value)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Seleccione especie" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="Perro">Perro</SelectItem>
                      <SelectItem value="Gato">Gato</SelectItem>
                      <SelectItem value="Ave">Ave</SelectItem>
                      <SelectItem value="Roedor">Roedor</SelectItem>
                      <SelectItem value="Reptil">Reptil</SelectItem>
                      <SelectItem value="Otro">Otro</SelectItem>
                    </SelectContent>
                  </Select>
                  {errors.pacienteEspecie && (
                    <p className="text-sm text-destructive">{errors.pacienteEspecie.message}</p>
                  )}
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="pacienteRaza">Raza</Label>
                  <Input
                    id="pacienteRaza"
                    placeholder="Labrador"
                    {...register('pacienteRaza')}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="pacienteSexo">Sexo</Label>
                  <Select
                    value={watch('pacienteSexo')}
                    onValueChange={(value) => setValue('pacienteSexo', value)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Seleccione" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="M">Macho</SelectItem>
                      <SelectItem value="F">Hembra</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="pacienteEdadMeses">Edad (meses)</Label>
                  <Input
                    id="pacienteEdadMeses"
                    type="number"
                    min="0"
                    placeholder="36"
                    {...register('pacienteEdadMeses')}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="pacientePesoKg">Peso (kg)</Label>
                  <Input
                    id="pacientePesoKg"
                    type="number"
                    step="0.1"
                    min="0"
                    placeholder="30.5"
                    {...register('pacientePesoKg')}
                  />
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="pacienteNotas">
                  <FileText className="inline h-4 w-4 mr-1" />
                  Notas Adicionales
                </Label>
                <Textarea
                  id="pacienteNotas"
                  placeholder="Alergias, condiciones especiales, etc."
                  {...register('pacienteNotas')}
                  rows={2}
                />
              </div>
            </CardContent>
          </Card>
        )}

        {/* Navegación entre pasos */}
        <div className="flex flex-col sm:flex-row justify-between items-center gap-4 pt-6 border-t border-primary/10">
          <div className="flex gap-4 w-full sm:w-auto">
            {currentStep > 1 && (
              <Button
                type="button"
                variant="outline"
                onClick={handlePrevious}
                className="flex-1 sm:flex-initial"
              >
                <ArrowLeft className="h-4 w-4 mr-2" />
                Anterior
              </Button>
            )}
            <Button
              type="button"
              variant="outline"
              onClick={() => window.location.href = '/'}
              className="flex-1 sm:flex-initial"
            >
              Cancelar
            </Button>
          </div>
          
          {currentStep < (tipoRegistro === 'existente' ? 2 : steps.length) ? (
            <Button 
              type="button"
              onClick={handleNext}
              disabled={!canGoNext}
              size="lg"
              className="w-full sm:w-auto shadow-lg hover:shadow-xl transition-all hover:scale-105"
            >
              Siguiente
              <ArrowRight className="h-4 w-4 ml-2" />
            </Button>
          ) : (
            <Button 
              type="submit" 
              disabled={isLoading}
              size="lg"
              className="w-full sm:w-auto shadow-lg hover:shadow-xl transition-all hover:scale-105"
            >
              {isLoading ? (
                <>
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                  Agendando...
                </>
              ) : (
                <>
                  Agendar Cita
                  <ArrowRight className="h-4 w-4 ml-2" />
                </>
              )}
            </Button>
          )}
        </div>
      </form>
      </div>
    </div>
  );
}

