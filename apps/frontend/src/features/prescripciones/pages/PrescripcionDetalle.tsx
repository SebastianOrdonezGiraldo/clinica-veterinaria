import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Download, Printer, Pill, AlertCircle } from 'lucide-react';
import { Button } from '@shared/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Separator } from '@shared/components/ui/separator';
import { Skeleton } from '@shared/components/ui/skeleton';
import { toast } from 'sonner';
import { prescripcionService } from '@features/prescripciones/services/prescripcionService';
import { consultaService } from '@features/historias/services/consultaService';
import { pacienteService } from '@features/pacientes/services/pacienteService';
import { propietarioService } from '@features/propietarios/services/propietarioService';
import { usuarioService } from '@features/usuarios/services/usuarioService';
import { Prescripcion, Consulta, Paciente, Usuario, Propietario } from '@core/types';
import { useLogger } from '@shared/hooks/useLogger';

export default function PrescripcionDetalle() {
  const logger = useLogger('PrescripcionDetalle');
  const { id } = useParams();
  const navigate = useNavigate();
  
  const [prescripcion, setPrescripcion] = useState<Prescripcion | null>(null);
  const [consulta, setConsulta] = useState<Consulta | null>(null);
  const [paciente, setPaciente] = useState<Paciente | null>(null);
  const [propietario, setPropietario] = useState<Propietario | null>(null);
  const [profesional, setProfesional] = useState<Usuario | null>(null);
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
      
      const prescripcionData = await prescripcionService.getById(id);
      setPrescripcion(prescripcionData);

      // Cargar consulta
      const consultaData = await consultaService.getById(prescripcionData.consultaId);
      setConsulta(consultaData);

      // Cargar paciente
      const pacienteData = await pacienteService.getById(consultaData.pacienteId);
      setPaciente(pacienteData);

      // Cargar propietario
      if (pacienteData.propietarioId) {
        try {
          const propietarioData = await propietarioService.getById(pacienteData.propietarioId);
          setPropietario(propietarioData);
        } catch (error) {
          logger.warn('Error al cargar propietario de la prescripción', {
            action: 'loadPropietario',
            prescripcionId: id,
            propietarioId: pacienteData.propietarioId,
          });
          // No es crítico, continuar sin propietario
        }
      }

      // Cargar profesional
      if (consultaData.profesionalId) {
        try {
          const profesionalData = await usuarioService.getById(consultaData.profesionalId);
          setProfesional(profesionalData);
        } catch (error) {
          logger.warn('Error al cargar profesional de la prescripción', {
            action: 'loadProfesional',
            prescripcionId: id,
            profesionalId: consultaData.profesionalId,
          });
          // No es crítico, continuar sin profesional
        }
      }
    } catch (error: any) {
      logger.error('Error al cargar detalles de la prescripción', error, {
        action: 'loadData',
        prescripcionId: id,
      });
      const statusCode = error?.response?.status;
      const errorMessage = error?.response?.data?.message;
      
      if (statusCode === 404) {
        setError('Prescripción no encontrada');
      } else if (statusCode === 403) {
        setError('No tienes permisos para ver esta prescripción');
      } else {
        setError(errorMessage || 'Error al cargar la prescripción');
      }
      toast.error(errorMessage || 'Error al cargar la prescripción');
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading) {
    return (
      <div className="space-y-6">
        <Skeleton className="h-10 w-64" />
        <Card>
          <CardHeader>
            <Skeleton className="h-8 w-48" />
          </CardHeader>
          <CardContent className="space-y-4">
            <Skeleton className="h-32 w-full" />
            <Skeleton className="h-20 w-full" />
          </CardContent>
        </Card>
      </div>
    );
  }

  if (error || !prescripcion || !consulta || !paciente) {
    return (
      <div className="space-y-6">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" onClick={() => navigate('/prescripciones')}>
            <ArrowLeft className="h-5 w-5" />
          </Button>
          <div className="flex-1">
            <h1 className="text-3xl font-bold text-foreground">Prescripción no encontrada</h1>
          </div>
        </div>
        <Card className="border-destructive">
          <CardContent className="flex flex-col items-center justify-center py-16">
            <div className="rounded-full bg-destructive/10 p-4 mb-4">
              <AlertCircle className="h-8 w-8 text-destructive" />
            </div>
            <h2 className="text-2xl font-bold mb-2">Prescripción no encontrada</h2>
            <p className="text-muted-foreground mb-4">
              {error || 'La prescripción que buscas no existe o no tienes permisos para verla.'}
            </p>
            <div className="flex gap-3">
              <Button onClick={() => navigate('/prescripciones')} variant="outline">
                Volver a Prescripciones
              </Button>
              {error && (
                <Button onClick={loadData}>
                  Reintentar
                </Button>
              )}
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  const handleGeneratePDF = async () => {
    if (!id) return;
    
    try {
      toast.loading('Generando PDF...', { id: 'pdf-generation' });
      
      const blob = await prescripcionService.downloadPdf(id);
      
      // Crear URL temporal para el blob
      const url = window.URL.createObjectURL(blob);
      
      // Crear elemento <a> para descargar
      const link = document.createElement('a');
      link.href = url;
      link.download = `receta-medica-${prescripcion?.id || id}.pdf`;
      document.body.appendChild(link);
      link.click();
      
      // Limpiar
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      
      toast.success('PDF descargado exitosamente', { id: 'pdf-generation' });
    } catch (error: any) {
      logger.error('Error al descargar PDF de prescripción', error, {
        action: 'downloadPDF',
        prescripcionId: id,
      });
      const errorMessage = error?.response?.data?.message || 'Error al generar el PDF';
      toast.error(errorMessage, { id: 'pdf-generation' });
    }
  };

  const handlePrint = () => {
    window.print();
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate('/prescripciones')}>
          <ArrowLeft className="h-5 w-5" />
        </Button>
        <div className="flex-1">
          <h1 className="text-3xl font-bold text-foreground">Receta Médica</h1>
          <p className="text-muted-foreground mt-1">Prescripción #{prescripcion.id}</p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={handlePrint} className="gap-2">
            <Printer className="h-4 w-4" />
            Imprimir
          </Button>
          <Button onClick={handleGeneratePDF} className="gap-2">
            <Download className="h-4 w-4" />
            Descargar PDF
          </Button>
        </div>
      </div>

      <Card className="print:shadow-none">
        <CardHeader className="bg-primary/5">
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="text-2xl">🐾 VetClinic Pro</CardTitle>
              <p className="text-sm text-muted-foreground mt-1">Sistema de Gestión Veterinaria</p>
            </div>
            <div className="h-16 w-16 rounded-full bg-primary/10 flex items-center justify-center">
              <Pill className="h-8 w-8 text-primary" />
            </div>
          </div>
        </CardHeader>
        <CardContent className="space-y-6 pt-6">
          <div className="grid md:grid-cols-2 gap-6">
            <div>
              <h3 className="font-semibold text-sm text-muted-foreground mb-2">Información del Paciente</h3>
              <div className="space-y-1">
                <p className="font-medium text-lg">{paciente.nombre}</p>
                <p className="text-sm">Especie: {paciente.especie}</p>
                <p className="text-sm">Raza: {paciente.raza || 'N/A'}</p>
                <p className="text-sm">Propietario: {propietario?.nombre || 'N/A'}</p>
              </div>
            </div>
            <div>
              <h3 className="font-semibold text-sm text-muted-foreground mb-2">Información de la Consulta</h3>
              <div className="space-y-1">
                <p className="text-sm">Fecha de Consulta: {new Date(consulta.fecha).toLocaleDateString('es-ES', { 
                  day: '2-digit', 
                  month: 'long', 
                  year: 'numeric' 
                })}</p>
                {prescripcion.fechaEmision && (
                  <p className="text-sm">Fecha de Emisión: {new Date(prescripcion.fechaEmision).toLocaleDateString('es-ES', { 
                    day: '2-digit', 
                    month: 'long', 
                    year: 'numeric' 
                  })}</p>
                )}
                <p className="text-sm">Profesional: {profesional?.nombre || 'N/A'}</p>
              </div>
            </div>
          </div>

          {prescripcion.indicacionesGenerales && (
            <>
              <Separator />
              <div>
                <h3 className="font-semibold text-sm text-muted-foreground mb-2">Indicaciones Generales</h3>
                <p className="text-sm whitespace-pre-wrap">{prescripcion.indicacionesGenerales}</p>
              </div>
            </>
          )}

          <Separator />

          <div>
            <h3 className="font-semibold text-lg mb-4 flex items-center gap-2">
              <Pill className="h-5 w-5 text-primary" />
              Medicamentos Prescritos
            </h3>
            <div className="space-y-4">
              {prescripcion.items.map((item, index) => (
                <div key={index} className="p-4 border rounded-lg bg-accent/30">
                  <div className="flex items-start justify-between mb-3">
                    <div>
                      <h4 className="font-semibold text-lg">{item.medicamento}</h4>
                      {item.presentacion && (
                        <p className="text-sm text-muted-foreground">{item.presentacion}</p>
                      )}
                    </div>
                    <div className="text-right">
                      <p className="text-sm font-medium text-primary">Duración</p>
                      <p className="text-lg font-bold">{item.duracionDias} días</p>
                    </div>
                  </div>
                  
                  <div className="grid md:grid-cols-2 gap-3 mb-3">
                    <div className="flex items-center gap-2">
                      <span className="text-sm font-medium text-muted-foreground">Dosis:</span>
                      <span className="font-medium">{item.dosis}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <span className="text-sm font-medium text-muted-foreground">Frecuencia:</span>
                      <span className="font-medium">{item.frecuencia}</span>
                    </div>
                    {item.viaAdministracion && (
                      <div className="flex items-center gap-2">
                        <span className="text-sm font-medium text-muted-foreground">Vía:</span>
                        <span className="font-medium">
                          {item.viaAdministracion === 'ORAL' ? 'Oral' :
                           item.viaAdministracion === 'INYECTABLE' ? 'Inyectable' :
                           item.viaAdministracion === 'TOPICA' ? 'Tópica' :
                           item.viaAdministracion === 'OFTALMICA' ? 'Oftálmica' :
                           item.viaAdministracion === 'OTICA' ? 'Ótica' :
                           item.viaAdministracion === 'OTRA' ? 'Otra' :
                           item.viaAdministracion}
                        </span>
                      </div>
                    )}
                  </div>

                  {item.indicaciones && (
                    <div className="pt-3 border-t">
                      <p className="text-sm font-medium text-muted-foreground mb-1">Indicaciones:</p>
                      <p className="text-sm">{item.indicaciones}</p>
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>

          <Separator />

          <div className="text-center text-sm text-muted-foreground">
            <p>Esta receta es válida por 30 días desde la fecha de emisión</p>
            <p className="mt-2">VetClinic Pro • Sistema de Gestión Veterinaria</p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

