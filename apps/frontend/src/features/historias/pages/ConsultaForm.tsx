import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { ArrowLeft, Save, FileText } from 'lucide-react';
import { Button } from '@shared/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Input } from '@shared/components/ui/input';
import { Label } from '@shared/components/ui/label';
import { Textarea } from '@shared/components/ui/textarea';
import { Skeleton } from '@shared/components/ui/skeleton';
import { toast } from 'sonner';
import { useAuth } from '@core/auth/AuthContext';
import { pacienteService } from '@features/pacientes/services/pacienteService';
import { consultaService } from '@features/historias/services/consultaService';
import { Paciente } from '@core/types';

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

export default function ConsultaForm() {
  const { pacienteId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [paciente, setPaciente] = useState<Paciente | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isLoadingData, setIsLoadingData] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const { register, handleSubmit, formState: { errors } } = useForm<ConsultaFormData>({
    resolver: zodResolver(consultaSchema),
  });

  useEffect(() => {
    if (pacienteId) {
      loadPaciente();
    }
  }, [pacienteId]);

  const loadPaciente = async () => {
    if (!pacienteId) return;
    
    try {
      setIsLoadingData(true);
      setError(null);
      const pacienteData = await pacienteService.getById(pacienteId);
      setPaciente(pacienteData);
    } catch (error: any) {
      console.error('Error al cargar paciente:', error);
      const statusCode = error?.response?.status;
      const errorMessage = error?.response?.data?.message || 'Error al cargar los datos del paciente';
      
      if (statusCode === 404) {
        setError('Paciente no encontrado');
      } else if (statusCode === 403) {
        setError('No tienes permisos para ver este paciente');
        toast.error('No tienes permisos para ver este paciente');
      } else {
        setError(errorMessage);
        toast.error(errorMessage);
      }
    } finally {
      setIsLoadingData(false);
    }
  };

  const onSubmit = async (data: ConsultaFormData) => {
    // Validaciones previas
    if (!pacienteId) {
      toast.error('Error: ID de paciente no válido');
      return;
    }

    if (!user?.id) {
      toast.error('Error: Debes estar autenticado para crear una consulta');
      return;
    }

    // Validar que el usuario tenga rol adecuado (VET o ADMIN)
    if (user.rol !== 'VET' && user.rol !== 'ADMIN') {
      toast.error('Error: Solo los veterinarios pueden crear consultas');
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
      
      // Preparar datos para enviar
      const consultaData = {
        pacienteId,
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
      navigate(`/historias/${pacienteId}`);
    } catch (error: any) {
      console.error('Error al guardar consulta:', error);
      const statusCode = error?.response?.status;
      const errorMessage = error?.response?.data?.message;
      
      if (statusCode === 400) {
        // Error de validación del backend
        toast.error(errorMessage || 'Error de validación: Verifica que todos los campos sean correctos');
      } else if (statusCode === 403) {
        toast.error('No tienes permisos para crear consultas');
      } else if (statusCode === 404) {
        toast.error('El paciente no existe');
      } else if (statusCode === 500) {
        toast.error('Error del servidor. Por favor, intenta nuevamente');
      } else if (error?.code === 'ECONNABORTED' || error?.message?.includes('timeout')) {
        toast.error('La solicitud tardó demasiado. Por favor, intenta nuevamente');
      } else if (error?.message?.includes('Network Error')) {
        toast.error('Error de conexión. Verifica tu conexión a internet');
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
        <Card>
          <CardHeader>
            <Skeleton className="h-6 w-48" />
          </CardHeader>
          <CardContent>
            <div className="grid gap-4 md:grid-cols-4">
              <Skeleton className="h-10 w-full" />
              <Skeleton className="h-10 w-full" />
              <Skeleton className="h-10 w-full" />
              <Skeleton className="h-10 w-full" />
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (!isLoadingData && !paciente) {
    return (
      <div className="text-center py-12">
        <div className="rounded-full bg-destructive/10 p-4 mb-4 inline-block">
          <FileText className="h-8 w-8 text-destructive" />
        </div>
        <h3 className="text-lg font-medium mb-2">Paciente no encontrado</h3>
        <p className="text-muted-foreground mb-4">
          {error || 'El paciente que buscas no existe o no tienes permisos para verlo.'}
        </p>
        <div className="flex gap-3 justify-center">
          <Button onClick={() => navigate('/historias')} variant="outline">
            Volver a Historias Clínicas
          </Button>
          {error && (
            <Button onClick={loadPaciente}>
              Reintentar
            </Button>
          )}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate(`/historias/${pacienteId}`)}>
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <div>
          <h1 className="text-3xl font-bold text-foreground">Nueva Consulta</h1>
          <p className="text-muted-foreground mt-1">Paciente: {paciente.nombre}</p>
        </div>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>Signos Vitales</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid gap-4 md:grid-cols-4">
              <div className="space-y-2">
                <Label htmlFor="frecuenciaCardiaca">FC (lpm)</Label>
                <Input
                  id="frecuenciaCardiaca"
                  type="number"
                  min="0"
                  {...register('frecuenciaCardiaca', { valueAsNumber: true })}
                  placeholder="Frecuencia cardíaca"
                />
                {errors.frecuenciaCardiaca && (
                  <p className="text-sm text-destructive">{errors.frecuenciaCardiaca.message}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="frecuenciaRespiratoria">FR (rpm)</Label>
                <Input
                  id="frecuenciaRespiratoria"
                  type="number"
                  min="0"
                  {...register('frecuenciaRespiratoria', { valueAsNumber: true })}
                  placeholder="Frecuencia respiratoria"
                />
                {errors.frecuenciaRespiratoria && (
                  <p className="text-sm text-destructive">{errors.frecuenciaRespiratoria.message}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="temperatura">Temperatura (°C)</Label>
                <Input
                  id="temperatura"
                  type="number"
                  step="0.1"
                  min="0"
                  {...register('temperatura', { valueAsNumber: true })}
                  placeholder="Temperatura"
                />
                {errors.temperatura && (
                  <p className="text-sm text-destructive">{errors.temperatura.message}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="pesoKg">Peso (kg)</Label>
                <Input
                  id="pesoKg"
                  type="number"
                  step="0.1"
                  min="0"
                  {...register('pesoKg', { valueAsNumber: true })}
                  placeholder="Peso"
                />
                {errors.pesoKg && (
                  <p className="text-sm text-destructive">{errors.pesoKg.message}</p>
                )}
              </div>
            </div>
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

        <div className="flex justify-end gap-3">
          <Button type="button" variant="outline" onClick={() => navigate(`/historias/${pacienteId}`)}>
            Cancelar
          </Button>
          <Button type="submit" className="gap-2" disabled={isLoading || isLoadingData}>
            <Save className="h-4 w-4" />
            {isLoading ? 'Guardando...' : 'Guardar Consulta'}
          </Button>
        </div>
      </form>
    </div>
  );
}
