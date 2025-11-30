import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Plus, Calendar, User, Activity, FileText, Pill, Download } from 'lucide-react';
import { Button } from '@shared/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Badge } from '@shared/components/ui/badge';
import { Separator } from '@shared/components/ui/separator';
import { Skeleton } from '@shared/components/ui/skeleton';
import { toast } from 'sonner';
import { pacienteService } from '@features/pacientes/services/pacienteService';
import { consultaService } from '@features/historias/services/consultaService';
import { propietarioService } from '@features/propietarios/services/propietarioService';
import { Paciente, Consulta, Propietario } from '@core/types';
import { useLogger } from '@shared/hooks/useLogger';
import { pdfService } from '@shared/services/pdfService';

export default function HistoriaDetalle() {
  const logger = useLogger('HistoriaDetalle');
  const { id } = useParams();
  const navigate = useNavigate();
  const [paciente, setPaciente] = useState<Paciente | null>(null);
  const [propietario, setPropietario] = useState<Propietario | null>(null);
  const [consultas, setConsultas] = useState<Consulta[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (id) {
      loadData();
    }
  }, [id]);

  const loadData = async () => {
    if (!id) return;
    
    try {
      setIsLoading(true);
      setError(null);
      
      // Cargar datos en paralelo, pero manejar errores individualmente
      const results = await Promise.allSettled([
        pacienteService.getById(id),
        consultaService.getByPaciente(id),
      ]);

      // Procesar resultados
      if (results[0].status === 'fulfilled') {
        const pacienteData = results[0].value;
        setPaciente(pacienteData);
        
        // Cargar propietario si está disponible
        if (pacienteData.propietarioId) {
          try {
            const propietarioData = await propietarioService.getById(pacienteData.propietarioId);
            setPropietario(propietarioData);
          } catch (error) {
            logger.warn('Error al cargar propietario', {
              action: 'loadPropietario',
              propietarioId: pacienteData.propietarioId,
            });
          }
        }
      } else {
        const error = results[0].reason;
        logger.error('Error al cargar paciente para historia clínica', error, {
          action: 'loadPaciente',
          pacienteId: id,
        });
        const errorMessage = error?.response?.data?.message || 'Error al cargar el paciente';
        const statusCode = error?.response?.status;
        
        if (statusCode === 404) {
          setError('Paciente no encontrado');
        } else if (statusCode === 403) {
          setError('No tienes permisos para ver este paciente');
          toast.error('No tienes permisos para ver este paciente');
        } else {
          setError(errorMessage);
          toast.error(errorMessage);
        }
        return;
      }

      if (results[1].status === 'fulfilled') {
        setConsultas(results[1].value);
      } else {
        const error = results[1].reason;
        logger.warn('Error al cargar consultas de la historia clínica', {
          action: 'loadConsultas',
          pacienteId: id,
        });
        const errorMessage = error?.response?.data?.message || 'Error al cargar las consultas';
        const statusCode = error?.response?.status;
        
        if (statusCode === 403) {
          toast.error('No tienes permisos para ver las consultas');
        } else {
          toast.error(errorMessage);
        }
        // Continuar mostrando el paciente aunque fallen las consultas
        setConsultas([]);
      }
    } catch (error: any) {
      logger.error('Error inesperado al cargar historia clínica', error, {
        action: 'loadData',
        pacienteId: id,
      });
      const errorMessage = error?.response?.data?.message || 'Error inesperado al cargar la historia clínica';
      setError(errorMessage);
      toast.error(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading) {
    return (
      <div className="space-y-6">
        <div className="flex items-center gap-4">
          <Skeleton className="h-10 w-10 rounded-md" />
          <div className="space-y-2">
            <Skeleton className="h-8 w-64" />
            <Skeleton className="h-4 w-48" />
          </div>
          <Skeleton className="h-10 w-32 ml-auto" />
        </div>
        <Card>
          <CardHeader>
            <Skeleton className="h-6 w-48" />
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              <Skeleton className="h-16 w-full" />
              <Skeleton className="h-16 w-full" />
              <Skeleton className="h-16 w-full" />
              <Skeleton className="h-16 w-full" />
            </div>
          </CardContent>
        </Card>
        <div className="space-y-4">
          <Skeleton className="h-6 w-48" />
          <Card>
            <CardHeader>
              <Skeleton className="h-6 w-64" />
            </CardHeader>
            <CardContent>
              <Skeleton className="h-32 w-full" />
            </CardContent>
          </Card>
        </div>
      </div>
    );
  }

  if (!isLoading && !paciente) {
    return (
      <div className="text-center py-12">
        <div className="rounded-full bg-destructive/10 p-4 mb-4 inline-block">
          <FileText className="h-8 w-8 text-destructive" />
        </div>
        <h2 className="text-2xl font-bold mb-2">Paciente no encontrado</h2>
        <p className="text-muted-foreground mb-4">
          {error || 'El paciente que buscas no existe o no tienes permisos para verlo.'}
        </p>
        <div className="flex gap-3 justify-center">
          <Button onClick={() => navigate('/historias')} variant="outline">
            Volver a Historias Clínicas
          </Button>
          {error && (
            <Button onClick={loadData}>
              Reintentar
            </Button>
          )}
        </div>
      </div>
    );
  }

  const handleExportPDF = () => {
    if (!paciente) {
      toast.error('No se puede exportar: falta información del paciente');
      return;
    }

    try {
      toast.loading('Generando PDF...', { id: 'pdf-historial' });
      pdfService.generarHistorialClinico(paciente, propietario, consultas);
      toast.success('PDF generado exitosamente', { id: 'pdf-historial' });
    } catch (error: any) {
      logger.error('Error al generar PDF del historial clínico', error, {
        action: 'exportPDF',
        pacienteId: id,
      });
      toast.error('Error al generar el PDF', { id: 'pdf-historial' });
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate('/historias')}>
          <ArrowLeft className="h-5 w-5" />
        </Button>
        <div className="flex-1">
          <h1 className="text-3xl font-bold text-foreground">Historia Clínica - {paciente.nombre}</h1>
          <p className="text-muted-foreground mt-1">{paciente.especie} • {paciente.raza}</p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" className="gap-2" onClick={handleExportPDF}>
            <Download className="h-4 w-4" />
            Exportar PDF
          </Button>
          <Button className="gap-2" onClick={() => navigate(`/historias/${id}/nueva-consulta`)}>
            <Plus className="h-4 w-4" />
            Nueva Consulta
          </Button>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Información del Paciente</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div>
              <p className="text-sm text-muted-foreground">Propietario</p>
              <p className="font-medium">{paciente.propietarioNombre || 'Sin propietario'}</p>
            </div>
            <div>
              <p className="text-sm text-muted-foreground">Sexo</p>
              <p className="font-medium">{paciente.sexo === 'M' ? 'Macho' : 'Hembra'}</p>
            </div>
            <div>
              <p className="text-sm text-muted-foreground">Edad</p>
              <p className="font-medium">
                {paciente.edadMeses ? `${Math.floor(paciente.edadMeses / 12)}a ${paciente.edadMeses % 12}m` : 'N/A'}
              </p>
            </div>
            <div>
              <p className="text-sm text-muted-foreground">Peso Actual</p>
              <p className="font-medium">{paciente.pesoKg ? `${paciente.pesoKg} kg` : 'N/A'}</p>
            </div>
          </div>
        </CardContent>
      </Card>

      <div>
        <h2 className="text-xl font-semibold mb-4">Timeline de Consultas</h2>
        <div className="space-y-4">
          {consultas.map((consulta, index) => {
            return (
              <Card key={consulta.id} className="relative">
                {index !== consultas.length - 1 && (
                  <div className="absolute left-6 top-16 bottom-0 w-0.5 bg-border -mb-4" />
                )}
                <CardHeader>
                  <div className="flex items-start justify-between">
                    <div className="flex items-center gap-3">
                      <div className="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center relative z-10">
                        <Activity className="h-5 w-5 text-primary" />
                      </div>
                      <div>
                        <CardTitle className="text-lg">
                          Consulta - {new Date(consulta.fecha).toLocaleDateString('es-ES', { 
                            day: '2-digit', 
                            month: 'long', 
                            year: 'numeric',
                            hour: '2-digit',
                            minute: '2-digit'
                          })}
                        </CardTitle>
                        <div className="flex items-center gap-2 mt-1">
                          <User className="h-4 w-4 text-muted-foreground" />
                          <p className="text-sm text-muted-foreground">{consulta.profesionalNombre || 'Sin profesional'}</p>
                        </div>
                      </div>
                    </div>
                    {consulta.prescripcionesIds && consulta.prescripcionesIds.length > 0 && (
                      <Badge className="bg-info/10 text-info border-info/20">
                        <Pill className="h-3 w-3 mr-1" />
                        Con Prescripción
                      </Badge>
                    )}
                  </div>
                </CardHeader>
                <CardContent className="space-y-4">
                  {(consulta.frecuenciaCardiaca || consulta.frecuenciaRespiratoria || consulta.temperatura || consulta.pesoKg) && (
                    <div>
                      <h4 className="font-semibold text-sm mb-2 flex items-center gap-2">
                        <Activity className="h-4 w-4 text-primary" />
                        Signos Vitales
                      </h4>
                      <div className="grid grid-cols-2 md:grid-cols-4 gap-3 p-3 bg-accent/50 rounded-lg">
                        {consulta.frecuenciaCardiaca && (
                          <div>
                            <p className="text-xs text-muted-foreground">FC</p>
                            <p className="text-sm font-medium">{consulta.frecuenciaCardiaca} lpm</p>
                          </div>
                        )}
                        {consulta.frecuenciaRespiratoria && (
                          <div>
                            <p className="text-xs text-muted-foreground">FR</p>
                            <p className="text-sm font-medium">{consulta.frecuenciaRespiratoria} rpm</p>
                          </div>
                        )}
                        {consulta.temperatura && (
                          <div>
                            <p className="text-xs text-muted-foreground">Temperatura</p>
                            <p className="text-sm font-medium">{consulta.temperatura}°C</p>
                          </div>
                        )}
                        {consulta.pesoKg && (
                          <div>
                            <p className="text-xs text-muted-foreground">Peso</p>
                            <p className="text-sm font-medium">{consulta.pesoKg} kg</p>
                          </div>
                        )}
                      </div>
                    </div>
                  )}

                  {consulta.examenFisico && (
                    <div>
                      <h4 className="font-semibold text-sm mb-2">Examen Físico</h4>
                      <p className="text-sm text-muted-foreground">{consulta.examenFisico}</p>
                    </div>
                  )}

                  {consulta.diagnostico && (
                    <div>
                      <h4 className="font-semibold text-sm mb-2">Diagnóstico</h4>
                      <p className="text-sm text-muted-foreground">{consulta.diagnostico}</p>
                    </div>
                  )}

                  {consulta.tratamiento && (
                    <div>
                      <h4 className="font-semibold text-sm mb-2">Tratamiento</h4>
                      <p className="text-sm text-muted-foreground">{consulta.tratamiento}</p>
                    </div>
                  )}

                  {consulta.observaciones && (
                    <div>
                      <h4 className="font-semibold text-sm mb-2">Observaciones</h4>
                      <p className="text-sm text-muted-foreground">{consulta.observaciones}</p>
                    </div>
                  )}

                  {consulta.prescripcionesIds && consulta.prescripcionesIds.length > 0 && (
                    <div className="pt-3 border-t">
                      <Button 
                        variant="outline" 
                        size="sm"
                        onClick={() => navigate(`/prescripciones?consultaId=${consulta.id}`)}
                        className="gap-2"
                      >
                        <FileText className="h-4 w-4" />
                        Ver Prescripción{consulta.prescripcionesIds.length > 1 ? 'es' : ''}
                      </Button>
                    </div>
                  )}
                </CardContent>
              </Card>
            );
          })}

          {consultas.length === 0 && (
            <Card>
              <CardContent className="text-center py-12">
                <FileText className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                <h3 className="text-lg font-medium text-foreground">Sin consultas registradas</h3>
                <p className="text-muted-foreground mt-1">Este paciente aún no tiene consultas</p>
              </CardContent>
            </Card>
          )}
        </div>
      </div>
    </div>
  );
}
