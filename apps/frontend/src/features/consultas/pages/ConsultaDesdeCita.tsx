import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { ArrowLeft, Save, FileText, AlertCircle, Loader2, CheckCircle2 } from 'lucide-react';
import { Button } from '@shared/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Textarea } from '@shared/components/ui/textarea';
import { Skeleton } from '@shared/components/ui/skeleton';
import { Checkbox } from '@shared/components/ui/checkbox';
import { Label } from '@shared/components/ui/label';
import { toast } from 'sonner';
import { useAuth } from '@core/auth/AuthContext';
import { citaService } from '@features/agenda/services/citaService';
import { pacienteService } from '@features/pacientes/services/pacienteService';
import { propietarioService } from '@features/propietarios/services/propietarioService';
import { consultaService } from '@features/historias/services/consultaService';
import { SignosVitalesForm } from '../components/SignosVitalesForm';
import { InfoPacientePanel } from '../components/InfoPacientePanel';
import { HistorialRapido } from '../components/HistorialRapido';
import { Cita, Paciente, Propietario } from '@core/types';
import { AxiosError } from 'axios';

const consultaSchema = z.object({
  frecuenciaCardiaca: z.number().positive().optional().or(z.literal('')),
  frecuenciaRespiratoria: z.number().positive().optional().or(z.literal('')),
  temperatura: z.number().positive().optional().or(z.literal('')),
  pesoKg: z.number().positive().optional().or(z.literal('')),
  examenFisico: z.string().max(2000).optional(),
  diagnostico: z.string().max(500).optional(),
  tratamiento: z.string().max(1000).optional(),
  observaciones: z.string().max(1000).optional(),
});

type ConsultaFormData = z.infer<typeof consultaSchema>;

export default function ConsultaDesdeCita() {
  const { citaId } = useParams<{ citaId: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  
  const [cita, setCita] = useState<Cita | null>(null);
  const [paciente, setPaciente] = useState<Paciente | null>(null);
  const [propietario, setPropietario] = useState<Propietario | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isLoadingData, setIsLoadingData] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [marcarCitaCompletada, setMarcarCitaCompletada] = useState(true);

  const { register, handleSubmit, formState: { errors } } = useForm<ConsultaFormData>({
    resolver: zodResolver(consultaSchema),
  });

  useEffect(() => {
    if (citaId) {
      loadCitaData();
    }
  }, [citaId]);

  const loadCitaData = async () => {
    if (!citaId) return;

    try {
      setIsLoadingData(true);
      setError(null);

      // Cargar cita
      const citaData = await citaService.getById(citaId);
      setCita(citaData);

      // Validar que el usuario tenga permisos (VET o ADMIN)
      if (user?.rol !== 'VET' && user?.rol !== 'ADMIN') {
        setError('Solo los veterinarios pueden crear consultas');
        toast.error('No tienes permisos para crear consultas');
        return;
      }

      // Validar que la cita esté en un estado válido para crear consulta
      if (citaData.estado === 'CANCELADA') {
        setError('No se puede crear una consulta para una cita cancelada');
        toast.error('La cita está cancelada');
        return;
      }

      if (citaData.estado === 'ATENDIDA') {
        setError('Esta cita ya fue atendida');
        toast.warning('La cita ya fue atendida anteriormente');
        // No retornamos, permitimos crear otra consulta si es necesario
      }

      // Cargar datos relacionados
      const [pacienteData, propietarioData] = await Promise.allSettled([
        pacienteService.getById(citaData.pacienteId),
        propietarioService.getById(citaData.propietarioId),
      ]);

      if (pacienteData.status === 'fulfilled') {
        setPaciente(pacienteData.value);
      }

      if (propietarioData.status === 'fulfilled') {
        setPropietario(propietarioData.value);
      }
    } catch (error) {
      console.error('Error al cargar datos de la cita:', error);
      const axiosError = error as AxiosError<{ message?: string }>;
      const statusCode = axiosError?.response?.status;
      const errorMessage = axiosError?.response?.data?.message || 'Error al cargar los datos de la cita';

      if (statusCode === 404) {
        setError('Cita no encontrada');
      } else if (statusCode === 403) {
        setError('No tienes permisos para ver esta cita');
        toast.error('No tienes permisos para ver esta cita');
      } else {
        setError(errorMessage);
        toast.error(errorMessage);
      }
    } finally {
      setIsLoadingData(false);
    }
  };

  const onSubmit = async (data: ConsultaFormData) => {
    if (!cita || !paciente || !user?.id) {
      toast.error('Error: Faltan datos necesarios');
      return;
    }

    // Validar que al menos haya algún dato de la consulta
    const hasData =
      data.frecuenciaCardiaca ||
      data.frecuenciaRespiratoria ||
      data.temperatura ||
      data.pesoKg ||
      data.examenFisico ||
      data.diagnostico ||
      data.tratamiento ||
      data.observaciones;

    if (!hasData) {
      toast.error('Error: Debes completar al menos un campo de la consulta');
      return;
    }

    try {
      setIsLoading(true);

      // Crear la consulta
      const consultaData = {
        pacienteId: cita.pacienteId,
        profesionalId: user.id,
        fecha: new Date().toISOString(),
        frecuenciaCardiaca: data.frecuenciaCardiaca || undefined,
        frecuenciaRespiratoria: data.frecuenciaRespiratoria || undefined,
        temperatura: data.temperatura || undefined,
        pesoKg: data.pesoKg || undefined,
        examenFisico: data.examenFisico?.trim() || undefined,
        diagnostico: data.diagnostico?.trim() || undefined,
        tratamiento: data.tratamiento?.trim() || undefined,
        observaciones: data.observaciones?.trim() || undefined,
      };

      await consultaService.create(consultaData);
      toast.success('Consulta registrada exitosamente');

      // Marcar cita como atendida si está seleccionado
      if (marcarCitaCompletada && cita.estado !== 'ATENDIDA') {
        try {
          await citaService.updateEstado(cita.id, 'ATENDIDA');
          toast.success('Cita marcada como atendida');
        } catch (error) {
          console.error('Error al marcar cita como atendida:', error);
          // No mostramos error porque la consulta ya se guardó exitosamente
        }
      }

      // Navegar de vuelta al detalle de la cita
      navigate(`/agenda/${citaId}`);
    } catch (error) {
      console.error('Error al guardar consulta:', error);
      const axiosError = error as AxiosError<{ message?: string }>;
      const statusCode = axiosError?.response?.status;
      const errorMessage = axiosError?.response?.data?.message;

      if (statusCode === 400) {
        toast.error(errorMessage || 'Error de validación: Verifica que todos los campos sean correctos');
      } else if (statusCode === 403) {
        toast.error('No tienes permisos para crear consultas');
      } else if (statusCode === 404) {
        toast.error('El paciente no existe');
      } else {
        toast.error(errorMessage || 'Error al registrar la consulta. Por favor, intenta nuevamente');
      }
    } finally {
      setIsLoading(false);
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
        <div className="grid gap-6 lg:grid-cols-3">
          <div className="lg:col-span-2 space-y-6">
            <Skeleton className="h-64 w-full" />
            <Skeleton className="h-64 w-full" />
          </div>
          <div className="space-y-6">
            <Skeleton className="h-64 w-full" />
            <Skeleton className="h-64 w-full" />
          </div>
        </div>
      </div>
    );
  }

  if (!isLoadingData && (!cita || !paciente || error)) {
    return (
      <div className="text-center py-12">
        <div className="rounded-full bg-destructive/10 p-4 mb-4 inline-block">
          <AlertCircle className="h-8 w-8 text-destructive" />
        </div>
        <h3 className="text-lg font-medium mb-2">Error al cargar datos</h3>
        <p className="text-muted-foreground mb-4">
          {error || 'No se pudieron cargar los datos de la cita.'}
        </p>
        <div className="flex gap-3 justify-center">
          <Button onClick={() => navigate('/agenda')} variant="outline">
            Volver a Agenda
          </Button>
          {citaId && (
            <Button onClick={() => navigate(`/agenda/${citaId}`)} variant="outline">
              Ver Detalle de Cita
            </Button>
          )}
          {error && (
            <Button onClick={loadCitaData}>
              Reintentar
            </Button>
          )}
        </div>
      </div>
    );
  }

  const fechaCita = cita ? new Date(cita.fecha) : undefined;

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate(`/agenda/${citaId}`)}>
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <div>
          <h1 className="text-3xl font-bold text-foreground">Nueva Consulta</h1>
          <p className="text-muted-foreground mt-1">
            Cita del {fechaCita && new Date(fechaCita).toLocaleDateString('es-ES', { 
              weekday: 'long', 
              year: 'numeric', 
              month: 'long', 
              day: 'numeric' 
            })}
          </p>
        </div>
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        {/* Formulario Principal */}
        <div className="lg:col-span-2 space-y-6">
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>Signos Vitales</CardTitle>
              </CardHeader>
              <CardContent>
                <SignosVitalesForm register={register} errors={errors} />
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Examen Físico</CardTitle>
              </CardHeader>
              <CardContent>
                <Textarea
                  {...register('examenFisico')}
                  placeholder="Descripción detallada del examen físico..."
                  rows={6}
                />
                {errors.examenFisico && (
                  <p className="text-sm text-destructive mt-1">{errors.examenFisico.message}</p>
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Diagnóstico</CardTitle>
              </CardHeader>
              <CardContent>
                <Textarea
                  {...register('diagnostico')}
                  placeholder="Diagnóstico de la consulta..."
                  rows={3}
                />
                {errors.diagnostico && (
                  <p className="text-sm text-destructive mt-1">{errors.diagnostico.message}</p>
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Tratamiento</CardTitle>
              </CardHeader>
              <CardContent>
                <Textarea
                  {...register('tratamiento')}
                  placeholder="Tratamiento prescrito..."
                  rows={4}
                />
                {errors.tratamiento && (
                  <p className="text-sm text-destructive mt-1">{errors.tratamiento.message}</p>
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Observaciones</CardTitle>
              </CardHeader>
              <CardContent>
                <Textarea
                  {...register('observaciones')}
                  placeholder="Observaciones adicionales..."
                  rows={3}
                />
                {errors.observaciones && (
                  <p className="text-sm text-destructive mt-1">{errors.observaciones.message}</p>
                )}
              </CardContent>
            </Card>

            {/* Opción para marcar cita como atendida */}
            {cita && cita.estado !== 'ATENDIDA' && (
              <Card>
                <CardContent className="pt-6">
                  <div className="flex items-center space-x-2">
                    <Checkbox
                      id="marcarCompletada"
                      checked={marcarCitaCompletada}
                      onCheckedChange={(checked) => setMarcarCitaCompletada(checked === true)}
                    />
                    <Label
                      htmlFor="marcarCompletada"
                      className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70 cursor-pointer"
                    >
                      Marcar cita como atendida al guardar
                    </Label>
                  </div>
                </CardContent>
              </Card>
            )}

            <div className="flex justify-end gap-3">
              <Button
                type="button"
                variant="outline"
                onClick={() => navigate(`/agenda/${citaId}`)}
                disabled={isLoading}
              >
                Cancelar
              </Button>
              <Button type="submit" className="gap-2" disabled={isLoading || isLoadingData}>
                {isLoading ? (
                  <>
                    <Loader2 className="h-4 w-4 animate-spin" />
                    Guardando...
                  </>
                ) : (
                  <>
                    <Save className="h-4 w-4" />
                    Guardar Consulta
                  </>
                )}
              </Button>
            </div>
          </form>
        </div>

        {/* Panel Lateral */}
        <div className="space-y-6">
          {paciente && (
            <InfoPacientePanel
              paciente={paciente}
              propietario={propietario || undefined}
              fechaCita={fechaCita}
            />
          )}
          {paciente && <HistorialRapido pacienteId={paciente.id} />}
        </div>
      </div>
    </div>
  );
}

