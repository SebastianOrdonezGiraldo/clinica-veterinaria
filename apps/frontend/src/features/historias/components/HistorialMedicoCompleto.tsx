import { useState, useEffect } from 'react';
import { Calendar, Activity, Pill, TrendingUp, FileText, Clock, User } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Badge } from '@shared/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@shared/components/ui/tabs';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import { historialMedicoService, HistorialMedico, ConsultaTimeline, EvolucionSignosVitales, HistorialMedicamento, ResumenMedico } from '../services/historialMedicoService';
import { useLogger } from '@shared/hooks/useLogger';
import { useApiError } from '@shared/hooks/useApiError';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

interface HistorialMedicoCompletoProps {
  pacienteId: string;
}

export default function HistorialMedicoCompleto({ pacienteId }: HistorialMedicoCompletoProps) {
  const logger = useLogger('HistorialMedicoCompleto');
  const { handleError } = useApiError();
  const [historial, setHistorial] = useState<HistorialMedico | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    loadHistorial();
  }, [pacienteId]);

  const loadHistorial = async () => {
    try {
      setIsLoading(true);
      const data = await historialMedicoService.getHistorialCompleto(pacienteId);
      setHistorial(data);
    } catch (error: any) {
      logger.error('Error al cargar historial médico', error, {
        action: 'loadHistorial',
        pacienteId,
      });
      handleError(error, 'Error al cargar el historial médico');
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading) {
    return (
      <div className="text-center py-12">
        <p className="text-muted-foreground">Cargando historial médico...</p>
      </div>
    );
  }

  if (!historial) {
    return (
      <div className="text-center py-12">
        <p className="text-muted-foreground">No se pudo cargar el historial médico</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Resumen Médico */}
      <ResumenMedicoCard resumen={historial.resumen} />

      <Tabs defaultValue="timeline" className="w-full">
        <TabsList className="grid w-full grid-cols-3">
          <TabsTrigger value="timeline">
            <Clock className="h-4 w-4 mr-2" />
            Timeline
          </TabsTrigger>
          <TabsTrigger value="evolucion">
            <TrendingUp className="h-4 w-4 mr-2" />
            Evolución
          </TabsTrigger>
          <TabsTrigger value="medicamentos">
            <Pill className="h-4 w-4 mr-2" />
            Medicamentos
          </TabsTrigger>
        </TabsList>

        <TabsContent value="timeline" className="space-y-4">
          <TimelineConsultas consultas={historial.timelineConsultas} />
        </TabsContent>

        <TabsContent value="evolucion" className="space-y-4">
          <GraficosEvolucion datos={historial.evolucionSignosVitales} />
        </TabsContent>

        <TabsContent value="medicamentos" className="space-y-4">
          <HistorialMedicamentos medicamentos={historial.historialMedicamentos} />
        </TabsContent>
      </Tabs>
    </div>
  );
}

function ResumenMedicoCard({ resumen }: { resumen: ResumenMedico }) {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Activity className="h-5 w-5" />
          Resumen Médico
        </CardTitle>
      </CardHeader>
      <CardContent>
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          <div>
            <p className="text-sm text-muted-foreground">Total Consultas</p>
            <p className="text-2xl font-bold">{resumen.totalConsultas}</p>
          </div>
          <div>
            <p className="text-sm text-muted-foreground">Total Prescripciones</p>
            <p className="text-2xl font-bold">{resumen.totalPrescripciones}</p>
          </div>
          {resumen.pesoActual && (
            <div>
              <p className="text-sm text-muted-foreground">Peso Actual</p>
              <p className="text-2xl font-bold">{resumen.pesoActual} kg</p>
              {resumen.variacionPeso && (
                <p className={`text-xs ${resumen.variacionPeso >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                  {resumen.variacionPeso >= 0 ? '+' : ''}{resumen.variacionPeso.toFixed(2)} kg
                </p>
              )}
            </div>
          )}
          {resumen.temperaturaPromedio && (
            <div>
              <p className="text-sm text-muted-foreground">Temp. Promedio</p>
              <p className="text-2xl font-bold">{resumen.temperaturaPromedio.toFixed(1)} °C</p>
            </div>
          )}
        </div>

        {resumen.diagnosticosFrecuentes.length > 0 && (
          <div className="mt-4 pt-4 border-t">
            <p className="text-sm font-medium mb-2">Diagnósticos Más Frecuentes</p>
            <div className="flex flex-wrap gap-2">
              {resumen.diagnosticosFrecuentes.map((diag, idx) => (
                <Badge key={idx} variant="outline">{diag}</Badge>
              ))}
            </div>
          </div>
        )}

        {resumen.medicamentosMasUsados.length > 0 && (
          <div className="mt-4 pt-4 border-t">
            <p className="text-sm font-medium mb-2">Medicamentos Más Usados</p>
            <div className="flex flex-wrap gap-2">
              {resumen.medicamentosMasUsados.map((med, idx) => (
                <Badge key={idx} variant="secondary">{med}</Badge>
              ))}
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  );
}

function TimelineConsultas({ consultas }: { consultas: ConsultaTimeline[] }) {
  if (consultas.length === 0) {
    return (
      <Card>
        <CardContent className="py-12 text-center">
          <p className="text-muted-foreground">No hay consultas registradas</p>
        </CardContent>
      </Card>
    );
  }

  return (
    <div className="space-y-4">
      {consultas.map((consulta, index) => (
        <Card key={consulta.id} className="relative">
          <div className="absolute left-0 top-0 bottom-0 w-1 bg-primary" />
          <CardHeader>
            <div className="flex items-start justify-between">
              <div className="flex-1">
                <div className="flex items-center gap-2 mb-2">
                  <Calendar className="h-4 w-4 text-muted-foreground" />
                  <span className="text-sm font-medium">
                    {format(new Date(consulta.fecha), 'PPP p', { locale: es })}
                  </span>
                </div>
                <div className="flex items-center gap-2">
                  <User className="h-4 w-4 text-muted-foreground" />
                  <span className="text-sm text-muted-foreground">{consulta.profesionalNombre}</span>
                </div>
              </div>
              {consulta.tienePrescripciones && (
                <Badge variant="outline" className="flex items-center gap-1">
                  <Pill className="h-3 w-3" />
                  {consulta.cantidadPrescripciones} {consulta.cantidadPrescripciones === 1 ? 'prescripción' : 'prescripciones'}
                </Badge>
              )}
            </div>
          </CardHeader>
          <CardContent className="space-y-3">
            {consulta.diagnostico && (
              <div>
                <p className="text-sm font-medium mb-1">Diagnóstico</p>
                <p className="text-sm text-muted-foreground">{consulta.diagnostico}</p>
              </div>
            )}
            {consulta.tratamiento && (
              <div>
                <p className="text-sm font-medium mb-1">Tratamiento</p>
                <p className="text-sm text-muted-foreground">{consulta.tratamiento}</p>
              </div>
            )}
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 pt-2 border-t">
              {consulta.pesoKg && (
                <div>
                  <p className="text-xs text-muted-foreground">Peso</p>
                  <p className="text-sm font-medium">{consulta.pesoKg} kg</p>
                </div>
              )}
              {consulta.temperatura && (
                <div>
                  <p className="text-xs text-muted-foreground">Temperatura</p>
                  <p className="text-sm font-medium">{consulta.temperatura} °C</p>
                </div>
              )}
              {consulta.frecuenciaCardiaca && (
                <div>
                  <p className="text-xs text-muted-foreground">Frec. Cardíaca</p>
                  <p className="text-sm font-medium">{consulta.frecuenciaCardiaca} lpm</p>
                </div>
              )}
              {consulta.frecuenciaRespiratoria && (
                <div>
                  <p className="text-xs text-muted-foreground">Frec. Respiratoria</p>
                  <p className="text-sm font-medium">{consulta.frecuenciaRespiratoria} rpm</p>
                </div>
              )}
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}

function GraficosEvolucion({ datos }: { datos: EvolucionSignosVitales[] }) {
  if (datos.length === 0) {
    return (
      <Card>
        <CardContent className="py-12 text-center">
          <p className="text-muted-foreground">No hay datos de signos vitales para mostrar</p>
        </CardContent>
      </Card>
    );
  }

  const chartData = datos.map(d => ({
    fecha: format(new Date(d.fecha), 'dd/MM/yyyy', { locale: es }),
    peso: d.pesoKg,
    temperatura: d.temperatura,
    frecuenciaCardiaca: d.frecuenciaCardiaca,
    frecuenciaRespiratoria: d.frecuenciaRespiratoria,
  }));

  const tienePeso = datos.some(d => d.pesoKg != null);
  const tieneTemperatura = datos.some(d => d.temperatura != null);
  const tieneFrecuencias = datos.some(d => d.frecuenciaCardiaca != null || d.frecuenciaRespiratoria != null);

  return (
    <div className="space-y-4">
      {tienePeso && (
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">Evolución del Peso</CardTitle>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="fecha" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Line type="monotone" dataKey="peso" stroke="#8884d8" name="Peso (kg)" />
              </LineChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      )}

      {tieneTemperatura && (
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">Evolución de la Temperatura</CardTitle>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="fecha" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Line type="monotone" dataKey="temperatura" stroke="#82ca9d" name="Temperatura (°C)" />
              </LineChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      )}

      {tieneFrecuencias && (
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">Frecuencias Cardíaca y Respiratoria</CardTitle>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="fecha" />
                <YAxis />
                <Tooltip />
                <Legend />
                {datos.some(d => d.frecuenciaCardiaca != null) && (
                  <Line type="monotone" dataKey="frecuenciaCardiaca" stroke="#ffc658" name="Frec. Cardíaca (lpm)" />
                )}
                {datos.some(d => d.frecuenciaRespiratoria != null) && (
                  <Line type="monotone" dataKey="frecuenciaRespiratoria" stroke="#ff7300" name="Frec. Respiratoria (rpm)" />
                )}
              </LineChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      )}
    </div>
  );
}

function HistorialMedicamentos({ medicamentos }: { medicamentos: HistorialMedicamento[] }) {
  if (medicamentos.length === 0) {
    return (
      <Card>
        <CardContent className="py-12 text-center">
          <p className="text-muted-foreground">No hay medicamentos prescritos</p>
        </CardContent>
      </Card>
    );
  }

  return (
    <div className="space-y-4">
      {medicamentos.map((med, index) => (
        <Card key={index}>
          <CardHeader>
            <div className="flex items-start justify-between">
              <div>
                <CardTitle className="text-lg">{med.medicamento}</CardTitle>
                <p className="text-sm text-muted-foreground mt-1">
                  {format(new Date(med.fechaPrescripcion), 'PPP', { locale: es })} • {med.profesionalNombre}
                </p>
              </div>
            </div>
          </CardHeader>
          <CardContent className="space-y-2">
            {med.presentacion && (
              <div>
                <p className="text-xs text-muted-foreground">Presentación</p>
                <p className="text-sm">{med.presentacion}</p>
              </div>
            )}
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              {med.dosis && (
                <div>
                  <p className="text-xs text-muted-foreground">Dosis</p>
                  <p className="text-sm font-medium">{med.dosis}</p>
                </div>
              )}
              {med.frecuencia && (
                <div>
                  <p className="text-xs text-muted-foreground">Frecuencia</p>
                  <p className="text-sm font-medium">{med.frecuencia}</p>
                </div>
              )}
              {med.duracion && (
                <div>
                  <p className="text-xs text-muted-foreground">Duración</p>
                  <p className="text-sm font-medium">{med.duracion}</p>
                </div>
              )}
            </div>
            {med.diagnostico && (
              <div className="pt-2 border-t">
                <p className="text-xs text-muted-foreground">Diagnóstico asociado</p>
                <p className="text-sm">{med.diagnostico}</p>
              </div>
            )}
          </CardContent>
        </Card>
      ))}
    </div>
  );
}

