import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { Calendar, Clock, User, Mail, Phone, MapPin, FileText, Dog, UserPlus, CheckCircle2 } from 'lucide-react';
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
  // Datos nuevos de paciente
  pacienteNombre: z.string().optional(),
  pacienteEspecie: z.string().optional(),
  pacienteRaza: z.string().optional(),
  pacienteSexo: z.string().optional(),
  pacienteEdadMeses: z.string().optional(),
  pacientePesoKg: z.string().optional(),
  pacienteMicrochip: z.string().optional(),
  pacienteNotas: z.string().optional(),
}).refine((data) => {
  if (data.tipoRegistro === 'existente') {
    return !!data.propietarioId && !!data.pacienteId;
  } else {
    return !!data.propietarioNombre && !!data.propietarioEmail && !!data.pacienteNombre && !!data.pacienteEspecie;
  }
}, {
  message: 'Complete todos los campos requeridos',
  path: ['tipoRegistro'],
});

type CitaPublicaFormData = z.infer<typeof citaPublicaSchema>;

export default function AgendarCitaPublica() {
  const [veterinarios, setVeterinarios] = useState<Usuario[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isLoadingVets, setIsLoadingVets] = useState(true);
  const [citaCreada, setCitaCreada] = useState(false);
  const [citaId, setCitaId] = useState<string | null>(null);

  const { register, handleSubmit, watch, setValue, formState: { errors } } = useForm<CitaPublicaFormData>({
    resolver: zodResolver(citaPublicaSchema),
    defaultValues: {
      tipoRegistro: 'nuevo',
    },
  });

  const tipoRegistro = watch('tipoRegistro');
  const fechaSeleccionada = watch('fecha');

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

  // Función para obtener las horas disponibles según el día
  const getHorasDisponibles = (): string[] => {
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

  // Función para deshabilitar domingos en el input de fecha
  const isDateDisabled = (date: Date): boolean => {
    return date.getDay() === 0; // Deshabilitar domingos
  };

  useEffect(() => {
    loadVeterinarios();
  }, []);

  // Resetear hora cuando cambia la fecha
  useEffect(() => {
    if (fechaSeleccionada) {
      const horasDisponibles = getHorasDisponibles();
      const horaActual = watch('hora');
      
      // Si la hora actual no está en las horas disponibles, resetear
      if (horaActual && !horasDisponibles.includes(horaActual)) {
        setValue('hora', '');
      }
    }
  }, [fechaSeleccionada]);

  const loadVeterinarios = async () => {
    try {
      setIsLoadingVets(true);
      const vets = await citaPublicaService.getVeterinarios();
      setVeterinarios(vets);
    } catch (error: any) {
      console.error('Error al cargar veterinarios:', error);
      toast.error('Error al cargar veterinarios disponibles');
    } finally {
      setIsLoadingVets(false);
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
          },
          pacienteNuevo: {
            nombre: data.pacienteNombre!,
            especie: data.pacienteEspecie!,
            raza: data.pacienteRaza || undefined,
            sexo: data.pacienteSexo || undefined,
            edadMeses: data.pacienteEdadMeses ? parseInt(data.pacienteEdadMeses) : undefined,
            pesoKg: data.pacientePesoKg ? parseFloat(data.pacientePesoKg) : undefined,
            microchip: data.pacienteMicrochip || undefined,
            notas: data.pacienteNotas || undefined,
          },
        };
      }

      const cita = await citaPublicaService.crearCita(request);
      setCitaId(cita.id);
      setCitaCreada(true);
      toast.success('¡Cita agendada exitosamente!');
    } catch (error: any) {
      console.error('Error al agendar cita:', error);
      const errorMessage = error.response?.data?.mensaje || error.response?.data?.message || 'Error al agendar la cita';
      toast.error(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  if (citaCreada) {
    return (
      <div className="container mx-auto py-8 px-4 max-w-2xl">
        <Card className="border-green-200 bg-green-50">
          <CardHeader className="text-center">
            <div className="flex justify-center mb-4">
              <CheckCircle2 className="h-16 w-16 text-green-600" />
            </div>
            <CardTitle className="text-2xl text-green-800">¡Cita Agendada Exitosamente!</CardTitle>
            <CardDescription className="text-lg mt-2">
              Su cita ha sido registrada con el ID: <strong>{citaId}</strong>
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <p className="text-center text-muted-foreground">
              Recibirá una confirmación por correo electrónico. Por favor, llegue 10 minutos antes de su cita.
            </p>
            <div className="flex justify-center gap-4">
              <Button onClick={() => window.location.reload()}>
                Agendar Otra Cita
              </Button>
              <Button variant="outline" onClick={() => window.location.href = '/'}>
                Volver al Inicio
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="container mx-auto py-8 px-4 max-w-4xl">
      <div className="mb-8 text-center">
        <h1 className="text-3xl font-bold mb-2">Agendar una Cita</h1>
        <p className="text-muted-foreground">
          Complete el formulario para agendar una cita con nuestros veterinarios
        </p>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
        {/* Información de la Cita */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Calendar className="h-5 w-5" />
              Información de la Cita
            </CardTitle>
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
                  <p className="text-sm text-destructive">Los domingos la clínica está cerrada</p>
                )}
                <p className="text-xs text-muted-foreground">
                  Horarios: Lunes-Viernes 8am-12m y 2pm-6pm | Sábados 8am-12m | Domingos cerrado
                </p>
              </div>

              <div className="space-y-2">
                <Label htmlFor="hora">Hora *</Label>
                {fechaSeleccionada && !esDomingo(fechaSeleccionada) ? (
                  <Select
                    value={watch('hora')}
                    onValueChange={(value) => setValue('hora', value)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Seleccione una hora" />
                    </SelectTrigger>
                    <SelectContent>
                      {getHorasDisponibles().map((hora) => (
                        <SelectItem key={hora} value={hora}>
                          {formatHora12h(hora)}
                        </SelectItem>
                      ))}
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
                {fechaSeleccionada && esSabado(fechaSeleccionada) && (
                  <p className="text-xs text-muted-foreground">
                    Sábados: solo disponible de 8:00 AM a 12:00 PM
                  </p>
                )}
                {fechaSeleccionada && !esSabado(fechaSeleccionada) && !esDomingo(fechaSeleccionada) && (
                  <p className="text-xs text-muted-foreground">
                    Horarios disponibles: 8:00 AM - 12:00 PM y 2:00 PM - 6:00 PM
                  </p>
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
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <UserPlus className="h-5 w-5" />
              Tipo de Registro
            </CardTitle>
            <CardDescription>
              ¿Ya está registrado en nuestro sistema o es su primera vez?
            </CardDescription>
          </CardHeader>
          <CardContent>
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
          </CardContent>
        </Card>

        {tipoRegistro === 'nuevo' ? (
          <>
            {/* Datos del Propietario */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <User className="h-5 w-5" />
                  Datos del Propietario
                </CardTitle>
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
              </CardContent>
            </Card>

            {/* Datos del Paciente */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Dog className="h-5 w-5" />
                  Datos de la Mascota
                </CardTitle>
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

                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
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

                  <div className="space-y-2">
                    <Label htmlFor="pacienteMicrochip">Microchip</Label>
                    <Input
                      id="pacienteMicrochip"
                      placeholder="123456789012345"
                      {...register('pacienteMicrochip')}
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
          </>
        ) : (
          <Card>
            <CardHeader>
              <CardTitle>Datos Existentes</CardTitle>
              <CardDescription>
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

        <div className="flex justify-end gap-4">
          <Button
            type="button"
            variant="outline"
            onClick={() => window.location.href = '/'}
          >
            Cancelar
          </Button>
          <Button type="submit" disabled={isLoading}>
            {isLoading ? 'Agendando...' : 'Agendar Cita'}
          </Button>
        </div>
      </form>
    </div>
  );
}

